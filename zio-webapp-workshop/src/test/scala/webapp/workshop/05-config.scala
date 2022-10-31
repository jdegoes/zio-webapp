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

import zhttp.http.{int => _, _}

import zio.test._

import zio.config._

object ConfigSpec extends ZIOSpecDefault {

  import ConfigSection._

  def spec = suite("ConfigSpec") {
    def assertRoundtrip[A: ConfigDescriptor](a: A): IO[String, TestResult] = {
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

    suite("ConfigSource") {
      suite("constructors") {
        test("fromMap") {
          assertCompletes
        } +
          test("fromPropertyTree") {
            assertCompletes
          }
      } +
        suite("operators") {
          test("orElse") {
            assertCompletes
          } +
            test("at") {
              assertCompletes
            }
        }
    } +
      suite("ConfigDescriptor") {
        suite("constructors") {
          test("string") {
            assertReadFlat(stringName, Map("name" -> "Sherlock Holmes"), "Sherlock Holmes")
          } +
            test("int") {
              assertReadFlat(intAge, Map("age" -> "42"), 42)
            }
        } +
          suite("operators") {
            test("optional") {
              assertReadFlat(optionalInt, Map("port" -> "8080"), Some(8080))
            } +
              test("orElse") {
                for {
                  assert1 <- assertReadFlat(passwordOrToken, Map("password" -> "secret"), "secret")
                  assert2 <- assertReadFlat(passwordOrToken, Map("token" -> "secret"), "secret")
                } yield assert1 && assert2
              } +
              test("zip") {
                assertReadFlat(nameZipAge, Map("name" -> "Sherlock Holmes", "age" -> "42"), ("Sherlock Holmes", 42))
              } +
              test("from") {
                for {
                  person <- read(personFromMap)
                } yield assertTrue(person == ("Sherlock Holmes", 42))
              }
          } + suite("adt construction") {
            test("port") {
              assertRoundtrip(Port(3306))
            } +
              test("database") {
                assertRoundtrip(Database("localhost", 3306))
              } +
              test("PersistenceConfig") {
                assertRoundtrip(PersistenceConfig(Database("localhost", 3306), Database("localhost", 3306)))
              }
          } +
          suite("derivation") {
            test("sealed trait") {
              assertRoundtrip[FeedSource](FeedSource.S3("bucket"))(feedSourceDerived)
            } +
              test("case class") {
                assertRoundtrip(PersistenceConfig(Database("localhost", 3306), Database("localhost", 3306)))(
                  PersistenceConfigDerived
                )
              } +
              test("annotations") {
                assertReadFlat(
                  Database2.configDescriptor,
                  Map("user_name" -> "sherlock", "pwd" -> "holmes"),
                  Database2("sherlock", "holmes")
                )
              }
          }
      } +
      suite("features") {
        test("read") {
          for {
            person <- readPerson
          } yield assertTrue(person == ("Sherlock Holmes", 42))
        } +
          test("write") {
            for {
              expected <- ZIO.fromEither(write(personFromMap, ("John Watson", 46)))
              actual   <- ZIO.fromEither(writePerson)
            } yield assertTrue(actual == expected)
          } +
          test("generateDocs") {
            assertTrue(docPersistenceConfig == generateDocs(PersistenceConfig.configDescriptor))
          } +
          test("generateReport") {
            val expected = generateReport(personFromMap, ("John Watson", 46))

            assertTrue(reportOfPersonFromMap == expected)
          }
      } +
      suite("graduation") {
        test("end-to-end") {
          assertTrue(UsersApp.run != null)
        }
      }
  }
}
