/**
 * METRICS
 *
 * The uptime, latency, and throughput of web applications directly impacts a
 * company's bottom-line, leading to very high pressure on the engineering to
 * ensure applications are performing as specified 24x7.
 *
 * In order to facilitate these goals, best-practice web applications must
 * collect and report on dozens of key metrics that offer insight into any
 * potential problems and provide assistance to diagnosing production issues.
 *
 * Although ZIO applications can use any JVM metrics library for collecting and
 * reporting such metrics, ZIO 2.0 brings built-in metrics to the table,
 * including both standardized metrics for the ZIO runtime, as well as a
 * general-purpose, pluggable means of adding user-defined metrics.
 *
 * In this section, you will learn how to create your own custom metrics, create
 * ZIO HTTP middleware for exporting new metrics, and how to plug ZIO 2.0
 * metrics into an external monitoring application.
 */
package webapp.workshop

import zio._
import zio.metrics._

import zhttp.http.middleware._

object MetricsSection {

  /**
   * EXERCISE
   *
   * Using `Metric.counter`, create a counter called `web-requests`.
   */
  lazy val webRequestsCounter: Metric.Counter[Long] = TODO

  /**
   * EXERCISE
   *
   * Using `Metric.histogram`, create a histogram called `web-request-durations`
   * that will keep track of the durations of web requests.
   */
  lazy val requestDurations: Metric.Histogram[Double] = TODO

  /**
   * EXERCISE
   *
   * Using `Metric.gauge`, create a gauge called `database-connections`, which
   * will count database connections on an effect producing an `Int` (which
   * represents the number of active connections).
   */
  lazy val databaseConnectionGauge: Metric.Gauge[Double] = TODO

  /**
   * EXERCISE
   *
   * Using `Metric.summary`, create a summary metric to be used for tracking
   * request durations on a sliding window of 60 minutes.
   */
  lazy val requestDurationsSummary: Metric.Summary[Double] = TODO

  /**
   * EXERCISE
   *
   * Using `Metric.frequency`, create a `Frequency` metric that keeps track of
   * the number of occurrences of each HTTP response status code.
   */
  lazy val httpResponseStatusCodes: Metric.Frequency[Int] = TODO

  //
  // METRICS USAGE
  //

  /**
   * EXERCISE
   *
   * Create an `HttpMiddleware` that counts the number of requests, using the
   * `webRequestsCounter` metric.
   */
  lazy val webRequestsMiddleware: HttpMiddleware[Any, Nothing] = TODO

  /**
   * EXERCISE
   *
   * Create an `HttpMiddleware` that observes the durations of requests, using
   * the `requestDurations` metric.
   */
  lazy val requestsDurationsMiddleware: HttpMiddleware[Any, Nothing] = TODO

  /**
   * EXERCISE
   *
   * Augment this database connection tracker with the ability to adjust the
   * `databaseConnectionGauge` metric.
   */
  class DatabaseConnectionTracker(ref: Ref[Int]) {
    def increment: UIO[Int] = ref.updateAndGet(_ + 1)

    def decrement: UIO[Int] = ref.updateAndGet(_ - 1)
  }

  /**
   * EXERCISE
   *
   * Create an `HttpMiddleware` that observes the HTTP response status codes,
   * using the `httpResponseStatusCodes` metric.
   */
  lazy val httpResponseStatusCodesMiddleware: HttpMiddleware[Any, Nothing] = TODO

  //
  // GRADUATION
  //

  import zio.metrics.MetricClient

  /**
   * EXERCISE
   *
   * Using the `unsafeSnapshot` method of `MetricClient`, create an
   * `HttpMiddleware` that intercepts any request to `/healthcheck`, and which
   * dumps out the metrics as JSON.
   */
  lazy val metricsMiddleware: HttpMiddleware[Any, Nothing] = TODO
}
