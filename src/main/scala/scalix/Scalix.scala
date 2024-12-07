package scalix

import org.json4s.*
import org.json4s.Formats
import org.json4s.native.JsonMethods.*
import org.json4s.native.{Json, Serialization}
import play.api.libs.json._
import org.json4s._
import org.json4s.JsonDSL._

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
  println(findMovieDirector(703284))
  val anna = FullName("Anna", "Popplewell")
  val skandar = FullName("Skandar", "Keynes")
  println(collaboration(anna, skandar))

  // 4. Amélioration
  val ActorId: mutable.Map[(String, String), Option[Int]] = mutable.Map()
  val ActorMovies: mutable.Map[Int, Set[(Int, String)]] = mutable.Map()
  val MovieDirector: mutable.Map[Int, Option[(Int, String)]] = mutable.Map()
  val collaboration: mutable.Map[(FullName, FullName), Int] = mutable.Map()
  println(findActorIdv2("Tom", "Hardy"))
  println(findActorMoviesv2(2524))

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

  def findMovieDirector(movieId: Int): Option[(Int, String)] = {
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
      val moviesAndDirector = moviesInCommon.map { case (movieId, movieTitle) =>  (movieTitle, findMovieDirector(movieId).get._2)}
      println(moviesAndDirector.toSet)
      moviesAndDirector.toSet
    }
  }

  def readSecondaryCache(filename: String): JValue = {
    val path = "src/data/" + filename + ".json"
    parse(Source.fromFile(path).mkString)
  }

  def writeSecondaryCache(filename: String, text: JValue) = {
    val path = "src/data/" + filename + ".json"
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
        (json \ "actor" \ "id").extractOpt[Int]
      }
      else {
        val query = s"$name+$surname".replace(" ", "+")
        val url = s"https://api.themoviedb.org/3/search/person?api_key=" + key + "&query=" + query
        val response = Source.fromURL(url)
        val content = response.mkString
        val json2 = parse(content)
        val actorId = (json2 \ "results").children.headOption.flatMap(result => (result \ "id").extractOpt[Int])
        val actorJson = ("actor" -> ("FullName" -> (name + " " + surname)) ~ ("id" -> actorId))
        writeSecondaryCache("actor$id", actorJson)
        ActorId += ((name, surname) -> actorId)
        println(actorId)
        println(ActorId)
        actorId
      }
    }
  }

  def findActorMoviesv2(actorId: Int): Set[(Int, String)] = {
    implicit val formats: Formats = DefaultFormats

    if (ActorMovies.contains(actorId)) {
      ActorMovies(actorId)
    }
    else {
      val json = readSecondaryCache("actor$movies")
      if ((json \ "actor" \ "id").equals(actorId)) {
        (json \ "actor" \ "Movies").extractOpt[Set[(Int, String)]].get
      }
      else {

        val url = s"https://api.themoviedb.org/3/person/$actorId/movie_credits?api_key=" + key

        val response = Source.fromURL(url)
        val content = response.mkString
        val json = parse(content)
        val movies = (json \ "cast").children.map { movie =>
          val movieId = (movie \ "id").extract[Int]
          val movieTitle = (movie \ "title").extract[String]
          movieId -> movieTitle
        }
        val moviesJson = JArray(movies.map { case (id, title) => JObject("id" -> JInt(id), "title" -> JString(title)) }.toList)
        val actorJson = ("actor" -> ("id" -> (actorId)) ~ ("Movies" -> moviesJson))
        writeSecondaryCache("actor$movies", actorJson)
        ActorMovies += ((actorId) -> movies.toSet)
        movies.toSet
      }
    }
  }











}