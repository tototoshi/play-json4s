package controllers

import javax.inject.{Inject, Singleton}

import com.github.tototoshi.play2.json4s.Json4s
import models.Person
import org.json4s._
import play.api.mvc.{AbstractController, ControllerComponents}

@Singleton
class HomeController @Inject()(cc: ControllerComponents, json4s: Json4s) extends AbstractController(cc) {

  import json4s.implicits._
  implicit val formats = DefaultFormats

  def get = Action { implicit request =>
    Ok(Extraction.decompose(Person(1, "pamyupamyu", 20)))
  }

  def post = Action(json4s.json) { implicit request =>
    Ok(request.body.extract[Person].name)
  }

}
