package fr.univlille.sr1.treeftp;


/**
 * Point d'entrée principal de l'application Tree FTP.
 * Responsable de l'initialisation de la configuration et de l'orchestration de la connexion FTP.
 *
 * @author Sid-Ahmed Ferroudj
 */
public class Main {

    /**
     * Point d'entrée de l'application.
     * Analyse les arguments de la ligne de commande et initie la logique de l'application.
     *
     * @param args Les arguments de la ligne de commande (serveur, port, utilisateur, mdp, options).
     */
    public static void main(String[] args) {

        try {

            // Déléguer la gestion des arguments à fr.univlille.sr1.treeftp.AppConfig (Seperation of Concerns)
            AppConfig config = new AppConfig(args);

            // Afficher le status de la configuration maintenue par fr.univlille.sr1.treeftp.AppConfig
            config.printStatus();

            // Lancer l'application principale
            runApplication(config);

        } catch (IllegalArgumentException e) {
            // Gérer les erreurs de configuration et afficher l'usage correct
            System.out.println("Error: " + e.getMessage());
            System.out.println("Usage: java fr.univlille.sr1.treeftp.Main <server> [port] [user] [pass]");
        }
    }

    /**
     * Orchestre la session FTP : Connexion -> Login -> Exploration -> Déconnexion.
     *
     * @param config L'objet de configuration de l'application contenant les détails du serveur et les modes.
     */
    private static void runApplication(AppConfig config) {
        ClientFTP client = new ClientFTP();
        try {
            client.connect(config.getServer(), config.getPort());

            boolean loggedIn = client.login(config.getUser(), config.getPassword());

            if (loggedIn) {
                Explorer explorer = new Explorer(client, config.getMaxDepth(), config);
                explorer.start(config.isJsonMode(), config.isBfsMode());
            } else {
                System.out.println("Login failed.");
            }
            client.disconnect();

        } catch (Exception e) {
            System.err.println("Unexpected error" + e.getMessage());
        }
    }
}