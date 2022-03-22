/**
 * LOGGING
 *
 * ZIO 2.0 brings fully integrated logging into core. Since ZIO has a runtime
 * system with access to a wealth of information on actively executing ZIO
 * applications, this allows the logging system to provide rich information
 * about the context of logging messages and failures, taking effect-based
 * logging to a whole new level that cannot be achieved with separate loggers.
 *
 * Understanding how this logging works, and how to integrate it into your
 * application, including connecting it to your logging backend of choice, is
 * key to building web applications whose behavior can be understood and whose
 * failures can be identified and corrected.
 *
 * In this section, you will learn how to connect ZIO 2.0 logging to any
 * backend, how to have control over what gets logged, and how to effectively
 * use the logging interface exposed by ZIO 2.0 to provide good diagnostics for
 * your web application.
 */
package webapp.workshop

import zio._
import zio.test._
import zio.test.TestAspect._
import zio.logging._

object LoggingSpec extends DefaultRunnableSpec {

  import LoggingSection._

  def spec = suite("LoggingSpec") {
    def assertLogged(log: UIO[Any])(substrings: String*) =
      for {
        _      <- log
        output <- logOutput.get
      } yield assertTrue(substrings.forall(substring => output.exists(_.contains(substring))))

    def assertNotLogged(log: UIO[Any])(substrings: String*) =
      for {
        _      <- log
        output <- logOutput.get
      } yield assertTrue(!substrings.exists(substring => output.exists(_.contains(substring))))

    def assertLoggedWith(log: UIO[Any], format: LogFormat)(substrings: String*) =
      ZIO.runtimeConfig.flatMap { runtimeConfig =>
        val stringLogger = format.toLogger.map(line => appendLogMessage(line))
        val causeLogger  = stringLogger.contramap[Cause[Any]](_.prettyPrint)

        val loggerSet = stringLogger.toSet[String] ++ causeLogger.toSet[Cause[Any]]

        (for {
          _      <- log
          output <- logOutput.get
        } yield assertTrue(substrings.forall(substring => output.exists(_.contains(substring)))))
          .withRuntimeConfig(runtimeConfig.copy(loggers = loggerSet))
      }

    suite("RuntimeConfig") {
      test("log formatter") {
        val message = "All work and no play makes jack a dull boy"
        val output  = testStringLogFormatter.test(message)

        assertTrue(output.contains(message)) &&
        assertTrue(output.contains("INFO"))
      } +
        test("backend") {
          val message = "All work and no play makes jack a dull boy"

          assertLogged(ZIO.logInfo(message))(message)
        }
    } +
      suite("logging methods") {
        test("logInfo") {
          assertLogged(coffeeLogInfo)("coffee", "INFO")
        } +
          test("logDebug") {
            assertLogged(teaLogDebug)("tea", "DEBUG")
          } +
          test("logError") {
            assertLogged(milkLogError)("milk", "ERROR")
          } +
          test("log") {
            assertLogged(curryLog)("curry", "INFO")
          } +
          test("LogLevel.DEBUG") {
            assertLogged(logLevelDebug)("DEBUG")
          } +
          test("LogLevel.ERROR") {
            assertLogged(logLevelError)("ERROR")
          } +
          test("logSpan") {
            assertLogged(queryingDatabase)("database-query", "INFO")
          }
      } +
      suite("ZIO Logging") {
        test("LogFormat") {
          assertLoggedWith(ZIO.log("Testing"), myLogFormat)("Testing", "INFO")
        } +
          test("console") {
            assertNotLogged(testEffect)("Hello World!")
          }
      }
  } @@ sequential @@ TestAspect.runtimeConfig(testLoggerAspect) @@ after(logOutput.set(Chunk.empty))
}
