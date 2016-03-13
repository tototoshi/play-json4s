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

import akka.stream.scaladsl.Sink
import akka.util.ByteString
import org.json4s.{ JValue => Json4sJValue, _ }
import play.api._
import play.api.http._
import play.api.libs.streams.Accumulator
import play.api.mvc._

import scala.concurrent.Future
import scala.language.reflectiveCalls
import scala.util.control.NonFatal

class Json4sParser[T](configuration: Configuration, methods: JsonMethods[T]) {

  import methods._

  val logger = Logger(classOf[Json4sParser[T]])

  implicit def writeableOf_NativeJValue(implicit codec: Codec): Writeable[Json4sJValue] = {
    Writeable((jval: Json4sJValue) => codec.encode(compact(render(jval))))
  }

  implicit def contentTypeOf_JsValue(implicit codec: Codec): ContentTypeOf[Json4sJValue] = {
    ContentTypeOf(Some(ContentTypes.JSON))
  }

  def DEFAULT_MAX_TEXT_LENGTH: Int = {
    val textMaxLength = configuration.getBytes("parsers.text.maxLength")
    val maxMemoryBuffer = configuration.getBytes("play.http.parser.maxMemoryBuffer")
    if (textMaxLength.nonEmpty && maxMemoryBuffer.isEmpty) {
      logger.warn("Configuration 'parsers.text.maxLength is deprecated. Use play.http.parser.maxMemoryBuffer")
    }
    maxMemoryBuffer.orElse(textMaxLength).map(_.toInt).getOrElse(102400)
  }

  final type ParseErrorHandler = (RequestHeader, ByteString, Throwable) => Future[Result]

  protected def defaultParseErrorMessage = "Invalid Json"

  protected def defaultParseErrorHandler: ParseErrorHandler = {
    (header, _, _) => internal.BodyParsers.parse.createBadResult(defaultParseErrorMessage)(header)
  }

  def defaultTolerantBodyParser[A](name: String, maxLength: Int)(parser: (RequestHeader, ByteString) => A): BodyParser[A] =
    tolerantBodyParser[A](name, maxLength, "Invalid json")(parser)(defaultParseErrorHandler)

  private def tolerantBodyParser[A](name: String, maxLength: Long, errorMessage: String)(parser: (RequestHeader, ByteString) => A)(errorHandler: ParseErrorHandler): BodyParser[A] =
    BodyParser(name + ", maxLength=" + maxLength) { request =>
      import play.api.libs.iteratee.Execution.Implicits.trampoline

      internal.BodyParsers.parse.enforceMaxLength(request, maxLength, Accumulator(
        Sink.fold[ByteString, ByteString](ByteString.empty)((state, bs) => state ++ bs)
      ) mapFuture { bytes =>
          try {
            Future.successful(Right(parser(request, bytes)))
          } catch {
            case NonFatal(e) =>
              logger.debug(errorMessage, e)
              errorHandler(request, bytes, e).map(Left(_))
          }
        })
    }

  private[this] val defaultParser: (RequestHeader, ByteString) => Json4sJValue = {
    (request, bytes) =>
      parse(bytes.iterator.asInputStream)
  }

  def tolerantJson(maxLength: Int): BodyParser[Json4sJValue] =
    defaultTolerantBodyParser[Json4sJValue]("json", maxLength)(defaultParser)

  def tolerantJsonWithErrorHandler(maxLength: Int, errorHandler: ParseErrorHandler): BodyParser[Json4sJValue] =
    tolerantBodyParser[Json4sJValue]("json", maxLength, "Invalid json")(defaultParser)(errorHandler)

  /**
   * Parse the body as Json without checking the Content-Type.
   */
  def tolerantJson: BodyParser[Json4sJValue] = tolerantJson(DEFAULT_MAX_TEXT_LENGTH)

  def jsonWithErrorHandler(maxLength: Int)(errorHandler: ParseErrorHandler): BodyParser[Json4sJValue] = BodyParsers.parse.when(
    _.contentType.exists(m => m.equalsIgnoreCase("text/json") || m.equalsIgnoreCase("application/json")),
    tolerantJsonWithErrorHandler(maxLength, errorHandler),
    internal.BodyParsers.parse.createBadResult("Expecting text/json or application/json body")
  )

  def json(maxLength: Int): BodyParser[Json4sJValue] =
    jsonWithErrorHandler(maxLength)(defaultParseErrorHandler)

  def json: BodyParser[Json4sJValue] = json(BodyParsers.parse.DefaultMaxTextLength)
}
