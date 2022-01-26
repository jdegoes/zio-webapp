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

  /**
   * EXERCISE
   *
   * Using the following pattern match as a reference, pattern match against the
   * provided request using literals, and if successful, return the string "It
   * matches!".
   */
  Request(method = Method.GET, url = URL(!! / "Baker" / "221B")) match {
    case Method.GET -> !! / "Baker" / "221B" => "It matches!"
  }
  val exampleRequest2                  = Request(method = Method.POST, url = URL(!! / "users" / "sholmes"))
  lazy val exampleRequestMatch: String = exampleRequest2.TODO

  /**
   * EXERCISE
   *
   * Using `Http.collect`, construct an `HttpApp` that will handle the following
   * paths:
   *
   * {{{
   * /greet
   * /farewell
   * }}}
   *
   * The `HttpApp` should respond with the greeting "Hello there!" (for the
   * first path), and the farewell "Goodbye!" (for the second path).
   */
  lazy val greetAndFarewell: HttpApp[Any, Nothing] =
    Http.collect[Request].TODO

  object UserRepo {
    def lookupUser(id: String): ZIO[Any, Option[Nothing], Person] = ZIO.fromOption(id match {
      case "sholmes" => Some(Person("Sherlock Holmes", 43))
      case "jwatson" => Some(Person("John Watson", 46))
      case _         => None
    })
  }

  /**
   * EXERCISE
   *
   * Using `Http.collectZIO`, construct an `HttpApp` that will handle the route
   * `/users/:id` and use `lookupUser` to lookup the user corresponding to the
   * specified id and return their full name as plain text.
   */
  lazy val lookupUserApp: HttpApp[Any, Option[Nothing]] =
    Http.collectZIO[Request].TODO

  /**
   * EXERCISE
   *
   * Using `Http.collectHttp`, collect the `greetAndFarewell` and
   * `lookupUserApp` HttpApps into one by using the provided routing table.
   */
  val exampleRoutingTable: PartialFunction[Request, HttpApp[Any, Option[Nothing]]] = {
    case Method.GET -> !! / "users" => lookupUserApp
    case other                      => greetAndFarewell
  }
  lazy val exampleRouting: HttpApp[Any, Option[Nothing]] =
    Http.collectHttp(exampleRoutingTable)

  //
  // SERVERS
  //

  /**
   * EXERCISE
   *
   * Using `zhttp.service.Server` and its `startDefault` method, start up the
   * `helloWorld` app.
   */
  import zhttp.service._
  type ServerType = ZIO[Any, Throwable, Nothing]
  lazy val helloWorldServer: ServerType = helloWorld.TODO

  //
  // GRADUATION
  //

  /**
   * EXERCISE
   *
   * Create a `JsonCodec` for the following TODO class.
   */
  final case class Todo(id: Long, description: String, created: java.time.Instant, modified: java.time.Instant)
  object Todo {
    implicit lazy val jsonCodec: JsonCodec[Todo] = TODO
  }

  /**
   * EXERCISE
   *
   * Create a `JsonCodec` for the following `TodoDescription` class.
   */
  final case class TodoDescription(description: String)
  object TodoDescription {
    implicit lazy val jsonCodec: JsonCodec[TodoDescription] = TODO
  }

  /**
   * EXERCISE
   *
   * Create a `JsonCodec` for the following `TodoCreated` class.
   */
  final case class TodoCreated(id: Long)
  object TodoCreated {
    implicit lazy val jsonCodec: JsonCodec[TodoCreated] = TODO
  }

  final case class TodoRepo(clock: Clock, idGen: Ref[Long], todos: Ref[Map[Long, Todo]]) {
    def getAll: UIO[Chunk[Todo]] =
      for {
        _     <- ZIO.debug("Getting all todos")
        chunk <- todos.get.map(map => Chunk.fromIterable(map.values))
        _     <- ZIO.debug(s"Retrieved todos $chunk")
      } yield chunk

    def create(description: => String): UIO[Todo] =
      for {
        _       <- ZIO.debug(s"Creating todo with description: $description")
        id      <- idGen.updateAndGet(_ + 1)
        created <- clock.instant
        todo     = Todo(id, description, created, created)
        _       <- todos.update(_ + (id -> todo))
      } yield todo

    def getById(id: Long): UIO[Option[Todo]] =
      for {
        _   <- ZIO.debug(s"Getting todo with id: $id")
        map <- todos.get
      } yield map.get(id)

    def updateTodo(id: Long, description: => String): UIO[Option[Todo]] =
      for {
        _        <- ZIO.debug(s"Updating todo with id: $id and description: $description")
        modified <- clock.instant
        todo <- todos.modify { map =>
                  map.get(id) match {
                    case Some(value) =>
                      val newTodo = value.copy(description = description, modified = modified)
                      (Some(newTodo), map + (id -> newTodo))

                    case None => (None, map)
                  }
                }
      } yield todo
  }
  object TodoRepo {
    val testLayer: ZLayer[Clock, Nothing, TodoRepo] =
      ZLayer {
        for {
          clock <- ZIO.service[Clock]
          idGen <- Ref.make(0L)
          todos <- Ref.make(Map.empty[Long, Todo])
        } yield TodoRepo(clock, idGen, todos)
      }

    def getAll: ZIO[TodoRepo, Nothing, Chunk[Todo]] = ZIO.serviceWithZIO(_.getAll)

    def create(description: => String): ZIO[TodoRepo, Nothing, Todo] = ZIO.serviceWithZIO(_.create(description))

    def getById(id: Long): ZIO[TodoRepo, Nothing, Option[Todo]] = ZIO.serviceWithZIO(_.getById(id))

    def updateTodo(id: Long, description: => String): ZIO[TodoRepo, Nothing, Option[Todo]] =
      ZIO.serviceWithZIO(_.updateTodo(id, description))
  }

  /**
   * EXERCISE
   *
   * Create an extension method that lets you convert any JSON-encodable value
   * to a `Response`, using the correct `Content-Type` header.
   */
  implicit class AnyExtensions[A](val any: A) extends AnyVal {
    def toResponse(implicit jsonEncoder: JsonEncoder[A]): Response = TODO
  }

  /**
   * EXERCISE
   *
   * Create an extension method that lets you convert any request to a type
   * having a `JsonDecoder`.
   */
  implicit class RequestExtensions(val request: Request) extends AnyVal {
    def as[A: JsonDecoder]: Task[A] = TODO
  }

  /**
   * EXERCISE
   *
   * Create an `HttpApp` that will handle the following routes: {{ GET /todos
   * Lists all of the todos as a JSON Array GET /todos/:id Gets the todo with
   * the specified id POST /todos Creates a new todo PUT /todos/:id Updates the
   * todo with the specified id }}
   */
  lazy val todoApp: HttpApp[TodoRepo, Throwable] =
    Http.collectZIO[Request].TODO

  implicit class ResponseExtensions(val response: Response) extends AnyVal {
    def as[A: JsonDecoder]: Task[A] =
      for {
        chunk      <- response.data.toByteBuf.map(buf => Chunk.fromArray(buf.array()))
        chunkString = new String(chunk.toArray)
        result <- ZIO
                    .fromEither(JsonDecoder[A].decodeJson(new String(chunk.toArray)))
                    .mapError(e => throw new RuntimeException(e))
      } yield result
  }

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
          } @@ ignore +
            test("Http.succeed") {
              for {
                result <- httpSuccess(())
              } yield assertTrue(result == 42)
            } @@ ignore +
            test("Http.fail") {
              for {
                result <- httpFailure(()).flip
              } yield assertTrue(result == Some(42))
            } @@ ignore +
            test("Http.identity") {
              for {
                result <- stringIdentity("hello")
              } yield assertTrue(result == "hello")
            } @@ ignore +
            test("Http.fromZIO") {
              for {
                _    <- TestConsole.feedLines("hello, mother")
                line <- consoleHttp(())
              } yield assertTrue(line == "hello, mother")
            } @@ ignore
        } +
          suite("HttpApp") {
            test("constructor") {
              val expected = Request(
                method = Method.PUT,
                url = URL.fromString("http://ziowebapp.com/greet").toOption.get,
                data = HttpData.fromString("Hello World!")
              )

              assertTrue(exampleRequest1 == expected)
            } @@ ignore +
              test("Response") {
                val expected =
                  Response(status = Status.OK, data = HttpData.fromString("Hello World!"))

                assertTrue(exampleResponse1 == expected)
              } @@ ignore +
              test("Http.ok") {
                for {
                  result <- httpOk(exampleRequest1)
                } yield assertTrue(result.status == Status.OK)
              } @@ ignore +
              test("Http.notFound") {
                for {
                  result <- httpOk(exampleRequest1)
                } yield assertTrue(result.status == Status.NOT_FOUND)
              } @@ ignore +
              test("Http.badRequest") {
                for {
                  result <- httpOk(exampleRequest1)
                } yield assertTrue(result.status == Status.BAD_REQUEST)
              } @@ ignore +
              test("Http.error") {
                val error = HttpError.InternalServerError("boom")

                for {
                  result <- httpError(error)(exampleRequest1)
                } yield assertTrue(result.status == error.status)
              } @@ ignore +
              test("Http.fromData") {
                val data = HttpData.fromString("Hello World!")

                val actual   = httpFromData(data)
                val expected = Http.fromData(data)

                assertTrue(actual == expected)
              } @@ ignore +
              test("Http.fromFile") {
                val file = new java.io.File("build.sbt")

                val actual   = httpFromFile(file)
                val expected = Http.fromFile(file)

                assertTrue(actual == expected)
              } @@ ignore +
              test("Http.response") {
                val actual   = httpFromResponse(exampleResponse1)
                val expected = Http.response(exampleResponse1)

                assertTrue(actual == expected)
              } @@ ignore +
              test("Http.responseZIO") {
                val zio = ZIO.succeed(exampleResponse1)

                val actual   = httpFromResponseZIO(zio)
                val expected = Http.responseZIO(zio)

                assertTrue(actual == expected)
              } @@ ignore
          }
      } +
      suite("transformations") {
        test("Http#map") {
          for {
            result <- stringHttp(())
          } yield assertTrue(result == "42")
        } @@ ignore +
          test("Http#mapZIO") {
            for {
              _      <- TestConsole.feedLines("John")
              name   <- interactiveHttp(())
              output <- TestConsole.output.map(_.mkString)
            } yield assertTrue(output.contains("What is your name?")) &&
              assertTrue(name == "John")
          } @@ ignore +
          test("Http#as") {
            for {
              result <- unitHttp(())
            } yield assertTrue(result == ())
          } @@ ignore +
          test("Http#contramap") {
            for {
              result <- requestUsingHttp(exampleRequest1)
            } yield assertTrue(result != exampleRequest1.url.asString)
          } @@ ignore
      } +
      suite("combinations") {
        test("Http#defaultWith") {
          for {
            result1 <- usOrUkHttp(Country.US)
            result2 <- usOrUkHttp(Country.UK)
          } yield assertTrue(result1.contains("US")) && assertTrue(result2.contains("UK"))
        } @@ ignore +
          test("Http#orElse") {
            for {
              result1 <- usOrUkHttp(Country.US)
              result2 <- usOrUkHttp(Country.UK)
            } yield assertTrue(result1.contains("US")) && assertTrue(result2.contains("UK"))
          } @@ ignore +
          test("Http#andThen") {
            for {
              result <- digitsInNumber(1234)
            } yield assertTrue(result == 4)
          } @@ ignore +
          test("Http#zipRight") {
            for {
              _    <- TestConsole.feedLines("Sherlock Holmes")
              name <- promptAndRead(())
            } yield assertTrue(name == "Sherlock Holmes")
          } @@ ignore +
          test("Http#race") {
            for {
              result <- neverOrRightAway(())
            } yield assertTrue(result == 42)
          } @@ ignore +
          test("Http#flatMap") {
            for {
              result1 <- dispatchExample("route1")
              result2 <- dispatchExample("route2")
            } yield assertTrue(result1 == "Handled by route1") &&
              assertTrue(result2 == "Handled by route2")
          } @@ ignore +
          test("Http#catchAll") {
            for {
              result <- httpRecovered(())
            } yield assertTrue(result == "I recovered")
          } @@ ignore
      } +
      suite("routes") {
        suite("Path") {
          test("root") {
            val expected = !!

            assertTrue(rootPath == expected)
          } @@ ignore +
            test("composite") {
              val expected = !! / "Baker" / "221B"

              assertTrue(compositePath == expected)
            } @@ ignore +
            test("extracted") {
              assertTrue(extractedStreet == "Baker") &&
              assertTrue(extractedNumber == "221B")
            } @@ ignore +
            test("request") {
              assertTrue(exampleRequestMatch == "It matches!")
            } @@ ignore
        } +
          suite("collectors") {
            test("Http.collect") {
              for {
                response1 <- greetAndFarewell(req(!! / "greet"))
                response2 <- greetAndFarewell(req(!! / "farewell"))
              } yield assertTrue(response1 == Response.text("Hello there!")) &&
                assertTrue(response2 == Response.text("Goodbye!"))
            } @@ ignore +
              test("Http.collectZIO") {
                for {
                  response1 <- lookupUserApp(req(!! / "users" / "sholmes"))
                  response2 <- lookupUserApp(req(!! / "users" / "jwatson"))
                } yield assertTrue(response1 == Response.text("Sherlock Holmes")) &&
                  assertTrue(response2 == Response.text("John Watson"))
              } @@ ignore +
              test("Http.collectHttp") {
                for {
                  response1 <- exampleRouting(req(!! / "greet"))
                  response2 <- exampleRouting(req(!! / "users" / "sholmes"))
                } yield assertTrue(response1 == Response.text("Hello there!")) &&
                  assertTrue(response2 == Response.text("Sherlock Holmes"))
              } @@ ignore
          }
      } +
      suite("server") {
        test("start") {
          assertTrue(helloWorldServer != null)
        } @@ ignore
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
        } @@ ignore
      }
  }
}
