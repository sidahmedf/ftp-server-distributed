package server;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class FTPServerTest {

    @Test
    public void testServerAcceptsConnection() throws InterruptedException {
        // On choisit un port différent du port principal (2121) pour éviter les conflits
        int testPort = 21212;
        FTPServer server = new FTPServer(testPort);

        // On lance le serveur dans un thread séparé pour ne pas bloquer JUnit
        Thread serverThread = new Thread(() -> {
            server.start();
        });
        serverThread.start();

        // On laisse au serveur 500 millisecondes pour ouvrir le port
        Thread.sleep(500);

        // On simule un client (comme FileZilla) qui tente de se connecter
        try (Socket testClient = new Socket("127.0.0.1", testPort)) {

            // L'Assertion : on vérifie que le client est bien connecté
            assertTrue(testClient.isConnected(), "Le client devrait réussir à se connecter au serveur FTP !");
            System.out.println("Test réussi : Le serveur accepte bien les connexions.");

        } catch (IOException e) {
            fail("La connexion au serveur a échoué : " + e.getMessage());
        } finally {
            // Nettoyage : On coupe le thread du serveur à la fin du test
            serverThread.interrupt();
        }
    }
}