package commands;
import server.ClientSession;

/**
 * DeleCommand.java
 *
 * Implémentation de la commande FTP "DELE".
 * Cette commande permet au client de supprimer un fichier
 * sur le serveur FTP.
 *
 * Auteur : Sid Ahmed Ferroudj
 * Date   : Février 2026
 */

import java.io.File;
import java.io.IOException;

/**
 * Classe DeleCommand
 * -----------------
 * Gère la commande "DELE".
 * - Vérifie que l’utilisateur est connecté.
 * - Applique une restriction de type chroot pour la sécurité.
 * - Supprime uniquement des fichiers (pas des répertoires).
 */

public class DeleCommand implements Command {

    /**
     * Méthode execute()
     * -----------------
     * Traite la commande "DELE".
     *
     * @param session La session du client qui a envoyé la commande.
     * @param args    Nom ou chemin du fichier à supprimer.
     */
    @Override
    public void execute(ClientSession session, String args) {

        // Sécurité : Vérifier que le client est connecté
        if (!session.isLoggedIn()) { session.sendMessage("530 Not logged in."); return; }

        // Vérifier la présence du paramètre
        if (args == null || args.isEmpty()) { session.sendMessage("501 Syntax error."); return; }

        try {
            // Calculer la cible à supprimer
            File target = args.startsWith("/")
                    ? new File(session.getRootDirectory(), args.substring(1))
                    : new File(session.getCurrentDirectory(), args);
            target = target.getCanonicalFile();

            // Sécurité : on ne laisse pas l'utilisateur supprimer la racine du serveur ou un répertoire !
            if (!target.getPath().startsWith(session.getRootDirectory().getCanonicalPath())) {
                session.sendMessage("550 Access denied."); return;
            }

            // delete() en Java ne supprime un fichier que s'il s'agit bien d'un fichier, ce qui est le comportement FTP standard

            if (target.exists() && target.isFile() && target.delete()) {
                session.sendMessage("250 File deleted successfully.");
            } else {
                session.sendMessage("550 Delete operation failed. File may not exist or is a directory.");
            }
        } catch (IOException e) {
            session.sendMessage("550 Error handling path.");
        }
    }
}