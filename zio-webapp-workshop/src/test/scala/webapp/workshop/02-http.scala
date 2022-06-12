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
import zio.test.TestAspect.ignore
import zio.json._
import zhttp.http._
import java.io.IOException
import io.netty.buffer.ByteBuf

object HttpSpec extends DefaultRunnableSpec {

  import HttpSection._

  final case class TodoAppTester[R](todoApp: HttpApp[R, Throwable]) {
    def getAll: ZIO[R, Throwable, Chunk[Todo]] =
      makeRequest[Chunk[Todo]](Request(Method.GET, URL(!! / "todos")))

    def getById(id: Long): ZIO[R, Throwable, Option[Todo]] =
      makeRequest[Todo](Request(Method.GET, URL(!! / "todos" / id.toString)))
        .map(Some(_))

    def create(description: String): ZIO[R, Throwable, Long] =
      makeRequest[TodoCreated](
        Request(
          method = Method.POST,
          url = URL(!! / "todos"),
          headers = Headers("Content-Type", HeaderValues.applicationJson),
          data = HttpData.fromString(TodoDescription(description).toJson)
        )
      ).map(_.id).mapError(_ => new RuntimeException("Cannot decode!"))

    def update(id: Long, description: String): ZIO[R, Throwable, Unit] =
      makeRequest[Option[Todo]](
        Request(
          method = Method.PUT,
          url = URL(!! / "todos" / id.toString),
          headers = Headers("Content-Type", HeaderValues.applicationJson),
          data = HttpData.fromString(TodoDescription(description).toJson)
        )
      ).unit.mapError(_ => new RuntimeException("Cannot decode!"))

    private def makeRequest[A: JsonDecoder](req: Request): ZIO[R, Throwable, A] =
      for {
        response <- todoApp(req).catchAll(_ => ZIO.succeed(Response.status(Status.INTERNAL_SERVER_ERROR)))
        result   <- response.as[A]
      } yield result
  }

  def spec = suite("HttpSpec") {
    def req(path: Path): Request = Request(url = URL(path))

    suite("tour") {
      test("hello world") {
        for {
          response <- helloWorld(Request(url = URL.fromString("/greet").toOption.get))
        } yield assertTrue(response == Response.text("Hello World!"))
      }
    } +
      suite("types") {
        test("string => int") {
          implicitly[StringToInt <:< Http[Any, Nothing, String, Int]]

          assertCompletes
        } +
          test("person => string") {
            implicitly[PersonNameExtractor <:< Http[Any, Nothing, Person, String]]

            assertCompletes
          } +
          test("function") {
            def test[A, B] =
              implicitly[HttpFunction[A, B] <:< Http[Any, Nothing, A, B]]

            assertCompletes
          } +
          test("zio") {
            def test[R, E, A] =
              implicitly[HttpZIO[R, E, A] <:< Http[R, E, Nothing, A]]

            assertCompletes
          } +
          test("app") {
            def test[R, E] =
              implicitly[HttpApp2[R, E] <:< Http[R, E, Request, Response]]

            assertCompletes
          } +
          test("uapp") {
            implicitly[UHttpApp2 <:< Http[Any, Nothing, Request, Response]]

            assertCompletes
          } +
          test("rapp") {
            def test[R] =
              implicitly[RHttpApp2[R] <:< Http[R, Throwable, Request, Response]]

            assertCompletes
          }
      } +
      suite("constructors") {
        suite("Http") {
          test("Http.empty") {
            for {
              result <- unhandled(()).flip
              cmp     = result == None
            } yield assertTrue(cmp)
          } +
            test("Http.succeed") {
              for {
                result <- httpSuccess(())
              } yield assertTrue(result == 42)
            } +
            test("Http.fail") {
              for {
                result <- httpFailure(()).flip
              } yield assertTrue(result == Some(42))
            } +
            test("Http.identity") {
              for {
                result <- stringIdentity("hello")
              } yield assertTrue(result == "hello")
            } +
            test("Http.fromZIO") {
              for {
                _    <- TestConsole.feedLines("hello, mother")
                line <- consoleHttp(())
              } yield assertTrue(line == "hello, mother")
            }
        } +
          suite("HttpApp") {
            test("constructor") {
              val expected = Request(
                method = Method.PUT,
                url = URL.fromString("http://ziowebapp.com/greet").toOption.get,
                data = HttpData.fromString("Hello World!")
              )
              exampleRequest1.getBodyAsString.zip(expected.getBodyAsString).map { case (exampleBody, expectedBody) =>
                assertTrue(exampleRequest1.method == expected.method) &&
                  assertTrue(exampleRequest1.url == expected.url) &&
                  assertTrue(exampleRequest1.getHeaders == expected.getHeaders) &&
                  assertTrue(exampleBody == expectedBody)
              }
            } +
              test("Response") {
                val expected =
                  Response(status = Status.OK, data = HttpData.fromString("Hello World!"))

                assertTrue(exampleResponse1 == expected)
              } +
              test("Http.ok") {
                for {
                  result <- httpOk(Request())
                } yield assertTrue(result.status == Status.OK)
              } +
              test("Http.notFound") {
                for {
                  result <- httpNotFound(Request())
                } yield assertTrue(result.status == Status.NOT_FOUND)
              } +
              test("Http.badRequest") {
                for {
                  result          <- httpBadRequest("bad request")(Request())
                  responseByteBuf <- result.data.toByteBuf
                  responseText     = responseByteBuf.toString(HTTP_CHARSET)
                } yield {
                  assertTrue(result.status == Status.BAD_REQUEST) &&
                  assertTrue(responseText == "bad request")
                }
              } +
              test("Http.error") {
                val error = HttpError.InternalServerError("boom")

                for {
                  result <- httpError(error)(Request())
                } yield assertTrue(result.status == error.status)
              } +
              test("Http.fromData") {
                val data = HttpData.fromString("Hello World!")

                val actual   = httpFromData(data)
                val expected = Http.fromData(data)

                assertTrue(actual == expected)
              } +
              test("Http.fromFile") {
                val file = new java.io.File("build.sbt")

                val actual   = httpFromFile(file)
                val expected = Http.fromFile(file)

                assertTrue(actual == expected)
              } +
              test("Http.response") {
                val actual   = httpFromResponse(exampleResponse1)
                val expected = Http.response(exampleResponse1)

                assertTrue(actual == expected)
              } +
              test("Http.responseZIO") {
                val zio = ZIO.succeed(Response())
                for {
                  actual   <- httpFromResponseZIO(zio)(Request())
                  expected <- Http.responseZIO(zio)(Request())
                } yield {
                  assertTrue(actual == expected)
                }
              }
          }
      } +
      suite("transformations") {
        test("Http#map") {
          for {
            result <- stringHttp(())
          } yield assertTrue(result == "42")
        } +
          test("Http#mapZIO") {
            for {
              _      <- TestConsole.feedLines("John")
              name   <- interactiveHttp(())
              output <- TestConsole.output.map(_.mkString)
            } yield assertTrue(output.contains("What is your name?")) &&
              assertTrue(name == "John")
          } +
          test("Http#as") {
            for {
              result <- unitHttp(())
            } yield assertTrue(result == ())
          } +
          test("Http#contramap") {
            for {
              result <- requestUsingHttp(exampleRequest1)
            } yield assertTrue(result == exampleRequest1.url.asString)
          }
      } +
      suite("combinations") {
        test("Http#defaultWith") {
          for {
            result1 <- usOrUkHttp(Country.US)
            result2 <- usOrUkHttp(Country.UK)
          } yield assertTrue(result1.contains("US")) && assertTrue(result2.contains("UK"))
        } +
          test("Http#orElse") {
            for {
              result1 <- usOrUkHttp(Country.US)
              result2 <- usOrUkHttp(Country.UK)
            } yield assertTrue(result1.contains("US")) && assertTrue(result2.contains("UK"))
          } +
          test("Http#andThen") {
            for {
              result <- digitsInNumber(1234)
            } yield assertTrue(result == 4)
          } +
          test("Http#zipRight") {
            for {
              _    <- TestConsole.feedLines("Sherlock Holmes")
              name <- promptAndRead(())
            } yield assertTrue(name == "Sherlock Holmes")
          } +
          test("Http#race") {
            for {
              result <- neverOrRightAway(())
            } yield assertTrue(result == 42)
          } +
          test("Http#flatMap") {
            for {
              result1 <- dispatchExample("route1")
              result2 <- dispatchExample("route2")
            } yield assertTrue(result1 == "Handled by route1") &&
              assertTrue(result2 == "Handled by route2")
          } +
          test("Http#catchAll") {
            for {
              result <- httpRecovered(())
            } yield assertTrue(result == "I succeeded")
          }
      } +
      suite("routes") {
        suite("Path") {
          test("root") {
            val expected = !!

            assertTrue(rootPath == expected)
          } +
            test("composite") {
              val expected = !! / "Baker" / "221B"

              assertTrue(compositePath == expected)
            } +
            test("extracted") {
              assertTrue(extractedStreet == "Baker") &&
              assertTrue(extractedNumber == "221B")
            } +
            test("request") {
              assertTrue(exampleRequestMatch == "It matches!")
            }
        } +
          suite("collectors") {
            test("Http.collect") {
              for {
                response1 <- greetAndFarewell(req(!! / "greet"))
                response2 <- greetAndFarewell(req(!! / "farewell"))
              } yield assertTrue(response1 == Response.text("Hello there!")) &&
                assertTrue(response2 == Response.text("Goodbye!"))
            } +
              test("Http.collectZIO") {
                for {
                  response1 <- lookupUserApp(req(!! / "users" / "sholmes"))
                  response2 <- lookupUserApp(req(!! / "users" / "jwatson"))
                } yield assertTrue(response1 == Response.text("Sherlock Holmes")) &&
                  assertTrue(response2 == Response.text("John Watson"))
              } +
              test("Http.collectHttp") {
                for {
                  response1 <- exampleRouting(req(!! / "greet"))
                  response2 <- exampleRouting(req(!! / "users" / "sholmes"))
                } yield assertTrue(response1 == Response.text("Hello there!")) &&
                  assertTrue(response2 == Response.text("Sherlock Holmes"))
              }
          }
      } +
      suite("server") {
        test("start") {
          assertTrue(helloWorldServer != null)
        }
      } +
      suite("graduation") {
        test("end-to-end") {
          val todo = TodoAppTester(todoApp)

          (for {
            initial <- todo.getAll
            id      <- todo.create("Buy milk")
            after   <- todo.getAll
            _       <- todo.update(id, "Buy almond milk")
            updated <- todo.getById(id)
          } yield assertTrue(initial == Chunk.empty) &&
            assertTrue(after(0).description == "Buy milk") &&
            assertTrue(updated.get.description == "Buy almond milk")).provideCustomLayer(TodoRepo.testLayer)
        }
      }
  }
}
