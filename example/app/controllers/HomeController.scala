package controllers

import javax.inject.{ Inject, Singleton }

import com.github.tototoshi.play2.json4s.jackson.Json4s
import models.Person
import org.json4s._
import play.api.mvc.{ Controller, Action }

@Singleton
class HomeController @Inject() (json4s: Json4s) extends Controller {

  import json4s._
  implicit val formats = DefaultFormats

  def get = Action { implicit request =>
    Ok(Extraction.decompose(Person(1, "pamyupamyu", 20)))
  }

  def post = Action(json) { implicit request =>
    Ok(request.body.extract[Person].name)
  }

}
