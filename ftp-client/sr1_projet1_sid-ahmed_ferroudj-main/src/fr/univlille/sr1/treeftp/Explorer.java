package fr.univlille.sr1.treeftp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;


public class Explorer {

    private  final ClientFTP client;
    private final int maxDepth;
    private final AppConfig config;

    /**
     * Crée une nouvelle instance de l'Explorateur.
     *
     * @param client   Le client FTP connecté.
     * @param maxDepth La profondeur maximale d'exploration.
     * @param config   La configuration complète de l'application (accès aux drapeaux -d, -p...).
     */
    public Explorer(ClientFTP client, int maxDepth, AppConfig  config) {
        this.client = client;
        this.maxDepth = maxDepth;
        this.config = config;
    }

    /**
     * Lance le processus d'exploration selon le mode sélectionné.
     * Aiguille l'exécution vers la logique DFS, BFS ou JSON.
     *
     * @param jsonMode Si vrai, active la sortie au format JSON.
     * @param bfsMode  Si vrai, utilise le parcours en largeur (BFS).
     */
    public void start(boolean jsonMode, boolean bfsMode) {

        try {
            if (jsonMode) {
                System.out.println("[");
                exploreJson(0);
                System.out.println("]");
            } else if (bfsMode) {
                // Mode largeur
                System.out.println("Starting Breadth-First Exploration (Largeur)...");
                exploreBfs();
                System.out.println("Exploration complete.");
            } else {
                // Mode profondeur (par défaut)
                System.out.println("Starting tree exploration...");
                explore("", 0);
                System.out.println("Exploration complete.");
            }
        } catch (Exception e) { System.err.println("Unexpected error: " + e.getMessage()); }
    }

    /**
     * Exploration standard récursive en profondeur (DFS - Vue Arborescente).
     *
     * @param prefix       Le préfixe visuel pour l'indentation (ex: "|   ").
     * @param currentLevel Le niveau de profondeur actuel.
     * @throws IOException En cas d'erreur de communication avec le serveur.
     */
    private void explore(String prefix, int currentLevel) throws IOException {

        // récupérer la liste des fichiers dans le répertoire courant
        List<String> files = client.listFiles();
        // pour chaque fichier/répertoire
        for (String line  : files) {

            // vérifier que la ligne n'est pas vide
            if (line.length() < 1) {
                continue;
            }

            //vérifier si c'est un répertoire ou un fichier
            char type = line.charAt(0);

            // extraire le nom (dernier mot de la ligne)
            String[] parts = line.split("\\s+");
            String name = parts[parts.length - 1];
            String permissions = parts[0];

            boolean isDir = (type == 'd');
            if (config.isOnlyDirs() && !isDir) {
                continue;
            }


            // ignorer les entrées spéciales . et .. (lien symboliques)
            if (name.equals(".") || name.equals("..")) {
                continue;
            }

            String displayName = name;
            if (config.isShowPerms()) {
                // We prepend the permissions to the name
                displayName = "[" + permissions + "] " + name;
            }
            System.out.println(prefix + "|-- " + displayName);

            // récursivement explorer les répertoires
            if (type == 'd') {

                // Ne pas dépasser la profondeur maximale
                if (currentLevel >= maxDepth) {
                    continue;
                }

                // Tenter d'aller dans le répertoire (CWD)
                boolean moved = client.changeDirectory(name);

                if (moved) {
                    //appeler récursivement explore avec un préfixe mis à jour
                    explore(prefix + "|   ", currentLevel + 1);

                    // Remonter d'un répertoire (CDUP) après l'exploration
                    client.changeDirectoryUp();
                }
            }
        }

    }


    /**
     * Exploration récursive pour générer un JSON structuré.
     *
     * @param currentLevel Le niveau de profondeur actuel (pour l'indentation du JSON).
     * @throws IOException En cas d'erreur réseau.
     */
    private void exploreJson(int currentLevel) throws IOException {

        List<String> files = client.listFiles();

        // Prétraitement
        List<String> filteredFiles = new ArrayList<>();
        for (String line : files) {
            if (line.length() < 1) continue;

            char type = line.charAt(0);

            boolean isDir = (type == 'd');

            // Si l'utilisateur veut uniquement les dossiers, et ce n'en est pas un , on skip
            if (config.isOnlyDirs() && !isDir) {
                continue;
            }

            // même extraction du nom que dans explore
            String[] parts = line.split("\\s+");
            String name = parts[parts.length - 1];
            if (!name.equals(".") && !name.equals("..")) {
                filteredFiles.add(line);
            }
        }

        // parcourir les fichiers filtrés
        for (int i = 0; i < filteredFiles.size(); i++) {
            String line = filteredFiles.get(i);

            // vérifier si c'est un répertoire ou un fichier
            char type = line.charAt(0);

            // extraire le nom
            String[] parts = line.split("\\s+");
            String name = parts[parts.length - 1];
            String permissions = parts[0];

            // Calcul de l'indentation JSON
            String indentBase = "  ".repeat(currentLevel + 1);
            String indentProp = "  ".repeat(currentLevel + 2);

            // Début de l'objet
            System.out.println(indentBase + "{");
            System.out.println(indentProp + "\"name\": \"" + name + "\",");

            // --- FEATURE 2: SHOW PERMISSIONS (-p) ---
            if (config.isShowPerms()) {
                // On ajoute un champ JSON spécifique pour les permissions
                System.out.println(indentProp + "\"permissions\": \"" + permissions + "\",");
            }

            // Logique de récursion
            boolean isDir = (type == 'd');
            boolean canGoDeeper = (currentLevel < maxDepth);

            if (isDir && canGoDeeper) {

                System.out.println(indentProp + "\"type\": \"directory\",");
                System.out.println(indentProp + "\"children\": [");

                // Tenter d'aller dans le répertoire (CWD)
                boolean moved = client.changeDirectory(name);

                if (moved) {
                    // appeler récursivement exploreJson (+2 pour l'indentation JSON)
                    exploreJson(currentLevel + 2);

                    // Remonter d'un répertoire (CDUP)
                    client.changeDirectoryUp();
                }
                System.out.println(indentProp + "]");

            } else {
                // Cas simple (dossier sans descendance ou fichier)
                System.out.println(indentProp + "\"type\": \"" + (isDir ? "directory" : "file") + "\"");
            }

            // Fin de l'objet
            System.out.print(indentBase + "}");

            // Gestion de la virgule (Sauf pour le dernier élément)
            if (i < filteredFiles.size() - 1) {
                System.out.println(",");
            } else {
                System.out.println("");
            }
        }
    }

    /**
     * Exploration itérative en Largeur d'abord (BFS).
     * Utilise une File (Queue) pour explorer couche par couche.
     *
     * @throws IOException En cas d'erreur réseau.
     */
    private void exploreBfs() throws IOException {

        // La Queue stocke les chemins relatifs à explorer (ex: "folder1/subfolder")
        Queue<String> queue = new LinkedList<>();

        // On commence par la racine (chaîne vide)
        queue.add("");

        // Tant qu'il y a des dossiers à visiter
        while (!queue.isEmpty()) {

            // Récupérer le prochain dossier à traiter
            String currentPath = queue.poll();

            // Calculer la profondeur (nombre de '/')
            int currentDepth = currentPath.isEmpty() ? 0 : currentPath.split("/").length;
            if (currentDepth >= maxDepth) continue;

            // Déplacement de la racine au dossier cible
            client.changeDirectoryToRoot();
            if (!currentPath.isEmpty()) {
                boolean moved = client.changeDirectoryPath(currentPath);
                if (!moved) {
                    System.out.println("Error: Could not reach " + currentPath);
                    continue;
                }
            }

            // Lister les fichiers
            List<String> files = client.listFiles();

            // Affichage et Remplissage de la Queue
            for (String line : files) {
                if (line.length() < 1) continue;

                // Parsing
                char type = line.charAt(0);

                boolean isDir = (type == 'd');
                if (config.isOnlyDirs() && !isDir) {
                    continue;
                }


                String[] parts = line.split("\\s+");
                String name = parts[parts.length - 1];
                String permissions = parts[0];

                if (name.equals(".") || name.equals("..")) continue;

                String displayPath = currentPath.isEmpty() ? name : currentPath + "/" + name;

                if (config.isShowPerms()) {
                    displayPath = "[" + permissions + "] " + displayPath;
                }
                // Affichage : On affiche le chemin complet pour que ce soit clair
                System.out.println("[Level " + currentDepth + "] " + displayPath);

                // Si c'est un dossier, on l'ajoute à la file (pour plus tard)
                if (isDir) {
                    String realPath = currentPath.isEmpty() ? name : currentPath + "/" + name;
                    queue.add(realPath);
                }
            }
        }
    }


}
