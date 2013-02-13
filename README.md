# json4s module for Play 2.1


This module allows you to use json4s in your play20 application.


## Usage

To use json4s-native
```scala
import com.github.tototoshi.play2.json4s.native._
```

To use json4s-jackson
```scala
import com.github.tototoshi.play2.json4s.jackson._
```


```scala
case class Person(id: Long, name: String, age: Int)

object Application extends Controller with Json4s {

  implicit val formats = DefaultFormats

  def get = Action { implicit request =>
    Ok(Extraction.decompose(Person(1, "pamyupamyu", 20)))
  }

  def post = Action(json) { implicit request =>
    Ok(request.body.extract[Person].name)
  }

}
```
