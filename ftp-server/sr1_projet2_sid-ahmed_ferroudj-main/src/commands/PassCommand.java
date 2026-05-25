package commands;
import server.ClientSession;


/**
 * PassCommand.java
 *
 * Implémentation de la commande FTP "PASS".
 * Cette commande permet au client de fournir son mot de passe
 * après avoir spécifié son nom d’utilisateur avec "USER".
 *
 * Auteur : Sid Ahmed Ferroudj
 * Date : Février 2026 */

public class PassCommand implements Command {

    @Override
    /**
     * Méthode execute() *
     * -----------------
     * Traite la commande "PASS".
     * - Vérifie si l’utilisateur a d’abord envoyé une commande "USER".
     * - Compare le couple (nom d’utilisateur / mot de passe) avec les valeurs attendues. *
     - Met à jour l’état de connexion de la session en cas de succès. *
     - Réinitialise le nom d’utilisateur en cas d’échec pour forcer une nouvelle tentative. *
     * @param session La session du client qui a envoyé la commande.
     * @param args Le mot de passe fourni par le client.
     * */
    public void execute(ClientSession session, String args) {

        // Vérification que le client a d’abord envoyé une commande "USER"
        if (session.getUsername() == null) {
            session.sendMessage("503 Login with USER first.");
            return;
        }

        // Identifiants attendus (ici codés en dur pour simplification)
        String expectedUser = "user";
        String expectedPass = "sr1";

        // vérification du couple (nom d’utilisateur / mot de passe)
        if (session.getUsername().equals(expectedUser) && args.equals(expectedPass)) {
            // Succès
            session.setLoggedIn(true);
            System.out.println("User '" + session.getUsername() + "' successfully logged in.");
            session.sendMessage("230 User logged in, proceed.");
        } else {
            // Erreur : Mauvais mot de passe ou mauvais utilisateur
            System.out.println("Failed login attempt for user: '" + session.getUsername() + "'");

            // On réinitialise le nom d'utilisateu pourl'obliger à tout retaper
            session.setUsername(null);
            session.sendMessage("530 Login incorrect.");
        }
    }

}
