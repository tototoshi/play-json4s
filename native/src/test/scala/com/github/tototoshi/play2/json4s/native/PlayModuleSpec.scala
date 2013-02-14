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

import org.specs2.mutable._

import play.api._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import org.json4s._
import org.json4s.native.JsonMethods._
import com.github.tototoshi.play2.json4s.test.native._

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


class PlayModuleSpec extends Specification with Json4s with NativeHelpers {

  val testJson = """{"id":1,"name":"ぱみゅぱみゅ","age":20}"""

  "Json4sPlayModule" should {

    "allow you to use json4s-native value as response" in {

      def removeWhiteSpaceAndNewLine(s: String): String = s.replace(" ", "").replace("\n", "")

      val res = TestApplication.get(FakeRequest("GET", ""))

      contentType(res) must beEqualTo (Some("application/json"))
      removeWhiteSpaceAndNewLine(contentAsString(res)) must beEqualTo (testJson)
    }

    "accept native json request" in {
      val header = FakeHeaders(Seq(("Content-Type" -> Seq("application/json"))))
      val res = TestApplication.post(FakeRequest().withJson4sBody(parse(testJson)))
      contentAsString(res) must beEqualTo ("ぱみゅぱみゅ")
    }

  }

}

