/**
 * MIDDLEWARE
 *
 * ZIO HTTP provides a very powerful, compositional approach to building web
 * applications using ZIO. In order to eliminate boilerplate and increase
 * modularity, the library provides compositional, type-safe _middleware_, which
 * can be used to provide certain features uniformly across parts or all of your
 * web application endpoints.
 *
 * In this section, you will learn about common middleware that is built into
 * ZIO HTTP, and how to create your own custom middleware.
 */
package webapp.workshop

import java.io.IOException

import zio._
import zio.json._
import zhttp.http._
import zhttp.http.middleware.HttpMiddleware
import zhttp.http.middleware.Cors

object MiddlewareSection {

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

  // type Middleware[R, E, AIn, BIn, AOut, BOut] = 
  //   Http[R, E, AIn, BIn] => Http[R, E, AOut, BOut]

  //
  // CONSTRUCTORS
  //

  /**
   * EXERCISE
   *
   * Using `Middleware.cors()`, construct a middleware for Cross-Origin Resource
   * Sharing (CORS).
   */
  lazy val corsMiddleware: HttpMiddleware[Any, Nothing] = 
    Middleware.cors(Cors.CorsConfig())

  /**
   * EXERCISE
   *
   * Using `Middleware.debug`, construct a middleware for debugging status,
   * method, URL, and response timing.
   */
  lazy val debugMiddleware: HttpMiddleware[Console with Clock, IOException] = 
    Middleware.debug 

  /**
   * EXERCISE
   *
   * Using `Middleware.addCookie`, construct a middleware that adds the
   * specified cookie to responses.
   */
  val testCookie                                          = Cookie("sessionId", "12345")
  lazy val cookieMiddleware: HttpMiddleware[Any, Nothing] = 
    Middleware.addCookie(testCookie)

  /**
   * EXERCISE
   *
   * Using `Middleware.timeout`, construct a middleware that times out requests
   * that take longer than 2 minutes.
   */
  lazy val timeoutMiddleware: HttpMiddleware[Clock, Nothing] = 
    Middleware.timeout(2.minutes)

  /**
   * EXERCISE
   *
   * Using `Middleware.runBefore`, construct a middleware that prints out
   * "Starting to process request!" before the request is processed.
   */
  lazy val beforeMiddleware: HttpMiddleware[Console, Nothing] = 
    Middleware.runBefore(Console.printLine("Starting to process request!").orDie)

  /**
   * EXERCISE
   *
   * Using `Middleware.runAfter`, construct a middleware that prints out "Done
   * with request!" after each request.
   */
  lazy val afterMiddleware: HttpMiddleware[Console, Nothing] = 
    Middleware.runAfter(Console.printLine("Done with request!").orDie)

  /**
   * EXERCISE
   *
   * Using `Middleware.basicAuth`, construct a middleware that performs fake
   * authorization for any user who has password "abc123".
   */
  lazy val authMiddleware: HttpMiddleware[Any, Nothing] = 
    Middleware.basicAuth(c => c.upassword == "abc123")

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
      request => 
        (for {
          charSeq <- request.body.asCharSeq
          in      <- ZIO.fromEither(JsonDecoder[In].decodeJson(charSeq)).mapError(msg => new RuntimeException(msg))
        } yield in).orDie,
      response => ZIO.succeed(Response.text(JsonEncoder[Out].encodeJson(response, None)))
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
  lazy val usersServiceHttpApp: HttpApp[Any, Nothing] = 
    usersService @@ json[UsersRequest, UsersResponse]

  /**
   * EXERCISE
   *
   * Using `Middleware.>>>` compose `beforeMiddleware`, `afterMiddleware`, and
   * `authMiddleware` to construct a middleware that performs each function in
   * sequence.
   */
  lazy val beforeAfterAndAuth1: HttpMiddleware[Console, Nothing] = 
    beforeMiddleware >>> afterMiddleware >>> authMiddleware

  /**
   * EXERCISE
   *
   * Using `Middleware.++` compose `beforeMiddleware`, `afterMiddleware`, and
   * `authMiddleware` to construct a middleware that performs each function in
   * sequence.
   */
  lazy val beforeAfterAndAuth2: HttpMiddleware[Console, Nothing] = 
    beforeMiddleware ++ afterMiddleware ++ authMiddleware

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
    Middleware.interceptZIO[Request, Response](
      request => Clock.instant <* ZIO.log(request.toString()))(
        (response, start) => 
          for {
            end <- Clock.instant 
            duration = end.toEpochMilli - start.toEpochMilli
            _   <- ZIO.log(response.toString())
            _   <- ZIO.log(s"Duration of request: $duration")
          } yield response 
      )
  

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
    Middleware.interceptZIO[Request, Response]({ request =>
      proj(request.headers) match {
        case None => ZIO.succeed(Headers.empty)
        case Some(a) => logContext.update(_.annotate(logAnn, a)) *> ZIO.succeed(unproj(a))
      }
    }){ (response, headers) => ZIO.succeed(response.updateHeaders(_ ++ headers)) }

  val XTraceId: LogAnnotation[String] = LogAnnotation[String](
    name = "trace_id",
    combine = (_: String, r : String) => r,
    render = _.toString
  )

  /**
   * EXERCISE
   *
   * Using the `logged` constructor above, construct middleware that will
   * extract the `X-Trace-Id` header, parse it as a UUID, store it in the
   * `LogAnnotation.TraceId` log annotation, and then propagate it back to the
   * response as a `X-Trace-Id` header.
   */
  lazy val traceId: HttpMiddleware[Any, Nothing] =
    logged[String](XTraceId, 
      (headers: Headers) => headers.headerValue("X-Trace-Id"), 
      (value: String) => Headers("X-Trace-Id" -> value))
}
