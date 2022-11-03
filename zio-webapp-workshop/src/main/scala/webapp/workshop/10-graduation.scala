package zio.jdbc 

import zio._ 
import zio.schema._
import zio.json._ 
import zio.jdbc._

object GraduationSection {
  final case class Todo(id: Long, description: String, created: Long, modified: Long)
  object Todo {
    implicit val schema: Schema[Todo] = DeriveSchema.gen[Todo]

    implicit lazy val jsonCodec: JsonCodec[Todo] = DeriveJsonCodec.gen[Todo]

    implicit val jdbcDecoder: JdbcDecoder[Todo] = JdbcDecoder.fromSchema[Todo]
  }

  final case class CreateTodo(description: String)
  object CreateTodo {
    implicit lazy val jsonCodec: JsonCodec[CreateTodo] = 
      JsonCodec[String].transform(CreateTodo(_), _.description)
  }

  final case class TodoCreated(id: Long)
  object TodoCreated {
    implicit lazy val jsonCodec: JsonCodec[TodoCreated] = 
      JsonCodec[Long].transform(TodoCreated(_), _.id)
  }

  trait TodoRepo {
    def getAll: UIO[Chunk[Todo]]

    def create(description: => String): UIO[Todo]

    def getById(id: Long): UIO[Option[Todo]]

    def updateTodo(id: Long, description: => String): UIO[Option[Todo]]
  }
  object TodoRepo {
    private final case class TestTodoRepo(idGen: Ref[Long], todos: Ref[Map[Long, Todo]]) extends TodoRepo {
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
          created <- Clock.instant.map(_.toEpochMilli)
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
          modified <- Clock.instant.map(_.toEpochMilli())
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

    final case class LiveTodoRepo(pool: ZConnectionPool) extends TodoRepo {
      def getAll: UIO[Chunk[Todo]] = 
        pool.transaction {
          selectAll {
            sql"SELECT * FROM todos".as[Todo]
          }
        }.orDie

      def create(description: => String): UIO[Todo] = 
        pool.transaction {
          for {
            id <- insertOne {
                    Sql.insertInto("todos")("description").values(description)
                  }
            todo <- selectOne(sql"SELECT * FROM todos WHERE id = $id".as[Todo])
          } yield todo.get
        }.orDie

      def getById(id: Long): UIO[Option[Todo]] = 
        pool.transaction {
          selectOne {
            sql"SELECT * FROM todos WHERE id = $id".as[Todo]
          }
        }.orDie

      def updateTodo(id: Long, description: => String): UIO[Option[Todo]] = 
        pool.transaction {
          execute {
            sql"update todos set description = $description where id = $id"
          }
        }.orDie *> getById(id)

       def insertOne(sql: SqlFragment): ZIO[ZConnection, Throwable, Long] =
        for {
          connection <- ZIO.service[ZConnection]
          result     <- connection.executeSqlWith(sql) { statement =>
            statement.executeLargeUpdate()

            statement.getGeneratedKeys().getLong(1)
          }
        } yield result
    }

    val testLayer: ZLayer[Any, Nothing, TodoRepo] =
      ZLayer {
        for {
          idGen <- Ref.make(0L)
          todos <- Ref.make(Map.empty[Long, Todo])
        } yield TestTodoRepo(idGen, todos)
      }

    val liveLayer: ZLayer[ZConnectionPool, Nothing, TodoRepo] = 
      ZLayer.fromFunction(LiveTodoRepo(_))

    def getAll: ZIO[TodoRepo, Nothing, Chunk[Todo]] = ZIO.serviceWithZIO(_.getAll)

    def create(description: => String): ZIO[TodoRepo, Nothing, Todo] = ZIO.serviceWithZIO(_.create(description))

    def getById(id: Long): ZIO[TodoRepo, Nothing, Option[Todo]] = ZIO.serviceWithZIO(_.getById(id))

    def updateTodo(id: Long, description: => String): ZIO[TodoRepo, Nothing, Option[Todo]] =
      ZIO.serviceWithZIO(_.updateTodo(id, description))
  }

  object TodoApp extends ZIOAppDefault {
    import zhttp.http._ 
    import zhttp.service.Server

    val app: HttpApp[TodoRepo, Throwable] = 
      Http.collectZIO[Request] {
        case Method.GET -> !! / "todos" => 
          for {
            todoRepo <- ZIO.service[TodoRepo]
            todos    <- todoRepo.getAll 
          } yield Response.json(todos.toJson)

        case req @ (Method.POST -> !! / "todos") =>
          for {
            todoRepo   <- ZIO.service[TodoRepo]
            string     <- req.body.asString
            createTodo <- ZIO.fromEither(string.fromJson[CreateTodo]).mapError(m => new Exception(m))
            todo       <- todoRepo.create(createTodo.description)
          } yield Response.json(todo.toJson)

        case req @ Method.PUT  -> !! / "todos" / id =>
          for {
            todoRepo   <- ZIO.service[TodoRepo]
            string     <- req.body.asString
            updateTodo <- ZIO.fromEither(string.fromJson[CreateTodo]).mapError(m => new Exception(m))
            todo       <- todoRepo.updateTodo(id.toLong, updateTodo.description)
          } yield Response.json(todo.toJson)
      }

    val run = Server.start(8080, app).provide(ZConnectionPool.h2test, TodoRepo.liveLayer)
  }



  /**
   * EXERCISE
   *
   * It's now time to begin your graduation project!!!
   *
   * Edit the `zio-webapp-core` project, which contains scaffolding for a simple
   * web application.
   *
   * The project is loaded with all the same dependencies as the workshop, so
   * you are free to use any of the libraries introduced in the workshop.
   *
   * For bonus points, construct a simple front-end that can generate requests
   * for your web server and handle responses.
   */
}
