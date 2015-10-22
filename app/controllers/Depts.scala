package controllers

import play.api.mvc._
import models._
import org.json4s._
import play.twirl.api.Html

class Depts extends AppController[Dept,Long] {

  override def dao: DAO[Dept,Long] = Dept

  override val renderList: Option[(String) => Html] = Some(views.html.dept.list.render _)

  override def create = Action(json) { req =>
    val o = req.body.extract[Dept]
    val created = Dept.create("name" -> o.name);
    Ok(encodeJson(created))
  }

  override def update(id: Long) = Action(json) { req =>
    Dept.find(id) match {
      case Some(d) => {
        val o = req.body.extract[Dept]
        val updated = Dept.update(id, "name" -> o.name)
        Ok(encodeJson(updated))
      }
      case _ => NotFound
    }
  }
}
