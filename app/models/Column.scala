package models

/**
  * Database Field
  *
  * @tparam A Field Data Type
  */
class Column[A](val name: String,
                val dbName: String,
                val nk: Boolean,
                val inserts: Boolean = true,
                val updates: Boolean = true,
                val generatedValue: Boolean = true,
                val expr: Option[String] = None) {

  val queryExpr = (if (expr.isDefined) "(" + expr.get + ")" else dbName) + " As " + name
  val updateExpr = dbName + " = {" + name + "}"

  def asType(value: Any): A = value.asInstanceOf[A]
}

object Column {
  def apply[A](name: String,
               dbName: String = null,
               nk: Boolean = false,
               inserts: Boolean = true,
               updates: Boolean = true,
               generatedValue: Boolean = true,
               expr: Option[String] = None): Column[A] = {
    new Column[A](name, Option(dbName).getOrElse(name), nk, inserts, updates, generatedValue, expr)
  }
}
