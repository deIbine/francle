package fr.francle.ui;

import fr.francle.donnees.Langue;
import fr.francle.stats.Verrou;

import java.io.IOException;
import java.util.Optional;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Francle {

    // le verrou d'un seul jeu ouvert, gardé jusqu'à la fermeture : un champ static, pour que rien ne le relâche avant
    private static Verrou verrou;

    public static void main(String[] args) {
        // Swing n'est pas fait pour être utilisé depuis plusieurs fils d'exécution (threads) : tout ce qui touche à
        // l'interface doit se faire sur un seul, l'Event Dispatch Thread. main() tourne sur un autre fil :
        // invokeLater confie donc la création de la fenêtre à l'EDT, qui l'exécutera dès qu'il sera prêt.
        SwingUtilities.invokeLater(() -> {
            // le thème (Look and Feel) s'installe avant de créer le moindre composant. Le clair d'abord : la fenêtre
            // passera au sombre elle-même si le joueur l'a choisi (elle seule lit les réglages)
            Theme.installer(false);
            if (!verrouiller()) {
                // déjà ouvert : rien n'a été créé, le programme s'arrête de lui-même une fois le message fermé
                JOptionPane.showMessageDialog(null, deja().texte("dejaOuvert"), "Francle", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            new Fenetre().afficher();
        });
    }

    // vrai si ce jeu est le seul ouvert. Un verrou impossible à créer (dossier en lecture seule…) n'empêche pas de
    // jouer : la Sauvegarde dira elle-même, au premier enregistrement, que le fichier ne s'écrit pas
    private static boolean verrouiller() {
        try {
            Optional<Verrou> pris = Verrou.prendre(Verrou.FICHIER);
            pris.ifPresent(v -> verrou = v);
            return pris.isPresent();
        } catch (IOException e) {
            return true;
        }
    }

    // le message dans la langue du système, sans lire les réglages : les charger mettrait de côté un fichier illisible,
    // alors que c'est l'autre jeu, déjà ouvert, qui s'en sert
    private static Textes deja() {
        return new Textes(Langue.duSysteme());
    }
}
