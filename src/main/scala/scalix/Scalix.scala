package scalix

import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
//commentaire
object Scalix extends App {
  println("Hello!")
  val key = "cb7e721297b8816dfc9f1055621de1cc"
  val url = s"https://api.themoviedb.org/3/movie/550?api_key=cb7e721297b8816dfc9f1055621de1cc"
  val source = scala.io.Source.fromURL(url)
  val contents = source.mkString
  val json = parse(contents)
  println(contents)

  //def findActorId(name: String, surname: String): Option[Int] = ???
}
