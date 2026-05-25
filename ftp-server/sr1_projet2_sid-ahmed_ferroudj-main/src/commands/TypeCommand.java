package commands;
import server.ClientSession;


/**
 * TypeCommand.java
 *
 * Implémentation de la commande FTP "TYPE".
 * Cette commande permet au client de définir le type de transfert
 * (ASCII ou Binaire). Dans cette version simplifiée, on accepte
 * simplement la valeur fournie sans vérification stricte.
 *
 * Auteur : Sid Ahmed Ferroudj
 * Date   : Février 2026
 */
public class TypeCommand implements Command {
    /**
     * Méthode execute()
     * -----------------
     * Traite la commande "TYPE".
     * - Vérifie que l’utilisateur est connecté.
     * - Répond au client avec un message de confirmation.
     *
     * @param session La session du client qui a envoyé la commande.
     * @param args    Argument de la commande (ex. "A" pour ASCII, "I" pour binaire).
     */
    @Override
    public void execute(ClientSession session, String args) {
        if (!session.isLoggedIn()) {
            session.sendMessage("530 Not logged in.");
            return;
        }

        // On dit simplement OK au client, peu importe s'il demande A ou I
        // TODO : Implémenter une vraie gestion des types si nécessaire
        session.sendMessage("200 Type set to " + args);
    }
}