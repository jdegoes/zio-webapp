/**
 * HTTP
 *
 * Scala has a variety of web server libraries and frameworks that work well
 * with ZIO, including Akka HTTP, http4s, Scalatra, and many others.
 *
 * Recently, however, the _ZIO HTTP_ project has achieved significant stability
 * milestones, and is currently used by a range of companies to power large-
 * scale, high-performance web applications using ZIO.
 *
 * In this section, you will learn the basics of using the _ZIO HTTP_ library to
 * create web applications.
 */
package webapp.workshop

import zio._
import zio.test._
import zhttp.http._

object HttpSpec extends ZIOSpecDefault {
  //
  // TOUR
  //
  val helloWorld =
    Http.collect[Request] { case Method.GET -> !! / "greet" =>
      Response.text("Hello World!")
    }

  //
  // TYPES
  //

  // type Http[-R, +E, -A, +B] = A => ZIO[R, Option[E], B]
  type HttpApp[-R, +E]                       = Http[R, E, Request, Response]
  type UHttpApp                              = HttpApp[Any, Nothing]
  type RHttpApp[-R]                          = HttpApp[R, Throwable]
  type UHttp[-A, +B]                         = Http[Any, Nothing, A, B]
  type SilentResponse[-E]                    = CanBeSilenced[E, Response]
  type ResponseZIO[-R, +E]                   = ZIO[R, E, Response]
  type Header                                = (CharSequence, CharSequence)
  type UMiddleware[+AIn, -BIn, -AOut, +BOut] = Middleware[Any, Nothing, AIn, BIn, AOut, BOut]

  def spec = suite("HttpSpec") {
    suite("tour") {
      test("hello world") {
        for {
          response <- helloWorld(Request(url = URL.fromString("/greet").toOption.get))
        } yield assertTrue(response == Response.text("Hello World!"))
      }
    } +
      suite("types") {
        test("example") {
          assertTrue(true)
        }
      } +
      suite("constructors") {
        test("example") {
          assertTrue(true)
        }
      } +
      suite("transformations") {
        test("example") {
          assertTrue(true)
        }
      } +
      suite("combinations") {
        test("example") {
          assertTrue(true)
        }
      }
  }
}
