package scalix

import org.json4s.*
import org.json4s.Formats
import org.json4s.native.JsonMethods.*
import org.json4s.native.{Json, Serialization}
import play.api.libs.json.*
import org.json4s.*
import org.json4s.JsonDSL.*

import scala.collection.mutable
import scala.io.Source
import java.io.PrintWriter
import javax.swing.JList

object Scalix extends App {
  println("Premières parties")

  // 3. Premières requêtes
  val key = "cb7e721297b8816dfc9f1055621de1cc"

  println("Test de la connewion à MDB")
  val url = s"https://api.themoviedb.org/3/movie/550?api_key=cb7e721297b8816dfc9f1055621de1cc"
  val source = scala.io.Source.fromURL(url)
  val contents = source.mkString
  val json = parse(contents)
  println(contents)

  println("Test de la partie 3 : Premières requêtes")
  println("Id de Tom Hardy =" + findActorId("Tom", "Hardy").toString)
  println("Les films de Tom Hardy :")
  val list = findActorMovies(2524)
  for ((id, title) <- list) {
    println(" - " + id.toString + ": " + title)
  }
  val (id, name) = findMovieDirector(374720).getOrElse((0,""))
  println("Le réalisateur de Dunkirk dans lequel Tom Hardy a joué est " + name + " d'identifiant " + id.toString )
  val anna = FullName("Anna", "Popplewell")
  val skandar = FullName("Skandar", "Keynes")
  println("Films dans lesquels Anna Popplewell et Skandar Keynes ont joué ensemble:")
  val list1 = collaboration(anna, skandar)
  for ((title, director) <- list1) {
    println(" - " + title + ", réalisé par " + director)
  }

  // 4. Amélioration
  println("Tests de la partie 4: Améloration")

  val ActorId: mutable.Map[(String, String), Option[Int]] = mutable.Map()
  val ActorMovies: mutable.Map[Int, Set[(Int, String)]] = mutable.Map()
  val MovieDirector: mutable.Map[Int, Option[(Int, String)]] = mutable.Map()
  val collaboration: mutable.Map[(FullName, FullName), Int] = mutable.Map()
  println("Id de Tom Hardy =" + findActorIdv2("Tom", "Hardy").toString)
  println("Id de Tom Hardy =" + findActorIdv2("Tom", "Hardy").toString)
  println("Les films de Tom Hardy :")
  val list2 = findActorMoviesv2(2524)
  for ((id, title) <- list2) {
    println(" - " + id.toString + ": " + title)
  }
  val (id1, name1) = findMovieDirectorv2(374720).getOrElse((0, ""))
  println("Le réalisateur de Dunkirk dans lequel Tom Hardy a joué est " + name + " d'identifiant " + id.toString)
  println("Films dans lesquels Anna Popplewell et Skandar Keynes ont joué ensemble:")
  val list3 = collaborationv2(anna, skandar)
  for ((title, director) <- list1) {
    println(" - " + title + ", réalisé par " + director)
  }

  def findActorId(name: String, surname: String): Option[Int] = {
    implicit val formats: Formats = DefaultFormats

    val query = s"$name+$surname".replace(" ", "+")
    val url = s"https://api.themoviedb.org/3/search/person?api_key=" + key + "&query=" + query

    val response = Source.fromURL(url)
    val content = response.mkString
    val json = parse(content)
    val actorId = (json \ "results").children.headOption.flatMap(result => (result \ "id").extractOpt[Int])
    actorId
  }


  def findActorMovies(actorId: Int): Set[(Int, String)] = {
    implicit val formats: Formats = DefaultFormats

    val url = s"https://api.themoviedb.org/3/person/$actorId/movie_credits?api_key=" + key

    val response = Source.fromURL(url)
    val content = response.mkString
    val json = parse(content)
    val movies = (json \ "cast").children.map { movie =>
      val movieId = (movie \ "id").extract[Int]
      val movieTitle = (movie \ "title").extract[String]
      movieId -> movieTitle
    }
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
      println("data found in the primary cache")
      ActorId((name, surname))
    }
    else {
      val json = readSecondaryCache("actor$id")
      val data = for {
        JObject(child) <- json
        JField("FullName", JString(fullname)) <- child if fullname == name+ " " + surname
        JField("id", JInt(id)) <- child
      } yield (name, id)

      if (!data.isEmpty) {
        println("Data found in the secondary cache")
        val answer = data.headOption.map { case (_, bigIntValue) => bigIntValue.toInt }
        ActorId += (name, surname) -> answer
        answer
      }
      else {
        println("Data found in the MDB")
        val query = s"$name+$surname".replace(" ", "+")
        val url = s"https://api.themoviedb.org/3/search/person?api_key=" + key + "&query=" + query
        val response = Source.fromURL(url)
        val content = response.mkString
        val json2 = parse(content)
        val actorId = (json2 \ "results").children.headOption.flatMap(result => (result \ "id").extractOpt[Int])
        val actorJson = ("FullName" -> s"$name $surname") ~ ("id" -> actorId)
        writeSecondaryCache("actor$id", json ++ actorJson)
        ActorId += ((name, surname) -> actorId)
        actorId
      }

    }
  }

  def findActorMoviesv2(actorId: Int): Set[(Int, String)] = {
    implicit val formats: Formats = DefaultFormats

    if (ActorMovies.contains(actorId)) {
      println("data found in the primary cache")
      ActorMovies(actorId)
    }
    else {
      val json = readSecondaryCache("actor$movies")
      val data = for {
        JArray(actors) <- json
        JObject(actor) <- actors
        JField("id", JInt(id)) <- actor if id == actorId
        JField("Movies", JArray(movies)) <- actor
      } yield movies
      if (!data.isEmpty) {
        println("data found in the secondary cache")
        val result: Set[(Int, String)] = data.flatten.collect {
          case JObject(fields) =>
            val id = fields.collectFirst { case JField("id", JInt(value)) if value.isValidInt => value.toInt }
            val title = fields.collectFirst { case JField("title", JString(value)) => value }
            (id, title)
        }.collect {
          case (Some(id), Some(title)) => (id, title) // Filtrer les valeurs valides uniquement
        }.toSet

        ActorMovies += actorId -> result
        result
      }
      else {
        println("data found in the MDB")

        val url = s"https://api.themoviedb.org/3/person/$actorId/movie_credits?api_key=" + key

        val response = Source.fromURL(url)
        val content = response.mkString
        val json2 = parse(content)
        val movies = (json2 \ "cast").children.map { movie =>
          val movieId = (movie \ "id").extract[Int]
          val movieTitle = (movie \ "title").extract[String]
          movieId -> movieTitle
        }
        val moviesJson = JArray(movies.map { case (id, title) => JObject("id" -> JInt(id), "title" -> JString(title)) }.toList)
        val actorJson = ("id" -> (actorId)) ~ ("Movies" -> moviesJson)
        writeSecondaryCache("actor$movies", json ++ actorJson)
        ActorMovies += ((actorId) -> movies.toSet)
        movies.toSet
      }
    }
  }

  def findMovieDirectorv2(movieId: Int): Option[(Int, String)] = {
    implicit val formats: Formats = DefaultFormats

    if (MovieDirector.contains(movieId)) {
      println("data found in the primary cache")
      MovieDirector(movieId)
    }
    else {
      val json = readSecondaryCache("movie$director")

      val data = for {
        JArray(movies) <- json
        JObject(movie) <- movies
        JField("idMovie", JInt(idMovie)) <- movie if idMovie == movieId
        JField("idDirector", JInt(idDirector)) <- movie
        JField("name", JString(name)) <- movie
      } yield (idDirector, name)

      if (!data.isEmpty) {
        println("data found in the secondary cache")
        val result: Option[(Int, String)] = data.headOption.map { case (bigInt, str) =>
          (bigInt.toInt, str)
        }
        MovieDirector += movieId -> result
        result
      }

      else {
        println("data found in the MDB")

        val url = s"https://api.themoviedb.org/3/movie/${movieId}/credits?api_key=" + key
        val response = Source.fromURL(url)
        val content = response.mkString
        val json2 = parse(content)

        // Extraire le champ "crew" comme un tableau
        val crew = (json2 \ "crew").extractOpt[List[Map[String, Any]]].getOrElse(List.empty)

        // Trouver le réalisateur dans la liste "crew"
        val director = crew.find(member => member.get("job").contains("Director")).flatMap { member =>
          for {
            idDirector <- member.get("id").collect { case id: BigInt => id.toInt }
            nameDirector <- member.get("name").collect { case name: String => name }
          } yield (idDirector, nameDirector)
        }

        director.foreach { case (idDirector, nameDirector) =>
          val answerJson = ("idMovie" -> movieId) ~  ("idDirector" -> idDirector) ~ ("name" -> nameDirector)
          writeSecondaryCache("movie$director", json ++ answerJson)
          MovieDirector += (movieId -> Some(idDirector, nameDirector))
        }

        director
      }
    }
  }

  def collaborationv2(actor1: FullName, actor2: FullName): Set[(String, String)] = {
    val idOfActor1 = findActorIdv2(actor1.firstName, actor1.lastName)
    val idOfActor2 = findActorIdv2(actor2.firstName, actor2.lastName)
    if (idOfActor1.isEmpty || idOfActor2.isEmpty) {
      Set.empty
    } else {
      val moviesOfActor1 = findActorMoviesv2(idOfActor1.get)
      val moviesOfActor2 = findActorMoviesv2(idOfActor2.get)
      val moviesInCommon = moviesOfActor1.intersect(moviesOfActor2)
      val moviesAndDirector = moviesInCommon.map { case (movieId, movieTitle) => (movieTitle, findMovieDirectorv2(movieId).getOrElse((0,""))._2) }
      moviesAndDirector.toSet
    }
  }









}