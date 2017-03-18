# play-json4s

[![Build Status](https://travis-ci.org/tototoshi/play-json4s.png)](https://travis-ci.org/tototoshi/play-json4s)

This module allows you to use json4s in your play20 application.


## Install
Current version is

  - 0.1.0 (for Play 2.1)
  - 0.2.0 (for Play 2.2)
  - 0.3.1 (for Play 2.3)
  - 0.4.2 (for Play 2.4)
  - 0.5.0 (for Play 2.5, Json4s 3.3.0)
  - 0.6.0 (for Play 2.5, Json4s 3.4.2)
  - 0.7.0 (for Play 2.5, Json4s 3.5.1)

Add the following lines in your build.sbt.

To use json4s-native
```scala
libraryDependencies ++= Seq(
  "com.github.tototoshi" %% "play-json4s-native" % "0.7.0",
  "com.github.tototoshi" %% "play-json4s-test-native" % "0.7.0" % "test"
)
```

To use json4s-jackson
```scala
libraryDependencies ++= Seq(
  "com.github.tototoshi" %% "play-json4s-jackson" % "0.7.0",
  "com.github.tototoshi" %% "play-json4s-test-jackson" % "0.7.0" % "test"
)
```

## Usage

If you are using Play 2.4, see [README.play24.md](./README.play24.md)

### With controllers

```
play.modules.enabled += "com.github.tototoshi.play2.json4s.jackson.Json4sModule"
// or play.modules.enabled += "com.github.tototoshi.play2.json4s.native.Json4sModule"
```

```scala
package controllers

import javax.inject.{ Inject, Singleton }

import com.github.tototoshi.play2.json4s.Json4s
import models.Person
import org.json4s._
import play.api.mvc.{ Controller, Action }

@Singleton
class HomeController @Inject() (json4s: Json4s) extends Controller {

  import json4s._
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
class PlayModuleSpec extends FunSpec with ShouldMatchers {

  val configuration = Configuration.empty

  val json4s = new Json4s(configuration)
  import json4s._

  describe("Json4sPlayModule") {

    describe("With controllers") {

      it("allow you to use json4s-jackson value as response") {
        val app = new TestApplication(json4s)
        val res = app.get(FakeRequest("GET", ""))
        contentType(res) should be(Some("application/json"))
        contentAsJson4s(res) should be(JObject(List(("id", JInt(1)), ("name", JString("ぱみゅぱみゅ")), ("age", JInt(20)))))
      }

      it("accept json4s-jackson request") {
        val app = new TestApplication(json4s)
        val res = app.post(FakeRequest().withJson4sBody(parse("""{"id":1,"name":"ぱみゅぱみゅ","age":20}""")))
        contentAsString(res) should be("ぱみゅぱみゅ")
      }

    }
  }
}
```

## ChangeLog

### 0.5.0
 - Support Play 2.5 and DI

### 0.4.2
 - Updated json4s version to 3.3.0

### 0.4.1
 - Re-published 0.4.0 because of invalid sha1 problem of sonatype

### 0.4.0
 - Support Play 2.4

### 0.3.1
 - Added custom parse error handler

### 0.3.0
 - Support Play 2.3

## License
[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)
