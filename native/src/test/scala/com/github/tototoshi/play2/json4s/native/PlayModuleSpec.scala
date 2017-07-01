/*
 * Copyright 2013 Toshiyuki Takahashi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tototoshi.play2.json4s.native

import javax.inject.{Inject, Singleton}

import com.github.tototoshi.play2.json4s.test.native.Helpers._
import com.github.tototoshi.play2.json4s.testkit.WithActorSystem
import org.json4s._
import org.json4s.native.JsonMethods._
import org.scalatest.{FunSpec, Matchers}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._

case class Person(id: Long, name: String, age: Int)

@Singleton
class TestController @Inject()(cc: ControllerComponents, json4s: Json4s) extends AbstractController(cc) {
  implicit val formats = DefaultFormats
  import json4s.implicits._

  def get = Action {
    Ok(Extraction.decompose(Person(1, "ぱみゅぱみゅ", 20)))
  }

  def post = Action(json4s.json) { implicit request: Request[JValue] =>
    Ok(request.body.extract[Person].name)
  }

  def postExtract = Action(json4s.extract[Person]) { implicit request: Request[Person] =>
    Ok(request.body.name)
  }

}

class PlayModuleSpec extends FunSpec with Matchers with WithActorSystem {

  describe("Json4sPlayModule") {

    describe("With controllers") {

      it("allow you to use json4s-native value as response") {
        val app = new GuiceApplicationBuilder()
          .bindings(new Json4sModule)
          .build()
        running(app) {
          val controller = app.injector.instanceOf[TestController]
          val res = call(controller.get, FakeRequest("GET", ""))
          contentType(res) should be(Some("application/json"))
          contentAsJson4s(res) should be(
            JObject(List(("id", JInt(1)), ("name", JString("ぱみゅぱみゅ")), ("age", JInt(20)))))
        }
      }

      it("accept native json request") {
        val app = new GuiceApplicationBuilder()
          .bindings(new Json4sModule)
          .build()
        running(app) {
          val controller = app.injector.instanceOf[TestController]
          val json4s = app.injector.instanceOf[Json4s]
          import json4s.implicits._
          val fakeRequest = FakeRequest().withJson4sBody(parse("""{"id":1,"name":"ぱみゅぱみゅ","age":20}"""))
          contentAsString(call(controller.post, fakeRequest)) should be("ぱみゅぱみゅ")
          contentAsString(call(controller.postExtract, fakeRequest)) should be("ぱみゅぱみゅ")
        }
      }

    }

  }
}
