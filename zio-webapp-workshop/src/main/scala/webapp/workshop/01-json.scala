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
import zio.test._
import zio.test.TestAspect.ignore
import zio.stream._

object JsonSpec extends ZIOSpecDefault {
  import zio.json.ast._

  final case class Person(name: String, age: Int)
  object Person {
    implicit val codec: JsonCodec[Person] = DeriveJsonCodec.gen[Person]
  }

  //
  // JSON AST
  //

  val json1 = Json.Obj(
    Chunk(
      "name" -> Json.Str("John"),
      "age"  -> Json.Num(42)
    )
  )

  /**
   * EXERCISE
   *
   * Create a JSON object with fields `name` (set to "Peter") and `age` (set to
   * 43).
   */
  lazy val json2: Json = TODO

  /**
   * EXERCISE
   *
   * Use the fromJson[Json] extension method to parse the string into a JSON
   * AST.
   */
  lazy val json3: Either[String, Json] = """{"name":"John","age":42}""".TODO

  /**
   * EXERCISE
   *
   * Insert a typo that renders this JSON invalid, so the test fails.
   */
  lazy val jsonString1: String = """{"name":"John","age":42}"""

  /**
   * EXERCISE
   *
   * Use `Json#foldUp` to count the number of Json.Str nodes in the JSON value.
   */
  lazy val strCount1: Int = json1.TODO

  //
  // JSON CURSORS
  //

  /**
   * EXERCISE
   *
   * Use `JsonCursor.identity` to construct an identity cursor.
   */
  lazy val identityCursor: JsonCursor[Json, Json] = JsonCursor.TODO

  /**
   * EXERCISE
   *
   * Use `JsonCursor#filterType` to refine `identityCursor` so that it selects
   * only Json.Obj nodes.
   */
  lazy val objCursor: JsonCursor[Json, Json.Obj] = identityCursor.TODO

  /**
   * EXERCISE
   *
   * Use `JsonCursor#filterType` to refine `identityCursor` so that it selects
   * only Json.Arr nodes.
   */
  lazy val arrCursor: JsonCursor[Json, Json.Arr] = identityCursor.TODO

  /**
   * EXERCISE
   *
   * Use `JsonCursor#field` to refine `identityCursor` so that it selects for
   * the field "name" inside a JSON object.
   */
  lazy val nameFieldCursor: JsonCursor[Json.Obj, Json] = identityCursor.TODO

  /**
   * EXERCISE
   *
   * Use `JsonCursor#element` to refine `identityCursor` so that it selects the
   * first element of a JSON array.
   */
  lazy val firstElementCursor: JsonCursor[Json.Arr, Json] = identityCursor.TODO

  /**
   * EXERCISE
   *
   * Use `Json#get` on a cursor that you create inline to select the "name"
   * field from `json1`.
   */
  lazy val nameFromJson1: Either[String, Json] = json1.TODO

  /**
   * EXERCISE
   *
   * Use `Json#merge` to merge to `json1` with `json4` together.
   */
  lazy val json4: Json   = """{"weight":"70","height":206}""".fromJson[Json].toOption.get
  lazy val merged1: Json = json1.TODO

  //
  // ENCODERS
  //

  /**
   * EXERCISE
   *
   * Use `JsonEncoder.apply` to summon a `JsonEncoder` for the `map1` type, and
   * then use the `JsonEncoder#encodeJson` method of the encoder to encode the
   * map as JSON.
   */
  val map1: Map[String, List[Int]] = Map(
    "John"  -> List(1, 2, 3),
    "Peter" -> List(4, 5, 6)
  )
  lazy val encodedMap1: CharSequence = JsonEncoder.TODO

  final case class Email(value: String)

  /**
   * EXERCISE
   *
   * Use the `JsonEncoder#contramap` method to contramap a `JsonEncoder[String]`
   * so that it can encode `Email` values.
   */
  lazy val emailEncoder: JsonEncoder[Email] = JsonEncoder[String].TODO

  /**
   * EXERCISE
   *
   * Use the `JsonEncoder#zip` method to zip together the following two
   * encoders.
   */
  val stringEncoder1 = JsonEncoder[String]
  val intEncoder1    = JsonEncoder[Int]

  lazy val tupleEncoder1: JsonEncoder[(String, Int)] = stringEncoder1.TODO

  /**
   * EXERCISE
   *
   * Using `DeriveJsonEncoder.gen`, automatically derive the encoder for the
   * following case class.
   */
  final case class RestaurantReview(review: String, stars: Int, username: String, restaurantId: Long)
  object RestaurantReview {
    implicit lazy val encoder: JsonEncoder[RestaurantReview] = DeriveJsonEncoder.TODO
    implicit lazy val decoder: JsonDecoder[RestaurantReview] = restaurantReviewDecoder
  }

  /**
   * EXERCISE
   *
   * Using `DeriveJsonEncoder.gen`, automatically derive the encoder for the
   * following sealed trait.
   */
  sealed trait PurchaseEvent
  object PurchaseEvent {
    final case class OrderPlaced(orderId: String, time: Long)  extends PurchaseEvent
    final case class CreditCheck(orderId: String, time: Long)  extends PurchaseEvent
    final case class CardCharged(orderId: String, time: Long)  extends PurchaseEvent
    final case class OrderSuccess(orderId: String, time: Long) extends PurchaseEvent
    final case class OrderFailure(orderId: String, time: Long) extends PurchaseEvent

    implicit lazy val encoder: JsonEncoder[PurchaseEvent] = DeriveJsonEncoder.TODO
    implicit lazy val decoder: JsonDecoder[PurchaseEvent] = purchaseEventDecoder
  }

  //
  // DECODERS
  //

  /**
   * EXERCISE
   *
   * Use `JsonDecoder.apply` to summon a `JsonDecoder` for the `map1` type, and
   * then use the `JsonDecoder#decodeJson` method of the decoder to decode the
   * string `jsonString2` into a map.
   */
  val jsonString2: String                      = """{"John":[1,2,3],"Peter":[4,5,6]}"""
  lazy val decodedMap1: Map[String, List[Int]] = jsonString2.TODO

  /**
   * EXERCISE
   *
   * Use `JsonDecoder#map` to map a `JsonDecoder[String]` into a
   * `JsonDecoder[Email]`.
   */
  lazy val emailDecoder: JsonDecoder[Email] = JsonDecoder[String].TODO

  /**
   * EXERCISE
   *
   * Use the `JsonDecoder#zip` method to zip together the following two
   * decoders.
   */
  val stringDecoder1 = JsonDecoder[String]
  val intDecoder1    = JsonDecoder[Int]

  lazy val tupleDecoder1: JsonDecoder[(String, Int)] = stringDecoder1.TODO

  /**
   * EXERCISE
   *
   * Use the `JsonDecoder#orElseEither` to try to decode a JSON value as a
   * `Boolean`, but if that fails, decode it as an `Int`.
   */
  lazy val boolOrIntDecoder: JsonDecoder[Either[Boolean, Int]] =
    JsonDecoder[Boolean].TODO

  /**
   * EXERCISE
   *
   * Using `DeriveJsonDecoder.gen`, automatically derive the decoder for the the
   * `RestaurantReview` case class.
   */
  lazy val restaurantReviewDecoder: JsonDecoder[RestaurantReview] = DeriveJsonDecoder.TODO

  /**
   * EXERCISE
   *
   * Using `DeriveJsonDecoder.gen`, automatically derive the decoder for the
   * `PurchaseEvent` sealed trait.
   */
  lazy val purchaseEventDecoder: JsonDecoder[PurchaseEvent] = DeriveJsonDecoder.TODO

  //
  // CODECS
  //

  /**
   * EXERCISE
   *
   * Using `DeriveJsonCodec.gen`, automatically derive a codec (which includes
   * both encoder and decoder) for the following case class.
   */
  final case class Doc(docId: String, content: String, owner: String)
  object Doc {
    implicit lazy val codec: JsonCodec[Doc] = DeriveJsonCodec.TODO
  }

  //
  // STREAMS
  //
  val people: List[Person] = List(
    Person("John Doe", 30),
    Person("Jane Doe", 25),
    Person("Peter Parker", 35)
  )
  val peopleStream = ZStream.fromIterable(people)
  val jsonStream   = ZStream.fromIterable(people.map(_.toJson))

  /**
   * EXERCISE
   *
   * Convert from a stream of people to a stream of JSON strings.
   */
  def toJsonStream(stream: ZStream[Any, Nothing, Person]): ZStream[Any, Nothing, String] =
    TODO

  /**
   * EXERCISE
   *
   * Convert from a stream of JSON strings to a stream of people.
   */
  def toPeopleStream(stream: ZStream[Any, Nothing, String]): ZStream[Any, String, Person] =
    TODO

  def spec = suite("JsonSpec") {
    suite("JSON AST") {
      test("construction") {
        assertTrue(json2.toString == """{"name":"Peter","age":43}""")
      } @@ ignore +
        test("successful parsing") {
          assertTrue(json3 == Right(json1))
        } @@ ignore +
        test("unsuccessful parsing") {
          assertTrue(jsonString1.fromJson[Json].isLeft)
        } @@ ignore +
        test("folding") {
          assertTrue(strCount1 == 1)
        } @@ ignore +
        suite("cursors") {
          test("identity cursor") {
            assertTrue(json1.get(identityCursor) == Right(json1))
          } @@ ignore +
            test("object cursor") {
              assertTrue(json1.get(objCursor) == Right(json1)) &&
              assertTrue(Json.Bool(true).get(objCursor).isLeft)
            } @@ ignore +
            test("array cursor") {
              val arr = Json.Arr(Chunk(Json.Num(1), Json.Num(2)))

              assertTrue(arr.get(arrCursor) == Right(arr)) &&
              assertTrue(json1.get(arrCursor).isLeft)
            } @@ ignore +
            test("field cursor") {
              assertTrue(json1.get(nameFieldCursor) == Right(Json.Str("John")))
            } @@ ignore +
            test("element cursor") {
              val arr = Json.Arr(Chunk(Json.Num(1), Json.Num(2)))

              assertTrue(arr.get(firstElementCursor) == Right(Json.Num(1)))
            } @@ ignore
        } +
        test("get") {
          assertTrue(nameFromJson1 == Right(Json.Str("John")))
        } @@ ignore +
        test("merge") {
          assertTrue(merged1.get(JsonCursor.field("age")).isRight) &&
          assertTrue(merged1.get(JsonCursor.field("weight")).isRight)
        } @@ ignore
    } +
      suite("encoders") {
        test("encode map of list") {
          assertTrue(encodedMap1.toString() == """{"John":[1,2,3],"Peter":[4,5,6]}""")
        } @@ ignore +
          test("contramap input") {
            val email = Email("sherlock@holmes.com")

            assertTrue(
              JsonEncoder[String].contramap[Email](_.value).encodeJson(email, None).toString() ==
                emailEncoder.encodeJson(email, None).toString()
            )
          } @@ ignore +
          test("both") {
            val tuple = ("John", 42)

            assertTrue(
              tupleEncoder1.encodeJson(tuple, None).fromJson[(String, Int)].isRight
            )
          } @@ ignore +
          suite("derivation") {
            test("case class") {
              val review =
                RestaurantReview("Best Indian food in the city!", 5, "sholmes", 123835L)

              val encoder = JsonEncoder[RestaurantReview]
              val decoder = DeriveJsonDecoder.gen[RestaurantReview]

              assertTrue(decoder.decodeJson(encoder.encodeJson(review, None)).right.get == review)
            } @@ ignore +
              test("sealed trait") {
                val event: PurchaseEvent = PurchaseEvent.OrderPlaced("order-1", 12345L)

                val encoder = JsonEncoder[PurchaseEvent]
                val decoder = DeriveJsonDecoder.gen[PurchaseEvent]

                assertTrue(decoder.decodeJson(encoder.encodeJson(event, None)).right.get == event)
              } @@ ignore
          }
      } +
      suite("decoders") {
        test("decode map of list") {
          assertTrue(decodedMap1 == map1)
        } @@ ignore +
          test("map output") {
            val email = emailEncoder.encodeJson(Email("sherlock@holmes.com"), None)

            assertTrue(
              JsonDecoder[String].map(Email(_)).decodeJson(email) ==
                emailDecoder.decodeJson(email)
            )
          } @@ ignore +
          test("tuple") {
            val tuple = tupleEncoder1.encodeJson(("John", 42), None)

            assertTrue(tupleDecoder1.decodeJson(tuple).isRight)
          } @@ ignore +
          test("fallback") {
            assertTrue(boolOrIntDecoder.decodeJson(Json.Bool(true).toString()) == Right(Left(true))) &&
            assertTrue(boolOrIntDecoder.decodeJson(Json.Num(1).toString()) == Right(Right(1)))
          } @@ ignore +
          suite("derivation") {
            test("case class") {
              val review =
                RestaurantReview("Best Indian food in the city!", 5, "sholmes", 123835L)

              val encoder = DeriveJsonEncoder.gen[RestaurantReview]
              val decoder = JsonDecoder[RestaurantReview]

              assertTrue(decoder.decodeJson(encoder.encodeJson(review, None)).right.get == review)
            } @@ ignore +
              test("sealed trait") {
                val event: PurchaseEvent = PurchaseEvent.OrderPlaced("order-1", 12345L)

                val encoder = DeriveJsonEncoder.gen[PurchaseEvent]
                val decoder = JsonDecoder[PurchaseEvent]

                assertTrue(decoder.decodeJson(encoder.encodeJson(event, None)).right.get == event)
              } @@ ignore
          }
      } +
      suite("codecs") {
        test("case class derivation") {
          val doc = Doc("doc-id-1", "A mostly empty notepad", "johnwatson")

          val encoder = JsonEncoder[Doc]
          val decoder = JsonDecoder[Doc]

          assertTrue(decoder.decodeJson(encoder.encodeJson(doc, None)).right.get == doc)
        } @@ ignore
      } +
      suite("streaming") {
        test("decode & encode") {
          for {
            people2 <- toPeopleStream(toJsonStream(peopleStream)).runCollect
          } yield assertTrue(people2.toList == people)
        } @@ ignore
      }
  }
}
