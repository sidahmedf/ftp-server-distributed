package fr.univlille.sr1.treeftp;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Gère la communication réseau de bas niveau avec le serveur FTP.
 * Implémente les commandes FTP standards (USER, PASS, LIST, CWD, PASV) selon la RFC 959.
 *
 * @author Sid-Ahmed Ferroudj
 */
public class ClientFTP {

    // Sockets et flux de communication
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    // Etat de la connexion
    private boolean isConnected = false;


    /**
     * Établit la connexion TCP avec le serveur FTP spécifié.
     *
     * @param server L'adresse du serveur (ex: "localhost" ou "ftp.ubuntu.com").
     * @param port   Le port FTP (généralement 21).
     * @throws IOException Si la connexion échoue.
     */
    public void connect (String server, int port) throws IOException {
        System.out.println("Connecting to " + server + " on port " + port);

        socket = new Socket(server, port);

        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);

        String response = readResponse();
        System.out.println("Server response: " + response);

        if (response.startsWith("220")) {
            isConnected = true;
            System.out.println("Connected to the FTP server.");
        } else {
            throw new IOException("Failed to connect to the FTP server : " + response);
        }
    }


    /**
     * Authentifie l'utilisateur auprès du serveur FTP.
     *
     * @param user Le nom d'utilisateur.
     * @param pass Le mot de passe.
     * @return true si l'authentification réussit, false sinon.
     * @throws IOException En cas d'erreur réseau.
     */
    public boolean login(String user, String pass) throws IOException  {
        System.out.println("Attempting login as " + user);

        // Envoyer USER
        writer.println("USER " + user);
        // Lire la réponse du serveur  (doit etre 331)
        String response = readResponse();
        if (!response.startsWith("331") && !response.startsWith("230")) {
            System.out.println("Login failed at USER step: " + response);
            return false;
        }

        // Envoyer PASS
        writer.println("PASS " + pass);
        // Lire la réponse du serveur (doit etre 230)
        response = readResponse();
        if (response.startsWith("230")) {
            System.out.println("Login successful.");
            return true;
        } else {
            System.out.println("Login failed at PASS step: " + response);
            return false ;
        }

    }

    /**
     * Récupère la liste des fichiers et dossiers du répertoire courant.
     * Utilise le Mode Passif (PASV) pour établir la connexion de données.
     *
     * @return Une liste de chaînes, où chaque chaîne représente une entrée de fichier.
     * @throws IOException Si la commande LIST échoue.
     */
    public List<String> listFiles() throws IOException {

        // Ouvrir une connexion de données en mode passif (Obligatoire pour LIST)
        Socket dataSocket = openDataConnection();

        // Envoyer la commande LIST et lire la réponse (Sockets writer et reader)
        writer.println("LIST");
        String response = readResponse();

        // Vérifier que la réponse est correcte (150 ou 125)
        if (!response.startsWith("150") && !response.startsWith("125")) {
            throw new IOException("Server refused LIST: " + response);
        }

        // Lire les données de la connexion de données (Socket dataSocket !)
        BufferedReader dataReader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

        // créer une liste pour stocker les fichiers
        List<String> files = new ArrayList<>();

        // Boucle les lignes reçues pour remplir la liste (les ajouter une par une)
        String line;
        while((line = dataReader.readLine()) != null) {
            files.add(line);
        }

        // Fermer la connexion de données et le reader de données
        dataSocket.close();
        dataReader.close();

        readResponse();

        return files;
    }

    /**
     * Change le répertoire de travail courant (CWD).
     *
     * @param dir Le nom du dossier dans lequel entrer.
     * @return true si le changement a réussi, false sinon.
     */
    public boolean changeDirectory(String dir) throws IOException {

        //System.out.println("DEBUG: Changing directory to: " + dir);

        // Enovyer la requête CWD
        writer.println("CWD " + dir);

        // lire la réponse
        String response = readResponse();

        // Retourner true si succès (code 250)
        if (response.startsWith("250")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Remonte d'un niveau dans l'arborescence (CDUP).
     *
     * @return true si l'opération a réussi.
     */
    public boolean changeDirectoryUp() throws IOException {

        //System.out.println("DEBUG: Moving up (CDUP)...");

        // Envoyer la requête CDUP
        writer.println("CDUP");

        // lire la réponse
        String response = readResponse();

        // Retourner true si succès (code 250 ou 200)
        if (response.startsWith("250") || response.startsWith("200")) {;
            return true;
        } else {
            return false;
        }
    }


    /**
     * Déconnecte proprement le client du serveur en envoyant la commande QUIT.
     */
    public void disconnect() throws IOException {

        if (isConnected && writer != null) {
            writer.println("QUIT");

            // On attends une réponse 221 Goodbye, mais on ferme quand même
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                // Ignore errors during close
            }
        }
        System.out.println("Disconnected.");
    }

    /**
     * Lit une ligne de réponse depuis le serveur (Connexion de commande).
     */
    private String readResponse() throws IOException {
        String line = reader.readLine();
        if (line == null) return null;

        // Format multi-lignes détecté : "XYZ-Texte"
        if (line.length() >= 4 && line.charAt(3) == '-') {
            String code = line.substring(0, 3);
            StringBuilder multiLineResponse = new StringBuilder(line);
            String nextLine;

            // On continue de lire tant qu'on n'a pas "XYZ " (avec un espace)
            while ((nextLine = reader.readLine()) != null) {
                multiLineResponse.append("\n").append(nextLine);
                // Si la ligne commence par "XYZ ", c'est la fin du message
                if (nextLine.startsWith(code + " ")) {
                    return nextLine; // On renvoie la dernière ligne pour le check des codes (ex: 230)
                }
            }
        }
        return line;
    }

    /**
     * Gère la commande PASV pour négocier un port de données.
     *
     * @return Un socket connecté au port de données du serveur.
     * @throws IOException Si le parsing de la réponse PASV échoue.
     */
    private Socket openDataConnection() throws IOException {

        // Envoyer la commande PASV et lire la réponse
        writer.println("PASV");
        String response = readResponse();

        // Analyser la réponse pour obtenir l'adresse IP et le port
        if (!response.startsWith("227")) {
            throw new IOException("Error entering passive mod: " + response);
        }

        int openParen = response.indexOf('(');
        int closeParen = response.indexOf(')');

        if (openParen == -1 || closeParen == -1) {
            throw new IOException("Error when parse PASV response: " + response);
        }

        String content  = response.substring(openParen+1, closeParen);

        // Ex: 192,168,1,2,7,138
        String[] parts = content.split(",");

        String ip = parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];

        String p1_str = parts[4];
        String p2_str = parts[5];

        int p1 = Integer.parseInt(p1_str);
        int p2 = Integer.parseInt(p2_str);

        // Calcul du port
        int dataPort = (p1* 256) + p2;

        // Etablir la connexion de données et la retourner
        Socket dataSocket = new Socket(ip, dataPort);
        return dataSocket;


    }

    /**
     * Déplace la session FTP à la racine du serveur (/).
     *
     * @return true si succès.
     */
    public boolean changeDirectoryToRoot() throws IOException {
        writer.println("CWD /"); // Commande pour aller à la racine
        String response = readResponse();
        return response.startsWith("250");
    }


    /**
     * Navigue vers un chemin spécifique en changeant de dossier séquentiellement.
     * Utilisé principalement pour l'algorithme BFS.
     *
     * @param path Le chemin relatif à atteindre (ex: "dossier1/sous_dossier").
     * @return true si tout le chemin a été parcouru avec succès.
     */
    public boolean changeDirectoryPath(String path) throws IOException {
        // Si le chemin est vide, on reste à la racine
        if (path == null || path.isEmpty()) return true;

        // On découpe le chemin par "/"
        String[] folders = path.split("/");

        for (String folder : folders) {
            if (folder.isEmpty()) continue;
            // On entre dossier par dossier
            boolean success = changeDirectory(folder);
            if (!success) return false; // Si un dossier échoue, on arrête
        }
        return true;
    }




}
