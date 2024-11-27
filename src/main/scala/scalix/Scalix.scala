package scalix

import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization

object Scalix extends App {
  println("Hello!")
  val key = "On ne l'a pas encore"
  val url = s"Bonne question"
  val source = scala.io.Source.fromURL(url)
  val contents = source.mkString
  val json = parse(contents)
  println(contents)
}
