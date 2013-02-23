# play-json4s

This module allows you to use json4s in your play20 application.


## Install
Current version is 0.1.0.

Add the following lines in your build.sbt.

To use json4s-native
```scala
libraryDependencies ++= Seq(
  "com.github.tototoshi" %% "play-json4s-native" % "0.1.0",
  "com.github.tototoshi" %% "play-json4s-test-native" % "0.1.0" % "test"
)
```

To use json4s-jackson
```scala
libraryDependencies ++= Seq(
  "com.github.tototoshi" %% "play-json4s-jackson" % "0.1.0",
  "com.github.tototoshi" %% "play-json4s-test-jackson" % "0.1.0" % "test"
)
```

## Usage

### With controllers

To use json4s-native
```scala
import com.github.tototoshi.play2.json4s.native._
```

To use json4s-jackson
```scala
import com.github.tototoshi.play2.json4s.jackson._
```

All you have to do is to mix-in Json4s trait.

```scala
object Application extends Controller with Json4s {
```

Json4s provides
- Body parser for Json4s
- Implicit objects to create response objects with json Body

See below

```scala
case class Person(id: Long, name: String, age: Int)

object Application extends Controller with Json4s {

  implicit val formats = DefaultFormats

  def get = Action { implicit request =>
    Ok(Extraction.decompose(Person(1, "pamyupamyu", 20)))
  }

  def post = Action(json) { implicit request =>
    Ok(request.body.extract[Person].name)
  }

}
```

### With WS

You can use json4s objects as request body.

```scala
WS.url("http://......")
  .post(Extraction.decompose(Person(1, "pamyupamyu", 20))),
```

### With tests

This module also provides test helpers for Json4s.

To use test helpers for json4s-native

```scala
import com.github.tototoshi.play2.json4s.test.native.Helpers._
```

To use test helpers for json4s-jackson

```scala
import com.github.tototoshi.play2.json4s.test.jackson.Helpers._
```

This add the features as the following
- FakeApplication#withJson4sBody
- contentAsJson4s


```scala
(some imports as usual ...)

import org.json4s._
import org.json4s.native.JsonMethods._
import com.github.tototoshi.play2.json4s.test.native.Helpers._

class ApplicationSpec extends Specification {

  "Json4sPlayModule" should {

    "allow you to use json4s-native value as response" in {
      val res = Application.get(FakeRequest())
      contentType(res) must beSome("application/json")
      contentAsJson4s(res) must_== (JObject(List(("id",JInt(1)), ("name",JString("ぱみゅぱみゅ")), ("age",JInt(20)))))
    }

    "accept native json request" in {
      val fakeRequest = FakeRequest().withJson4sBody(parse("""{"id":1,"name":"ぱみゅぱみゅ","age":20}"""))
      val res = Application.post(fakeRequest)
      contentAsString(res) must beEqualTo ("ぱみゅぱみゅ")
    }

  }

}
```

## License
[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)
