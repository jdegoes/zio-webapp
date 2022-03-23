/**
 * JSON
 *
 * In modern web applications, JSON has become the lingua franca of data,
 * pervasive in GraphQL, REST APIs, and even some RPC architectures.
 *
 * In this section, you will learn how use _ZIO JSON_ to bypass intermediate
 * JSON ASTs, and serialize and deserialize your custom data structures
 * directly. This approach results in better performance, with fewer potential
 * security exploits, and far less boilerplate.
 */
package webapp.workshop

import zio.Chunk
import zio.json._
import zio.json.ast.{Json, JsonCursor}
import zio.test._
import zio.test.TestAspect.ignore
import zio.stream._

object JsonSpec extends DefaultRunnableSpec {

  import JsonSection._

  def spec = suite("JsonSpec") {
    suite("JSON AST") {
      test("construction") {
        assertTrue(json2.toString == """{"name":"Peter","age":43}""")
      } +
        test("successful parsing") {
          assertTrue(json3 == Right(json1))
        } +
        test("unsuccessful parsing") {
          assertTrue(jsonString1.fromJson[Json].isLeft)
        } +
        test("folding") {
          assertTrue(strCount1 == 1)
        } +
        suite("cursors") {
          test("identity cursor") {
            assertTrue(json1.get(identityCursor) == Right(json1))
          } +
            test("object cursor") {
              assertTrue(json1.get(objCursor) == Right(json1)) &&
              assertTrue(Json.Bool(true).get(objCursor).isLeft)
            } +
            test("array cursor") {
              val arr = Json.Arr(Chunk(Json.Num(1), Json.Num(2)))

              assertTrue(arr.get(arrCursor) == Right(arr)) &&
              assertTrue(json1.get(arrCursor).isLeft)
            } +
            test("field cursor") {
              assertTrue(json1.get(nameFieldCursor) == Right(Json.Str("John")))
            } +
            test("element cursor") {
              val arr = Json.Arr(Chunk(Json.Num(1), Json.Num(2)))

              assertTrue(arr.get(firstElementCursor) == Right(Json.Num(1)))
            }
        } +
        test("get") {
          assertTrue(nameFromJson1 == Right(Json.Str("John")))
        } +
        test("merge") {
          assertTrue(merged1.get(JsonCursor.field("age")).isRight) &&
          assertTrue(merged1.get(JsonCursor.field("weight")).isRight)
        }
    } +
      suite("encoders") {
        test("encode map of list") {
          assertTrue(encodedMap1.toString() == """{"John":[1,2,3],"Peter":[4,5,6]}""")
        } +
          test("contramap input") {
            val email = Email("sherlock@holmes.com")

            assertTrue(
              JsonEncoder[String].contramap[Email](_.value).encodeJson(email, None).toString() ==
                emailEncoder.encodeJson(email, None).toString()
            )
          } +
          test("both") {
            val tuple = ("John", 42)

            assertTrue(
              tupleEncoder1.encodeJson(tuple, None).fromJson[(String, Int)].isRight
            )
          } +
          suite("derivation") {
            test("case class") {
              val review =
                RestaurantReview("Best Indian food in the city!", 5, "sholmes", 123835L)

              val encoder = JsonEncoder[RestaurantReview]
              val decoder = DeriveJsonDecoder.gen[RestaurantReview]

              assertTrue(decoder.decodeJson(encoder.encodeJson(review, None)).right.get == review)
            } +
              test("sealed trait") {
                val event: PurchaseEvent = PurchaseEvent.OrderPlaced("order-1", 12345L)

                val encoder = JsonEncoder[PurchaseEvent]
                val decoder = DeriveJsonDecoder.gen[PurchaseEvent]

                assertTrue(decoder.decodeJson(encoder.encodeJson(event, None)).right.get == event)
              }
          }
      } +
      suite("decoders") {
        test("decode map of list") {
          assertTrue(decodedMap1 == map1)
        } +
          test("map output") {
            val email = emailEncoder.encodeJson(Email("sherlock@holmes.com"), None)

            assertTrue(
              JsonDecoder[String].map(Email(_)).decodeJson(email) ==
                emailDecoder.decodeJson(email)
            )
          } +
          test("tuple") {
            val tuple = tupleEncoder1.encodeJson(("John", 42), None)

            assertTrue(tupleDecoder1.decodeJson(tuple).isRight)
          } +
          test("fallback") {
            assertTrue(boolOrIntDecoder.decodeJson(Json.Bool(true).toString()) == Right(Left(true))) &&
            assertTrue(boolOrIntDecoder.decodeJson(Json.Num(1).toString()) == Right(Right(1)))
          } +
          suite("derivation") {
            test("case class") {
              val review =
                RestaurantReview("Best Indian food in the city!", 5, "sholmes", 123835L)

              val encoder = DeriveJsonEncoder.gen[RestaurantReview]
              val decoder = JsonDecoder[RestaurantReview]

              assertTrue(decoder.decodeJson(encoder.encodeJson(review, None)).right.get == review)
            } +
              test("sealed trait") {
                val event: PurchaseEvent = PurchaseEvent.OrderPlaced("order-1", 12345L)

                val encoder = DeriveJsonEncoder.gen[PurchaseEvent]
                val decoder = JsonDecoder[PurchaseEvent]

                assertTrue(decoder.decodeJson(encoder.encodeJson(event, None)).right.get == event)
              }
          }
      } +
      suite("codecs") {
        test("case class derivation") {
          val doc = Doc("doc-id-1", "A mostly empty notepad", "johnwatson")

          val encoder = JsonEncoder[Doc]
          val decoder = JsonDecoder[Doc]

          assertTrue(decoder.decodeJson(encoder.encodeJson(doc, None)).right.get == doc)
        }
      } +
      suite("streaming") {
        test("decode & encode") {
          for {
            people2 <- toPeopleStream(toJsonStream(peopleStream)).runCollect
          } yield assertTrue(people2.toList == people)
        }
      }
  }
}
