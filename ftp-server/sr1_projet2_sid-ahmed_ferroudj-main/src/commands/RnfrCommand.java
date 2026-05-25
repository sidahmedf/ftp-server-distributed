package commands;
import server.ClientSession;


/**
 * RnfrCommand.java
 *
 * Implémentation de la commande FTP "RNFR".
 * Cette commande permet d’indiquer au serveur le fichier ou dossier
 * que le client souhaite renommer. Elle doit obligatoirement être
 * suivie d’une commande "RNTO".
 *
 * Auteur : Sid Ahmed Ferroudj
 * Date   : Février 2026
 */
import java.io.File;
import java.io.IOException;

/**
 * Classe RnfrCommand
 * -----------------
 * Gère la commande "RNFR".
 * - Vérifie que l’utilisateur est connecté.
 * - Vérifie l’existence du fichier ou dossier cible.
 * - Applique une restriction de type chroot pour la sécurité.
 * - Mémorise la cible en attente de la commande "RNTO".
 */
public class RnfrCommand implements Command {
    /**
     * Méthode execute()
     * -----------------
     * Traite la commande "RNFR".
     *
     * @param session La session du client qui a envoyé la commande.
     * @param args    Nom ou chemin du fichier/dossier à renommer.
     */
    @Override
    public void execute(ClientSession session, String args) {

        // Sécurité
        if (!session.isLoggedIn()) { session.sendMessage("530 Not logged in."); return; }

        // Vérifier la présence du paramètre
        if (args == null || args.isEmpty()) { session.sendMessage("501 Syntax error."); return; }

        try {
            // Calculer la cible à renommer
            File target = args.startsWith("/")
                    ? new File(session.getRootDirectory(), args.substring(1))
                    : new File(session.getCurrentDirectory(), args);

            target = target.getCanonicalFile();

            // Sécurité (Chroot)
            if (!target.getPath().startsWith(session.getRootDirectory().getCanonicalPath())
                    || target.getPath().equals(session.getRootDirectory().getCanonicalPath())) {
                session.sendMessage("550 Access denied.");
                return;
            }

            // Vérifier que le fichier ou dossier existe bien avant de promettre de le renommer
            if (target.exists()) {
                // Mémoriser la cible pour la commande RNTO
                session.setRenameTarget(target);
                session.sendMessage("350 Requested file action pending further information.");
            } else {
                session.sendMessage("550 File or directory not found.");
            }
        } catch (IOException e) {
            session.sendMessage("550 Error handling path.");
        }
    }
}