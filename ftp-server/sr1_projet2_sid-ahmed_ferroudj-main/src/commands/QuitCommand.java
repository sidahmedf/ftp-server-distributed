package commands;
import server.ClientSession;

/**
 * QuitCommand.java
 *
 * Implémentation de la commande FTP "QUIT".
 * Cette commande permet au client de terminer proprement
 * sa session avec le serveur FTP.
 *
 * Auteur : Sid Ahmed Ferroudj
 * Date   : Février 2026
 */

public class QuitCommand implements Command {
    /**
     * Méthode execute()
     * -----------------
     * Traite la commande "QUIT".
     * - Envoie un message de fermeture au client.
     * - Ferme la connexion associée à la session.
     *
     * @param session La session du client qui a envoyé la commande.
     * @param args    Arguments éventuels (non utilisés ici).
     */
    @Override
    public void execute(ClientSession session, String args) {
        // Envoi d'un message de fermeture au client
        session.sendMessage("221 Goodbye.");
        // Fermeture propre
        session.disconnect();
    }
}