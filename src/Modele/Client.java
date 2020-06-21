package Modele;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

  private String hote;
  private int port;

  public static void main(String[] args) throws UnknownHostException, IOException {
    new Client("127.0.0.1", 12345).start();
  }

  public Client(String hote, int port) {
    this.hote = hote;
    this.port = port;
  }

  public void start() throws UnknownHostException, IOException {

    Socket client = new Socket(hote, port);
    System.out.println("Le client s'est connecté avec succés au serveur");

    PrintStream sortie = new PrintStream(client.getOutputStream());

    // ask for a nickname
    Scanner sc = new Scanner(System.in);
    System.out.print("Entrer un pseudo: ");
    String nickname = sc.nextLine();

    // send nickname to server
    sortie.println(nickname);

    // create a new thread for server messages handling
    new Thread(new MessagesHandler(client.getInputStream())).start();

    // read messages from keyboard and send to server
    System.out.println("Message: \n");

    // while new messages
    while (sc.hasNextLine()) {
      sortie.println(sc.nextLine());
    }


    sortie.close();
    sc.close();
    client.close();
  }
}

class MessagesHandler implements Runnable {

  private InputStream server;

  public MessagesHandler(InputStream server) {
    this.server = server;
  }

  public void run() {

    Scanner s = new Scanner(server);
    String tmp;
    while (s.hasNextLine()) {
      tmp = s.nextLine();
      if (tmp.charAt(0) == '[') {
        tmp = tmp.substring(1, tmp.length()-1);
        System.out.println("\nListe des utilisateurs: " +
            new ArrayList<String>(Arrays.asList(tmp.split(","))) + "\n"
            );
      }else{
        try {
          System.out.println("\n" + getTagValue(tmp));
        } catch(Exception ignore){}
      }
    }
    s.close();
  }

  public static String getTagValue(String xml){
    return  xml.split(">")[2].split("<")[0] + xml.split("<span>")[1].split("</span>")[0];
  }

}
