package models

import java.sql.{Connection, PreparedStatement}

import anorm.ParameterValue._
import anorm._
import play.api.libs.json.JsValue
import play.api.db.{ConnectionPool, DB}
import play.api.Play.current

/**
  * Data Access Object with all basic functionalities
  *
  * @tparam T Entity Object
  * @tparam K Primary Key Type
  */
trait DAO[T,K] {
  val table: String
  val key: Field[K] = Field[K]("id", updates = false)
  val columns: List[Field[_]]
  val orderBy: List[Field[_]] = Nil
  val limit: Int = 100
  val searchColumns: List[Field[_]] = Nil

  def parser: RowParser[T]
  def construct(id: Option[K], values: Map[String,Any]): Option[T]
  def toStringAnySeq(obj: T): Seq[(String,Any)]
  def toStringAnySeq(values: Seq[(String,JsValue)]): Seq[(String,Any)]

  implicit def customToStatement: ToStatement[Any] = new ToStatement[Any] {
    def set(statement: PreparedStatement, i: Int, value: Any): Unit =
      statement.setObject(i, value)
  }
  implicit def pToStatement: ToStatement[K] = new ToStatement[K] {
    def set(statement: PreparedStatement, i: Int, value: K): Unit =
      statement.setObject(i, value)
  }

  implicit def P2Long(id: K): Long = id.asInstanceOf[Long]
  implicit def Long2P(id: Long): K = id.asInstanceOf[K]

  /**
    * build sqls for various dml operations
    */
  lazy val insertColumns: List[Field[_]] = (if (!key.generatedValue && key.inserts) List(key) else Nil) ::: columns.filter(_.inserts)
  lazy val colsMap: Map[String,Field[_]] = columns.map(c => c.name -> c).toMap
  lazy val colsNames: List[String] = columns.map(_.name)

  lazy val sqlAll =
    s"""select ${(key.queryExpr :: columns.map(_.queryExpr)).mkString(", ")}
        |from ${table}
        |${if (orderBy.nonEmpty) orderBy.map(_.dbName).mkString(", ") else "" } limit ${limit}
     """.stripMargin

  lazy val sqlSearch = s"""select ${(key.queryExpr :: columns.map(_.queryExpr)).mkString(", ")}
                           |from ${table}
                           |where ${searchColumns.map(c => c.dbName + " like {str}").mkString("(", " OR ", ")")}
                           |${if (orderBy.nonEmpty) orderBy.map(_.dbName).mkString(", ") else "" } limit ${limit}
     """.stripMargin

  lazy val sqlOne =
    s"""select ${(key.queryExpr :: columns.map(_.queryExpr)).mkString(", ")}
        |from ${table}
        |where ${key.updateExpr}
     """.stripMargin

  lazy val sqlInsert =
    s"""insert into ${table}
        | (${insertColumns.map(_.dbName).mkString(", ")})
        |values
        | (${insertColumns.map("{" + _.name + "}").mkString(", ")})
     """.stripMargin

  lazy val sqlUpdate =
    s"""update ${table} set
        |${columns.filter(_.updates).map(_.updateExpr).mkString(", ")}
        |where ${key.updateExpr}
     """.stripMargin

  lazy val sqlDelete =
    s"""delete from ${table}
        |where ${key.updateExpr}
     """.stripMargin

  def findAll: Seq[T] = DB.withConnection { implicit conn =>
    SQL(sqlAll).as(parser.*)
  }

  def find(id: K): Option[T] = DB.withConnection { implicit conn =>
    val item = SQL(sqlOne).on(NamedParameter(key.name, id)).as(parser.singleOpt)
    item
  }

  def findBy(str: String): Seq[T] = DB.withConnection { implicit conn =>
    SQL(sqlSearch).on(NamedParameter("str", "%" + str + "%")).as(parser.*)
  }

  def create(values: (String,Any)*): Option[T] = DB.withConnection { implicit conn =>
    val onValues: Seq[NamedParameter] = values.map(v => NamedParameter(v._1, toParameterValue[Any](v._2))).toSeq
    if (key.generatedValue) {
      SQL(sqlInsert).on(onValues: _*).executeInsert() match {
        case Some(id: Long) => find(id) //construct(Some(id), values.toMap)
        case None => None
      }
    } else {
      SQL(sqlInsert).on(onValues: _*).execute()
      construct(None, values.toMap)
    }
  }

  def update(id: K, values: (String,Any)*): Option[T] = DB.withConnection { implicit conn =>
    val sqlUpdateC =
      s"""update ${table} set
          |${values.filter(v => colsMap.contains(v._1) && colsMap(v._1).updates).map(v => colsMap(v._1).updateExpr).mkString(", ")}
          |where ${key.updateExpr}
     """.stripMargin

    val onValues: Seq[NamedParameter] = NamedParameter(key.name, toParameterValue[Any](id)) ::
      values.filter(v => colsMap.contains(v._1)).map(v => NamedParameter(v._1, toParameterValue[Any](v._2))).toList
    val res: Int = SQL(sqlUpdateC).on(onValues: _*).executeUpdate()
    res match {
      case 1 => find(id)
      case _ => None
    }
  }

  def updateAllColumns(id: K, values: (String,Any)*): Option[T] = DB.withConnection { implicit conn =>
    val onValues: Seq[NamedParameter] = values.map(v => NamedParameter(v._1, toParameterValue[Any](v._2))).toSeq
    val res: Int = SQL(sqlUpdate).on(onValues: _*).executeUpdate()
    if (res > 0) construct(Some(id), values.toMap) else None
  }

  def delete(id: K): Boolean = DB.withConnection { implicit conn =>
    val res = SQL(sqlDelete).on(NamedParameter(key.name, id)).executeUpdate()
    res match {
      case 1 => true
      case _ => false
    }
  }

  def query(where: (String,Any)*): Seq[T] = DB.withConnection { implicit conn =>
    val sql =
      s"""select ${(key.queryExpr :: columns.map(_.queryExpr)).mkString(", ")}
          |from ${table}
          |where ${where.map(c => c._1 + " = {" + c._1 + "}").mkString(" AND ")}
          |limit ${limit}
       """.stripMargin
    val onParams: Seq[NamedParameter] = where.map(p => NamedParameter(p._1, toParameterValue[Any](p._2))).toSeq
    SQL(sql).on(onParams: _*).as(parser.*)
  }

  def querySql(sql: String, params: (String,Any)*): Seq[T] = DB.withConnection { implicit conn =>
    val onParams: Seq[NamedParameter] = params.map(p => NamedParameter(p._1, toParameterValue[Any](p._2))).toSeq
    SQL(sql).on(onParams: _*).as(parser.*)
  }

  def queryOne(where: (String,Any)*): Option[T] = DB.withConnection { implicit conn =>
    val sql =s"""|select ${(key.queryExpr :: columns.map(_.queryExpr)).mkString(", ")}
                 |from ${table}
                 |where ${where.map(c => c._1 + " = {" + c._1 + "}").mkString(" AND ")}
                 |limit 1
     """.stripMargin
    val onParams: Seq[NamedParameter] = where.map(p => NamedParameter(p._1, toParameterValue[Any](p._2))).toSeq
    SQL(sql).on(onParams: _*).as(parser.singleOpt)
  }

  def queryOneSql(sql: String, params: (String,Any)*): Option[T] = DB.withConnection { implicit conn =>
    val onParams: Seq[NamedParameter] = params.map(p => NamedParameter(p._1, toParameterValue[Any](p._2))).toSeq
    SQL(sql).on(onParams: _*).as(parser.singleOpt)
  }
}
