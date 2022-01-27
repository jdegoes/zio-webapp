package webapp.workshop

import java.io.IOException

import zio._
import zio.json._
import zio.test._
import zio.test.TestAspect._
import zhttp.http._
import zhttp.http.middleware.HttpMiddleware
import zhttp.http.middleware.Cors

object MiddlewareSpec extends ZIOSpecDefault {

  final case class User(name: String, email: String, id: String)
  object User {
    implicit val codec: JsonCodec[User] = DeriveJsonCodec.gen[User]
  }
  sealed trait UsersRequest
  object UsersRequest {
    final case class Get(id: Int)                        extends UsersRequest
    final case class Create(name: String, email: String) extends UsersRequest

    implicit val codec: JsonCodec[UsersRequest] = DeriveJsonCodec.gen[UsersRequest]
  }
  sealed trait UsersResponse
  object UsersResponse {
    final case class Got(user: User)     extends UsersResponse
    final case class Created(user: User) extends UsersResponse

    implicit val codec: JsonCodec[UsersResponse] = DeriveJsonCodec.gen[UsersResponse]
  }

  //
  // TOUR
  //
  val helloWorld =
    Http.collect[Request] { case Method.GET -> !! / "greet" =>
      Response.text("Hello World!")
    } @@ Middleware.debug

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

  /**
   * EXERCISE
   *
   * Using `Middleware.codecZIO`, create a middleware that can decode JSON into
   * some type `In`, and encode some type `Out` into JSON, allowing the
   * definition of Http functions that do not work directly on Request/Response,
   * but rather some user-defined data t ypes.
   */
  def json[In: JsonDecoder, Out: JsonEncoder]: Middleware[Any, Nothing, In, Out, Request, Response] =
    Middleware.codecZIO[Request, Out](
      request => TODO: ZIO[Any, Nothing, In],
      response => TODO: ZIO[Any, Nothing, Response]
    )

  //
  // OPERATORS
  //

  /**
   * EXERCISE
   *
   * Using `Http.@@`, apply the `codecMiddleware` to transform the following
   * `Http` into an `HttpApp`.
   */
  val usersService: Http[Any, Nothing, UsersRequest, UsersResponse] = Http.collect {
    case UsersRequest.Create(name, email) => UsersResponse.Created(User(name, email, "abc123"))
    case UsersRequest.Get(id)             => UsersResponse.Got(User(id.toString, "", ""))
  }
  lazy val usersServiceHttpApp: HttpApp[Any, Nothing] = TODO

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
   * Using `Middleware.interceptZIO`, create middleware that logs requests using
   * `ZIO.log*` family of functions. For bonus points, expose a few LogFormat
   * values backed by LogAnnotations populated by the middleware.
   */
  lazy val requestLogger: HttpMiddleware[Any, Nothing] =
    Middleware.TODO

  /**
   * EXERCISE
   *
   * Using `Middleware.interceptZIO`, create middleware that logs responses
   * using `ZIO.log*` family of functions.
   */
  lazy val responseLogger: HttpMiddleware[Any, Nothing] = TODO

  import zio.logging._

  /**
   * EXERCISE
   *
   * Using `Middleware.interceptZIO`, create middleware that will populate the
   * `zio.logging.LogContext` with the specified annotation (if it can be
   * obtained from the request headers), and then which will attach response
   * headers backed by the annotation value.
   */
  def logged[A](
    logAnn: LogAnnotation[A],
    proj: Headers => Option[A],
    unproj: A => Headers
  ): HttpMiddleware[Any, Nothing] =
    Middleware.interceptZIO.TODO

  /**
   * EXERCISE
   *
   * Using the `logged` constructor above, construct middleware that will
   * extract the `X-Trace-Id` header, parse it as a UUID, store it in the
   * `LogAnnotation.TraceId` log annotation, and then propagate it back to the
   * response as a `X-Trace-Id` header.
   */
  lazy val traceId: HttpMiddleware[Any, Nothing] =
    TODO

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
        } @@ ignore +
        test("codec") {
          val http: Http[Any, Nothing, UsersRequest, UsersResponse] = Http.collect {
            case UsersRequest.Create(name, email) => UsersResponse.Created(User(name, email, "abc123"))
            case UsersRequest.Get(id)             => UsersResponse.Got(User(id.toString, "", ""))
          }
          val http2 = http @@ json[UsersRequest, UsersResponse]

          assertTrue(http2 != null)
        } @@ ignore
    } +
      suite("operators") {
        test("Http.@@") {
          assertTrue(usersService != null)
        } @@ ignore +
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
