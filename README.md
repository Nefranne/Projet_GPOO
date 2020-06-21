# Projet GLPO  chat en java 
  
  
  
Pour pouvoir compiler le projet il est nécessaire de télécharger la    [JDK version 14.0.1](https://www.oracle.com/java/technologies/javase-jdk14-downloads.html)

Il faut d'abord lancer le serveur.java et ensuite lancer les clients. 
Client.java et ClientGui.java sont la même chose à la différence que ClientGui a une interface graphique. Lancer les deux permets d'avoir deux instances sur la même machine et pouvoir tester le chat.

Au lancement du programme vous devez taper "localhost" si votre serveur et client se trouvent sur la même machine, ou bien taper l'adresse ip <b>locale</b> 

Si le serveur et client se trouvent sur la même machine, n'importe quel port devrait fonctionner ( 12345 par défaut ) sinon vous devez changer le port en fonction de votre configuration réseau. Le port 55555 devrait fonctionner sinon créez une exception dans le pare feu.

La javaDoc se trouve dans /JavaDoc/index.html
<ul>  
<li><b>@pseudo</b> Pour envoyer un Message privé à l'utilisateur "pseudo</li>  
<li>Quelques smileys sont implémentés</li>  
<li><b>flèche du haut</b> pour reprendre le dernier message tapé</li>  
</ul><br/>
