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

object LoggingSpec extends ZIOSpecDefault {
  val logOutput: Ref[Chunk[String]] = Runtime.default.unsafeRun(Ref.make(Chunk.empty))

  def appendLogMessage(s: String): Unit =
    Runtime.default.unsafeRun(logOutput.update(_ :+ s))

  //
  // RUNTIMECONFIG
  //

  /**
   * EXERCISE
   *
   * Create a `ZLogger[String, String]`, that is, one which accepts string
   * inputs, and produces string outputs. The format you choose is up to you,
   * but you should include the log level, the message, and the spans in the
   * output. You can choose to include other details such as line numbers, fiber
   * id, context, the fiber location, etc.
   */
  val testStringLogFormatter: ZLogger[String, String] =
    new ZLogger[String, String] {
      def apply(
        trace: ZTraceElement,
        fiberId: FiberId,
        logLevel: LogLevel,
        message: () => String,
        context: Map[FiberRef.Runtime[_], AnyRef],
        spans: List[LogSpan],
        location: ZTraceElement
      ): String = TODO
    }

  /**
   * EXERCISE
   *
   * Using `map`, modify the `testStringLogFormatter` so that for each log
   * message generated, it will append the message to `logOutput` using the
   * function `appendLogMessage`.
   */
  lazy val testStringLogger: ZLogger[String, Unit]    = testStringLogFormatter.TODO
  lazy val testCauseLogger: ZLogger[Cause[Any], Unit] = testStringLogger.contramap[Cause[Any]](_.prettyPrint)

  lazy val testLoggerSet: ZLogger.Set[String & Cause[Any], Any] =
    testStringLogger.toSet[String] ++ testCauseLogger.toSet[Cause[Any]]

  /**
   * EXERCISE
   *
   * Create a `RuntimeConfigAspect` which clears out any existing loggers, and
   * which installs the `testLoggerSet` into the `RuntimeConfig`.
   */
  lazy val testLoggerAspect: RuntimeConfigAspect =
    RuntimeConfigAspect { config =>
      config.TODO
    }

  //
  // LOGGING FRONTEND
  //

  /**
   * EXERCISE
   *
   * Using `ZIO.logInfo`, log a message that includes the word `coffee`.
   */
  lazy val coffeeLogInfo: UIO[Any] = TODO

  /**
   * EXERCISE
   *
   * Using `ZIO.logDebug`, log a message that includes the word `tea`.
   */
  lazy val teaLogDebug: UIO[Any] = TODO

  /**
   * EXERCISE
   *
   * Using `ZIO.logError`, log a message that includes the word `milk`.
   */
  lazy val milkLogError: UIO[Any] = TODO

  /**
   * EXERCISE
   *
   * Using `ZIO.log`, log a message that includes the word `curry`.
   */
  lazy val curryLog: UIO[Any] = TODO

  /**
   * EXERCISE
   *
   * Using `ZIO.logLevel`, set the log level for an inner `ZIO.log` message to
   * `DEBUG`.
   */
  lazy val logLevelDebug: UIO[Any] = TODO

  /**
   * EXERCISE
   *
   * Using `ZIO.logLevel`, set the log level for an inner `ZIO.log` message to
   * `ERROR`.
   */
  lazy val logLevelError: UIO[Any] = TODO

  //
  // ZIO LOGGING
  //

  /**
   * EXERCISE
   *
   * Using the constructors inside `LogFormat` and the operators provided by the
   * data type, construct a log format. The log format should nicely render the
   * major elements of log messages, including time stamp, message, and log
   * level.
   */
  lazy val myLogFormat: LogFormat = LogFormat.default

  /**
   * EXERCISE
   *
   * Using `ZIO#withRuntimeConfig`, replace the `RuntimeConfig` with a new one
   * that utilizes the `zio.logging.console(myLogFormat)` for the scope of the
   * effect provided below. below.
   */
  lazy val testEffect =
    (for {
      runtimeConfig <- ZIO.runtimeConfig
      _             <- ZIO.log("Hello World!")
    } yield ()).TODO

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
    } @@ ignore +
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
          }
      } @@ ignore +
      suite("ZIO Logging") {
        test("LogFormat") {
          assertLoggedWith(ZIO.log("Testing"), myLogFormat)("Testing", "INFO")
        } +
          test("console") {
            assertNotLogged(testEffect)("Hello World!")
          }
      }
  } @@ sequential @@ runtimeConfig(testLoggerAspect) @@ after(logOutput.set(Chunk.empty))
}
