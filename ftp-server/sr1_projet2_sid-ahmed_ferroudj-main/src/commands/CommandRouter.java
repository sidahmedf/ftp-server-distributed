package commands;
import server.ClientSession;
import server.FTPServer;
/**
 * CommandRouter.java
 *
 * Classe responsable du routage des commandes FTP.
 * Elle associe chaque commande reçue à son implémentation
 * correspondante et déclenche son exécution.
 *
 * Auteur : Sid Ahmed Ferroudj
 * Date   : Février 2026
 */

import java.util.HashMap;
import java.util.Map;

/**
 * Classe CommandRouter
 * -------------------
 * - Maintient une table de correspondance entre les noms de commandes FTP
 *   et leurs implémentations.
 * - Analyse les messages reçus du client.
 * - Délègue l’exécution à la commande appropriée.
 */
public class CommandRouter {
    /**
     * * Table de correspondance entre le nom de la commande et son implémentation.
     * */
    private final Map<String, Command> commandMap = new HashMap<>();

    public CommandRouter() {

        // Authentification
        commandMap.put("USER", new UserCommand());
        commandMap.put("PASS", new PassCommand());
        commandMap.put("QUIT", new QuitCommand());

        // Navigation et gestion des fichiers
        commandMap.put("PWD", new PwdCommand());
        commandMap.put("CWD", new CwdCommand());
        commandMap.put("CDUP", (session, args) -> new CwdCommand().execute(session, "..")); // CDUP peut être traité comme CWD ..
        commandMap.put("XPWD", new PwdCommand()); // Pour résoudre problème vieux client Windows qui envoie XPWD au lieu de PWD

        // Informations système
        commandMap.put("OPTS", (session, args) -> session.sendMessage("200 OK"));
        commandMap.put("SYST", (session, args) -> session.sendMessage("215 UNIX Type: L8"));

        //Connexions de données
        commandMap.put("PASV", new PasvCommand());
        commandMap.put("EPSV", new EpsvCommand());
        commandMap.put("TYPE", new TypeCommand());

        // Transferts de fichiers
        commandMap.put("LIST", new ListCommand());
        commandMap.put("RETR", new RetrCommand());
        commandMap.put("STOR", new StorCommand());

        // Gestion des fichiers et dossiers
        commandMap.put("MKD", new MkdCommand());
        commandMap.put("DELE", new DeleCommand());
        commandMap.put("RMD", new RmdCommand());

        // Renommage de fichiers
        commandMap.put("RNFR", new RnfrCommand());
        commandMap.put("RNTO", new RntoCommand());

    }

    /**
     * Méthode route()
     * ---------------
     * Analyse un message FTP reçu et exécute la commande correspondante.
     *
     * @param session La session du client.
     * @param message La ligne de commande reçue (ex: "USER sid").
     */
    public void route(ClientSession session, String message) {

        String[] parts = message.trim().split("\\s+", 2);

        String commandName = parts[0].toUpperCase().trim();
        String args = (parts.length > 1) ? parts[1].trim() : "";

        if (commandMap.containsKey(commandName)) {

            Command cmd = commandMap.get(commandName);
            cmd.execute(session, args);
        } else {

            session.sendMessage("502 Command not implemented.");
        }
    }
}