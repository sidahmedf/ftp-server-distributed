package commands;
import server.ClientSession;


/**
 * UserCommand.java
 *
 * Implémentation de la commande FTP "USER".
 * Cette commande permet au client de spécifier son nom d’utilisateur.
 *
 * Auteur : Sid Ahmed Ferroudj
 * Date   : Février 2026
 */
public class UserCommand implements Command {
    @Override
    /**
     * Méthode execute()
     * -----------------
     * Traite la commande "USER".
     * - Affiche un log côté serveur.
     * - Met à jour le nom d’utilisateur dans la session.
     * - Envoie une réponse FTP standard indiquant que le mot de passe est requis.
     *
     * @param session La session du client qui a envoyé la commande.
     * @param args    L’argument de la commande (par ex. "sid" dans "USER sid").
     */
    public void execute(ClientSession session, String args) {
        // Log de la commande pour le serveur
        System.out.println("Processing USER command with argument: " + args);
        // Mise à jour du nom d’utilisateur dans la session
        session.setUsername(args);
        // Envoi de la réponse FTP standard pour indiquer que le mot de passe est requis
        session.sendMessage("331 User name okay, need password.");
    }
}