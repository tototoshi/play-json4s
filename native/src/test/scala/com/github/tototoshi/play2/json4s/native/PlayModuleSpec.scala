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

import com.github.tototoshi.play2.json4s.test.native.Helpers._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.scalatest.{ FunSpec, ShouldMatchers }
import play.api._
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import play.core.server.Server

case class Person(id: Long, name: String, age: Int)

class TestApplication(json4s: Json4s) extends Controller {

  implicit val formats = DefaultFormats

  import json4s._

  def get = Action { implicit request =>
    Ok(Extraction.decompose(Person(1, "ぱみゅぱみゅ", 20)))
  }

  def post = Action(json) { implicit request =>
    Ok(request.body.extract[Person].name)
  }

}

class PlayModuleSpec extends FunSpec with ShouldMatchers {

  val configuration = Configuration.empty

  val json4s = new Json4s(configuration)
  import json4s._

  describe("Json4sPlayModule") {

    describe("With controllers") {

      it("allow you to use json4s-native value as response") {
        val app = new TestApplication(json4s)
        val res = app.get(FakeRequest("GET", ""))
        contentType(res) should be(Some("application/json"))
        contentAsJson4s(res) should be(JObject(List(("id", JInt(1)), ("name", JString("ぱみゅぱみゅ")), ("age", JInt(20)))))
      }

      it("accept native json request") {
        val fakeRequest = FakeRequest().withJson4sBody(parse("""{"id":1,"name":"ぱみゅぱみゅ","age":20}"""))
        val app = new TestApplication(json4s)
        val res = app.post(fakeRequest)
        contentAsString(res) should be("ぱみゅぱみゅ")
      }

    }

    describe("With WS") {

      it("should enable you to use json4s objects as request body") {
        import scala.concurrent._
        import scala.concurrent.duration._
        import scala.language.postfixOps

        implicit val formats = DefaultFormats

        Server.withRouter() {
          case _ => Action(json4s.json) { request =>
            val person = request.body.extract[Person]
            play.api.mvc.Results.Ok(Extraction.decompose(person))
          }
        } { implicit port =>
          WsTestClient.withClient { client =>
            val person = Person(1, "ぱみゅぱみゅ", 20)
            val res = Await.result(
              client.url("http://localhost:" + port)
                .post(Extraction.decompose(person)),
              5 seconds
            )
            val chars = person.name.toCharArray
            val name = (0 until chars.length).map { i =>
              "\\u%04x".format(Character.codePointAt(chars, i))
            }.mkString
            res.body should be(s"""{"id":1,"name":"${name}","age":20}""")
          }
        }
      }

    }

  }
}
