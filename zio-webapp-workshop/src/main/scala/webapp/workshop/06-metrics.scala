package webapp.workshop

import zio._
import zio.test._
import zio.test.TestAspect.ignore

import zhttp.http._
import zhttp.http.middleware._

object MetricsSpec extends ZIOSpecDefault {

  /**
   * EXERCISE
   *
   * Using `ZIOMetric.count`, create a counter called `web-requests`.
   */
  lazy val webRequestsCounter: ZIOMetric.Counter[Any] = TODO

  /**
   * EXERCISE
   *
   * Using `ZIOMetric.observeDurations`, create a histogram called
   * `web-request-durations` that will keep track of the durations of web
   * requests.
   */
  lazy val requestDurations: ZIOMetric.Histogram[Any] = TODO

  /**
   * EXERCISE
   *
   * Using `ZIOMetric.setGaugeWith`, create a gauge called
   * `database-connections`, which will count database connections on an effect
   * producing an `Int` (which represents the number of active connections).
   */
  lazy val databaseConnectionGauge: ZIOMetric.Gauge[Int] = TODO

  /**
   * EXERCISE
   *
   * Using `ZIOMetric.observeSummary`, create a summary metric to be used for
   * tracking request durations on a sliding window of 60 minutes.
   */
  lazy val requestDurationsSummary: ZIOMetric.Summary[Double] = TODO

  /**
   * EXERCISE
   *
   * Using `ZIOMetric.occurrencesWith`, create a `SetCount` metric that keeps
   * track of the number of occurrences of each HTTP response status code.
   */
  lazy val httpResponseStatusCodes: ZIOMetric.SetCount[Int] = TODO

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

  def spec = suite("MetricsSpec") {
    suite("metrics construction") {
      test("web requests") {
        assertTrue(webRequestsCounter != null)
      } @@ ignore +
        test("request durations") {
          assertTrue(requestDurations != null)
        } @@ ignore +
        test("database connections") {
          assertTrue(databaseConnectionGauge != null)
        } @@ ignore +
        test("requests durations summary") {
          assertTrue(requestDurationsSummary != null)
        } @@ ignore +
        test("http response status codes") {
          assertTrue(httpResponseStatusCodes != null)
        } @@ ignore
    } +
      suite("metrics usage") {
        test("web requests") {
          assertTrue(webRequestsMiddleware != null)
        } @@ ignore +
          test("request durations") {
            assertTrue(requestsDurationsMiddleware != null)
          } @@ ignore +
          test("http response status codes") {
            assertTrue(httpResponseStatusCodesMiddleware != null)
          } @@ ignore
      } +
      suite("graduation") {
        test("end-to-end") {
          assertTrue(metricsMiddleware != null)
        } @@ ignore
      }
  }
}
