/**
 * SCHEMA
 *
 * At the center of every modern web application is data: web applications
 * generate data, consume data, process data, read data, store data, transform
 * data, and aggregate and report on data.
 *
 * Data comes from many different places, in many different formats, and is
 * mashed up with lots of other data, and stored in many diverse destinations.
 *
 * In this data-intensive world, the ZIO ecosystem has evolved a powerful tool
 * called _ZIO Schema_, which allows you to treat the _structure_ of your data
 * as a first-class value. If you have a schema for your own data types, which
 * _ZIO Schema_ can derive automatically at compile-time, then you can instantly
 * tap into a wealth of features, ranging from automatic codecs (JSON, Protobuf,
 * Thrift, XML, Avro, etc) to diffing, migrations, patching, and more.
 *
 * In this section, you will become familiar with _ZIO Schema_ and how you can
 * effectively eliminate many classes of boilerplate in your web applications.
 */
package webapp.workshop

import zio.schema._
import zio.test._
import zio.test.TestAspect.ignore

object SchemaSpec extends ZIOSpecDefault {
  //
  // SCHEMA CAPABILITIES FOR RECORDS
  //
  final case class Person(name: String, age: Int)
  object Person {
    import Schema._

    implicit val schema =
      Schema.CaseClass2[String, Int, Person](
        Field("name", Schema[String]),
        Field("age", Schema[Int]),
        Person(_, _),
        _.name,
        _.age
      )
  }

  /**
   * EXERCISE
   *
   * Define a generic method that can extract out the first field of any case
   * class that has two fields.
   */
  def extractFirstField[F1, F2, T](schema: Schema.CaseClass2[F1, F2, T], t: T): F1 =
    TODO

  /**
   * EXERCISE
   *
   * Define a generic method that can extract out the second field of any case
   * class that has two fields.
   */
  def extractSecondField[F1, F2, T](schema: Schema.CaseClass2[F1, F2, T], t: T): F2 =
    TODO

  /**
   * EXERCISE
   *
   * Define a generic method that can construct a value of any case class that
   * has two fields.
   */
  def construct2[F1, F2, T](schema: Schema.CaseClass2[F1, F2, T], f1: F1, f2: F2): T =
    TODO

  //
  // SCHEMA CAPABILITIES FOR ENUMS
  //

  sealed trait PaymentMethod
  object PaymentMethod {
    import Schema._

    final case class CreditCard(number: String) extends PaymentMethod
    object CreditCard {
      implicit val schema =
        Schema.CaseClass1[String, CreditCard](Field("number", Schema[String]), CreditCard(_), _.number)
    }
    final case class BankTransfer(account: String) extends PaymentMethod
    object BankTransfer {
      implicit val schema =
        Schema.CaseClass1[String, BankTransfer](Field("account", Schema[String]), BankTransfer(_), _.account)
    }

    implicit val schema =
      Schema.Enum2[CreditCard, BankTransfer, PaymentMethod](
        Case("CreditCard", Schema[CreditCard], _.asInstanceOf[CreditCard]),
        Case("BankTransfer", Schema[BankTransfer], _.asInstanceOf[BankTransfer])
      )
  }

  /**
   * EXERCISE
   *
   * Define a generic method that can attempt to cast a sealed trait value to
   * the first of its subtypes.
   */
  def extractFirstCase[C1 <: T, C2 <: T, T](schema: Schema.Enum2[C1, C2, T], t: T): Option[C1] =
    TODO

  /**
   * EXERCISE
   *
   * Define a generic method that can attempt to cast a sealed trait value to
   * the second of its subtypes.
   */
  def extractSecondCase[C1 <: T, C2 <: T, T](schema: Schema.Enum2[C1, C2, T], t: T): Option[C2] =
    TODO

  /**
   * EXERCISE
   *
   * Define a generic method that can attempt to the first subtype of a sealed
   * trait. Hint: This is easier than you think due to the type bounds!
   */
  def constructFirstCase[C1 <: T, C2 <: T, T](schema: Schema.Enum2[C1, C2, T], c1: C1): T =
    TODO

  /**
   * EXERCISE
   *
   * Define a generic method that can attempt to the second subtype of a sealed
   * trait. Hint: This is easier than you think due to the type bounds!
   */
  def constructSecondCase[C1 <: T, C2 <: T, T](schema: Schema.Enum2[C1, C2, T], c2: C2): T =
    TODO

  //
  // MANUAL CREATION OF SCHEMAS
  //
  object primitives {

    /**
     * EXERCISE
     *
     * Manually define a schema for the primitive type `Int`.
     */
    implicit lazy val schemaInt: Schema[Int] = TODO

    /**
     * EXERCISE
     *
     * Manually define a schema for the primitive type `String`.
     */
    implicit lazy val schemaString: Schema[String] = TODO

    /**
     * EXERCISE
     *
     * Manually define a schema for the primitive type `Boolean`.
     */
    implicit lazy val schemaBoolean: Schema[Boolean] = TODO
  }

  final case class Point(x: Int, y: Int)
  object Point {

    /**
     * EXERCISE
     *
     * Manually define a schema for the case class `Point`.
     */
    implicit lazy val schema: Schema.CaseClass2[Int, Int, Point] = TODO
  }

  sealed trait Amount
  object Amount {
    final case class USD(dollars: Int, cents: Int) extends Amount
    final case class GBP(pounds: Int, pence: Int)  extends Amount

    /**
     * EXERCISE
     *
     * Manually define a schema for the sealed trait `Amount`.
     */
    implicit lazy val schema: Schema.Enum2[USD, GBP, Amount] = TODO
  }

  //
  // DERIVATION OF SCHEMAS
  //
  final case class Actor(name: String)
  object Actor {
    val harrisonFord = Actor("Harrison Ford")
  }
  final case class Director(name: String)
  object Director {
    val ridleyScott = Director("Ridley Scott")
  }
  final case class Movie(title: String, stars: List[Actor], director: Director)
  object Movie {

    /**
     * EXERCISE
     *
     * Automatically derive a schema for the case class `Movie` using
     * `DeriveSchema.gen` method.
     */
    implicit lazy val schema: Schema.CaseClass3[String, List[Actor], Director, Movie] =
      TODO

    val bladeRunner = Movie("Blade Runner", List(Actor.harrisonFord), Director.ridleyScott)
  }

  sealed trait Color
  object Color {
    case object Red                                    extends Color
    case object Green                                  extends Color
    case object Blue                                   extends Color
    case class Custom(red: Int, green: Int, blue: Int) extends Color

    /**
     * EXERCISE
     *
     * Automatically derive a schema for the sealed trait `Color` using
     * `DeriveSchema.gen` method.
     */
    implicit lazy val schema: Schema.Enum4[Blue.type, Custom, Green.type, Red.type, Color] =
      TODO
  }

  //
  // OPERATIONS
  //

  /**
   * EXERCISE
   *
   * Use `Schema#transform` to change the `Schema[String]` into a
   * `Schema[UserId]`.
   */
  final case class UserId(value: String)
  object UserId {
    implicit lazy val schema: Schema[UserId] =
      Schema[String].TODO
  }

  /**
   * EXERCISE
   *
   * Use `Schema#transformOrFail` to change the `Schema[String]` into an Email,
   * but only if the string is a valid email.
   */
  sealed abstract case class Email private (value: String)
  object Email {
    def isValidEmail(email: String): Boolean =
      """(?=[^\s]+)(?=(\w+)@([\w\.]+))""".r.findFirstIn(email).isDefined

    def fromString(value: String): Either[String, Email] =
      if (isValidEmail(value)) Right(new Email(value) {})
      else Left(s"Invalid email: $value")

    implicit lazy val schema: Schema[Email] =
      Schema[String].TODO
  }

  //
  // GENERIC PROGRAMMING
  //

  /**
   * EXERCISE
   *
   * Define a generic method that can extract out all the fields of any record.
   */
  def fieldNames[C](schema: Schema.Record[C]): List[String] = TODO

  final case class User(name: String, password: String)
  object User {
    import Schema._

    implicit lazy val schema: Schema.CaseClass2[String, String, User] =
      Schema.CaseClass2[String, String, User](
        Field("name", Schema[String]),
        Field("password", Schema[String]),
        User(_, _),
        _.name,
        _.password
      )
  }

  /**
   * EXERCISE
   *
   * Define a generic method that can take strings in any "password" field and
   * replace their characters by asterisks, for security purposes.
   */
  def maskPasswords[A](schema: Schema.Record[A], a: A): A = TODO

  final case class CSV(headers: List[String], data: List[List[String]]) {
    def get(row: Int, field: String): Either[String, String] =
      data.lift(row).toRight(s"The row $row does not exist").flatMap { row =>
        val index = headers.indexOf(field)

        if (index < 0) Left(s"The field $field does not exist in $headers")
        else row.lift(index).toRight(s"The column $index does not exist in row $row")
      }
  }
  object CSV {
    val example = CSV(
      List("name", "age", "country"),
      List(
        List("John", "32", "USA"),
        List("Jane", "25", "UK"),
        List("Mary", "28", "USA")
      )
    )
  }

  /**
   * EXERCISE
   *
   * Define a generic method that can read CSV files into flat records. Note:
   * You do not have to support all types, just those required to get the
   * corresponding test to pass.
   */
  def fromCSV[A](csv: CSV, row: Int)(implicit schema: Schema[A]): Either[String, A] = TODO

  //
  // CODECS
  //
  import zio.Chunk

  /**
   * EXERCISE
   *
   * Define a protobuf encoder for the class `Movie`.
   */
  lazy val movieEncoder: Movie => Chunk[Byte] = TODO

  /**
   * EXERCISE
   *
   * Define a protobuf decoder for the class `Movie`.
   */
  lazy val movieDecoder: Chunk[Byte] => Either[String, Movie] = TODO

  def spec = suite("SchemaSpec") {
    suite("record capabilities") {
      test("field 1 extraction") {
        assertTrue(extractFirstField(Person.schema, Person("John", 42)) == "John")
      } @@ ignore +
        test("field 2 extraction") {
          assertTrue(extractSecondField(Person.schema, Person("John", 42)) == 42)
        } @@ ignore +
        test("construction") {
          assertTrue(construct2(Person.schema, "John", 42) == Person("John", 42))
        } @@ ignore
    } +
      suite("enum capabilities") {
        test("case 1 extraction") {
          assertTrue(
            extractFirstCase(PaymentMethod.schema, PaymentMethod.CreditCard("12345")) == Some(
              PaymentMethod.CreditCard("12345")
            )
          )
        } @@ ignore +
          test("case 1 extraction") {
            assertTrue(extractSecondCase(PaymentMethod.schema, PaymentMethod.CreditCard("12345")) == None)
          } @@ ignore +
          test("case 1 construction") {
            assertTrue(
              constructFirstCase(PaymentMethod.schema, PaymentMethod.CreditCard("12345")) == PaymentMethod.CreditCard(
                "12345"
              )
            )
          } @@ ignore +
          test("case 2 construction") {
            assertTrue(
              constructSecondCase(PaymentMethod.schema, PaymentMethod.BankTransfer("12345")) == PaymentMethod
                .BankTransfer("12345")
            )
          } @@ ignore
      } +
      suite("manual creation") {
        test("int") {
          assertTrue(primitives.schemaInt == Schema[Int])
        } @@ ignore +
          test("string") {
            assertTrue(primitives.schemaString == Schema[String])
          } @@ ignore +
          test("duration") {
            assertTrue(primitives.schemaBoolean == Schema[Boolean])
          } @@ ignore +
          test("case class") {
            val point = Point(1, 2)

            assertTrue(extractFirstField(Point.schema, point) == 1) &&
            assertTrue(extractSecondField(Point.schema, point) == 2)
          } @@ ignore +
          test("enum") {
            val usd = Amount.USD(12, 5)
            val gbp = Amount.GBP(12, 5)

            assertTrue(extractFirstCase(Amount.schema, usd: Amount) == Some(usd)) &&
            assertTrue(extractFirstCase(Amount.schema, gbp: Amount) == None)
          } @@ ignore
      } +
      suite("derivation") {
        test("record derivation") {
          val movieSchema = Schema[Movie]

          assertTrue(Movie.schema.extractField1(Movie.bladeRunner) == "Blade Runner")
        } @@ ignore +
          test("enum derivation") {
            val color = Color.Custom(1, 2, 3)

            assertTrue(Color.schema.case2.deconstruct(color) == Some(color))
          } @@ ignore
      } +
      suite("operations") {
        test("transform") {
          val userId = UserId("sholmes")

          assertTrue(
            Schema[UserId].toDynamic(userId) ==
              DynamicValue.Primitive("sholmes", StandardType.StringType)
          )
        } @@ ignore +
          test("transformOrFail") {
            val validEmail   = DynamicValue.Primitive("sherlock@holmes.com", StandardType.StringType)
            val invalidEmail = DynamicValue.Primitive("sherlock", StandardType.StringType)

            assertTrue(Schema[Email].fromDynamic(validEmail).isRight) &&
            assertTrue(Schema[Email].fromDynamic(invalidEmail).isLeft)
          } @@ ignore
      } +
      suite("generic programming") {
        test("field names") {
          assertTrue(fieldNames(Movie.schema) == List("title", "stars", "director"))
        } @@ ignore +
          test("mask passwords") {
            val masked = maskPasswords(User.schema, User("John", "abc123"))

            assertTrue(masked == User("John", "******"))
          } @@ ignore +
          test("from CSV") {
            val john = fromCSV[Person](CSV.example, 0)

            assertTrue(john == Right(Person("John", 32)))
          } @@ ignore
      } +
      suite("codecs") {
        test("protobuf") {
          import zio.Chunk
          import zio.schema.codec._

          assertTrue(movieDecoder(movieEncoder(Movie.bladeRunner)) == Right(Movie.bladeRunner))
        } @@ ignore
      }
  }
}
