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

import play.api._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import org.json4s._
import org.json4s.native.JsonMethods._
import com.github.tototoshi.play2.json4s.test.native.Helpers._
import com.github.tototoshi.play2.json4s.test.MockServer

import org.scalatest.FunSpec
import org.scalatest.matchers._

case class Person(id: Long, name: String, age: Int)

object TestApplication extends Controller with Json4s {

  implicit val formats = DefaultFormats

  def get = Action { implicit request =>
    Ok(Extraction.decompose(Person(1, "ぱみゅぱみゅ", 20)))
  }

  def post = Action(json) { implicit request =>
    Ok(request.body.extract[Person].name)
  }

}


class PlayModuleSpec extends FunSpec with ShouldMatchers with MockServer with Json4s {

  describe("Json4sPlayModule") {

    describe ("With controllers") {

      it ("allow you to use json4s-native value as response") {
        val res = TestApplication.get(FakeRequest("GET", ""))
        contentType(res) should be (Some("application/json"))
        contentAsJson4s(res) should be (JObject(List(("id",JInt(1)), ("name",JString("ぱみゅぱみゅ")), ("age",JInt(20)))))
      }

      it ("accept native json request") {
        val fakeRequest = FakeRequest().withJson4sBody(parse("""{"id":1,"name":"ぱみゅぱみゅ","age":20}"""))
        val res = TestApplication.post(fakeRequest)
        contentAsString(res) should be ("ぱみゅぱみゅ")
      }

    }

    describe ("With WS") {

      it ("should enable you to use json4s objects as request body") {
        import unfiltered.filter._
        import unfiltered.request._
        import unfiltered.response._
        import play.api.libs.ws.WS
        import scala.concurrent._
        import scala.concurrent.duration._
        import scala.language.postfixOps
        import org.apache.commons.io.IOUtils

        implicit val formats = DefaultFormats

        val plan = Planify {
          case request @ Path("/foo") => {
            val in = request.inputStream
            try {
              ResponseString(IOUtils.toString(in))
            } finally {
              IOUtils.closeQuietly(in)
            }
          }
        }
        withMockServer(plan) { port =>
          implicit val app = play.api.test.FakeApplication()
          val person = Person(1, "ぱみゅぱみゅ", 20)
          val res = Await.result(
            WS.url("http://localhost:" + port + "/foo")
              .post(Extraction.decompose(person)),
            5 seconds
          )
          val chars = person.name.toCharArray
          val name = (0 until chars.length).map{ i =>
            "\\u%04x".format(Character.codePointAt(chars, i))
          }.mkString
          res.body should be (s"""{"id":1,"name":"${name}","age":20}""")
        }
      }

    }

  }
}
