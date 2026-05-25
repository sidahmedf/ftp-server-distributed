package fr.univlille.sr1.treeftp;

import java.util.ArrayList;
import java.util.List;

/**
 * Gère la configuration de l'application en analysant les arguments de la ligne de commande.
 * Supporte la gestion des arguments positionnels (serveur, port...) et des drapeaux optionnels (-json, -bfs, -depth...).
 *
 * @author Sid-Ahmed Ferroudj
 */
public class AppConfig {

    // Valeurs par défaut
    private static final int DEFAULT_PORT = 21;
    private static final String DEFAULT_USER = "anonymous";
    private static final String DEFAULT_PASS = "guest";
    private static final int DEFAULT_DEPTH = Integer.MAX_VALUE;

    // variables de configuration
    private String server;
    private int port = DEFAULT_PORT;
    private String user = DEFAULT_USER;
    private String password = DEFAULT_PASS;
    private int maxDepth = DEFAULT_DEPTH;

    // Modes et options
    private boolean jsonMode = false;
    private boolean bfsMode = false;
    private boolean onlyDirs = false;  // pour -d (afficher seulement les répertoires)
    private boolean showPerms = false; // pur -p (afficher les permissions)

    /**
     * Initialise la configuration en analysant les arguments fournis.
     *
     * @param args Les arguments de la ligne de commande passés au programme principal.
     * @throws IllegalArgumentException Si des arguments obligatoires (comme l'adresse du serveur) sont manquants.
     */
    public AppConfig(String[] args) {
        parseArguments(args);
    }

    /**
     * Logique principale pour séparer les drapeaux (flags) des arguments positionnels.
     *
     * @param args Le tableau d'arguments brut.
     */
    private void parseArguments(String[] args) {
        List<String> positionalArgs = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            // Est-ce une option ?
            if (arg.startsWith("-")) {
                switch (arg.toLowerCase()) {
                    case "-json":
                    case "--json":
                        this.jsonMode = true;
                        break;
                    case "-bfs":
                    case "--bfs":
                        this.bfsMode = true;
                        break;
                    case "-d":
                        this.onlyDirs = true;
                        break;
                    case "-p":
                        this.showPerms = true;
                        break;

                        // Aide
                    case "--help":
                        printHelp();
                        System.exit(0); // Quitter après avoir affiché l'aide
                        break;

                    case "-depth": // Option avec valeur
                        if (i + 1 < args.length) {
                            try {
                                this.maxDepth = Integer.parseInt(args[i + 1]);
                                i++; // passer à l'argument suivant comme on l'a consommé
                            } catch (NumberFormatException e) {
                                System.out.println(" Warning: Invalid depth value. Using Unlimited.");
                            }
                        }
                        break;
                    default:
                        System.out.println(" Warning: Unknown option '" + arg + "' ignored.");
                }
            } else {
                // Est-ce un argument positionnel ?
                positionalArgs.add(arg);
            }
        }

        // Valider les arguments positionnels
        if (positionalArgs.isEmpty()) {
            throw new IllegalArgumentException("Server address is missing.");
        }

        // l'ordre des mots restants détermine Serveur, Port, Utilisateur, Mot de passe
        this.server = positionalArgs.get(0);

        if (positionalArgs.size() >= 2) {
            try {
                this.port = Integer.parseInt(positionalArgs.get(1));
            } catch (NumberFormatException e) {

                // garder la valeur par défaut
                System.out.println(" Warning: Port must be a number. Using "+DEFAULT_PORT+".");
            }
        }

        if (positionalArgs.size() >= 3) this.user = positionalArgs.get(2);
        if (positionalArgs.size() >= 4) this.password = positionalArgs.get(3);
    }

    /**
     * Affiche les options de ligne de commande disponibles.
     */
    private void printHelp() {
        System.out.println("Usage: java fr.univlille.sr1.treeftp.Main <server> [port] [user] [pass] [OPTIONS]");
        System.out.println("Options:");
        System.out.println("  -depth <n> : Limit recursion depth");
        System.out.println("  -json      : Output in JSON format");
        System.out.println("  -bfs       : Breadth-First Search mode");
        System.out.println("  -d         : Directories only");
        System.out.println("  -p         : Show permissions");
    }

    // En gros des getters
    public String getServer() { return server; }
    public int getPort() { return port; }
    public String getUser() { return user; }
    public String getPassword() { return password; }
    public int getMaxDepth() { return maxDepth; }
    public boolean isJsonMode() { return jsonMode; }
    public boolean isBfsMode() { return bfsMode; }
    public boolean isOnlyDirs() { return onlyDirs; }
    public boolean isShowPerms() { return showPerms; }


    /**
     * Affiche la configuration actuelle dans la console pour débogage/confirmation.
     * Les mots de passe sont masqués pour des raisons de sécurité.
     */
    public void printStatus() {
        System.out.println("FTP Client Configuration");
        System.out.println("------------------------");
        System.out.println("Target: " + server + ":" + port);
        System.out.println("Auth  : " + user + " / " + (password.isEmpty() ? "(none)" : password));
        System.out.println("Depth : " + (maxDepth == Integer.MAX_VALUE ? "Unlimited" : maxDepth));

        List<String> modes = new ArrayList<>();
        if (jsonMode) modes.add("JSON");
        else if (bfsMode) modes.add("BFS");
        else modes.add("Text Tree");

        if (onlyDirs) modes.add("DirsOnly");
        if (showPerms) modes.add("ShowPerms");

        System.out.println("Modes : " + String.join(", ", modes));
        System.out.println("------------------------");
    }
}