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
package com.github.tototoshi.play2.json4s.test.core

import play.api.mvc.{ Result, Content, Request }
import play.api.test._
import play.api.test.Helpers._
import org.json4s._
import com.github.tototoshi.play2.json4s.core._

trait Helpers[T] { self: MethodsImport[T] =>

  import self.methods._

  def contentAsJson4s(of: Result): JValue = parse(contentAsString(of))

  def contentAsJson4s(of: Content): JValue = parse(of.body)

  implicit class Json4sFakeRequest[A](fakeRequest: FakeRequest[A]) {
    def withJson4sBody(jval: JValue): Request[JValue] = fakeRequest.withBody(body = jval)
  }

}

trait NativeHelpers extends Helpers[scala.text.Document] with MethodsImport[scala.text.Document]


trait JacksonHelpers extends Helpers[JValue] with MethodsImport[JValue]


