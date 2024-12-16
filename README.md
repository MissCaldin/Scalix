# Scalix

## Informations générales

lien du github : https://github.com/MissCaldin/Scalix.git

version Scala : 3.3.4

dépendances : json4s

## Pour éxécuter notre code

Les parties 3 et 4 peuvent être trouvées dans scalix/Scalix.scala. La partie 5 se trouve dans Scalix5, il faut exécuter Test.scala. Exécuter Test.scala déclanche aussi le test des autres parties. En effet, pour ne recoder pasles fonctions dans la nouvelle structure, les fonctions de scalix.scala ont été réutilisé.

Dans test.scala et au début de Scalix.scala, nous avons appelés les fonctions pour les tester. Il ne s'agit cependant que de courts tests. Nous conseillons de lancer Scalix.scala au moins de fois pour pour tester le système de caches.

## Choix de conception

Pour les premières parties, nous avons simplement choisi d'implémenter un objet étendant App pour pouvoir les tester simplement.
Dans la dernière partie, nous avons créer des objets pour Actor, Director et Movie, ce qui permet de ne récupérer les informations en interrogeant MDB qu'à la création des différents objets. De plus, nous n'avons pas créer de classe Collaboration car il n'a pas besoin d'interroger la base de donnée.
La fonction collaborationBetween a été implémenté dans l'objet compagnon à Actor, car bien que concernant les acteurs, il nous semblait plus logique qu'elle ne soit pas liée à un seule acteur et que l'autre soit un argument

## Question de la partie 5

Avantages:  plus de modularité, les appels à la base de données ne sont fait qu'une fois à la création de l'objet, Réutilisabilité des objets, structure plus compréhensible donc plus facile pou la maintenance

Inconvéniants: Structure plus lourde et pas forcément nécessaire pour des petites manipulations de données
