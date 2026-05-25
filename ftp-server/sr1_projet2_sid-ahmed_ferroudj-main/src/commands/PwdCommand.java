package commands;
import server.ClientSession;

/**
 * PwdCommand.java
 *
 * Implémentation de la commande FTP "PWD".
 * Cette commande permet au client de connaître le répertoire
 * courant sur le serveur FTP.
 *
 * Auteur : Sid Ahmed Ferroudj
 * Date   : Février 2026
 */
public class PwdCommand implements Command {
    /**
     * Méthode execute()
     * -----------------
     * Traite la commande "PWD".
     * - Vérifie que l’utilisateur est connecté.
     * - Récupère le chemin relatif du répertoire courant.
     * - Envoie la réponse FTP standard au client.
     *
     * @param session La session du client qui a envoyé la commande.
     * @param args    Arguments éventuels (non utilisés ici).
     */
    @Override
    public void execute(ClientSession session, String args) {

        // Sécurité : Être connecté
        if (!session.isLoggedIn()) {
            session.sendMessage("530 Not logged in.");
            return;
        }

        // Récupérer le chemin relatif du répertoire courant
        String path = session.getRelativePath();

        // Envoyer la réponse FTP standard
        session.sendMessage("257 \"" + path + "\" is current directory.");
    }
}