package Scalix5

import scalix.Scalix as s

case class Director(id: Int, name: String) {

  def this(movieid: Int) = {
    this(
      s.findMovieDirector(movieid).map(_._1).getOrElse(-1),
      s.findMovieDirector(movieid).map(_._2).getOrElse("")
    )
  }

}
