package commands;
import server.ClientSession;


/**
 * RntoCommand.java
 *
 * Implémentation de la commande FTP "RNTO".
 * Cette commande permet de finaliser une opération de renommage
 * après une commande "RNFR" réussie.
 *
 * Auteur : Sid Ahmed Ferroudj
 * Date   : Février 2026
 */

import java.io.File;
import java.io.IOException;

/**
 * Classe RntoCommand
 * -----------------
 * Gère la commande "RNTO".
 * - Vérifie que l’utilisateur est connecté.
 * - Vérifie qu’une commande RNFR a été exécutée juste avant.
 * - Applique une restriction de type chroot pour la sécurité.
 * - Renomme le fichier ou le dossier cible.
 */

public class RntoCommand implements Command {
    /**
     * Méthode execute()
     * -----------------
     * Traite la commande "RNTO".
     *
     * @param session La session du client qui a envoyé la commande.
     * @param args    Nouveau nom ou chemin de destination.
     */
    @Override
    public void execute(ClientSession session, String args) {
        if (!session.isLoggedIn()) { session.sendMessage("530 Not logged in."); return; }
        if (args == null || args.isEmpty()) { session.sendMessage("501 Syntax error."); return; }

        // Vérifier que le client a bien fait RNFR juste avant !
        File source = session.getRenameTarget();
        if (source == null) {
            session.sendMessage("503 Bad sequence of commands. Send RNFR first.");
            return;
        }

        try {
            File destination = args.startsWith("/") ? new File(session.getRootDirectory(), args.substring(1)) : new File(session.getCurrentDirectory(), args);
            destination = destination.getCanonicalFile();

            // Sécurité (Chroot)
            if (!destination.getPath().startsWith(session.getRootDirectory().getCanonicalPath())) {
                session.sendMessage("550 Access denied.");
                session.setRenameTarget(null); // On efface la mémoire en cas d'erreur
                return;
            }

            // Renommage (Fonctionne aussi bien pour les fichiers que pour les dossiers)
            if (source.renameTo(destination)) {
                session.sendMessage("250 File renamed successfully.");
            } else {
                session.sendMessage("550 Rename failed.");
            }

        } catch (IOException e) {
            session.sendMessage("550 Error handling path.");
        } finally {
            // Finally : Quoi qu'il arrive, l'opération est finie, on oublie l'ancienne cible
            session.setRenameTarget(null);
        }
    }
}