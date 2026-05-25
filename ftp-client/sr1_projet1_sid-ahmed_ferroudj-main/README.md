# Client FTP – Exploration d'Arborescence

**Auteur :** Sid-Ahmed Ferroudj  
**Date :** 29/01/2026

---

## Introduction

Ce projet est un client FTP en ligne de commande développé en **Java**.  
Il permet de se connecter à un serveur FTP distant, d'explorer son arborescence de fichiers et d'afficher le résultat sous différentes formes.

L'objectif principal est de fournir un outil flexible capable de :

- Parcourir récursivement les dossiers (similaire à la commande `tree` sous Linux).
- Générer une sortie structurée au format JSON.
- Explorer l'arborescence couche par couche (**Breadth-First Search**).
- Filtrer les résultats (dossiers uniquement) et afficher les permissions.

---

## Instructions de Build et d'Exécution

### Prérequis

- Java **JDK 17** ou supérieur
- Une connexion internet (pour les tests sur serveurs publics)

---

### Compilation

Pour compiler le projet depuis la racine :

```bash
# Création du dossier de destination (si nécessaire)
mkdir -p bin

# Compilation de l'ensemble des sources
javac -d bin src/fr/univlille/sr1/treeftp/*.java
```

---

### Exécution

Deux méthodes sont possibles pour lancer l'application :

---

## Option 1 (Recommandée) : Exécution via un JAR

Le projet peut être empaqueté sous forme d'une archive Java (**JAR**) exécutable.

### 1. Création du JAR

```bash
jar cfe TreeFtp.jar fr.univlille.sr1.treeftp.Main -C bin .
```

### 2. Lancement via le JAR

```bash
java -jar TreeFtp.jar ftp.free.fr 21 anonymous guest
```

---

## Option 2 : Exécution classique (via classpath)

Pour lancer directement depuis le dossier `bin` :

```bash
java -cp bin fr.univlille.sr1.treeftp.Main <serveur> [port] [user] [pass] [OPTIONS]
```

### Exemple concret (serveur public)

```bash
java -cp bin fr.univlille.sr1.treeftp.Main ftp.free.fr 21 anonymous guest -depth 1
```

> **Note :** Le serveur `ftp.ubuntu.com` répondant parfois lentement aux commandes passives,  
> il est recommandé d'utiliser `ftp.free.fr` pour les tests publics.

---

## Génération du JAR (Exécutable)

Le projet peut être empaqueté sous forme d'une archive Java (**JAR**) exécutable.

### 1. Création du JAR

Utilisez la commande suivante pour générer l'archive `TreeFtp.jar` :

```bash
jar cfe TreeFtp.jar fr.univlille.sr1.treeftp.Main -C bin .
```

### 2. Exécution via le JAR

Lancez ensuite l'application directement :

```bash
java -jar TreeFtp.jar ftp.free.fr 21 anonymous guest
```



---

## Options Disponibles

| Option | Description |
|-------|-------------|
| `-depth <n>` | Limite la profondeur de récursion (ex: `-depth 2`) |
| `-json` | Affiche la sortie au format JSON |
| `-bfs` | Utilise un parcours en largeur (**Breadth-First Search**) |
| `-d` | Affiche uniquement les répertoires |
| `-p` | Affiche les permissions (ex: `drwxr-xr-x`) |

---

## Architecture Logicielle

L'application est structurée autour de **quatre classes principales**, respectant le principe de séparation des responsabilités (*Separation of Concerns*).

### Classes Principales

- **Main**  
  Point d'entrée de l'application. Orchestre l'initialisation et la connexion FTP.

- **AppConfig**  
  Gère le parsing des arguments de ligne de commande et stocke la configuration  
  (mode JSON, BFS, identifiants, etc.).

- **ClientFTP**  
  Encapsule la logique réseau bas niveau (Sockets, commandes FTP `USER`, `PASS`, `LIST`, `CWD`).  
  Implémente une partie du protocole **RFC 959**.

- **Explorer**  
  Contient la logique algorithmique d'exploration (**DFS**, **BFS**, génération JSON).  
  Utilise `ClientFTP` pour naviguer.

---

## Code Samples

Voici 5 extraits illustrant des points intéressants de la conception.

---

### 1. Délégation de la Configuration (AppConfig)

La classe `Main` ne gère pas directement le parsing des arguments.  
Elle délègue cette tâche à `AppConfig`.

```java
// Dans Main.java
public static void main(String[] args) {
  try {
    // Délégation du parsing (Separation of Concerns)
    AppConfig config = new AppConfig(args);

    // ... Lancement de l'application ...

  } catch (IllegalArgumentException e) {
    System.err.println("Erreur de configuration : " + e.getMessage());
  }
}
```

---

### 2. Gestion de la Connexion de Données (Mode Passif)

FTP nécessite deux canaux : commandes + données.  
La commande `PASV` permet de récupérer dynamiquement l'IP et le port.

```java
// Dans ClientFTP.java
private Socket openDataConnection() throws IOException {
  writer.println("PASV");

  // ... Parsing de la réponse "227 Entering Passive Mode (h1,h2,h3,h4,p1,p2)" ...

  // Calcul du port selon la formule FTP (p1 * 256 + p2)
  int dataPort = (p1 * 256) + p2;

  // Connexion dédiée au transfert de données
  return new Socket(ip, dataPort);
}
```

---

### 3. Exploration Récursive avec Limite de Profondeur (DFS)

L'exploration standard utilise une récursion naturelle (**DFS**).  
Une condition d'arrêt évite les parcours trop longs.

```java
// Dans Explorer.java
private void explore(String prefix, int currentLevel) throws IOException {

  if (isDir) {
    // Condition d'arrêt : Ne pas dépasser la profondeur max
    if (currentLevel >= maxDepth) return;

    // Descente dans le dossier
    if (client.changeDirectory(name)) {
      explore(prefix + "|   ", currentLevel + 1);

      // Remontée (Backtracking)
      client.changeDirectoryUp();
    }
  }
}
```

---

### 4. Parcours en Largeur (BFS) avec File d'Attente

Le mode BFS utilise une approche itérative avec une `Queue`  
pour explorer niveau par niveau.

```java
// Dans Explorer.java
private void exploreBfs() throws IOException {
  Queue<String> queue = new LinkedList<>();
  queue.add(""); // Racine

  while (!queue.isEmpty()) {
    String currentPath = queue.poll();

    // Déplacement FTP vers la cible
    client.changeDirectoryToRoot();
    client.changeDirectoryPath(currentPath);

    // ... Traitement et ajout des enfants ...
  }
}
```

---

### 5. Génération de JSON Structuré

Le JSON est généré manuellement pour éviter des dépendances externes.  
L'indentation est calculée selon la profondeur.

```java
// Dans Explorer.java (Mode JSON)
String indentProp = "  ".repeat(currentLevel + 2);

System.out.println(indentProp + "\"name\": \"" + name + "\",");

if (config.isShowPerms()) {
  // Ajout conditionnel d'un champ JSON
  System.out.println(indentProp + "\"permissions\": \"" + permissions + "\",");
}
```

---

## Tests et Validation

Le projet inclut une suite de tests automatisés et un test unitaire.

---

### 1. Test Unitaire (Logique pure)

Le fichier :

- `src/fr/univlille/sr1/treeftp/AppConfigTest.java`

permet de valider le parsing des arguments sans connexion réseau.

Commande :

```bash
java -cp bin fr.univlille.sr1.treeftp.AppConfigTest
```

---

### 2. Tests d'Intégration (Scripts)

Des scripts sont fournis dans `scripts/` pour tester sur un serveur public (`ftp.free.fr`).

- **Windows** : double-cliquez sur `scripts/test_public_free.bat`
- **Linux / Mac** : exécutez :

```bash
./scripts/test_public_free.sh
```

Ces scripts testent :

- L'affichage en arbre simple
- L'export JSON filtré (dossiers uniquement)
- Le mode BFS avec affichage des permissions


