package scalix

import org.json4s._
import org.json4s.Formats
import org.json4s.native.JsonMethods.*
import org.json4s.native.Serialization

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
  findActorMovies(2524)

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

  def findActorMovies(actorId: Int): Set[(Int, String)] = {
    implicit val formats: Formats = DefaultFormats

    val url = s"https://api.themoviedb.org/3/person/$actorId/movie_credits?api_key=" + key

    val response = Source.fromURL(url)
    val content = response.mkString
    val json = parse(content)
    println(content)
    val movies = (json \ "cast").children.map { movie =>
      val movieId = (movie \ "id").extract[Int]
      val movieTitle = (movie \ "title").extract[String]
      movieId -> movieTitle
    }
    println(movies.toSet)
    movies.toSet
  }




}