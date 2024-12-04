package scalix

import org.json4s.*
import org.json4s.Formats
import org.json4s.native.JsonMethods.*
import org.json4s.native.{Json, Serialization}
import play.api.libs.json._

import scala.io.Source

object Scalix extends App {
  println("Hello!")
  val key = "cb7e721297b8816dfc9f1055621de1cc"
  val url = s"https://api.themoviedb.org/3/movie/550?api_key=cb7e721297b8816dfc9f1055621de1cc"
  val source = scala.io.Source.fromURL(url)
  val contents = source.mkString
  val json = parse(contents)
  println(contents)
  findActorId("Tom", "Hardy")
  println(findMovieDirector(703284))

  def findActorId(name: String, surname: String): Option[Int] = {
    implicit val formats: Formats = DefaultFormats

    val query = s"$name+$surname".replace(" ", "+")
    val url = s"https://api.themoviedb.org/3/search/person?api_key=" + key + "&query=" + query

    val response = Source.fromURL(url)
    val content = response.mkString
    val json = parse(content)
    val actorId = (json \ "results").children.headOption.flatMap(result => (result \ "id").extractOpt[Int])
    println(content)
    println(actorId)
    actorId
  }

  def findMovieDirector(movieId: Int): Option[(Int, String)]= {
    implicit val formats: Formats = DefaultFormats

    val url = s"https://api.themoviedb.org/3/movie/${movieId}/credits?api_key=" + key
    val response = Source.fromURL(url)
    val content = response.mkString
    val json = parse(content)

    // Extraire le champ "crew" comme un tableau
    val crew = (json \ "crew").extractOpt[List[Map[String, Any]]].getOrElse(List.empty)

    // Trouver le réalisateur dans la liste "crew"
    val director = crew.find(member => member.get("job").contains("Director"))
    println(director)

    // Extraire le nom du réalisateur (s'il existe)
    director.flatMap { member =>
      for {
        id <- member.get("id").collect { case id: BigInt => id.toInt } // Convertir BigInt en Int
        name <- member.get("name").collect { case name: String => name }
      } yield (id, name)
    }

  }

}