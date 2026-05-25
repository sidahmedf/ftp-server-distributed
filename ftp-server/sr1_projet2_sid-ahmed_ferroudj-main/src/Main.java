import server.FTPServer;
/**
 * src.Main.java
 *
 * Point d’entrée du programme.
 * Initialise et démarre le serveur FTP sur un port donné.
 *
 * Auteur : Sid Ahmed Ferroudj
 * Date   : Février 2026
 */
public class Main {
    /**
     *  Méthode main()
     * -------------- *
     * Lance l’application FTP.
     * - Définit le port par défaut (2121).
     * - Affiche un message de démarrage.
     * - Crée et démarre une instance de FTPServer.
     * @param args arguments passés en ligne de commande (non utilisés ici)
     */
    public static void main(String[] args) {

        // Port par défaut pour le serveur FTP
        int port = 2121;
        System.out.println("Starting FTP Server on port " + port + ".");

        // Création et lancement du serveur
        FTPServer server = new FTPServer(port);
        server.start();
    }
}