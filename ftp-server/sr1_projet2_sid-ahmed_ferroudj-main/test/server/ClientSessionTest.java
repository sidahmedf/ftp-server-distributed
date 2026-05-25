package server;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class ClientSessionTest {

    @Test
    public void testGetRelativePath() {
        ClientSession session = new ClientSession(new Socket());
        File rootDir = session.getRootDirectory();

        session.setCurrentDirectory(rootDir);
        assertEquals("/", session.getRelativePath(), "À la racine, le chemin doit être '/'");

        File sousDossier = new File(rootDir, "images");
        session.setCurrentDirectory(sousDossier);
        assertEquals("/images", session.getRelativePath(), "Dans le sous-dossier, le chemin doit être '/images'");
    }

    @Test
    public void testInitialStateIsSecure() {
        // Vérifier qu'un nouveau client n'a aucun droit par défaut


        ClientSession session = new ClientSession(new Socket());

        assertFalse(session.isLoggedIn(), "Un nouveau client ne doit pas être connecté par défaut !");
        assertNull(session.getUsername(), "Le pseudo doit être null au départ.");
        assertNull(session.getDataConnection(), "Il ne doit pas y avoir de port de données ouvert au départ.");
        assertNull(session.getRenameTarget(), "Aucun fichier ne doit être en attente de renommage.");

        // Vérifier que le dossier courant de départ est bien la prison (rootDirectory)
        assertEquals(session.getRootDirectory(), session.getCurrentDirectory(), "Le dossier initial doit être la racine FTP.");
    }

    @Test
    public void testAuthenticationState() {
        // Vérifier que les setters de connexion fonctionnent bien
        ClientSession session = new ClientSession(new Socket());

        session.setUsername("sid");
        session.setLoggedIn(true);

        assertEquals("sid", session.getUsername(), "Le nom d'utilisateur doit être mémorisé.");
        assertTrue(session.isLoggedIn(), "Le client doit être marqué comme connecté.");
    }

    @Test
    public void testRenameStateMechanism() {
        //Vérifier la mémorisation pour le combo RNFR + RNTO
        ClientSession session = new ClientSession(new Socket());
        File targetFile = new File(session.getRootDirectory(), "mon_fichier.txt");

        // Simulation de la commande RNFR
        session.setRenameTarget(targetFile);
        assertEquals(targetFile, session.getRenameTarget(), "La session doit mémoriser le fichier à renommer.");

        // Simulation de la fin de la commande RNTO (nettoyage)
        session.setRenameTarget(null);
        assertNull(session.getRenameTarget(), "La cible doit pouvoir être réinitialisée (nettoyée).");
    }

    @Test
    public void testSendMessageSafety() {
        //  Vérifier que l'appel à sendMessage ne fait pas crasher le serveur

        // si le flux d'écriture (writer) n'a pas encore été initialisé par le thread.
        ClientSession session = new ClientSession(new Socket());

        // La méthode assertDoesNotThrow garantit qu'aucune exception (comme NullPointerException) ne sera levée.
        assertDoesNotThrow(() -> session.sendMessage("200 Test message"),
                "L'envoi d'un message sans writer initialisé doit être ignoré en silence, sans crasher.");
    }
}