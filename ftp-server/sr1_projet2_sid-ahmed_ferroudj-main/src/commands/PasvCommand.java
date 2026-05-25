package commands;
import server.ClientSession;


/**
 * PasvCommand.java
 *
 * Implémentation de la commande FTP "PASV".
 * Cette commande place le serveur en mode passif et ouvre
 * une connexion de données sur un port aléatoire.
 *
 * Auteur : Sid Ahmed Ferroudj
 * Date   : Février 2026
 */

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Classe PasvCommand
 * ------------------
 * Gère la commande "PASV" envoyée par le client.
 * - Vérifie que l’utilisateur est connecté.
 * - Ferme toute ancienne connexion de données.
 * - Crée un nouveau ServerSocket sur un port libre.
 * - Envoie au client la réponse 227 avec l’IP et le port.
 */
public class PasvCommand implements Command {
    /**
     * Méthode execute()
     * -----------------
     * Traite la commande "PASV".
     *
     * @param session La session du client qui a envoyé la commande.
     * @param args    Arguments éventuels (non utilisés ici).
     */
    @Override
    public void execute(ClientSession session, String args) {

        if (!session.isLoggedIn()) {
            session.sendMessage("530 Not logged in.");
            return;
        }

        try {
            // Fermer l'ancienne connexion de données si elle existait déjà
            if (session.getDataConnection() != null && !session.getDataConnection().isClosed()) {
                session.getDataConnection().close();
            }

            // Créer un nouveau ServerSocket sur le port 0
            ServerSocket dataServer = new ServerSocket(0);
            session.setDataConnection(dataServer);

            // Récupérer le port réel qui a été généré
            int port = dataServer.getLocalPort();

            // Calculer p1 et p2 selon la norme FTP (Port = p1 * 256 + p2)
            int p1 = port / 256;
            int p2 = port % 256;

            // Pour mes tests en local, on force l'IP "127.0.0.1" (avec des virgules !)
            String ip = "127,0,0,1";

            // Envoyer la réponse 227
            session.sendMessage("227 Entering Passive Mode (" + ip + "," + p1 + "," + p2 + ").");

            System.out.println("Mode passif prêt sur le port " + port);

        } catch (IOException e) {
            session.sendMessage("425 Can't open data connection.");
        }
    }
}