package commands;

import org.junit.jupiter.api.Test;
import server.ClientSession;

import java.io.File;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class CwdCommandTest {

    @Test
    public void testCwdFailsOnFakeDirectory() {
        ClientSession session = new ClientSession(new Socket());
        session.setLoggedIn(true);

        CwdCommand cmd = new CwdCommand();

        // On mémorise le dossier de départ
        File dossierDepart = session.getCurrentDirectory();

        // On tente d'entrer dans un dossier imaginaire
        cmd.execute(session, "dossier_qui_n_existe_pas_du_tout_123");

        // Le dossier courant ne doit pas avoir changé
        assertEquals(dossierDepart, session.getCurrentDirectory(),
                "CWD ne doit pas modifier le dossier courant si la cible n'existe pas !");
    }

    @Test
    public void testCwdWorksOnRealDirectory() {

        // On crée une session de test avec un socket factice (on n'envoie rien, on ne reçoit rien)
        ClientSession session = new ClientSession(new Socket());

        // On autorise le client et on s'assure que la prison existe
        session.setLoggedIn(true);
        session.getRootDirectory().mkdirs();

        CwdCommand cmd = new CwdCommand();

        // On crée un vrai dossier temporaire pour le test
        File dossierDepart = session.getCurrentDirectory();
        File vraiDossier = new File(dossierDepart, "vrai_dossier");
        vraiDossier.mkdirs(); // On le crée physiquement

        // On exécute la commande
        cmd.execute(session, "vrai_dossier");

        // Le dossier courant doit maintenant être le nouveau dossier
        assertEquals(vraiDossier.getAbsolutePath(), session.getCurrentDirectory().getAbsolutePath(),
                "CWD doit mettre à jour le dossier courant quand la cible existe.");

        // Nettoyage
        vraiDossier.delete();
    }
}