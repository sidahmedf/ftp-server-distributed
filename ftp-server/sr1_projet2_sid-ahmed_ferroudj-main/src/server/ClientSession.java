package server;
import commands.CommandRouter;
/**
 * ClientSession.java
 *
 * Représente une session FTP côté serveur pour un client donné.
 * Chaque session est exécutée dans un thread séparé et gère
 * la communication avec le client via des commandes FTP.
 *
 * Auteur : Sid Ahmed Ferroudj
 * Date   : Février 2026
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.File;
import java.nio.file.Path;
import java.net.ServerSocket;


/**
 * Classe ClientSession
 * --------------------
 * Gère la connexion d’un client au serveur FTP.
 * - Maintient l’état de la session (utilisateur, répertoire courant, etc.).
 * - Reçoit et traite les commandes envoyées par le client.
 * - Utilise un CommandRouter pour déléguer l’exécution des commandes.
 */
public class ClientSession implements Runnable {
    private final Socket socket; // Socket de communication avec le client
    private final CommandRouter router;
    private PrintWriter writer;
    private String username = null;
    private boolean isLoggedIn =false;

    private final File rootDirectory = new File(System.getProperty("user.dir"), "ftp_root"); // Répertoire racine pour ce client
    private File currentDirectory = rootDirectory; // Répertoire courant, initialisé à la racine

    private ServerSocket dataConnection; // ServerSocket pour la connexion de données en mode passif

    private File renameTarget = null; // Fichier temporaire pour stocker la cible du RNFR en attente d'un RNTO

    public ClientSession(Socket socket) {
        this.socket = socket;
        this.router = new CommandRouter();

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {

        }
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(File newDirectory) {
        this.currentDirectory = newDirectory;
    }


    /**
     * * Retourne le chemin relatif du répertoire courant par rapport à la racine FTP.
     *
     * * * @return chemin relatif sous forme de chaîne */

    public String getRelativePath() {
        Path rootPath = rootDirectory.toPath();
        Path currentPath = currentDirectory.toPath();

        // Calcule le chemin relatif
        String relative = rootPath.relativize(currentPath).toString();

        // Pour les systèmes Windows, remplacer les backslashes par des slashes (sinon ca crashe sur les chemins)
        relative = relative.replace("\\", "/");

        // Si c'est vide, c'est la racine "/"
        if (relative.isEmpty()) return "/";

        return "/" + relative;
    }

    public ServerSocket getDataConnection() {
        return dataConnection;
    }

    public void setDataConnection(ServerSocket dataConnection) {
        this.dataConnection = dataConnection;
    }

    public File getRenameTarget() {
        return renameTarget;
    }

    public void setRenameTarget(File renameTarget) {
        this.renameTarget = renameTarget;
    }

    /**
     * Méthode run() *
     *  ------------- *
     *  Point d’entrée du thread de la session client.
     * - Initialise les flux de communication.
     * - Envoie un message de bienvenue.
     * - Boucle pour lire et traiter les commandes du client.
     */
    @Override
    public void run() {
        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writer = new PrintWriter(socket.getOutputStream(), true);

            // Envoyer un message de bienvenue au client
            sendMessage("220 Welcome to SR1 FTP Server");

            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {
                System.out.println("Client " + socket.getInetAddress() + " " + clientMessage);

                // déléguer le traitement de la commande au CommandRouter
                router.route(this, clientMessage);
            }

        } catch (IOException e) {
            System.err.println("Client session error: " + e.getMessage());
        } finally {
            System.out.println("Client disconnected.");
            try {
                socket.close();
            } catch (IOException e) {

            }
        }
    }
}