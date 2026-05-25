# Système Réparti : Client & Serveur FTP (Java)

## Description
Ce dépôt contient une implémentation complète d'une architecture client-serveur FTP développée en Java. Il est divisé en deux sous-projets distincts permettant d'illustrer la communication réseau et la gestion de fichiers distribués.

* **`/ftp-server`** : Un serveur FTP gérant les requêtes entrantes, l'authentification et le transfert de fichiers.
* **`/ftp-client`** : Une application cliente permettant de se connecter au serveur, de lister les fichiers (différents formats, récursivité) et de télécharger/envoyer des données.

## Technologies Utilisées
* **Langage :** Java
* **Concepts :** Sockets, Threads (Concurrency), Flux d'E/S (I/O Streams), Réseau

## Comment exécuter le projet

### 1. Lancer le Serveur
1. Naviguer dans le dossier du serveur : `cd ftp-server`
2. Compiler et exécuter : `[votre commande Java, ex: javac Main.java puis java Main]`

### 2. Lancer le Client
1. Ouvrir un nouveau terminal.
2. Naviguer dans le dossier du client : `cd ftp-client`
3. Compiler et exécuter : `[votre commande Java]`
4. Se connecter au port configuré sur le serveur (ex: localhost:21).