package commands;
import server.ClientSession;


/**
 * EpsvCommand.java
 *
 * Implémentation de la commande FTP "EPSV".
 * Cette commande active le mode passif étendu, principalement utilisé
 * pour IPv6, mais également compatible avec IPv4.
 *
 * Auteur : Sid Ahmed Ferroudj
 * Date   : Février 2026
 */

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Classe EpsvCommand
 * -----------------
 * Gère la commande "EPSV".
 * - Vérifie que l’utilisateur est connecté.
 * - Ferme toute ancienne connexion de données.
 * - Ouvre une nouvelle connexion de données sur un port aléatoire.
 * - Envoie la réponse FTP 229 au client.
 */
public class EpsvCommand implements Command {

    /**
     * Méthode execute()
     * -----------------
     * Traite la commande "EPSV".
     *
     * @param session La session du client qui a envoyé la commande.
     * @param args    Arguments éventuels (non utilisés ici).
     */
    @Override
    public void execute(ClientSession session, String args) {

        // Sécurité : Vérifier que le client est connecté
        if (!session.isLoggedIn()) {
            session.sendMessage("530 Not logged in.");
            return;
        }

        try {
            // Fermer l'ancienne connexion de données si elle existait déjà
            if (session.getDataConnection() != null && !session.getDataConnection().isClosed()) {
                session.getDataConnection().close();
            }

            // Créer un nouveau ServerSocket sur le port 0 (port aléatoire)
            ServerSocket dataServer = new ServerSocket(0);
            session.setDataConnection(dataServer);
            int port = dataServer.getLocalPort();

            // Envoyer la réponse 229 au client (format : |||port|)
            session.sendMessage("229 Entering Extended Passive Mode (|||" + port + "|).");


            System.out.println("Mode EPSV (IPv6) prêt sur le port " + port);

        } catch (IOException e) {
            session.sendMessage("425 Can't open data connection.");
        }
    }
}