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
import zio.test._
import zio.test.TestAspect.ignore

import scalikejdbc._
import zio.schema.Schema
import zio.schema.DeriveSchema

object PersistenceSpec extends ZIOSpecDefault {
  import scalikejdbc._

  final case class DatabaseConfig(driverClass: String, url: String, user: String, password: String)
  object DatabaseConfig {
    val test =
      DatabaseConfig("org.h2.Driver", "jdbc:h2:mem:hello", "user", "pass")
  }

  trait ZConnectionPool {
    def connection: ZManaged[Any, Throwable, java.sql.Connection]
  }
  object ZConnectionPool {
    val live: ZLayer[DatabaseConfig, Throwable, ZConnectionPool] =
      ZLayer {
        for {
          _   <- ZManaged.blocking
          cfg <- ZManaged.service[DatabaseConfig]
          _   <- ZManaged.attempt(Class.forName(cfg.driverClass))
          _   <- ZManaged.attempt(ConnectionPool.singleton(cfg.url, cfg.user, cfg.password))
        } yield new ZConnectionPool {
          def connection: ZManaged[Any, Throwable, java.sql.Connection] =
            for {
              _          <- ZManaged.blocking
              connection <- ZManaged.acquireReleaseAttemptWith(ConnectionPool.borrow())(_.close())
            } yield connection
        }
      }
  }

  object ZScalikeJDBC {

    val transaction: ZLayer[ZConnectionPool, Throwable, DBSession] =
      ZLayer {
        for {
          _          <- ZManaged.blocking
          pool       <- ZManaged.service[ZConnectionPool]
          connection <- pool.connection
          db         <- ZManaged.attempt(DB(connection))
          _ <- (ZManaged.attempt(db.begin()) *> ZManaged
                 .finalizerExit(exit => ZIO.succeed(if (exit.isSuccess) db.commit() else db.rollback())))
          session <- ZManaged.attempt(db.withinTxSession())
        } yield session
      }

    private def session[A](f: DBSession => A): ZIO[DBSession, Throwable, A] =
      for {
        session <- ZIO.service[DBSession]
        result  <- ZIO.attemptBlocking(f(session))
      } yield result

    def execute[A](sql: => SQL[A, _ <: WithExtractor]): RIO[DBSession, Unit] =
      session { implicit dbSession =>
        sql.execute.apply()
      }

    def update[A](sql: => SQL[A, _ <: WithExtractor]): RIO[DBSession, Int] =
      session { implicit dbSession =>
        sql.update.apply()
      }

    def single[A](sql: => SQL[A, HasExtractor])(extract: WrappedResultSet => A): RIO[DBSession, Option[A]] =
      session { implicit dbSession =>
        sql.single.apply()
      }

    def many[A](sql: => SQL[A, HasExtractor]): RIO[DBSession, List[A]] =
      session { implicit dbSession =>
        sql.list.apply()
      }
  }

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

  val createTable: ZIO[DBSession, Throwable, Unit] =
    ZScalikeJDBC.execute {
      sql"""
      create table detectives (
        id    IDENTITY NOT NULL PRIMARY KEY,
        name  VARCHAR NOT NULL
      )
      """
    }

  val executedCreateTable: ZIO[ZConnectionPool, Throwable, Unit] =
    ZScalikeJDBC.transaction {
      createTable
    }

  val insertedDetectives: ZIO[ZConnectionPool, Throwable, List[Unit]] =
    ZScalikeJDBC.transaction {
      ZIO.foreach(Detective.famous) { detective =>
        ZScalikeJDBC.execute {
          sql"insert into detectives (name) values (${detective.name})"
        }
      }
    }

  val queriedDetectives: ZIO[ZConnectionPool, Throwable, List[Detective]] =
    ZScalikeJDBC.transaction {
      ZScalikeJDBC.many {
        sql"select * from detectives".map(rs => Detective(rs.string("name")))
      }
    }

  val updatedDetectives: ZIO[ZConnectionPool, Throwable, Int] =
    ZScalikeJDBC.transaction {
      ZScalikeJDBC.update {
        sql"update detectives set name = 'Sherlock Holmes and John Watson' where name = 'Sherlock Holmes'"
      }
    }

  val deletedJimRockford: ZIO[ZConnectionPool, Throwable, Int] =
    ZScalikeJDBC.transaction {
      ZScalikeJDBC.update {
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
  lazy val createTable2: ZIO[DBSession, Throwable, Unit] = TODO

  /**
   * EXERCISE
   *
   * Execute the `createTable2` command by using the `ZScalikeJDBC.transaction`
   * layer.
   */
  lazy val executedCreateTable2: ZIO[ZConnectionPool, Throwable, Unit] = TODO

  //
  // ADT
  //

  /**
   * EXERCISE
   *
   * Define a constructor for `User` that takes in a `WrappedResultSet`.
   */
  final case class User(id: Option[Long], name: String, age: Int, email: String)
  object User {
    implicit val schema: Schema[User] = DeriveSchema.gen[User]

    def apply(rs: WrappedResultSet): User = TODO
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
  lazy val deletedJimRockfordUser: RIO[ZConnectionPool, Int] = TODO

  //
  // UPDATES
  //

  /**
   * EXERCISE
   *
   * Change the name of the user `Sherlock Holmes` to `Sherlock Holmes and John
   * Watson`.
   */
  lazy val updatedSherlockHolmesUser: RIO[ZConnectionPool, Int] = TODO

  //
  // SELECTION
  //

  /**
   * EXERCISE
   *
   * Select all users from the `users` table.
   */
  lazy val allUsers: RIO[ZConnectionPool, List[User]] = TODO

  /**
   * EXERCISE
   *
   * Select the single user from the table whose email is 'sherlock@holmes.com'.
   */
  lazy val singleUser: RIO[ZConnectionPool, Option[User]] = TODO

  //
  // GRADUATION
  //

  /**
   * EXERCISE
   *
   * Use ZIO Schema to decode the result set into a case class, or fail with a
   * descriptive error message if this is not possible.
   */
  def selectOneAs[A: Schema](sql: => SQL[_, _ <: WithExtractor]): RIO[ZConnectionPool, Option[A]] =
    ???

  /**
   * EXERCISE
   *
   * Use ZIO Schema to decode the result set into a case class, or fail with a
   * descriptive error message if this is not possible.
   */
  def selectManyAs[A: Schema, E <: WithExtractor](sql: => SQL[_, _ <: WithExtractor]): RIO[ZConnectionPool, List[A]] =
    ???

  /**
   * EXERCISE
   *
   * Use ZIO Schema to encode the case class into a SQL insertion statement, or
   * fail with a descriptive error message if this is not possible.
   */
  def insertAllFrom[A: Schema](table: String, a: => Iterable[A]): RIO[ZConnectionPool, Int] =
    ???

  def insertOneFrom[A: Schema](table: String, a: => A): RIO[ZConnectionPool, Int] =
    insertAllFrom(table, List(a))

  val testPersistenceLayer: ZLayer[Any, TestFailure[Nothing], ZConnectionPool] =
    (ZLayer.succeed(DatabaseConfig.test) >>> ZConnectionPool.live).mapError(TestFailure.die(_))

  def spec = suite("PersistenceSpec") {
    suite("tour") {
      test("end-to-end") {
        (for {
          _ <- executedCreateTable
          _ <- insertedDetectives
          _ <- queriedDetectives
          _ <- updatedDetectives
          _ <- deletedJimRockford
        } yield assertCompletes)
      }
    }
  }.provideCustomLayer(testPersistenceLayer)
}
