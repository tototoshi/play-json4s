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
  - 0.8.0 (for Play 2.6, Json4s 3.5.2)

Add the following lines in your build.sbt.

To use json4s-native
```scala
libraryDependencies ++= Seq(
  "com.github.tototoshi" %% "play-json4s-native" % "0.8.0",
  "com.github.tototoshi" %% "play-json4s-test-native" % "0.8.0" % "test"
)
```

To use json4s-jackson
```scala
libraryDependencies ++= Seq(
  "com.github.tototoshi" %% "play-json4s-jackson" % "0.8.0",
  "com.github.tototoshi" %% "play-json4s-test-jackson" % "0.8.0" % "test"
)
```

## Usage

If you are using Play 2.5, see [README.play25.md](./README.play25.md)

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

  import json4s.implicits._
  implicit val formats = DefaultFormats

  def get = Action { implicit request =>
    Ok(Extraction.decompose(Person(1, "pamyupamyu", 20)))
  }

  def post = Action(json4s.json) { implicit request =>
    Ok(request.body.extract[Person].name)
  }

}
```

### With WS

[play-ws-standalone-json4s](https://github.com/tototoshi/play-ws-standalone-json4s)

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
class PlayModuleSpec extends FunSpec with Matchers with WithActorSystem {

  describe("Json4sPlayModule") {

    describe("With controllers") {

      it("allow you to use json4s-jackson value as response") {
        val app = new GuiceApplicationBuilder()
          .bindings(new Json4sModule)
          .build()
        running(app) {
          val controller = app.injector.instanceOf[TestController]
          val res = call(controller.get, FakeRequest("GET", ""))
          contentType(res) should be(Some("application/json"))
          contentAsJson4s(res) should be(
            JObject(List(("id", JInt(1)), ("name", JString("ぱみゅぱみゅ")), ("age", JInt(20))))
          )
        }
      }

      it("accept json4s-jackson request") {
        val request = FakeRequest().withJson4sBody(parse("""{"id":1,"name":"ぱみゅぱみゅ","age":20}"""))
        val app = new GuiceApplicationBuilder()
          .bindings(new Json4sModule)
          .build()
        running(app) {
          val controller = app.injector.instanceOf[TestController]
          val json4s = app.injector.instanceOf[Json4s]
          import json4s.implicits._
          contentAsString(call(controller.post, request)) should be("ぱみゅぱみゅ")
          contentAsString(call(controller.postExtract, request)) should be("ぱみゅぱみゅ")
        }
      }

    }

  }

}
```

## ChangeLog

### 0.8.0

 - Support Play 2.6.0
 - Json4s 3.5.2

### 0.7.0
 - Json4s 3.5.1

### 0.6.0
 - Json4s 3.4.2

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
