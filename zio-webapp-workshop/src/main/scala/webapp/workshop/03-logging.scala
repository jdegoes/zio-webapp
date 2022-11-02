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
import zio.logging._

object LoggingSection {
  def unsafeRun[E, A](io: IO[E, A]): A =
    Unsafe.unsafe(implicit u => Runtime.default.unsafe.run(io).getOrThrowFiberFailure())

  val logOutput: Ref[Chunk[String]] = unsafeRun(Ref.make(Chunk.empty))

  def appendLogMessage(s: String): Unit =
    unsafeRun(logOutput.update(_ :+ s))

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
        trace: Trace,
        fiberId: FiberId,
        logLevel: LogLevel,
        message: () => String,
        cause: Cause[Any],
        context: FiberRefs,
        spans: List[LogSpan],
        annotations: Map[String, String]
      ): String = {
        val causeStr = if (cause != Cause.empty) " " + cause.prettyPrint else ""
        val annsStr  = annotations.map { case (k, v) => s"$k=$v" }.mkString(",")

        s"[${logLevel.label}] ${spans.map(_.label).mkString("[", ", ", "]")} ${fiberId.threadName} ${message()}${causeStr} ${annsStr}"
      }
    }

  /**
   * EXERCISE
   *
   * Using `map`, modify the `testStringLogFormatter` so that for each log
   * message generated, it will append the message to `logOutput` using the
   * function `appendLogMessage`.
   */
  lazy val testStringLogger: ZLogger[String, Unit] = 
    testStringLogFormatter.map(println(_))

  lazy val testLoggerSet: ZLogger[String, Any] = testStringLogger

  /**
   * EXERCISE
   *
   * Using `Runtime.removeDefaultLoggers` and `ZIO#provide`, clear out default
   * loggers for the specified effet.
   */
  lazy val loggingNoDefault =
    (for {
      _ <- ZIO.log("This won't go anywhere!")
    } yield ()).provide(Runtime.removeDefaultLoggers, Runtime.addLogger(testStringLogger))

  //
  // LOGGING FRONTEND
  //

  /**
   * EXERCISE
   *
   * Using `ZIO.logInfo`, log a message that includes the word `coffee`.
   */
  lazy val coffeeLogInfo: UIO[Any] = 
    ZIO.logInfo("I love coffee")

  // ZIO.log("I love coffee")

  /**
   * EXERCISE
   *
   * Using `ZIO.logDebug`, log a message that includes the word `tea`.
   */
  lazy val teaLogDebug: UIO[Any] = 
    ZIO.logDebug("I love tea")

  /**
   * EXERCISE
   *
   * Using `ZIO.logError`, log a message that includes the word `milk`.
   */
  lazy val milkLogError: UIO[Any] = 
    ZIO.logError("I love milk")

  /**
   * EXERCISE
   *
   * Using `ZIO.log`, log a message that includes the word `curry`.
   */
  lazy val curryLog: UIO[Any] = 
    ZIO.log("I love curry")

  /**
   * EXERCISE
   *
   * Using `ZIO.logLevel`, set the log level for an inner `ZIO.log` message to
   * `DEBUG`.
   */
  lazy val logLevelDebug: UIO[Any] = 
    ZIO.logLevel(LogLevel.Debug) {
      ZIO.log("I love curry")
    }

  /**
   * EXERCISE
   *
   * Using `ZIO.logLevel`, set the log level for an inner `ZIO.log` message to
   * `ERROR`.
   */
  lazy val logLevelError: UIO[Any] = 
    ZIO.logLevel(LogLevel.Error) {
      ZIO.log("I love curry")
    }

  /**
   * Using `ZIO.logSpan` with a label of "database-query", log a message that
   * says "querying database".
   */
  lazy val queryingDatabase: UIO[Any] =
    ZIO.logAnnotate("user-id", "jdegoes") {
      ZIO.logSpan("database-query") {
        ZIO.log("querying database")
      }
    }

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
  lazy val myLogFormat: LogFormat = 
    LogFormat.level |-| LogFormat.timestamp.color(LogColor.CYAN) |-| LogFormat.quoted(LogFormat.line)

  /*
  LogFormat.colored |-| LogFormat.level |-| 
  LogFormat.spans |-| LogFormat.quoted(LogFormat.line) |-| LogFormat.cause
  */

  /**
   * EXERCISE
   *
   * Using `ZIO#provideLayer`, provide a layer to the effect that will use a
   * logger constructed using `zio.logging.console(myLogFormat)`.
   */
  lazy val loggedToConsole: ZIO[Any, Nothing, Unit] =
    (for {
      _ <- ZIO.log("Hello World!")
    } yield ()).provide(zio.logging.backend.SLF4J.slf4j(myLogFormat), zio.logging.console(myLogFormat))
}
