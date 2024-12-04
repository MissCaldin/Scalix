package scalix

import org.json4s.*
import org.json4s.Formats
import org.json4s.native.JsonMethods.*
import org.json4s.native.Serialization

import scala.collection.mutable
import scala.io.Source

import java.io.PrintWriter

object Scalix extends App {
  println("Hello!")

  // 3. Premières requêtes
  val key = "cb7e721297b8816dfc9f1055621de1cc"
  val url = s"https://api.themoviedb.org/3/movie/550?api_key=cb7e721297b8816dfc9f1055621de1cc"
  val source = scala.io.Source.fromURL(url)
  val contents = source.mkString
  val json = parse(contents)
  println(contents)
  findActorId("Tom", "Hardy")
  findActorMovies(2524)
  val anna = FullName("Anna", "Popplewell")
  val skandar = FullName("Skandar", "Keynes")
  println(collaboration(anna, skandar))

  // 4. Amélioration
  val ActorId: mutable.Map[(String, String), Option[Int]] = mutable.Map()
  val ActorMovies: mutable.Map[Int, Set[(Int, String)]] = mutable.Map()
  val MovieDirector: mutable.Map[Int, Option[(Int, String)]] = mutable.Map()
  val collaboration: mutable.Map[(FullName, FullName), Int] = mutable.Map()



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


  case class FullName(firstName: String, lastName: String)

  def collaboration(actor1: FullName, actor2: FullName): Set[(String, String)] = {
    val idOfActor1 = findActorId(actor1.firstName, actor1.lastName)
    val idOfActor2 = findActorId(actor2.firstName, actor2.lastName)
    if (idOfActor1.isEmpty || idOfActor2.isEmpty) {
      Set.empty
    } else {
      val moviesOfActor1 = findActorMovies(idOfActor1.get)
      val moviesOfActor2 = findActorMovies(idOfActor2.get)
      val moviesInCommon = moviesOfActor1.intersect(moviesOfActor2)
      val moviesAndDirector = moviesInCommon.map { case (movieId, movieTitle) =>  (movieTitle, findMovieDirector(movieId))}
      println(moviesAndDirector.toSet)
      moviesAndDirector.toSet
    }
  }

  def readSecondaryCache(filename: String): JValue = {
    val path = "../../data/" + filename + ".json"
    parse(Source.fromFile(path).mkString)
  }

  def writeSecondaryCache(filename: String, text: JValue) = {
    val path = "../../data/" + filename + ".json"
    val out = new PrintWriter(path)
    out.print(compact(render(text)))  
    out.close()
  }

  def findActorIdv2(name: String, surname: String): Option[Int] = {
    implicit val formats: Formats = DefaultFormats

    if (ActorId.contains((name, surname))) {
      ActorId((name, surname))
    }
    else {
      val json = readSecondaryCache("actor$id")
      if ((json \ "actor" \ "FullName").equals(name + " " + surname)) {
        (json \ "actor" \ "FullName").extractOpt[Int]
      }
      else {
        val query = s"$name+$surname".replace(" ", "+")
        val url = s"https://api.themoviedb.org/3/search/person?api_key=" + key + "&query=" + query
        val response = Source.fromURL(url)
        val content = response.mkString
        val json2 = parse(content)
        val actorId = (json2 \ "results").children.headOption.flatMap(result => (result \ "id").extractOpt[Int])
        val actorJson = ("actor" -> ("FullName" -> (name + " " + surname)) ~ ("id" -> id))
        writeSecondaryCache("actor$id", actorJson)
        ActorId += ((name, surname) -> actorId.get)
        actorId
      }
    }





  }

}