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
import zio.stream._

object JsonSection {
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
  lazy val json2: Json = 
    Json.Obj(Chunk(("name", Json.Str("Peter")), ("age", Json.Num(42))))

  /**
   * EXERCISE
   *
   * Use the fromJson[Json] extension method to parse the string into a JSON
   * AST.
   */
  lazy val json3: Either[String, Json] = """{"name":"John","age":42}""".fromJson[Json]

  /**
   * EXERCISE
   *
   * Insert a typo that renders this JSON invalid, so the test fails.
   */
  lazy val jsonString1: String = """{"name:"John","age":42}"""

  /**
   * EXERCISE
   *
   * Use `Json#foldUp` to count the number of Json.Str nodes in the JSON value.
   */
    lazy val strCount1: Int = json1.foldUpSome(0) { 
      case (count, Json.Str(_)) => count + 1 
    }

  //
  // JSON CURSORS
  //

  /**
   * EXERCISE
   *
   * Use `JsonCursor.identity` to construct an identity cursor.
   */
  lazy val identityCursor: JsonCursor[Json, Json] = JsonCursor.identity

  /**
   * EXERCISE
   *
   * Use `JsonCursor#filterType` to refine `identityCursor` so that it selects
   * only Json.Obj nodes.
   */
  lazy val objCursor: JsonCursor[Json, Json.Obj] = identityCursor.filterType(JsonType.Obj)

  /**
   * EXERCISE
   *
   * Use `JsonCursor#filterType` to refine `identityCursor` so that it selects
   * only Json.Arr nodes.
   */
  lazy val arrCursor: JsonCursor[Json, Json.Arr] = identityCursor.filterType(JsonType.Arr)

  /**
   * EXERCISE
   *
   * Use `JsonCursor.field` to refine `identityCursor` so that it selects for
   * the field "name" inside a JSON object.
   */
  lazy val nameFieldCursor: JsonCursor[Json.Obj, Json] = JsonCursor.field("name")

  /**
   * EXERCISE
   *
   * Use `JsonCursor#element` to refine `identityCursor` so that it selects the
   * first element of a JSON array.
   */
  lazy val firstElementCursor: JsonCursor[Json.Arr, Json] = JsonCursor.element(0)

  /**
   * EXERCISE
   *
   * Use `Json#get` on a cursor that you create inline to select the "name"
   * field from `json1`.
   */
  lazy val nameFromJson1: Either[String, Json.Str] = 
    json1.get(JsonCursor.field("name").filterType(JsonType.Str))

  /**
   * EXERCISE
   *
   * Use `Json#merge` to merge to `json1` with `json4` together.
   */
  lazy val json4: Json   = """{"weight":"70","height":206}""".fromJson[Json].toOption.get
  lazy val merged1: Json = json1.merge(json4)

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
  lazy val encodedMap1: CharSequence = JsonEncoder[Map[String, List[Int]]].encodeJson(map1, None)

  final case class Email(value: String)

  /**
   * EXERCISE
   *
   * Use the `JsonEncoder#contramap` method to contramap a `JsonEncoder[String]`
   * so that it can encode `Email` values.
   */
  lazy val emailEncoder: JsonEncoder[Email] = JsonEncoder[String].contramap(_.value)

  /**
   * EXERCISE
   *
   * Use the `JsonEncoder#zip` method to zip together the following two
   * encoders.
   */
  val stringEncoder1 = JsonEncoder[String]
  val intEncoder1    = JsonEncoder[Int]

  lazy val tupleEncoder1: JsonEncoder[(String, Int)] = stringEncoder1.zip(intEncoder1)

  /**
   * EXERCISE
   *
   * Using `DeriveJsonEncoder.gen`, automatically derive the encoder for the
   * following case class.
   */
  final case class RestaurantReview(review: String, stars: Int, username: String, restaurantId: Long)
  object RestaurantReview {
    implicit lazy val encoder: JsonEncoder[RestaurantReview] = DeriveJsonEncoder.gen[RestaurantReview]
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

    // {"_type": "OrderPlaced", "orderId": "123ABC", "time": 123}

    implicit lazy val encoder: JsonEncoder[PurchaseEvent] = DeriveJsonEncoder.gen[PurchaseEvent]
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
  lazy val decodedMap1: Map[String, List[Int]] = JsonDecoder[Map[String, List[Int]]].decodeJson(jsonString2).toOption.get

  /**
   * EXERCISE
   *
   * Use `JsonDecoder#map` to map a `JsonDecoder[String]` into a
   * `JsonDecoder[Email]`.
   */
  lazy val emailDecoder: JsonDecoder[Email] = JsonDecoder[String].map(Email(_))

  /**
   * EXERCISE
   *
   * Use the `JsonDecoder#zip` method to zip together the following two
   * decoders.
   */
  val stringDecoder1 = JsonDecoder[String]
  val intDecoder1    = JsonDecoder[Int]

  lazy val tupleDecoder1: JsonDecoder[(String, Int)] = stringDecoder1.zip(intDecoder1)

  /**
   * EXERCISE
   *
   * Use the `JsonDecoder#orElseEither` to try to decode a JSON value as a
   * `Boolean`, but if that fails, decode it as an `Int`.
   */
  lazy val boolOrIntDecoder: JsonDecoder[Either[Boolean, Int]] =
    JsonDecoder[Boolean].orElseEither(JsonDecoder[Int])

  /**
   * EXERCISE
   *
   * Using `DeriveJsonDecoder.gen`, automatically derive the decoder for the the
   * `RestaurantReview` case class.
   */
  lazy val restaurantReviewDecoder: JsonDecoder[RestaurantReview] = DeriveJsonDecoder.gen[RestaurantReview]

  /**
   * EXERCISE
   *
   * Using `DeriveJsonDecoder.gen`, automatically derive the decoder for the
   * `PurchaseEvent` sealed trait.
   */
  lazy val purchaseEventDecoder: JsonDecoder[PurchaseEvent] = DeriveJsonDecoder.gen[PurchaseEvent]

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
    implicit lazy val codec: JsonCodec[Doc] = DeriveJsonCodec.gen[Doc]
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
    stream.map(person => person.toJson)

  /**
   * EXERCISE
   *
   * Convert from a stream of JSON strings to a stream of people.
   */
  def toPeopleStream(stream: ZStream[Any, Nothing, String]): ZStream[Any, String, Person] =
    stream.flatMap(string => ZStream.fromZIO(zio.ZIO.fromEither(string.fromJson[Person])))
}
