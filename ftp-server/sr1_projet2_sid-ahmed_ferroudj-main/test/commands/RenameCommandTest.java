package commands;

import org.junit.jupiter.api.Test;
import server.ClientSession;

import java.io.File;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class RenameCommandTest {

    @Test
    public void testRntoFailsWithoutRnfr() {
        ClientSession session = new ClientSession(new Socket());

        // On autorise le faux client à passer le contrôle de sécurité
        session.setLoggedIn(true);

        RntoCommand rnto = new RntoCommand();

        // On s'assure que la mémoire est vide
        session.setRenameTarget(null);

        // On tente un RNTO direct (Interdit par le protocole)
        rnto.execute(session, "nouveau_nom.txt");

        // L'état de la session ne doit pas avoir changé, et surtout, ça ne doit pas crasher !
        assertNull(session.getRenameTarget(), "RNTO ne doit rien faire s'il n'y a pas eu de RNFR avant.");
    }

    @Test
    public void testRenameStateIsClearedAfterRnto() {
        ClientSession session = new ClientSession(new Socket());

        // On autorise le faux client à passer le contrôle de sécurité
        session.setLoggedIn(true);

        RntoCommand rnto = new RntoCommand();

        // On simule qu'un RNFR a bien eu lieu juste avant
        File fauxFichier = new File(session.getCurrentDirectory(), "vieux_fichier.txt");
        session.setRenameTarget(fauxFichier);

        // On lance la commande RNTO
        rnto.execute(session, "nouveau_fichier.txt");

        // Après un RNTO (réussi ou raté), le serveur doit TOUJOURS vider la mémoire
        assertNull(session.getRenameTarget(), "La session doit purger sa mémoire (renameTarget = null) après un RNTO.");
    }
}