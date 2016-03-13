package com.github.tototoshi.play2.json4s

import akka.util.ByteString
import org.json4s.JValue
import play.api.http.{ ContentTypeOf, Writeable }
import play.api.mvc.{ BodyParser, Codec, RequestHeader, Result }

import scala.concurrent.Future

trait Json4s {

  implicit def writeableOf_NativeJValue(implicit codec: Codec): Writeable[JValue]

  implicit def contentTypeOf_JsValue(implicit codec: Codec): ContentTypeOf[JValue]

  final type ParseErrorHandler = (RequestHeader, ByteString, Throwable) => Future[Result]

  protected def defaultParseErrorMessage = "Invalid Json"

  protected def defaultParseErrorHandler: ParseErrorHandler

  def defaultTolerantBodyParser[A](name: String, maxLength: Int)(parser: (RequestHeader, ByteString) => A): BodyParser[A]

  def tolerantJsonWithErrorHandler(maxLength: Int, errorHandler: ParseErrorHandler): BodyParser[JValue]

  def tolerantJson(maxLength: Int): BodyParser[JValue]

  def tolerantJson: BodyParser[JValue]

  def jsonWithErrorHandler(maxLength: Int)(errorHandler: ParseErrorHandler): BodyParser[JValue]

  def json(maxLength: Int): BodyParser[JValue]

  def json: BodyParser[JValue]

}

