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

  import SchemaSection._

  def spec = suite("SchemaSpec") {
    suite("record capabilities") {
      test("field 1 extraction") {
        assertTrue(extractFirstField(Person.schema, Person("John", 42)) == "John")
      } +
        test("field 2 extraction") {
          assertTrue(extractSecondField(Person.schema, Person("John", 42)) == 42)
        } +
        test("construction") {
          assertTrue(construct2(Person.schema, "John", 42) == Person("John", 42))
        }
    } +
      suite("enum capabilities") {
        test("case 1 extraction") {
          assertTrue(
            extractFirstCase(PaymentMethod.schema, PaymentMethod.CreditCard("12345")) == Some(
              PaymentMethod.CreditCard("12345")
            )
          )
        } +
          test("case 1 extraction") {
            assertTrue(extractSecondCase(PaymentMethod.schema, PaymentMethod.CreditCard("12345")) == None)
          } +
          test("case 1 construction") {
            assertTrue(
              constructFirstCase(PaymentMethod.schema, PaymentMethod.CreditCard("12345")) == PaymentMethod.CreditCard(
                "12345"
              )
            )
          } +
          test("case 2 construction") {
            assertTrue(
              constructSecondCase(PaymentMethod.schema, PaymentMethod.BankTransfer("12345")) == PaymentMethod
                .BankTransfer("12345")
            )
          }
      } +
      suite("manual creation") {
        test("int") {
          assertTrue(primitives.schemaInt == Schema[Int])
        } +
          test("string") {
            assertTrue(primitives.schemaString == Schema[String])
          } +
          test("duration") {
            assertTrue(primitives.schemaBoolean == Schema[Boolean])
          } +
          test("case class") {
            val point = Point(1, 2)

            assertTrue(extractFirstField(Point.schema, point) == 1) &&
            assertTrue(extractSecondField(Point.schema, point) == 2)
          } +
          test("enum") {
            val usd = Amount.USD(12, 5)
            val gbp = Amount.GBP(12, 5)

            assertTrue(extractFirstCase(Amount.schema, usd: Amount) == Some(usd)) &&
            assertTrue(extractFirstCase(Amount.schema, gbp: Amount) == None)
          }
      } +
      suite("derivation") {
        test("record derivation") {
          val movieSchema = Schema[Movie]

          assertTrue(Movie.schema.extractField1(Movie.bladeRunner) == "Blade Runner")
        } +
          test("enum derivation") {
            val color = Color.Custom(1, 2, 3)

            assertTrue(Color.schema.case2.deconstruct(color) == Some(color))
          }
      } +
      suite("operations") {
        test("transform") {
          val userId = UserId("sholmes")

          assertTrue(
            Schema[UserId].toDynamic(userId) ==
              DynamicValue.Primitive("sholmes", StandardType.StringType)
          )
        } +
          test("transformOrFail") {
            val validEmail =
              DynamicValue.Primitive("sherlock@holmes.com", StandardType.StringType)
            val invalidEmail = DynamicValue.Primitive("sherlock", StandardType.StringType)

            assertTrue(Schema[Email].fromDynamic(validEmail).isRight) &&
            assertTrue(Schema[Email].fromDynamic(invalidEmail).isLeft)
          }
      } +
      suite("generic programming") {
        test("field names") {
          assertTrue(fieldNames(Movie.schema) == List("title", "stars", "director"))
        } +
          test("mask passwords") {
            val masked = maskPasswords(User.schema, User("John", "abc123"))

            assertTrue(masked == User("John", "******"))
          } +
          test("from CSV") {
            val john = fromCSV[Person](CSV.example, 0)

            assertTrue(john == Right(Person("John", 32)))
          }
      } +
      suite("codecs") {
        test("protobuf") {
          import zio.Chunk
          import zio.schema.codec._

          assertTrue(movieDecoder(movieEncoder(Movie.bladeRunner)) == Right(Movie.bladeRunner))
        }
      }
  }
}
