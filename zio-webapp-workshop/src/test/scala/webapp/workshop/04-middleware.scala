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

import zio.test._
import zhttp.http._

object MiddlewareSpec extends ZIOSpecDefault {

  import MiddlewareSection._

  def spec = suite("MiddlewareSpec") {
    suite("constructors") {
      test("cors") {
        assertTrue(corsMiddleware != null)
      } +
        test("debug") {
          assertTrue(debugMiddleware != null)
        } +
        test("cookie") {
          assertTrue(cookieMiddleware != null)
        } +
        test("timeout") {
          assertTrue(timeoutMiddleware != null)
        } +
        test("runBefore") {
          assertTrue(beforeMiddleware != null)
        } +
        test("runAfter") {
          assertTrue(afterMiddleware != null)
        } +
        test("basicAuth") {
          assertTrue(authMiddleware != null)
        } +
        test("codec") {
          val http: Http[Any, Nothing, UsersRequest, UsersResponse] = Http.collect {
            case UsersRequest.Create(name, email) => UsersResponse.Created(User(name, email, "abc123"))
            case UsersRequest.Get(id)             => UsersResponse.Got(User(id.toString, "", ""))
          }
          val http2 = http @@ json[UsersRequest, UsersResponse]

          assertTrue(http2 != null)
        }
    } +
      suite("operators") {
        test("Http.@@") {
          assertTrue(usersService != null)
        } +
          test("Middleware.andThen") {
            assertTrue(beforeAfterAndAuth1 != null)
          } +
          test("Middleware.combine") {
            assertTrue(beforeAfterAndAuth2 != null)
          }
      } +
      suite("graduation") {
        test("requestLogger") {
          assertTrue(requestLogger != null)
        } +
          test("responseLogger") {
            assertTrue(responseLogger != null)
          }
      }
  }
}
