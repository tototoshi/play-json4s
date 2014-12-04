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
import org.json4s.{JValue => Json4sJValue}
import scala.language.reflectiveCalls
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

trait MethodsImport[T] {
  val methods: JsonMethods[T]
}

trait Json4sParser[T] {
  self: MethodsImport[T] =>

  import self.methods._

  implicit def writeableOf_NativeJValue(implicit codec: Codec): Writeable[Json4sJValue] = {
    Writeable((jval: Json4sJValue) => codec.encode(compact(render(jval))))
  }

  implicit def contentTypeOf_JsValue(implicit codec: Codec): ContentTypeOf[Json4sJValue] = {
    ContentTypeOf(Some(ContentTypes.JSON))
  }

  /**
   * This method is copied from Play 2.2
   */
  def DEFAULT_MAX_TEXT_LENGTH: Int = Play.maybeApplication.flatMap { app =>
    app.configuration.getBytes("parsers.text.maxLength").map(_.toInt)
  }.getOrElse(1024 * 100)

  /**
   * This method is copied from Play 2.2
   */
  private def tolerantBodyParser[A](name: String, maxLength: Int, errorMessage: String)(parser: (RequestHeader, Array[Byte]) => A): BodyParser[A] =
    BodyParser(name + ", maxLength=" + maxLength) {
      request =>
        import scala.util.control.Exception._

        val bodyParser: Iteratee[Array[Byte], Either[Result, Either[Future[Result], A]]] =
          Traversable.takeUpTo[Array[Byte]](maxLength).transform(
            Iteratee.consume[Array[Byte]]().map {
              bytes =>
                allCatch[A].either {
                  parser(request, bytes)
                }.left.map {
                  e =>
                    createBadResult(errorMessage)(request)
                }
            }
          ).flatMap(Iteratee.eofOrElse(Results.EntityTooLarge))

        bodyParser.mapM {
          case Left(tooLarge) => Future.successful(Left(tooLarge))
          case Right(Left(badResult)) => badResult.map(Left.apply)
          case Right(Right(body)) => Future.successful(Right(body))
        }
    }

  def tolerantJson(maxLength: Int): BodyParser[Json4sJValue] =
    tolerantBodyParser[Json4sJValue]("json", maxLength, "Invalid Json") {
      (request, bytes) =>
        parse(new String(bytes, request.charset.getOrElse("utf-8")))
    }

  /**
   * Parse the body as Json without checking the Content-Type.
   */
  def tolerantJson: BodyParser[Json4sJValue] = tolerantJson(DEFAULT_MAX_TEXT_LENGTH)

  private def createBadResult(msg: String): RequestHeader => Future[Result] = {
    request =>
      Play.maybeApplication.map(_.global.onBadRequest(request, "Expecting json body"))
        .getOrElse(Future.successful(Results.BadRequest))
  }

  def json(maxLength: Int): BodyParser[Json4sJValue] = BodyParsers.parse.when(
    _.contentType.exists(m => m.equalsIgnoreCase("text/json") || m.equalsIgnoreCase("application/json")),
    tolerantJson(maxLength),
    createBadResult("Expecting text/json or application/json body")
  )

  def json: BodyParser[Json4sJValue] = json(BodyParsers.parse.DEFAULT_MAX_TEXT_LENGTH)

}

