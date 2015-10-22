package models

import anorm._
import anorm.SqlParser._
import play.api.libs.json.JsValue
import utils.Conversions._

case class Emp(id: Long, name: String, deptId: Option[Long] = None, deptName: Option[String] = None)

object Emp extends DAO[Emp,Long] {

  override val key = Column[Long]("id", "id", updates = false)
  override val table: String = "emp"
  override val columns: List[Column[_]] = List(
    Column[String]("name", "name"),
    Column[String]("deptId", "dept_id"),
    Column[String]("deptName", "dept_name", inserts = false, updates = false, expr = Some("select d.name from dept d where d.id = emp.dept_id"))
  )
  override val searchColumns: List[Column[_]] = List(
    Column[String]("name", "name"),
    Column[String]("deptId", "dept_id"),
    Column[String]("deptName", "dept_name")
  )

  override def parser: RowParser[Emp] = {
    long("id")~str("name")~get[Option[Long]]("deptId")~get[Option[String]]("deptName") map {
      case id~name~deptId~deptName =>
        Emp(id, name, deptId, deptName)
    }
  }

  override def construct(id: Option[Long], values: Map[String, Any]): Option[Emp] = {
    Some(Emp(id, values("name"), Some(values("deptId")), None))
  }

  override def toStringAnySeq(obj: Emp): Seq[(String, Any)] = {
    List(
      "name" -> obj.name,
      "deptId" -> obj.deptId,
      "deptName" -> obj.deptName
    )
  }

  override def toStringAnySeq(values: Seq[(String, JsValue)]): Seq[(String, Any)] = {
    values.map(v => v._1 -> (v._1 match {
      case "id" => v._2.as[Long]
      case "name" => v._2.as[String]
      case "deptId" => v._2.as[Long]
      case "deptName" => v._2.as[String]
      case _ => ""
    }))
  }
}