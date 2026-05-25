package commands;
import server.ClientSession;

/**
 * CwdCommand.java
 *
 * Implémentation de la commande FTP "CWD".
 * Cette commande permet au client de changer le répertoire
 * courant sur le serveur FTP.
 *
 * Auteur : Sid Ahmed Ferroudj
 * Date   : Février 2026
 */


import java.io.File;
import java.io.IOException;


/**
 * Classe CwdCommand
 * ----------------
 * Gère la commande "CWD".
 * - Vérifie que l’utilisateur est connecté.
 * - Résout les chemins absolus et relatifs.
 * - Applique une restriction de type chroot pour la sécurité.
 * - Met à jour le répertoire courant de la session.
 */
public class CwdCommand implements Command {
    /**
     * Méthode execute()
     * -----------------
     * Traite la commande "CWD".
     *
     * @param session La session du client qui a envoyé la commande.
     * @param args    Nom ou chemin du répertoire cible.
     */
    @Override
    public void execute(ClientSession session, String args) {

        // Sécurité : Vérifier que le client est connecté
        if (!session.isLoggedIn()) {
            session.sendMessage("530 Not logged in.");
            return;
        }

        File newDir;

        // Gérer les chemins absolus (commençant par /)
        if (args.startsWith("/")) {

            String cleanArg = args.substring(1);
            newDir = new File(session.getRootDirectory(), cleanArg);
        } else {
            // Sinon c'est relatif au dossier courant
            newDir = new File(session.getCurrentDirectory(), args);
        }

        try {
            // Simplifier le chemin (enlever les ".." et les ".")
            File canonicalDir = newDir.getCanonicalFile();

            // Vérifier la "Prison" (Chroot)
            if (!canonicalDir.getPath().startsWith(session.getRootDirectory().getCanonicalPath())) {
                session.sendMessage("550 Access denied.");
                return;
            }

            // Vérifier que c'est bien un dossier qui existe
            if (canonicalDir.exists() && canonicalDir.isDirectory()) {
                session.setCurrentDirectory(canonicalDir);
                session.sendMessage("250 Directory changed to " + session.getRelativePath());
            } else {
                session.sendMessage("550 Directory not found.");
            }

        } catch (IOException e) {
            session.sendMessage("550 Error resolving path.");
        }
    }
}