package Scalix5

object Test extends App {

  println("Test de la partie 5")

  println("Test de actor")
  val anna = new Actor("Anna", "Popplewell")
  val skandar = new Actor("Skandar", "Keynes")
  println(anna)
  println(s"Acteur : ${anna.name} ${anna.surname}, ID : ${anna.id}, Films : ${anna.movies}")

  println("Test de Director")
  println("Nous cherchons le directeur du film: ")
  val movie = anna.movies.head
  println(movie)
  println("qui se trouve être:")
  val director = new Director(movie.id)
  println(director)

  println("Test de Collaboration")
  println(s"${anna.name} ${anna.surname} et ${skandar.name} ${skandar.surname} ont collaboré sur:")
  val movies = Actor.collaborationBetween(anna, skandar)
  for (m <- movies) {
    println(s"  - ${m._1} de ${m._2}")
  }

}
