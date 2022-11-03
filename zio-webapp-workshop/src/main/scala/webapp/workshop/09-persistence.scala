/**
 * PERSISTENCE
 *
 * Nearly every web application needs to use a persistence layer. In fact, most
 * web applications will need multiple persistence layers for different purposes
 * (message queuing, binary storage & streaming, transactional data,
 * semi-structured data, in-memory caching, etc.).
 *
 * ZIO 2.0 has powerful facilities for integrating with different persistence
 * layers, even those not natively designed to support ZIO, and there are two
 * promising solutions for DBMS (ZIO Quill and ZIO SQL).
 *
 * In this section, you will learn the general principles of integrating support
 * for a persistence layer into your ZIO 2.0 web application.
 */
package webapp.workshop

import zio._
import zio.jdbc._

import zio.schema.Schema
import zio.schema.DeriveSchema

object PersistenceSection {

  //
  // TOUR
  //

  final case class Detective(name: String)
  object Detective {
    val famous: List[Detective] =
      List(
        Detective("Sherlock Holmes"),
        Detective("Lieutenant Columbo"),
        Detective("Jessica Fletcher"),
        Detective("Nancy Drew"),
        Detective("Joe and Frank Hardy"),
        Detective("Phillip Marlowe"),
        Detective("Jim Rockford"),
        Detective("Miss Marple")
      )
  }

  val createTable: ZIO[ZConnection, Throwable, Unit] =
    execute {
      sql"""
      create table detectives (
        id    IDENTITY NOT NULL PRIMARY KEY,
        name  VARCHAR NOT NULL
      )
      """
    }

  val executedCreateTable: ZIO[ZConnectionPool, Throwable, Unit] =
    transaction {
      createTable
    }

  val insertedDetectives: ZIO[ZConnectionPool, Throwable, Unit] =
    transaction {
      ZIO.foreachDiscard(Detective.famous) { detective =>
        execute {
          sql"insert into detectives (name) values (${detective.name})"
        }
      }
    }

  val queriedDetectives: ZIO[ZConnectionPool, Throwable, Chunk[Detective]] =
    transaction {
      selectAll[Detective] {
        sql"select * from detectives".as[String].map(Detective(_))
      }
    }

  val updatedDetectives: ZIO[ZConnectionPool, Throwable, Long] =
    transaction {
      update {
        sql"update detectives set name = 'Sherlock Holmes and John Watson' where name = 'Sherlock Holmes'"
      }
    }

  val deletedJimRockford: ZIO[ZConnectionPool, Throwable, Long] =
    transaction {
      update {
        sql"delete from detectives where name = 'Jim Rockford'"
      }
    }

  //
  // CREATION
  //

  /**
   * EXERCISE
   *
   * Create a table called `users` with the following columns:
   *
   *   - `id` - auto-incremented primary key
   *   - `name` - variable-length string (non-nullable)
   *   - `age` - integer (non-nullable)
   *   - `email` - variable-length string (non-nullable)
   */
  lazy val createTable2: ZIO[ZConnectionPool, Throwable, Unit] = 
    transaction {
      execute {
        sql"""
        create table users (
          id    IDENTITY,
          name  VARCHAR NOT NULL,
          age   INT NOT NULL,
          email VARCHAR NOT NULL
        )
        """
      }
    }

  /**
   * EXERCISE
   *
   * Execute the `createTable2` command by using the `transaction`
   * layer.
   */
  lazy val executedCreateTable2: ZIO[Any, Throwable, Unit] =
    createTable2.provide(ZConnectionPool.h2test)

  //
  // ADT
  //

  /**
   * EXERCISE
   *
   * Define a JDBC decoder for `User`.
   */
  final case class User(id: Option[Long], name: String, age: Int, email: String)
  object User {
    implicit val schema: Schema[User] = DeriveSchema.gen[User]

    implicit val userDecoder: JdbcDecoder[User] =
      JdbcDecoder.fromSchema[User]

    implicit val userEncoder: JdbcEncoder[User] = 
      JdbcEncoder.fromSchema[User]
  }

  //
  // INSERTION
  //

  val detectiveUsers: List[User] =
    List(
      User(None, "Sherlock Holmes", 42, "sherlock@holmes.com"),
      User(None, "Lieutenant Columbo", 43, "lieutenant@columbo.com"),
      User(None, "Jessica Fletcher", 44, "jessica@fletcher.com"),
      User(None, "Nancy Drew", 45, "nancy@drew.com"),
      User(None, "Joe and Frank Hardy", 46, "joefrank@hardy.com"),
      User(None, "Phillip Marlowe", 47, "phillip@marlowe.com"),
      User(None, "Jim Rockford", 48, "jim@rockford.com"),
      User(None, "Miss Marple", 49, "miss@marple.com")
    )

  lazy val insertedDetectiveUsers: RIO[ZConnectionPool, Unit] = TODO

  //
  // DELETION
  //

  /**
   * EXERCISE
   *
   * Delete 'Jim Rockford' from the `users` table.
   */
  lazy val deletedJimRockfordUser: RIO[ZConnectionPool, Long] = 
    transaction {
      update {
        sql"delete from users where name = 'Jim Rockford'"
      }
    }

  //
  // UPDATES
  //

  /**
   * EXERCISE
   *
   * Change the name of the user `Sherlock Holmes` to `Sherlock Holmes and John
   * Watson`.
   */
  lazy val updatedSherlockHolmesUser: RIO[ZConnectionPool, Long] =
    transaction {
      update {
        sql"update users set name = 'Sherlock Holmes and John Watson' where name = 'Sherlock Holmes'"
      }
    }


  //
  // SELECTION
  //

  /**
   * EXERCISE
   *
   * Select all users from the `users` table.
   */
  lazy val allUsers: RIO[ZConnectionPool, Chunk[User]] = 
    transaction {
      selectAll {
        sql"select * from users".as[User]
      }
    }

  /**
   * EXERCISE
   *
   * Select the single user from the table whose email is 'sherlock@holmes.com'.
   */
  lazy val singleUser: RIO[ZConnectionPool, Option[User]] = 
    transaction {
      selectOne {
        sql"select * from users where email = 'sherlock@holmes.com'".as[User]
      }
    }

  //
  // GRADUATION
  //

  /**
   * EXERCISE
   *
   * Use ZIO Schema to decode the result set into a case class.
   */
  def selectOneAs[A: Schema](sql: => Sql[_]): RIO[ZConnectionPool, Option[A]] = {
    implicit val decoder = JdbcDecoder.fromSchema[A]

    transaction {
      selectOne {
        sql.as[A]
      }
    }
  }

  /**
   * EXERCISE
   *
   * Use ZIO Schema to decode the result set into a case class, or fail with a
   * descriptive error message if this is not possible.
   */
  def selectManyAs[A: Schema](sql: => Sql[_]): RIO[ZConnectionPool, Chunk[A]] = {
    implicit val decoder = JdbcDecoder.fromSchema[A]

    transaction {
      selectAll {
        sql.as[A]
      }
    }
  }

  /**
   * EXERCISE
   *
   * Use ZIO Schema to encode the case class into a SQL insertion statement, or
   * fail with a descriptive error message if this is not possible.
   */
  def insertAllFrom[A: Schema](table: String, as: => Iterable[A]): RIO[ZConnectionPool, Long] = {
    implicit val encoder = JdbcEncoder.fromSchema[A]

    val schema = Schema[A]

    val fieldNames = schema match {
      case record : Schema.Record[_] => record.structure.map(_.label)
      case _ => Chunk.empty 
    }

    transaction {
      update {
        Sql.insertInto(table)(fieldNames: _*).values(as)
      }
    }
  }

  def insertOneFrom[A: Schema](table: String, a: => A): RIO[ZConnectionPool, Long] =
    insertAllFrom(table, List(a))
}
