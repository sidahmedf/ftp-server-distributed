package server;

/**
 * FTPServer.java
 *
 * Implémentation simple d’un serveur FTP.
 * Le serveur écoute sur un port donné et accepte les connexions
 * des clients. Chaque client est géré dans un thread séparé.
 *
 * Auteur : Sid Ahmed Ferroudj
 * Date   : Février 2026
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * Classe FTPServer
 * ----------------
 * Cette classe encapsule la logique du serveur FTP.
 * Elle ouvre un ServerSocket et crée une nouvelle session
 * pour chaque client qui se connecte.
 */
public class FTPServer {
    private final int port; // Numéro de port sur lequel le serveur écoute

    public FTPServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("FTP Server is listening on port " + port);

            // Boucle principale : accepter les clients en continu
            while (true) {


                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // TODO : Ajouter une étape d’authentification ou de journalisation
                ClientSession session = new ClientSession(clientSocket);

                // Création d’un thread pour gérer la session du client
                new Thread(session).start();
            }

        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}