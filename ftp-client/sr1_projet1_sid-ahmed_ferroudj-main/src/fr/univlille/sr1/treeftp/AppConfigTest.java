package fr.univlille.sr1.treeftp;

/**
 * Test unitaire simple pour la classe AppConfig.
 * Exécutez ce fichier pour vérifier la logique d'analyse des arguments (parsing)
 * sans avoir besoin de vous connecter à un serveur FTP.
 */
public class AppConfigTest {

    public static void main(String[] args) {
        System.out.println("Running AppConfig Tests...");

        testStandardArgs();
        testFlags();
        testDepth();

        System.out.println("All tests passed!");
    }

    private static void testStandardArgs() {
        String[] args = {"localhost", "2121", "user", "pass"};
        AppConfig config = new AppConfig(args);

        assert config.getServer().equals("localhost") : "Server parsing failed";
        assert config.getPort() == 2121 : "Port parsing failed";
        assert config.getUser().equals("user") : "User parsing failed";
        System.out.println("Standard Args OK");
    }

    private static void testFlags() {
        String[] args = {"localhost", "-json", "-d", "-bfs"};
        AppConfig config = new AppConfig(args);

        assert config.isJsonMode() : "JSON flag failed";
        assert config.isOnlyDirs() : "DirsOnly flag failed";
        assert config.isBfsMode() : "BFS flag failed";
        System.out.println("Flags OK");
    }

    private static void testDepth() {
        String[] args = {"localhost", "-depth", "5"};
        AppConfig config = new AppConfig(args);

        assert config.getMaxDepth() == 5 : "Depth parsing failed";
        System.out.println("Depth Parameter OK");
    }
}