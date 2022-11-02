/**
 * CONFIGURATION
 *
 * Application configuration starts out simple enough, often with hard-coded
 * data inside an application, and then rapidly cascades out of control as more
 * demands are placed on the need to change, validation, generate, and source
 * application configuration data.
 *
 * The challenges are even more prominent for cloud-native web applications,
 * which have to have a greater degree of flexibility given the environments
 * they are hosted in and the demands of connecting to and communicating with
 * external cloud services.
 *
 * In this section, you will learn how ZIO Config provides a comprehensive
 * approach to application configuration management.
 */
package webapp.workshop

import zio._
import zio.json._

import zhttp.http.{int => _, _}

import zio.config._
import zio.config.ConfigDescriptor._
import zio.config.magnolia._
import zhttp.service.Server

object ConfigSection {
  //
  // TYPES
  //

  // ConfigDescriptor[A]
  // PropertyTree[K, V]
  // ConfigSource

  //
  // CONFIGSOURCE CONSTRUCTORS
  //

  // API_V2_HOST="localhost","localhost"
  // API_V2_PORT=8080,8080

  // api.v2.host=localhost,localhost
  // api.v2.port=8080,8081

  /**
   * EXERCISE
   *
   * Using `ConfigSource.fromMap`, create a config source backed by a Scala map,
   * which contains "name" and "age" properties, equal to "John" and "42",
   * respectively.
   */
  lazy val configSourceMap: ConfigSource = 
    ConfigSource.fromMap(Map("name" -> "John", "age" -> "42"))

  /**
   * EXERCISE
   *
   * Using `ConfigSource.fromPropertyTree`, create a config source backed by a
   * `PropertyTree`.
   */
  lazy val configSourcePropertyTree: ConfigSource = ConfigSource.fromPropertyTree(propertyTree.head, "PropertyTree")
  lazy val propertyTree                           = PropertyTree.fromStringMap(Map("name" -> "John", "age" -> "42"), Some('.'), None)

  //
  // CONFIGSOURCE OPERATORS
  //

  /**
   * EXERCISE
   *
   * Using `ConfigSource#<>`, create a config source that reads from either
   * `configSourceMap` or `configSourcePropertyTree`.
   */
  lazy val configSourceMapOrPropertyTree: ConfigSource = 
    configSourceMap <> configSourcePropertyTree

  /**
   * EXERCISE
   *
   * Using `ConfigSource#at`, shift the following config source to the "prod"
   * node.
   */
  lazy val configSourceAtProd: ConfigSource = nestedConfigSource.at(PropertyTreePath.$("prod"))
  lazy val propertyTree2 =
    PropertyTree.fromStringMap(Map("prod.port" -> "123", "prod.host" -> "localhost"), Some('.'), None)
  lazy val nestedConfigSource = ConfigSource.fromPropertyTree(propertyTree2.head, "property-tree-2")

  //
  // SIMPLE CONFIGDESCRIPTOR CONSTRUCTORS
  //

  /**
   * EXERCISE
   *
   * Using `string`, create a `ConfigDescriptor` that describes a `String` read
   * from a property called "name".
   */
  lazy val stringName: ConfigDescriptor[String] = 
    ConfigDescriptor.string("name")

  /**
   * EXERCISE
   *
   * Using `int`, create a `ConfigDescriptor` that describes an `Int` read from
   * a property called "age".
   */
  lazy val intAge: ConfigDescriptor[Int] = 
    ConfigDescriptor.int("age")

  //
  // CONFIGDESCRIPTOR OPERATORS
  //

  /**
   * EXERCISE
   *
   * Using `ConfigDescriptor#optional`, turn the following
   * `ConfigDescriptor[Int]` into `ConfigDescriptor[Option[Int]]`.
   */
  lazy val optionalInt: ConfigDescriptor[Option[Int]] = 
    int("port").optional

  /**
   * EXERCISE
   *
   * Using `ConfigDescriptor#<>`, combine the `password` and `token` config
   * descriptors into one, which will try the left hand side first, but if that
   * fails, try the right hand side.
   * @return
   */
  lazy val passwordOrToken = password <> token
  val password             = string("password")
  val token                = string("token")

  /**
   * EXERCISE
   *
   * Using `ConfigDescriptor#zip`, combine the `stringName` and `intAge` config
   * descriptors into one, which will produce a tuple of both the name and the
   * age.
   */
  lazy val nameZipAge: ConfigDescriptor[(String, Int)] = 
    stringName zip (intAge ?? "The age of the person, as an integer")

  /**
   * EXERCISE
   *
   * Using `ConfigDescriptor#from`, specify the source of the `nameZipAge`
   * configuration data to be a Map literal (`ConfigSource.fromMap`) with "name"
   * equal to "Sherlock Holmes" and age equal to "42".
   */
  lazy val personFromMap: ConfigDescriptor[(String, Int)] = 
    nameZipAge.from(ConfigSource.fromMap(Map("name" -> "Sherlock Holmes", "age" -> "42")))


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
    implicit lazy val portDescriptor: ConfigDescriptor[Port] = 
      int("port").to[Port]
  }

  /**
   * EXERCISE
   *
   * Using `string`, `int`, and `zip`, followed by `to`, manually create a
   * `ConfigDescriptor` for `Database`.
   */
  final case class Database(url: String, port: Int)
  object Database {
    implicit lazy val configDescriptor: ConfigDescriptor[Database] =
      (string("url") zip int("port")).to[Database]
  }

  /**
   * EXERCISE
   *
   * Using `nested`, manually create a `ConfigDescriptor` for
   * `PersistenceConfig`.
   */
  final case class PersistenceConfig(userDB: Database, adminDB: Database)
  object PersistenceConfig {
    implicit lazy val configDescriptor: ConfigDescriptor[PersistenceConfig] = 
      (ConfigDescriptor.nested("userDB")(Database.configDescriptor) zip 
      ConfigDescriptor.nested("adminDB")(Database.configDescriptor)).to[PersistenceConfig]
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

    val s3Case        = string("bucket").to[S3]
    val urlCase       = string("value").to[URL]
    val localFileCase = string("file").to[LocalFile]

    implicit lazy val configDescriptor: ConfigDescriptor[FeedSource] =
      enumeration[FeedSource](s3Case, urlCase, localFileCase)
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
  lazy val feedSourceDerived: ConfigDescriptor[FeedSource] = Descriptor.descriptor[FeedSource]

  /**
   * EXERCISE
   *
   * Using `Descriptor.descriptor`, automatically derive a `ConfigDescriptor`
   * for `PersistenceConfig`.
   */
  lazy val PersistenceConfigDerived: ConfigDescriptor[PersistenceConfig] = 
    Descriptor.descriptor[PersistenceConfig]

  /**
   * EXERCISE
   *
   * Using the @name annotation, ensure the automatically derived config
   * descriptor for `Database2` can handle a username encoded with the property
   * name "user_name", and a password encoded with the property name "pwd".
   */
  final case class Database2(@name("user_name") username: String, @name("pwd") password: String)
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
  lazy val readPerson: IO[ReadError[String], (String, Int)] = 
    read(personFromMap)

  /**
   * EXERCISE
   *
   * Using `write`, write the value ("John Watson", 46) to a
   * `PropertyTree[String, String]`.
   */
  lazy val writePerson: Either[String, PropertyTree[String, String]] = 
    write(nameZipAge, ("John Watson", 46))

  /**
   * EXERCISE
   *
   * Using `generateDocs`, generate documentation for the `PersistenceConfig`
   * structure.
   */
  lazy val docPersistenceConfig: ConfigDocs = 
    generateDocs(PersistenceConfig.configDescriptor)

  /**
   * EXERCISE
   *
   * Using `generateReport`, generate a report for the `personFromMap`, given
   * the value ("John Watson", 46).
   */
  lazy val reportOfPersonFromMap: Either[String, ConfigDocs] = 
    generateReport(personFromMap, ("John Watson", 46))

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
    val configLayer: ZLayer[ZIOAppArgs, Nothing, UserDatabaseConfig] = 
      ZLayer {
        for {
          args   <- getArgs 
          source = ConfigSource.fromCommandLineArgs(args.toList)
          config <- read(UserDatabaseConfig.configDescriptor from source).orDie
        } yield config 
      } 

    def run: ZIO[ZIOAppArgs, Any, Any] =       
      Server.start(8080, usersHttpApp).provideSome[ZIOAppArgs](UserRepo.live, configLayer)
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
          def lookupUserById(id: String): Task[User] = ZIO.fail(new Exception("Not implemented"))
        }
      }
  }

  val usersHttpApp: HttpApp[UserRepo, Throwable] =
    Http.collectZIO[Request] { case Method.GET -> !! / "users" / id =>
      for {
        userRepo <- ZIO.service[UserRepo]
        user     <- userRepo.lookupUserById(id)
      } yield Response.json(user.toJson)
    }
}
