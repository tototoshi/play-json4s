package com.github.tototoshi.play2.json4s

import org.json4s.{Formats, JValue, Reader}
import play.api.http.{ContentTypeOf, Writeable}
import play.api.mvc.{BodyParser, Codec}

trait Json4s {

  val implicits: Json4sImplicits

  def tolerantJson(maxLength: Int): BodyParser[JValue]

  def tolerantJson: BodyParser[JValue]

  def json(maxLength: Int): BodyParser[JValue]

  def json: BodyParser[JValue]

  def extract[A](implicit format: Formats, manifest: Manifest[A]): BodyParser[A]

}

trait Json4sImplicits {

  implicit def writeableOf_JValue(implicit codec: Codec): Writeable[JValue]

  implicit def contentTypeOf_JValue(implicit codec: Codec): ContentTypeOf[JValue]

}
