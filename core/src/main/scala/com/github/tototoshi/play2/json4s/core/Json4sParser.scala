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

import akka.stream.Materializer
import akka.util.ByteString
import com.github.tototoshi.play2.json4s.{Json4s, Json4sImplicits}
import org.json4s.{Formats, JValue, JsonMethods, Reader}
import play.api.http._
import play.api.libs.Files.TemporaryFileCreator
import play.api.mvc._

import scala.concurrent.Future

abstract class Json4sParser[T](
    methods: JsonMethods[T],
    val config: ParserConfiguration,
    val errorHandler: HttpErrorHandler,
    val materializer: Materializer,
    val temporaryFileCreator: TemporaryFileCreator)
  extends Json4s {

  import methods._
  import BodyParsers.utils._
  import play.api.http.Status._

  val implicits: Json4sImplicits = new Json4sImplicits {

    implicit def writeableOf_JValue(implicit codec: Codec): Writeable[JValue] = {
      Writeable((jval: JValue) => codec.encode(compact(render(jval))))
    }

    implicit def contentTypeOf_JValue(implicit codec: Codec): ContentTypeOf[JValue] = {
      ContentTypeOf(Some(ContentTypes.JSON))
    }

  }

  private class InternalPlayBodyParser(
      val config: ParserConfiguration,
      val errorHandler: HttpErrorHandler,
      val materializer: Materializer,
      val temporaryFileCreator: TemporaryFileCreator)
    extends PlayBodyParsers {

    def publicTolerantBodyParser[A](
        name: String,
        maxLength: Long,
        errorMessage: String
      )(parser: (RequestHeader, ByteString) => A
      ): BodyParser[A] = tolerantBodyParser(name, maxLength, errorMessage)(parser)

    def publicCreateBadResult(msg: String, statusCode: Int = BAD_REQUEST): RequestHeader => Future[Result] = {
      super.createBadResult(msg, statusCode)
    }
  }

  private val internalParser: InternalPlayBodyParser =
    new InternalPlayBodyParser(config, errorHandler, materializer, temporaryFileCreator)

  private def createBadResult(msg: String, statusCode: Int = BAD_REQUEST): RequestHeader => Future[Result] =
    internalParser.publicCreateBadResult(msg, statusCode)

  def extract[A](implicit format: Formats, manifest: Manifest[A]): BodyParser[A] =
    parseWith(j => j.extractOpt[A])

  private def parseWith[A](f: JValue => Option[A]): BodyParser[A] =
    BodyParser("json reader") { request =>
      import internal.Execution.Implicits.trampoline
      json(request).mapFuture {
        case Left(simpleResult) =>
          Future.successful(Left(simpleResult))
        case Right(jsValue) =>
          f(jsValue) match {
            case Some(a) => Future.successful(Right(a))
            case None =>
              val msg = s"Json validation error"
              createBadResult(msg)(request) map Left.apply
          }
      }
    }

  protected def tolerantBodyParser[A](
      name: String,
      maxLength: Long,
      errorMessage: String
    )(parser: (RequestHeader, ByteString) => A
    ): BodyParser[A] = internalParser.publicTolerantBodyParser[A](name, maxLength, errorMessage)(parser)

  def DefaultMaxTextLength: Int = internalParser.DefaultMaxTextLength

  def tolerantJson(maxLength: Int): BodyParser[JValue] =
    tolerantBodyParser[JValue]("json", maxLength, "Invalid Json") { (request, bytes) =>
      // Encoding notes: RFC 4627 requires that JSON be encoded in Unicode, and states that whether that's
      // UTF-8, UTF-16 or UTF-32 can be auto detected by reading the first two bytes. So we ignore the declared
      // charset and don't decode, we passing the byte array as is because Jackson supports auto detection.
      methods.parse(bytes.iterator.asInputStream)
    }

  def tolerantJson: BodyParser[JValue] = tolerantJson(DefaultMaxTextLength)

  def json(maxLength: Int): BodyParser[JValue] = when(
    _.contentType.exists(m => m.equalsIgnoreCase("text/json") || m.equalsIgnoreCase("application/json")),
    tolerantJson(maxLength),
    createBadResult("Expecting text/json or application/json body", UNSUPPORTED_MEDIA_TYPE)
  )

  def json: BodyParser[JValue] = json(DefaultMaxTextLength)

}
