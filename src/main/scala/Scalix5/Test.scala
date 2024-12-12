package Scalix5

object Test extends App {

  println("Test de la partie 5: Exercice d'architecture")


  val anna = new Actor("Anna", "Popplewell")
  val skandar = new Actor("Skandar", "Keynes")
  println(anna)
  println(s"L'actrice : ${anna.name} ${anna.surname} a pour ID : ${anna.id}. Elle a joué dans:")
  for(movie <- anna.movies) {
    println(" - " + movie.id.toString + ": " + movie.title)
  }
  val movie = anna.movies.head
  val director = new Director(movie.id)
  println("Le directeur du film " + movie.title + " est " + director.name + "dont l'id est " + director.id.toString)

  println(s"${anna.name} ${anna.surname} et ${skandar.name} ${skandar.surname} ont collaboré sur:")
  val movies = Actor.collaborationBetween(anna, skandar)
  for (m <- movies) {
    println(s"  - ${m._1} de ${m._2}")
  }

}
