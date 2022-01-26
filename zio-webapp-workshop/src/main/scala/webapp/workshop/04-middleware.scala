package webapp.workshop

import java.io.IOException

import zio._
import zio.test._
import zio.test.TestAspect._
import zhttp.http._
import zhttp.http.middleware.HttpMiddleware

object MiddlewareSpec extends ZIOSpecDefault {

  //
  // TYPES
  //

  // type Middleware[R, E, AIn, BIn, AOut, BOut] = Http[R, E, AIn, BIn] => Http[R, E, AOut, BOut]

  //
  // CONSTRUCTORS
  //

  /**
   * EXERCISE
   *
   * Using `Middleware.cors()`, construct a middleware for Cross-Origin Resource
   * Sharing (CORS).
   */
  lazy val corsMiddleware: HttpMiddleware[Any, Nothing] = TODO

  /**
   * EXERCISE
   *
   * Using `Middleware.debug`, construct a middleware for debugging status,
   * method, URL, and response timing.
   */
  lazy val debugMiddleware: HttpMiddleware[Console with Clock, IOException] = TODO

  /**
   * EXERCISE
   *
   * Using `Middleware.addCookie`, construct a middleware that adds the
   * specified cookie to responses.
   */
  val testCookie                                          = Cookie("sessionId", "12345")
  lazy val cookieMiddleware: HttpMiddleware[Any, Nothing] = TODO

  /**
   * EXERCISE
   *
   * Using `Middleware.timeout`, construct a middleware that times out requests
   * that take longer than 2 minutes.
   */
  lazy val timeoutMiddleware: HttpMiddleware[Clock, Nothing] = TODO

  /**
   * EXERCISE
   *
   * Using `Middleware.runBefore`, construct a middleware that prints out
   * "Starting to process request!" before the request is processed.
   */
  lazy val beforeMiddleware: HttpMiddleware[Console, Nothing] = TODO

  /**
   * EXERCISE
   *
   * Using `Middleware.runAfter`, construct a middleware that prints out "Done
   * with request!" after each request.
   */
  lazy val afterMiddleware: HttpMiddleware[Console, Nothing] = TODO

  /**
   * EXERCISE
   *
   * Using `Middleware.basicAuth`, construct a middleware that performs fake
   * authorization for any user who has password "abc123".
   */
  lazy val authMiddleware: HttpMiddleware[Any, Nothing] = TODO

  //
  // OPERATORS
  //

  /**
   * EXERCISE
   *
   * Using `Middleware.>>>` compose `beforeMiddleware`, `afterMiddleware`, and
   * `authMiddleware` to construct a middleware that performs each function in
   * sequence.
   */
  lazy val beforeAfterAndAuth1: HttpMiddleware[Console, Nothing] = TODO

  /**
   * EXERCISE
   *
   * Using `Middleware.++` compose `beforeMiddleware`, `afterMiddleware`, and
   * `authMiddleware` to construct a middleware that performs each function in
   * sequence.
   */
  lazy val beforeAfterAndAuth2: HttpMiddleware[Console, Nothing] = TODO

  //
  // GRADUATION
  //

  /**
   * EXERCISE
   *
   * Create middleware that logs requests using `ZIO.log*` family of functions.
   * For bonus points, integrate with ZIO Logging's LogFormat.
   */
  lazy val requestLogger: HttpMiddleware[Any, Nothing] = ???

  /**
   * EXERCISE
   *
   * Create middleware that logs responses using `ZIO.log*` family of functions.
   * For bonus points, integrate with ZIO Logging's LogFormat.
   */
  lazy val responseLogger: HttpMiddleware[Any, Nothing] = ???

  def spec = suite("MiddlewareSpec") {
    suite("constructors") {
      test("cors") {
        assertTrue(corsMiddleware != null)
      } @@ ignore +
        test("debug") {
          assertTrue(debugMiddleware != null)
        } @@ ignore +
        test("cookie") {
          assertTrue(cookieMiddleware != null)
        } @@ ignore +
        test("timeout") {
          assertTrue(timeoutMiddleware != null)
        } @@ ignore +
        test("runBefore") {
          assertTrue(beforeMiddleware != null)
        } @@ ignore +
        test("runAfter") {
          assertTrue(afterMiddleware != null)
        } @@ ignore +
        test("basicAuth") {
          assertTrue(authMiddleware != null)
        } @@ ignore
    } +
      suite("operators") {
        test("Middleware.andThen") {
          assertTrue(beforeAfterAndAuth1 != null)
        } @@ ignore +
          test("Middleware.combine") {
            assertTrue(beforeAfterAndAuth2 != null)
          } @@ ignore
      } +
      suite("graduation") {
        test("requestLogger") {
          assertTrue(requestLogger != null)
        } @@ ignore +
          test("responseLogger") {
            assertTrue(responseLogger != null)
          } @@ ignore
      }
  }
}
