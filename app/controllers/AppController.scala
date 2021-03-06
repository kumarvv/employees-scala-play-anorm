package controllers


import com.github.tototoshi.play2.json4s.native.Json4s
import play.api.mvc._
import models.DAO
import org.json4s._
import org.json4s.jackson._
import play.twirl.api.Html

trait AppController[T,K] extends Controller with Json4s {

  implicit val formats = DefaultFormats

  def dao: DAO[T,K]
  val renderList: Option[String => Html]

  def all = Action {
    val json = Serialization.write(dao.findAll)
    Ok(renderList.get(json));
  }

  def create: Action[JValue]

  def update(id: K): Action[JValue]

  def delete(id: K) = Action {
    dao.find(id) match {
      case Some(d) => dao.delete(id); Ok
      case _ => NotFound
    }
  }

  def encodeJson(src: AnyRef): JValue = {
    implicit val formats = Serialization.formats(NoTypeHints)
    Extraction.decompose(src)
  }
}
