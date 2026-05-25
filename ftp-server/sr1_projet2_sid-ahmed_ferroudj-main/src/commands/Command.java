package commands;
import server.ClientSession;

/**
 * Command.java
 *
 * Interface représentant une commande FTP.
 * Chaque commande doit être capable de s’exécuter dans le contexte
 * d’une session client donnée.
 *
 * Auteur : Sid Ahmed Ferroudj
 * Date   : Février 2026
 */
public interface Command {
    /**
     * Méthode execute()
     * -----------------
     * Exécute la commande FTP.
     *
     * @param session La session du client qui a envoyé la commande.
     * @param args    L’argument de la commande (par ex. "sid" dans "USER sid").
     */
    void execute(ClientSession session, String args);
}