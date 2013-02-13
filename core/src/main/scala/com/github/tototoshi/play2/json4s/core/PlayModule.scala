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

package com.github.tototoshi.play2.json4s.core

import play.api._
import play.api.http._
import play.api.mvc._
import play.api.libs.iteratee._
import org.json4s._
import org.json4s.{ JValue => Json4sJValue }
import scala.language.reflectiveCalls


trait MethodsImport[T] {
  val methods: JsonMethods[T]
}

trait Json4sParser[T] { self: MethodsImport[T] =>

  import self.methods._

  implicit def writeableOf_NativeJValue(implicit codec: Codec): Writeable[Json4sJValue] = {
    Writeable((jval: Json4sJValue) => codec.encode(pretty(render(jval))))
  }

  implicit def contentTypeOf_JsValue(implicit codec: Codec): ContentTypeOf[Json4sJValue] = {
    ContentTypeOf(Some(ContentTypes.JSON))
  }

  def tolerantJson(maxLength: Int): BodyParser[Json4sJValue] = BodyParser("json, maxLength=" + maxLength) { request =>
    play.api.libs.iteratee.Traversable.takeUpTo[Array[Byte]](maxLength).apply(Iteratee.consume[Array[Byte]]().map { bytes =>
      scala.util.control.Exception.allCatch[Json4sJValue].either {
        parse(new String(bytes, request.charset.getOrElse("utf-8")))
      }.left.map { e =>
        (Play.maybeApplication.map(_.global.onBadRequest(request, "Invalid Json")).getOrElse(Results.BadRequest), bytes)
      }
    }).flatMap(Iteratee.eofOrElse(Results.EntityTooLarge))
    .flatMap {
      case Left(b) => Done(Left(b), Input.Empty)
      case Right(it) => it.flatMap {
        case Left((r, in)) => Done(Left(r), Input.El(in))
        case Right(json) => Done(Right(json), Input.Empty)
      }
    }
  }

  def tolerantJson: BodyParser[Json4sJValue] = tolerantJson(BodyParsers.parse.DEFAULT_MAX_TEXT_LENGTH)

  def json(maxLength: Int): BodyParser[Json4sJValue] = BodyParsers.parse.when(
    _.contentType.exists(m => m == "text/json" || m == "application/json"),
    tolerantJson(maxLength),
    request => Play.maybeApplication.map(_.global.onBadRequest(request, "Expecting text/json or application/json body")).getOrElse(Results.BadRequest)
  )

  def json: BodyParser[Json4sJValue] = json(BodyParsers.parse.DEFAULT_MAX_TEXT_LENGTH)

}

