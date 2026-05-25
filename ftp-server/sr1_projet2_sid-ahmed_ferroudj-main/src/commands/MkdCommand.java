package commands;
import server.ClientSession;


/**
 * MkdCommand.java
 *
 * Implémentation de la commande FTP "MKD".
 * Cette commande permet au client de créer un nouveau répertoire
 * sur le serveur FTP.
 *
 * Auteur : Sid Ahmed Ferroudj
 * Date   : Février 2026
 */

import java.io.File;
import java.io.IOException;

/**
 * Classe MkdCommand
 * ----------------
 * Gère la commande "MKD".
 * - Vérifie que l’utilisateur est connecté.
 * - Résout le chemin du répertoire à créer (absolu ou relatif).
 * - Applique une restriction de type chroot pour la sécurité.
 * - Crée le répertoire demandé si possible.
 */
public class MkdCommand implements Command {

    /**
     * Méthode execute()
     * -----------------
     * Traite la commande "MKD".
     *
     * @param session La session du client qui a envoyé la commande.
     * @param args    Nom ou chemin du répertoire à créer.
     */
    @Override
    public void execute(ClientSession session, String args) {

        // Sécurité : Vérifier que le client est connecté
        if (!session.isLoggedIn()) {
            session.sendMessage("530 Not logged in.");
            return;
        }

        // Vérifier la présence du paramètre
        if (args == null || args.isEmpty()) {
            session.sendMessage("501 Syntax error in parameters.");
            return;
        }

        try {
            // Résolution du chemin (absolu ou relatif)
            File newDir = args.startsWith("/")
                    ? new File(session.getRootDirectory(), args.substring(1))
                    : new File(session.getCurrentDirectory(), args);

            newDir = newDir.getCanonicalFile();

            // Sécurité (Chroot)
            if (!newDir.getPath().startsWith(session.getRootDirectory().getCanonicalPath())) {
                session.sendMessage("550 Access denied.");
                return;
            }

            // Création du dossier
            if (newDir.mkdir()) {
                session.sendMessage("257 \"" + args + "\" directory created.");
            } else {
                session.sendMessage("550 Failed to create directory. It may already exist.");
            }

        } catch (IOException e) {
            session.sendMessage("550 Error handling path.");
        }
    }
}