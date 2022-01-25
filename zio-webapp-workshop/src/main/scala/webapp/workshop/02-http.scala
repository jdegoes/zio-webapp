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
import zio.json._
import zhttp.http._
import java.io.IOException
import io.netty.buffer.ByteBuf

object HttpSpec extends ZIOSpecDefault {
  final case class Person(name: String, age: Int)
  object Person {
    implicit val codec: JsonCodec[Person] = DeriveJsonCodec.gen[Person]
  }
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

  // Http[-R, +E, -A, +B] <: A => ZIO[R, Option[E], B]

  /**
   * EXERCISE
   *
   * Define an `Http` type that accepts a `String`, cannot fail with any typed
   * error, does not use the environment, and returns an `Int`.
   */
  type StringToInt = TODO

  /**
   * EXERCISE
   *
   * Define an `Http` type that accepts a `Person`, cannot fail with any typed
   * error, does not use the environment, and returns a `String`.
   */
  type PersonNameExtractor = TODO

  /**
   * EXERCISE
   *
   * Define an `Http` type that accepts an `A`, cannot fail with any typed
   * error, does not use the environment, and returns a `B`.
   */
  type HttpFunction[-A, +B] = TODO

  /**
   * EXERCISE
   *
   * Define an `Http` type that does not accept anything, can fail with an error
   * of type `E`, uses an environment `R`, and returns an `A`.
   */
  type HttpZIO[-R, +E, +A] = TODO

  /**
   * EXERCISE
   *
   * Define an `Http` type that accepts a `Request`, can fail with an error of
   * type `E`, uses an environment `R`, and returns a `Response`.
   */
  type HttpApp2[-R, +E] = TODO

  /**
   * EXERCISE
   *
   * Define a specialization of `HttpApp2` that does not use an environment, and
   * which cannot fail with a typed error.
   */
  type UHttpApp2 = TODO

  /**
   * Define a specialization of `HttpApp2` that uses an environment `R`, and
   * which can fail with an error of type `Throwable`.
   */
  type RHttpApp2[-R] = TODO

  //
  // HTTP CONSTRUCTORS
  //

  /**
   * EXERCISE
   *
   * Use `Http.empty` to construct an `Http` that does not handle any inputs.
   */
  def unhandled: Http[Any, Nothing, Any, Nothing] = TODO

  /**
   * EXERCISE
   *
   * Use `Http.succeed` to construct an `Http` that succeeds with the constant
   * value `42`.
   */
  def httpSuccess: Http[Any, Nothing, Any, Int] = TODO

  /**
   * EXERCISE
   *
   * Use `Http.fail` to construct an `Http` that fails with the constant value
   * `42`.
   */
  def httpFailure: Http[Any, Int, Any, Nothing] = TODO

  /**
   * EXERCISE
   *
   * Use `Http.identity` to create an Http whose input is a string, and which
   * succeeds with that same string.
   */
  def stringIdentity: Http[Any, Nothing, String, String] = TODO

  /**
   * EXERCISE
   *
   * Use `Http.fromZIO` to turn `Console.readLine` into an `Http` that succeeds
   * with a line of text from the console.
   */
  def consoleHttp: Http[Console, IOException, Any, String] = TODO

  //
  // HTTPAPP CONSTRUCTORS
  //

  /**
   * EXERCISE
   *
   * Create a `Request` whose method is `PUT`, whose URL is
   * "http://ziowebapp.com/greet", whose headers are empty, and whose data is
   * the plain text string "Hello World!".
   */
  lazy val exampleRequest1: Request = TODO

  /**
   * EXERCISE
   *
   * Create a `Response` whose status code is `200`, whose headers are empty,
   * and whose body is the plain text string "Hello World!".
   */
  lazy val exampleResponse1: Response = TODO

  /**
   * EXERCISE
   *
   * Create an `HttpApp` that returns an OK status.
   */
  lazy val httpOk: HttpApp[Any, Nothing] = TODO

  /**
   * EXERCISE
   *
   * Create an `HttpApp` that returns a NOT_FOUND status.
   */
  lazy val httpNotFound: HttpApp[Any, Nothing] = TODO

  /**
   * EXERCISE
   *
   * Create an `HttpApp` that returns a `Response` with the specified error.
   */
  def httpError(cause: HttpError): HttpApp[Any, Nothing] = TODO

  /**
   * EXERCISE
   *
   * Create a `HttpApp` that returns a BAD_REQUEST status.
   */
  def httpBadRequest(msg: String): HttpApp[Any, Nothing] = TODO

  /**
   * EXERCISE
   *
   * Create a `HttpApp` that successfully returns the specified data. Hint: See
   * the HttpData constructors.
   */
  def httpFromData(data: HttpData): HttpApp[Any, Nothing] = TODO

  /**
   * EXERCISE
   *
   * Create a `HttpApp` that successfully returns a response based on the
   * contents of the specified file. Hint: See the HttpData constructors.
   */
  def httpFromFile(file: java.io.File): HttpApp[Any, Nothing] = TODO

  /**
   * EXERCISE
   *
   * Create a `HttpApp` that successfully returns the provided response.
   */
  def httpFromResponse(response: Response): HttpApp[Any, Nothing] = TODO

  /**
   * EXERCISE
   *
   * Create a `HttpApp` that successfully returns the provided, effectfully
   * computed response.
   */
  def httpFromResponseZIO[R, E](response: ZIO[R, E, Response]): HttpApp[R, E] = TODO

  //
  // TRANSFORMATIONS
  //

  /**
   * EXERCISE
   *
   * Using `Http#map`, turn the `intHttp` (which returns an integer) into one
   * that returns a string, namely, the string rendering of the integer.
   */
  val intHttp                                          = Http.succeed(42)
  lazy val stringHttp: Http[Any, Nothing, Any, String] = intHttp.TODO

  /**
   * EXERCISE
   *
   * Using `Http#mapZIO`, print out the return value of `promptHttp` (which is a
   * string), and then use `Console.readLine` to read a line of text from the
   * console.
   */
  val promptHttp                                                    = Http.succeed("What is your name?")
  lazy val interactiveHttp: Http[Console, IOException, Any, String] = TODO

  /**
   * EXERCISE
   *
   * Using `Http#as`, map the integer return value of `intHttp` into the
   * constant unit value (`()`).
   */
  lazy val unitHttp: Http[Any, Nothing, Any, Unit] = intHttp.TODO

  /**
   * EXERCISE
   *
   * Using `Http#contramap`, change the input of the provided `Http` from `URL`,
   * to `Request` (which contains a `URL` inside of it).
   */
  val httpDataUsingHttp: Http[Any, Throwable, URL, String] =
    Http.identity[URL].map(_.asString)
  lazy val requestUsingHttp: Http[Any, Throwable, Request, String] =
    httpDataUsingHttp.TODO

  //
  // COMBINATIONS
  //
  def lift[A, B](pf: PartialFunction[A, B]): Http[Any, Nothing, A, B] =
    for {
      a <- Http.identity[A]
      b <- if (pf.isDefinedAt(a)) Http.succeed(pf(a)) else Http.empty
    } yield b

  def liftEither[E, A, B](f: A => Either[E, B]): Http[Any, E, A, B] =
    for {
      a <- Http.identity[A]
      b <- f(a).fold(Http.fail(_), Http.succeed(_))
    } yield b

  sealed trait Country
  object Country {
    case object US extends Country
    case object UK extends Country
  }

  /**
   * EXERCISE
   *
   * Using `Http#++`, compose the following two Http into one, in such a fashion
   * that if the first one does not handle some input, the second one will
   * handle it.
   */
  val usHttp: Http[Any, Nothing, Country, String]          = lift { case Country.US => "I handle the US" }
  val ukHttp: Http[Any, Nothing, Country, String]          = lift { case Country.UK => "I handle the UK" }
  lazy val usOrUkHttp: Http[Any, Nothing, Country, String] = usHttp.TODO

  /**
   * EXERCISE
   *
   * Using `Http#<>`, compose the following two Http into one, in such a fashion
   * that if the first one fails to handle the input, the second one will be
   * given a chance to handle it.
   *
   * BONUS: Describe the differences between `++` and `<>`.
   */
  val usOrFail: Http[Any, String, Country, String] = liftEither {
    case Country.US => Right("I handle the US"); case _ => Left("I only handle the US")
  }
  val ukOrFail: Http[Any, String, Country, String] = liftEither {
    case Country.UK => Right("I handle the UK"); case _ => Left("I only handle the UK")
  }
  lazy val usOrUkOrFail: Http[Any, String, Country, String] = usOrFail.TODO

  /**
   * EXERCISE
   *
   * Using `Http#>>>`, compose the following two Http into one, such that the
   * output of the first one is the input of the second.
   */
  val numberToString: Http[Any, Nothing, Int, String]   = Http.fromFunction[Int](_.toString)
  val stringToLength: Http[Any, Nothing, String, Int]   = Http.fromFunction[String](_.length)
  lazy val digitsInNumber: Http[Any, Nothing, Int, Int] = numberToString.TODO

  /**
   * EXERCISE
   *
   * Using `Http#*>`, compose the following two Http into one, such that the
   * resulting `Http` produces the output of the right hand side.
   */
  val printPrompt                                                 = Http.fromZIO(Console.printLine("What is your name?"))
  val readAnswer                                                  = Http.fromZIO(Console.readLine)
  lazy val promptAndRead: Http[Console, IOException, Any, String] = printPrompt.TODO

  /**
   * EXERCISE
   *
   * Using `Http#race`, compose the following two Http into one, such that the
   * resulting `Http` will complete with whichever `Http` completes first.
   */
  val httpNever                                           = Http.fromZIO(ZIO.never)
  val rightAway                                           = Http.succeed(42)
  lazy val neverOrRightAway: Http[Any, Nothing, Any, Int] = httpNever.TODO

  /**
   * EXERCISE
   *
   * Using `flatMap` (directly or with a `for` comprehension), implement the
   * following combinator.
   */
  def dispatch[R, E, A, B](options: (A, Http[R, E, A, B])*): Http[R, E, A, B] =
    TODO
  lazy val dispatchExample: Http[Any, Nothing, String, String] = dispatch(
    "route1" -> Http.succeed("Handled by route1"),
    "route2" -> Http.succeed("Handled by route2")
  )

  /**
   * EXERCISE
   *
   * Using `Http#catchAll`, recover from this failed `Http` by switching to the
   * provided successful `Http`.
   */
  val httpFailed: Http[Any, String, Any, Nothing]         = Http.fail("I failed")
  val httpSucceeded: Http[Any, Nothing, Any, String]      = Http.succeed("I succeeded")
  lazy val httpRecovered: Http[Any, Nothing, Any, String] = httpFailed.TODO

  //
  // ROUTES
  //

  /**
   * EXERCISE
   *
   * Using `!!` (Path.End), construct the root path.
   */
  lazy val rootPath: Path = TODO

  /**
   * EXERCISE
   *
   * Using `/`, construct a path `/Baker/221B`.
   */
  lazy val compositePath: Path = TODO

  /**
   * EXERCISE
   *
   * Pattern match on `compositePath` and extract out both components into a
   * tuple.
   */
  lazy val (extractedStreet, extractedNumber) = (compositePath match {
    case _ => throw new RuntimeException("Unexpected path")
  }): (String, String)

  def spec = suite("HttpSpec") {
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
              implicitly[HttpZIO[R, E, A] <:< Http[R, E, String, A]]

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

              assertTrue(exampleRequest1 == expected)
            } +
              test("Response") {
                val expected =
                  Response(status = Status.OK, data = HttpData.fromString("Hello World!"))

                assertTrue(exampleResponse1 == expected)
              } +
              test("Http.ok") {
                for {
                  result <- httpOk(exampleRequest1)
                } yield assertTrue(result.status == Status.OK)
              } +
              test("Http.notFound") {
                for {
                  result <- httpOk(exampleRequest1)
                } yield assertTrue(result.status == Status.NOT_FOUND)
              } +
              test("Http.badRequest") {
                for {
                  result <- httpOk(exampleRequest1)
                } yield assertTrue(result.status == Status.BAD_REQUEST)
              } +
              test("Http.error") {
                val error = HttpError.InternalServerError("boom")

                for {
                  result <- httpError(error)(exampleRequest1)
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
                val zio = ZIO.succeed(exampleResponse1)

                val actual   = httpFromResponseZIO(zio)
                val expected = Http.responseZIO(zio)

                assertTrue(actual == expected)
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
            } yield assertTrue(result != exampleRequest1.url.asString)
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
            } yield assertTrue(result == "I recovered")
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
            }
        } +
          suite("Http.collect") {
            test("constructor") {
              assertTrue(true)
            }
          } +
          suite("Http.collectZIO") {
            test("constructor") {
              assertTrue(true)
            }
          } +
          suite("Http.collectHttp") {
            test("constructor") {
              assertTrue(true)
            }
          }
      } +
      suite("server") {
        test("example") {
          assertTrue(true)
        }
      } +
      suite("challenges") {
        test("example") {
          assertTrue(true)
        }
      }
  }
}
