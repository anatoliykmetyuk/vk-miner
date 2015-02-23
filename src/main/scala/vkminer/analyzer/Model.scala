package vkminer.analyzer

case class User(
  id       : Long,
  firstName: String,
  lastName : String,
  groups   : Seq[Group]
)

case class Group(
  id         : Long,
  name       : String,
  screenName : String,
  groupType  : String,
  deactivated: Option[String]
)