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
import zio.test._
import zio.test.TestAspect.ignore

import zhttp.http._
import zhttp.http.middleware._

object MetricsSpec extends ZIOSpecDefault {

  import MetricsSection._

  def spec = suite("MetricsSpec") {
    suite("metrics construction") {
      test("web requests") {
        assertTrue(webRequestsCounter != null)
      } +
        test("request durations") {
          assertTrue(requestDurations != null)
        } +
        test("database connections") {
          assertTrue(databaseConnectionGauge != null)
        } +
        test("requests durations summary") {
          assertTrue(requestDurationsSummary != null)
        } +
        test("http response status codes") {
          assertTrue(httpResponseStatusCodes != null)
        }
    } +
      suite("metrics usage") {
        test("web requests") {
          assertTrue(webRequestsMiddleware != null)
        } +
          test("request durations") {
            assertTrue(requestsDurationsMiddleware != null)
          } +
          test("http response status codes") {
            assertTrue(httpResponseStatusCodesMiddleware != null)
          }
      } +
      suite("graduation") {
        test("end-to-end") {
          assertTrue(metricsMiddleware != null)
        }
      }
  }
}
