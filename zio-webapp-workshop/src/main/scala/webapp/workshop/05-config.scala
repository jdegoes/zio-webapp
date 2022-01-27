package webapp.workshop

import zio._
import zio.json._

import zhttp.http.{int => _, _}

import zio.test._
import zio.test.TestAspect.ignore

import zio.config._
import zio.config.ConfigDescriptor._
import zio.config.magnolia._

object ConfigSpec extends ZIOSpecDefault {
  //
  // TYPES
  //

  // ConfigDescriptor[A]
  // PropertyTree[K, V]
  // ConfigSource

  //
  // SIMPLE CONSTRUCTORS
  //

  /**
   * EXERCISE
   *
   * Using `string`, create a `ConfigDescriptor` that describes a `String` read
   * from a property called "name".
   */
  lazy val stringName: ConfigDescriptor[String] = TODO

  /**
   * EXERCISE
   *
   * Using `int`, create a `ConfigDescriptor` that describes an `Int` read from
   * a property called "age".
   */
  lazy val intAge: ConfigDescriptor[Int] = TODO

  //
  // OPERATORS
  //

  /**
   * EXERCISE
   *
   * Using `ConfigDescriptor#optional`, turn the following
   * `ConfigDescriptor[Int]` into `ConfigDescriptor[Option[Int]]`.
   */
  lazy val optionalInt: ConfigDescriptor[Option[Int]] = int("port").TODO

  /**
   * EXERCISE
   *
   * Using `ConfigDescriptor#<>`, combine the `password` and `token` config
   * descriptors into one, which will try the left hand side first, but if that
   * fails, try the right hand side.
   * @return
   */
  lazy val passwordOrToken = TODO
  val password             = string("password")
  val token                = string("token")

  /**
   * EXERCISE
   *
   * Using `ConfigDescriptor#zip`, combine the `stringName` and `intAge` config
   * descriptors into one, which will produce a tuple of both the name and the
   * age.
   */
  lazy val nameZipAge: ConfigDescriptor[(String, Int)] = TODO

  /**
   * EXERCISE
   *
   * Using `ConfigDescriptor#from`, specify the source of the `nameZipAge`
   * configuration data to be a Map literal (`ConfigSource.fromMap`) with "name"
   * equal to "Sherlock Holmes" and age equal to "42".
   */
  lazy val personFromMap: ConfigDescriptor[(String, Int)] = TODO

  //
  // ADT CONSTRUCTION
  //

  /**
   * EXERCISE
   *
   * Using `int`, followed by `to`, manually create a `ConfigDescriptor` for
   * `Port`.
   */
  final case class Port(port: Int)
  object Port {
    implicit lazy val portDescriptor: ConfigDescriptor[Port] = TODO
  }

  /**
   * EXERCISE
   *
   * Using `string`, `int`, and `zip`, followed by `to`, manually create a
   * `ConfigDescriptor` for `Database`.
   */
  final case class Database(url: String, port: Int)
  object Database {
    implicit lazy val configDescriptor: ConfigDescriptor[Database] = TODO
  }

  /**
   * EXERCISE
   *
   * Using `nested`, manually create a `ConfigDescriptor` for
   * `PersistenceConfig`.
   */
  final case class PersistenceConfig(userDB: Database, adminDB: Database)
  object PersistenceConfig {
    implicit lazy val configDescriptor: ConfigDescriptor[PersistenceConfig] = TODO
  }

  /**
   * EXERCISE
   *
   * Using `enumeration`, manually create a `ConfigDescriptor` for `FeedSource`.
   */
  sealed trait FeedSource
  object FeedSource {
    final case class S3(bucket: String)      extends FeedSource
    final case class URL(value: String)      extends FeedSource
    final case class LocalFile(file: String) extends FeedSource

    implicit lazy val configDescriptor: ConfigDescriptor[FeedSource] =
      enumeration[FeedSource].TODO
  }

  //
  // DERIVATION
  //

  import zio.config.derivation._ // @describe, @name, @names

  /**
   * EXERCISE
   *
   * Using `Descriptor.descriptor`, automatically derive a `ConfigDescriptor`
   * for `FeedSource`.
   */
  lazy val feedSourceDerived: ConfigDescriptor[FeedSource] = TODO

  /**
   * EXERCISE
   *
   * Using `Descriptor.descriptor`, automatically derive a `ConfigDescriptor`
   * for `PersistenceConfig`.
   */
  lazy val PersistenceConfigDerived: ConfigDescriptor[PersistenceConfig] = TODO

  /**
   * EXERCISE
   *
   * Using the @name annotation, ensure the automatically derived config
   * descriptor for `Database2` can handle a username encoded with the property
   * name "user_name", and a password encoded with the property name "pwd".
   */
  final case class Database2(username: String, password: String)
  object Database2 {
    implicit val configDescriptor: ConfigDescriptor[Database2] =
      Descriptor.descriptor[Database2]
  }

  //
  // FEATURES
  //

  /**
   * EXERCISE
   *
   * Using `read`, read the value of `personFromMap` config descriptor (which
   * should succeed since it's data is fully specified).
   */
  lazy val readPerson: IO[ReadError[String], (String, Int)] = read(personFromMap)

  /**
   * EXERCISE
   *
   * Using `write`, write the value ("John Watson", 46) to a
   * `PropertyTree[String, String]`.
   */
  lazy val writePerson: Either[String, PropertyTree[String, String]] = TODO

  /**
   * EXERCISE
   *
   * Using `generateDocs`, generate documentation for the `PersistenceConfig`
   * structure.
   */
  lazy val docPersistenceConfig: ConfigDocs = TODO

  /**
   * EXERCISE
   *
   * Using `generateReport`, generate a report for the `personFromMap`, given
   * the value ("John Watson", 46).
   */
  lazy val reportOfPersonFromMap: Either[String, ConfigDocs] = TODO

  //
  // GRADUATION
  //

  /**
   * EXERCISE
   *
   * Figure out how to wire up `usersHttpApp` with config information
   * constructed from `UserDatabaseConfig`.
   */
  object UsersApp extends ZIOAppDefault {
    def run: ZIO[ZEnv with ZIOAppArgs, Any, Any] = TODO
  }

  final case class UserDatabaseConfig(url: String)
  object UserDatabaseConfig {
    implicit val configDescriptor: ConfigDescriptor[UserDatabaseConfig] =
      Descriptor.descriptor[UserDatabaseConfig]
  }
  final case class User(name: String, email: String, id: String)
  object User {
    implicit val userCodec: JsonCodec[User] = DeriveJsonCodec.gen[User]
  }
  trait UserRepo {
    def lookupUserById(id: String): Task[User]
  }
  object UserRepo {
    val live: ZLayer[UserDatabaseConfig, Nothing, UserRepo] =
      ZLayer {
        for {
          config <- ZIO.service[UserDatabaseConfig]
        } yield new UserRepo {
          def lookupUserById(id: String): Task[User] = Task.fail(new Exception("Not implemented"))
        }
      }
  }

  val usersHttpApp: HttpApp[UserRepo, Throwable] =
    Http.collectZIO[Request] { case Method.GET -> !! / "users" / id =>
      for {
        user <- ZIO.serviceWithZIO[UserRepo](_.lookupUserById(id))
      } yield Response.json(user.toJson)
    }

  def spec = suite("ConfigSpec") {
    def assertRoundtrip[A: ConfigDescriptor](a: A): IO[String, Assert] = {
      val cd = implicitly[ConfigDescriptor[A]]

      for {
        written <- ZIO.fromEither(write(cd, a))
        reread  <- read(cd from ConfigSource.fromPropertyTree(written, "test")).mapError(_.getMessage)
      } yield assertTrue(reread == a)
    }
    def assertReadFlat[A](cd: ConfigDescriptor[A], pt: Map[String, String], expected: A) =
      for {
        value <- read(cd from ConfigSource.fromMap(pt, "test")).mapError(_.getMessage)
      } yield assertTrue(value == expected)

    suite("constructors") {
      test("string") {
        assertReadFlat(stringName, Map("name" -> "Sherlock Holmes"), "Sherlock Holmes")
      } @@ ignore +
        test("int") {
          assertReadFlat(intAge, Map("age" -> "42"), 42)
        } @@ ignore
    } +
      suite("operators") {
        test("optional") {
          assertReadFlat(optionalInt, Map("port" -> "8080"), Some(8080))
        } @@ ignore +
          test("orElse") {
            for {
              assert1 <- assertReadFlat(passwordOrToken, Map("password" -> "secret"), "secret")
              assert2 <- assertReadFlat(passwordOrToken, Map("token" -> "secret"), "secret")
            } yield assert1 && assert2
          } @@ ignore +
          test("zip") {
            assertReadFlat(nameZipAge, Map("name" -> "Sherlock Holmes", "age" -> "42"), ("Sherlock Holmes", 42))
          } @@ ignore +
          test("from") {
            for {
              person <- read(personFromMap)
            } yield assertTrue(person == ("Sherlock Holmes", 42))
          } @@ ignore
      } + suite("adt construction") {
        test("port") {
          assertRoundtrip(Port(3306))
        } @@ ignore +
          test("database") {
            assertRoundtrip(Database("localhost", 3306))
          } @@ ignore +
          test("PersistenceConfig") {
            assertRoundtrip(PersistenceConfig(Database("localhost", 3306), Database("localhost", 3306)))
          } @@ ignore
      } +
      suite("derivation") {
        test("sealed trait") {
          assertRoundtrip[FeedSource](FeedSource.S3("bucket"))(feedSourceDerived)
        } @@ ignore +
          test("case class") {
            assertRoundtrip(PersistenceConfig(Database("localhost", 3306), Database("localhost", 3306)))(
              PersistenceConfigDerived
            )
          } @@ ignore +
          test("annotations") {
            assertReadFlat(
              Database2.configDescriptor,
              Map("user_name" -> "sherlock", "pwd" -> "holmes"),
              Database2("sherlock", "holmes")
            )
          } @@ ignore
      } +
      suite("features") {
        test("read") {
          for {
            person <- readPerson
          } yield assertTrue(person == ("Sherlock Holmes", 42))
        } @@ ignore +
          test("write") {
            for {
              expected <- ZIO.fromEither(write(personFromMap, ("John Watson", 46)))
              actual   <- ZIO.fromEither(writePerson)
            } yield assertTrue(actual == expected)
          } @@ ignore +
          test("generateDocs") {
            assertTrue(docPersistenceConfig == generateDocs(PersistenceConfig.configDescriptor))
          } @@ ignore +
          test("generateReport") {
            val expected = generateReport(personFromMap, ("John Watson", 46))

            assertTrue(reportOfPersonFromMap == expected)
          } @@ ignore
      } +
      suite("graduation") {
        test("end-to-end") {
          assertTrue(UsersApp.run != null)
        } @@ ignore
      }
  }
}
