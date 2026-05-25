package commands;
import server.ClientSession;


/**
 * RmdCommand.java
 *
 * Implémentation de la commande FTP "RMD".
 * Cette commande permet au client de supprimer un répertoire
 * sur le serveur, à condition qu’il soit vide.
 *
 * Auteur : Sid Ahmed Ferroudj
 * Date   : Février 2026
 */
import java.io.File;
import java.io.IOException;

/**
 * Classe RmdCommand
 * ----------------
 * Gère la commande "RMD".
 * - Vérifie que l’utilisateur est connecté.
 * - Applique une restriction de type chroot pour la sécurité.
 * - Supprime uniquement les répertoires vides (comportement FTP standard).
 */
public class RmdCommand implements Command {
    /**
     * Méthode execute()
     * -----------------
     * Traite la commande "RMD".
     *
     * @param session La session du client qui a envoyé la commande.
     * @param args    Nom ou chemin du répertoire à supprimer.
     */
    @Override
    public void execute(ClientSession session, String args) {

        // Sécurité
        if (!session.isLoggedIn()) { session.sendMessage("530 Not logged in."); return; }

        // Vérifier la présence du paramètre
        if (args == null || args.isEmpty()) { session.sendMessage("501 Syntax error."); return; }

        try {
            // Calculer la cible à supprimer
            File target = args.startsWith("/")
                    ? new File(session.getRootDirectory(), args.substring(1))
                    : new File(session.getCurrentDirectory(), args);
            target = target.getCanonicalFile();

            // Sécurité additionnelle : on ne laisse pas l'utilisateur supprimer la racine du serveur !
            if (!target.getPath().startsWith(session.getRootDirectory().getCanonicalPath())
                    || target.getPath().equals(session.getRootDirectory().getCanonicalPath())) {
                session.sendMessage("550 Access denied. Cannot delete root directory."); return;
            }

            // delete() en Java ne supprime un dossier que s'il est vide, ce qui est le comportement FTP standard
            if (target.exists() && target.isDirectory() && target.delete()) {
                session.sendMessage("250 Directory removed successfully.");
            } else {
                session.sendMessage("550 Remove directory failed. Directory must be empty.");
            }
        } catch (IOException e) {
            session.sendMessage("550 Error handling path.");
        }
    }
}