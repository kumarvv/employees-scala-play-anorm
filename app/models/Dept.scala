package models

import anorm._
import anorm.SqlParser._
import play.api.libs.json.JsValue
import utils.Conversions._

case class Dept(id: Long, name: String)

object Dept extends DAO[Dept,Long] {

  override val key = Field[Long]("id", "id", updates = false)
  override val table: String = "dept"
  override val columns: List[Field[_]] = List(
    Field[String]("name", "name")
  )
  override val searchColumns: List[Field[_]] = List(
    Field[String]("name", "name")
  )

  override def parser: RowParser[Dept] = {
    long("id")~str("name") map {
      case id~name =>
        Dept(id, name)
    }
  }

  override def construct(id: Option[Long], values: Map[String, Any]): Option[Dept] = {
    Some(Dept(id, values("name")))
  }

  override def toStringAnySeq(obj: Dept): Seq[(String, Any)] = {
    List(
      "name" -> obj.name
    )
  }

  override def toStringAnySeq(values: Seq[(String, JsValue)]): Seq[(String, Any)] = {
    values.map(v => v._1 -> (v._1 match {
      case "id" => v._2.as[Long]
      case "name" => v._2.as[String]
      case _ => ""
    }))
  }
}