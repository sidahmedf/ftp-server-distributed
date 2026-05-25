package commands;

import org.junit.jupiter.api.Test;
import server.ClientSession;

import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class CommandRouterTest {

    @Test
    public void testRouteKnownCommandWithArgs() {
        //  Vérifier que le routeur trouve bien la commande USER et lui passe l'argument
        CommandRouter router = new CommandRouter();
        ClientSession session = new ClientSession(new Socket());

        // On envoie une commande texte pure
        router.route(session, "USER administrateur");

        // la classe UserCommand a dû être appelée et modifier le pseudo.
        assertEquals("administrateur", session.getUsername(),
                "Le routeur doit découper correctement la commande et l'argument.");
    }

    @Test
    public void testRouteCommandCaseInsensitiveAndSpaces() {
        //  Vérifier la robustesse face à un client qui envoie des espaces ou des minuscules
        CommandRouter router = new CommandRouter();
        ClientSession session = new ClientSession(new Socket());

        // FileZilla peut parfois envoyer des commandes avec des espaces ou en minuscules
        router.route(session, "   uSeR    sid_hacker   ");

        // Le code utilise .toUpperCase().trim() et .trim() pour les arguments, ça devrait marcher sans problème
        assertEquals("sid_hacker", session.getUsername(),
                "Le routeur doit ignorer la casse (majuscules/minuscules) et nettoyer les espaces.");
    }

    @Test
    public void testRouteUnknownCommand() {
        // Vérifier que le serveur ne crashe pas si on envoie n'importe quoi
        CommandRouter router = new CommandRouter();
        ClientSession session = new ClientSession(new Socket());

        // assertDoesNotThrow garantit qu'aucune exception n'est levée
        assertDoesNotThrow(() -> router.route(session, "HACK_SERVER 1234"),
                "Une commande inconnue doit simplement renvoyer 502 sans faire crasher le routeur.");
    }

    @Test
    public void testRouteCommandWithoutArgs() {
        // Vérifier que les commandes sans arguments (ex: PWD, PASV) ne causent pas d'erreur d'index (
        CommandRouter router = new CommandRouter();
        ClientSession session = new ClientSession(new Socket());

        // PWD n'a pas d'arguments. Si le split(" ", 2) est mal fait, ça pourrait crasher.
        assertDoesNotThrow(() -> router.route(session, "PWD"),
                "Une commande sans argument doit être traitée sans générer d'erreur de tableau.");
    }
}