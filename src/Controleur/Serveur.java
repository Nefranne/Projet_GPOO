package Controleur;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.awt.Color;

public class Serveur {

  private int port;
  private List<User> clients;
  private ServerSocket serveur;

  public static void main(String[] args) throws IOException {
    new Serveur(12345).run();
  }

  public Serveur(int port) {
    this.port = port;
    this.clients = new ArrayList<User>();
  }

  public void run() throws IOException {
    serveur = new ServerSocket(port) {
      protected void finalize() throws IOException {
        this.close();
      }
    };
    System.out.println("Le port 12345 est ouvert.");

    while (true) {
      // accepts a new client
      Socket client = serveur.accept();

      // get nickname of newUser
      @SuppressWarnings("resource")
	String nickname = (new Scanner (client.getInputStream() )).nextLine();
      nickname = nickname.replace(",", ""); //  ',' use for serialisation
      nickname = nickname.replace(" ", "_");
      System.out.println("Nouveau Client: \"" + nickname + "\"\n\t     Hôte:" + client.getInetAddress().getHostAddress());

      // create new User
      User newUser = new User(client, nickname);

      // add newUser message to list
      this.clients.add(newUser);

      // Bienvenu
      newUser.getOutStream().println(
         "<b>Bonjour</b> "  );

      // create a new thread for newUser incoming messages handling
      new Thread(new UserHandler(this, newUser)).start();

      }
  }

  // supprimer de la liste
  public void removeUser(User user){
    this.clients.remove(user);
  }

  // envois des message à tous les utilisateurs
  public void broadcastMessages(String msg, User userSender) {
    for (User client : this.clients) {
      client.getOutStream().println(
          userSender.toString() + "<span>: " + msg+"</span>");
    }
  }

  // envois de la liste des personnes à tous le monde
  public void broadcastAllUsers(){
    for (User client : this.clients) {
      client.getOutStream().println(this.clients);
    }
  }

  // envois d'un message en privé
  public void sendMessageToUser(String msg, User userSender, String user){
    boolean find = false;
    for (User client : this.clients) {
      if (client.getNickname().equals(user) && client != userSender) {
        find = true;
        userSender.getOutStream().println(userSender.toString() + " -> " + client.toString() +": " + msg);
        client.getOutStream().println(
            "(<b>Message privé</b>)" + userSender.toString() + "<span>: " + msg+"</span>");
      }
    }
    if (!find) {
      userSender.getOutStream().println(userSender.toString() + " -> (<b>Il ya personne</b>): " + msg);
    }
  }
}

class UserHandler implements Runnable {

  private Serveur server;
  private User user;

  public UserHandler(Serveur server, User user) {
    this.server = server;
    this.user = user;
    this.server.broadcastAllUsers();
  }

  public void run() {
    String message;

    // quand on a un nouveau message on l'envoie à tout le monde
    Scanner sc = new Scanner(this.user.getInputStream());
    while (sc.hasNextLine()) {
      message = sc.nextLine();

      // smiley
      message = message.replace(":)", "<img src='http://4.bp.blogspot.com/-ZgtYQpXq0Yo/UZEDl_PJLhI/AAAAAAAADnk/2pgkDG-nlGs/s1600/facebook-smiley-face-for-comments.png'>");
      message = message.replace(":(", "<img src='http://2.bp.blogspot.com/-rnfZUujszZI/UZEFYJ269-I/AAAAAAAADnw/BbB-v_QWo1w/s1600/facebook-frown-emoticon.png'>");
      message = message.replace(";)", "<img src='http://1.bp.blogspot.com/-lX5leyrnSb4/Tv5TjIVEKfI/AAAAAAAAAi0/GR6QxObL5kM/s400/wink%2Bemoticon.png'>");
         // Gestion des messages privé
      if (message.charAt(0) == '@'){
        if(message.contains(" ")){
          System.out.println("Message privé : " + message);
          int firstSpace = message.indexOf(" ");
          String userPrivate= message.substring(1, firstSpace);
          server.sendMessageToUser(
              message.substring(
                firstSpace+1, message.length()
                ), user, userPrivate
              );
        }

      // Gestion du changement
      }else if (message.charAt(0) == '#'){
        user.changeColor(message);
        // Changement de la couleur pour les utilisateurs
        this.server.broadcastAllUsers();
      }else{
        // mise à jour de la liste des clients
        server.broadcastMessages(message, user);
      }
    }
    server.removeUser(user);
    this.server.broadcastAllUsers();
    sc.close();
  }
}

class User {
  private static int nbUser = 0;
  private int userId;
  private PrintStream streamOut;
  private InputStream streamIn;
  private String nickname;
  private Socket client;
  private String color;


  public User(Socket client, String name) throws IOException {
    this.streamOut = new PrintStream(client.getOutputStream());
    this.streamIn = client.getInputStream();
    this.client = client;
    this.nickname = name;
    this.userId = nbUser;
    this.color = ColorInt.getColor(this.userId);
    nbUser += 1;
  }

  // changement de la couleur du client
  public void changeColor(String hexColor){
    // on vérifie si la couleur est valide
    Pattern colorPattern = Pattern.compile("#([0-9a-f]{3}|[0-9a-f]{6}|[0-9a-f]{8})");
    Matcher m = colorPattern.matcher(hexColor);
    if (m.matches()){
      Color c = Color.decode(hexColor);
      double luma = 0.2126 * c.getRed() + 0.7152 * c.getGreen() + 0.0722 * c.getBlue(); // per ITU-R BT.709
      if (luma > 160) {
        this.getOutStream().println("<b>Colour trop brillante</b>");
        return;
      }
      this.color = hexColor;
      this.getOutStream().println("<b>Color changement fait</b> " + this.toString());
      return;
    }
    this.getOutStream().println("<b> changement de couleur echoue</b>");
  }


  public PrintStream getOutStream(){
    return this.streamOut;
  }

  public InputStream getInputStream(){
    return this.streamIn;
  }

  public String getNickname(){
    return this.nickname;
  }

  public String toString(){

    return "<u><span style='color:"+ this.color
      +"'>" + this.getNickname() + "</span></u>";

  }
}

class ColorInt {
    public static String[] mColors = {
            "#3079ab",
            "#e15258",
            "#f9845b",
            "#7d669e",
            "#53bbb4",
            "#51b46d",
            "#e0ab18",
            "#f092b0",
            "#e8d174",
            "#e39e54",
            "#d64d4d",
            "#4d7358",
    };

    public static String getColor(int i) {
        return mColors[i % mColors.length];
    }
}
