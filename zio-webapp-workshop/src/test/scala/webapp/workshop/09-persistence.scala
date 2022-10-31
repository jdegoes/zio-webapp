/**
 * PERSISTENCE
 *
 * Nearly every web application needs to use a persistence layer. In fact, most
 * web applications will need multiple persistence layers for different purposes
 * (message queuing, binary storage & streaming, transactional data,
 * semi-structured data, in-memory caching, etc.).
 *
 * ZIO 2.0 has powerful facilities for integrating with different persistence
 * layers, even those not natively designed to support ZIO, and there are two
 * promising solutions for DBMS (ZIO Quill and ZIO SQL).
 *
 * In this section, you will learn the general principles of integrating support
 * for a persistence layer into your ZIO 2.0 web application.
 */
package webapp.workshop

import zio._
import zio.test._

object PersistenceSpec extends ZIOSpecDefault {

  import PersistenceSection._

  val testPersistenceLayer: ZLayer[Any, TestFailure[Nothing], zio.jdbc.ZConnectionPool] =
    zio.jdbc.ZConnectionPool.h2test.mapError(TestFailure.die(_))

  def spec = suite("PersistenceSpec") {
    suite("tour") {
      test("end-to-end") {
        (for {
          _ <- executedCreateTable
          _ <- insertedDetectives
          _ <- queriedDetectives
          _ <- updatedDetectives
          _ <- deletedJimRockford
        } yield assertCompletes)
      }
    }
  }.provideLayer(testPersistenceLayer)
}
