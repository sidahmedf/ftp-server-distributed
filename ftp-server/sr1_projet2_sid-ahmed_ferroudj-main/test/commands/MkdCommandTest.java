package commands;

import org.junit.jupiter.api.Test;
import server.ClientSession;

import java.io.File;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class MkdCommandTest {

    @Test
    public void testMkdCreatesDirectory() {


        ClientSession session = new ClientSession(new Socket());
        session.setLoggedIn(true);

        // On s'assure que la prison "ftp_root" existe physiquement avant de tester
        session.getRootDirectory().mkdirs();

        MkdCommand cmd = new MkdCommand();

        // On définit un nom de dossier de test
        String newDirName = "dossier_test_unitaire";
        File expectedDir = new File(session.getCurrentDirectory(), newDirName);

        // On exécute la commande
        cmd.execute(session, newDirName);

        // On vérifie que le dossier a bien été créé physiquement sur le disque
        assertTrue(expectedDir.exists(), "La commande MKD doit créer le dossier sur le disque.");
        assertTrue(expectedDir.isDirectory(), "La cible doit bien être un dossier.");

        // Nettoyage très important : on supprime le dossier pour les prochains tests
        expectedDir.delete();
    }

    @Test
    public void testMkdSecurityChrootJail() {
        ClientSession session = new ClientSession(new Socket());

        session.setLoggedIn(true);
        session.getRootDirectory().mkdirs();

        MkdCommand cmd = new MkdCommand();

        // Tentative de piratage : remonter au-dessus de ftp_root
        String hackerPath = "../../dossier_interdit";
        File hackedDir = new File(session.getCurrentDirectory(), hackerPath);

        // On exécute la commande malveillante
        cmd.execute(session, hackerPath);

        // On vérifie que le serveur a bloqué la création !
        assertFalse(hackedDir.exists(), "SECURITÉ FATALE : La commande MKD a laissé le client sortir de la racine !");
    }
}