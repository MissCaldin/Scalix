package Scalix5

import scalix.Scalix as s

import scala.jdk.CollectionConverters.*
import scala.language.postfixOps

case class Actor(id: Option[Int], name: String, surname: String, movies: Set[Movie]) {

  def this(name: String, surname: String) = {
    this(
      s.findActorIdv2(name, surname),
      name,
      surname,
      s.findActorIdv2(name, surname).map(id => Actor.findActorMovies5(id)).getOrElse(Set.empty)
    )
  }




}

object Actor {

  def findActorMovies5(actorId: Int): Set[Movie] = {
    val set = s.findActorMovies(actorId)
    set.map { case (id, title) => Movie(id, title) }
  }

  def collaborationBetween(actor1: Actor, actor2: Actor): Set[(String, String)] = {
    val commonMovies = actor1.movies.intersect(actor2.movies)
    commonMovies.map(movie => (movie.title, (new Director(movie.id)).name))
  }

}