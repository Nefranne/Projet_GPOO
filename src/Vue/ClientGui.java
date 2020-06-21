package Vue;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.*;

import java.util.ArrayList;
import java.util.Arrays;


public class ClientGui extends Thread{

    final JTextPane jtextDiscussionFil = new JTextPane();
    final JTextPane jtextListeUtilisateur = new JTextPane();
    final JTextField jtextEntreeChat = new JTextField();
    private String vieuxMessage = "";
    private Thread lecture;
    private String serverName;
    private int PORT;
    private String name;
    BufferedReader entree;
    PrintWriter sortie;
    Socket serveur;

    public ClientGui() {
        System.setProperty( "file.encoding", "UTF-8" );
        this.serverName = "localhost";
        this.PORT = 12345;
        this.name = "Pseudo";

        String fontfamily = "Arial, sans-serif";
        Font font = new Font(fontfamily, Font.PLAIN, 15);

        final JFrame jfr = new JFrame("Chat");
        jfr.getContentPane().setLayout(null);
        jfr.setSize(700, 500);
        jfr.setResizable(false);
        jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Module du fil de discussion
        jtextDiscussionFil.setBounds(25, 25, 490, 320);
        jtextDiscussionFil.setFont(font);
        jtextDiscussionFil.setMargin(new Insets(6, 6, 6, 6));
        jtextDiscussionFil.setEditable(false);
        JScrollPane jtextDiscussionFilSP = new JScrollPane(jtextDiscussionFil);
        jtextDiscussionFilSP.setBounds(25, 25, 490, 320);

        jtextDiscussionFil.setContentType("text/html");
        jtextDiscussionFil.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

        // Module de la liste des utilisateurs
        jtextListeUtilisateur.setBounds(520, 25, 156, 320);
        jtextListeUtilisateur.setEditable(true);
        jtextListeUtilisateur.setFont(font);
        jtextListeUtilisateur.setMargin(new Insets(6, 6, 6, 6));
        jtextListeUtilisateur.setEditable(false);
        JScrollPane jsplistuser = new JScrollPane(jtextListeUtilisateur);
        jsplistuser.setBounds(520, 25, 156, 320);

        jtextListeUtilisateur.setContentType("text/html");
        jtextListeUtilisateur.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

        // champ de message
        jtextEntreeChat.setBounds(0, 350, 400, 50);
        jtextEntreeChat.setFont(font);
        jtextEntreeChat.setMargin(new Insets(6, 6, 6, 6));
        final JScrollPane jtextEntreeChatSP = new JScrollPane(jtextEntreeChat);
        jtextEntreeChatSP.setBounds(25, 350, 650, 50);

        // bouton envoyer
        final JButton jsbtn = new JButton("Envoyer");
        jsbtn.setFont(font);
        jsbtn.setBounds(575, 410, 100, 35);

        // bouton déconnexion
        final JButton jsbtndeco = new JButton("Déconnexion");
        jsbtndeco.setFont(font);
        jsbtndeco.setBounds(25, 410, 130, 35);

        jtextEntreeChat.addKeyListener(new KeyAdapter() {
            // envois message
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }

                // prendre le dernier message taper
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    String currentMessage = jtextEntreeChat.getText().trim();
                    jtextEntreeChat.setText(vieuxMessage);
                    vieuxMessage = currentMessage;
                }

                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    String currentMessage = jtextEntreeChat.getText().trim();
                    jtextEntreeChat.setText(vieuxMessage);
                    vieuxMessage = currentMessage;
                }
            }
        });

        // Clic du bouton envoi
        jsbtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                sendMessage();
            }
        });

        // Connection vue
        final JTextField jtfName = new JTextField(this.name);
        final JTextField jtfport = new JTextField(Integer.toString(this.PORT));
        final JTextField jtfAddr = new JTextField(this.serverName);
        final JButton jcbtn = new JButton("Connectée");

        // regarder si le contenu est vide
        jtfName.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));
        jtfport.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));
        jtfAddr.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));

        // position des Modules
        jcbtn.setFont(font);
        jtfAddr.setBounds(25, 380, 135, 40);
        jtfName.setBounds(375, 380, 135, 40);
        jtfport.setBounds(200, 380, 135, 40);
        jcbtn.setBounds(575, 380, 100, 40);

        // couleur par défaut des Modules fil de discussion et liste des utilisateurs
        jtextDiscussionFil.setBackground(Color.LIGHT_GRAY);
        jtextListeUtilisateur.setBackground(Color.LIGHT_GRAY);

        // ajout des éléments
        jfr.add(jcbtn);
        jfr.add(jtextDiscussionFilSP);
        jfr.add(jsplistuser);
        jfr.add(jtfName);
        jfr.add(jtfport);
        jfr.add(jtfAddr);
        jfr.setVisible(true);



        // On se connecte
        jcbtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    name = jtfName.getText();
                    String port = jtfport.getText();
                    serverName = jtfAddr.getText();
                    PORT = Integer.parseInt(port);

                    EnvoyerAuPane(jtextDiscussionFil, "<span>Connexion à " + serverName + " sur le port " + PORT + "...</span>");
                    serveur = new Socket(serverName, PORT);

                    EnvoyerAuPane(jtextDiscussionFil, "<span>Connecté a " +
                            serveur.getRemoteSocketAddress()+"</span>");

                    entree = new BufferedReader(new InputStreamReader(serveur.getInputStream()));
                    sortie = new PrintWriter(serveur.getOutputStream(), true);

                    // send nickname to server
                    sortie.println(name);

                    // create new Read Thread
                    lecture = new lecture();
                    lecture.start();
                    jfr.remove(jtfName);
                    jfr.remove(jtfport);
                    jfr.remove(jtfAddr);
                    jfr.remove(jcbtn);
                    jfr.add(jsbtn);
                    jfr.add(jtextEntreeChatSP);
                    jfr.add(jsbtndeco);
                    jfr.revalidate();
                    jfr.repaint();
                    jtextDiscussionFil.setBackground(Color.WHITE);
                    jtextListeUtilisateur.setBackground(Color.WHITE);
                } catch (Exception ex) {
                    EnvoyerAuPane(jtextDiscussionFil, "<span>Impossible de se connecter au serveur</span>");
                    JOptionPane.showMessageDialog(jfr, ex.getMessage());
                }
            }

        });

        // on deco
        jsbtndeco.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent ae) {
                jfr.add(jtfName);
                jfr.add(jtfport);
                jfr.add(jtfAddr);
                jfr.add(jcbtn);
                jfr.remove(jsbtn);
                jfr.remove(jtextEntreeChatSP);
                jfr.remove(jsbtndeco);
                jfr.revalidate();
                jfr.repaint();
                lecture.interrupt();
                jtextListeUtilisateur.setText(null);
                jtextDiscussionFil.setBackground(Color.LIGHT_GRAY);
                jtextListeUtilisateur.setBackground(Color.LIGHT_GRAY);
                EnvoyerAuPane(jtextDiscussionFil, "<span>Connection terminée.</span>");
                sortie.close();
            }
        });

    }

    // les zone sont elles vide
    public class TextListener implements DocumentListener{
        JTextField jtf1;
        JTextField jtf2;
        JTextField jtf3;
        JButton jcbtn;

        public TextListener(JTextField jtf1, JTextField jtf2, JTextField jtf3, JButton jcbtn){
            this.jtf1 = jtf1;
            this.jtf2 = jtf2;
            this.jtf3 = jtf3;
            this.jcbtn = jcbtn;
        }

        public void changedUpdate(DocumentEvent e) {}

        public void removeUpdate(DocumentEvent e) {
            if(jtf1.getText().trim().equals("") ||
                    jtf2.getText().trim().equals("") ||
                    jtf3.getText().trim().equals("")
            ){
                jcbtn.setEnabled(false);
            }else{
                jcbtn.setEnabled(true);
            }
        }
        public void insertUpdate(DocumentEvent e) {
            if(jtf1.getText().trim().equals("") ||
                    jtf2.getText().trim().equals("") ||
                    jtf3.getText().trim().equals("")
            ){
                jcbtn.setEnabled(false);
            }else{
                jcbtn.setEnabled(true);
            }
        }

    }

    // envoi des messages
    public void sendMessage() {
        try {
            String message = jtextEntreeChat.getText().trim();
            if (message.equals("")) {
                return;
            }
            this.vieuxMessage = message;
            sortie.println(message);
            jtextEntreeChat.requestFocus();
            jtextEntreeChat.setText(null);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            System.exit(0);
        }
    }

    public static void main(String[] args) throws Exception {
        ClientGui client = new ClientGui();
    }

    // on lis les messages qui viennent
    class lecture extends Thread {
        public void run() {
            String message;
            while(!Thread.currentThread().isInterrupted()){
                try {
                    message = entree.readLine();
                    if(message != null){
                        if (message.charAt(0) == '[') {
                            message = message.substring(1, message.length()-1);
                            ArrayList<String> ListUser = new ArrayList<String>(
                                    Arrays.asList(message.split(", "))
                            );
                            jtextListeUtilisateur.setText(null);
                            for (String user : ListUser) {
                                EnvoyerAuPane(jtextListeUtilisateur, "@" + user);
                            }
                        }else{
                            EnvoyerAuPane(jtextDiscussionFil, message);
                        }
                    }
                }
                catch (IOException ex) {
                    System.err.println("Échec de l'analyse du message entrant");
                }
            }
        }
    }

    // envois du html au panneau
    private void EnvoyerAuPane(JTextPane tp, String msg){
        HTMLDocument doc = (HTMLDocument)tp.getDocument();
        HTMLEditorKit editorKit = (HTMLEditorKit)tp.getEditorKit();
        try {
            editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
            tp.setCaretPosition(doc.getLength());
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
