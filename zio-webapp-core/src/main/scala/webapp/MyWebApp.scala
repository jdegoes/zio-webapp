package webapp

import zio._
import zhttp.http._
import zhttp.service.Server

/**
 * REPOSITORY
 *
 * https://github.com/jdegoes/zio-webapp
 *
 * Download and compile the project using SBT or IDE of choice.
 */

object what_is_zio {
  /*
   * `Runnable` provides a simple way to describe a task that can be run.
   */
  object phase1 {
    trait Runnable {
      def run(): Unit

      /*
       * EXERCISE
       *
       * Add a `retry` method that retries failed runnables for the specified
       * number of times.
       */
      def retry(n: Int): Runnable = ???
    }

    def uploadFile(url: String): Runnable = ???

    /*
     * EXERCISE
     *
     * Implement a resilient upload.
     */
    lazy val resilientUpload = ???
  }

  object phase2 {
    import scala.util.Try

    /*
     * `Callable[A]` provides a simple way to describe a task that can be run to
     * produce a success value of a certain type `A`.
     */
    trait Callable[+A] { self =>
      def call(): A

      /*
       * EXERCISE
       *
       * Implement a sequential operator that computes two callables in
       * sequence.
       */
      def sequential[B](that: Callable[B]): Callable[(A, B)] = ???

      def parallel[B](that: Callable[B]): Callable[(A, B)] =
        new Callable[(A, B)] {
          def call(): (A, B) = {
            var a: Option[Either[Throwable, A]] = None
            var b: Option[Either[Throwable, B]] = None

            val countDownLatch = new java.util.concurrent.CountDownLatch(2)

            new Thread {
              override def run() = {
                a = Some(Try(self.call()).toEither)

                countDownLatch.countDown()
              }
            }.start

            new Thread {
              override def run() = {
                b = Some(Try(that.call()).toEither)

                countDownLatch.countDown()
              }
            }.start()

            countDownLatch.await()

            val a0 = a.get.fold(throw _, identity(_))
            val b0 = b.get.fold(throw _, identity(_))

            (a0, b0)
          }
        }

      def retry(n: Int): Callable[A] =
        () => {
          try {
            self.call()
          } catch {
            case t: Throwable =>
              if (n > 1) retry(n - 1).call() else throw t
          }
        }
    }

    def downloadFile(url: String): Callable[String] =
      () => {
        val source = scala.io.Source.fromURL(url)

        try source.mkString("\n")
        finally source.close()
      }

    /*
     * EXERCISE
     *
     * Describe downloading two files in parallel, where if either download
     * fails, it is repeatedly tried until it succeeds.
     */
    lazy val downloadFilesInParallel = ???
  }

  /*
   * `IO[E, A]` provides a simple way to describe a task that can be run to
   * produce either a failure value of type `E`, or a success value of type `A`.
   */
  object phase3 {
    import scala.util.Try

    trait IO[+E, +A] { self =>
      def run(): Either[E, A]

      /*
       * EXERCISE
       *
       * Implement a `catchAll` method that catches all errors.
       */
      def catchAll[E2, A2 >: A](f: E => IO[E2, A2]): IO[E2, A2] =
        ???

      def either: IO[Nothing, Either[E, A]] =
        new IO[Nothing, Either[E, A]] {
          def run(): Either[Nothing, Either[E, A]] = Right(self.run())
        }

      def flatMap[EE >: E, B](f: A => IO[EE, B]): IO[EE, B] =
        new IO[EE, B] {
          def run(): Either[EE, B] = self.run().flatMap(a => f(a).run())
        }

      def map[B](f: A => B): IO[E, B] =
        new IO[E, B] {
          def run(): Either[E, B] = self.run().map(f)
        }

      /*
       * EXERCISE
       *
       * Implement a fallback operator.
       */
      def orElse[E2, A2 >: A](that: IO[E2, A2]): IO[E2, A2] =
        ???
    }
    object IO {
      def attempt[A](a: => A): IO[Throwable, A] =
        new IO[Throwable, A] {
          def run(): Either[Throwable, A] = Try(a).toEither
        }

      def fail[E](e: => E): IO[E, Nothing] =
        new IO[E, Nothing] {
          def run(): Either[E, Nothing] = Left(e)
        }

      def succeed[A](a: => A): IO[Nothing, A] =
        new IO[Nothing, A] {
          def run(): Either[Nothing, A] = Right(a)
        }
    }

    def downloadFile(url: String): IO[Throwable, String] =
      IO.attempt {
        val source = scala.io.Source.fromURL(url)

        try source.mkString("\n")
        finally source.close()
      }
  }
  object phase4 {
    import scala.util.Try

    trait ZIO[-R, +E, +A] { self =>
      def map[B](f: A => B): ZIO[R, E, B] =
        new ZIO[R, E, B] {
          def run(r: R): Either[E, B] = self.run(r).map(f)
        }

      def flatMap[RR <: R, EE >: E, B](f: A => ZIO[RR, EE, B]): ZIO[RR, EE, B] =
        new ZIO[RR, EE, B] {
          def run(r: RR): Either[EE, B] = self.run(r).flatMap(a => f(a).run(r))
        }

      def run(r: R): Either[E, A]
    }
    object ZIO {
      def attempt[A](a: => A): ZIO[A, Throwable, A] =
        new ZIO[Any, Throwable, A] {
          def run(r: Any): Either[Throwable, A] = Try(a).toEither
        }

      def fail[E](e: => E): ZIO[Any, E, Nothing] =
        new ZIO[Any, E, Nothing] {
          def run(r: Any): Either[E, Nothing] = Left(e)
        }

      def succeed[A](a: => A): ZIO[Any, Nothing, A] =
        new ZIO[Any, Nothing, A] {
          def run(r: Any): Either[Nothing, A] = Right(a)
        }
    }
  }
}

/*
 * EXERCISE
 *
 * Implement a number guessing game.
 */
object RandomNumberGuessingGame extends ZIOAppDefault {
  def run = ???
}

/*
 * EXERCISE
 *
 * Implement a word count app that counts words in standard input or a file
 * (your choice).
 */
object WordCountApp extends ZIOAppDefault {

  def countWords(line: String, counter: Ref[Map[String, Int]]) =
    ZIO.foreach(line.split("\\s+")) { word =>
      counter.update { map =>
        map + (word -> (map.getOrElse(word, 0) + 1))
      }
    }

  def run = ???
}

object MyWebApp extends ZIOAppDefault {
  val app = Http.collect[Request] { case req @ Method.GET -> !! / "text" =>
    Response.text("Hello World!")
  }

  def run = Server.start(8090, app)
}
