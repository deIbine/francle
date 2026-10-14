package fr.francle.ui;

import javax.swing.SwingUtilities;

public class Francle {

    public static void main(String[] args) {
        // Swing n'est pas fait pour être utilisé depuis plusieurs fils d'exécution (threads) : tout ce qui touche à
        // l'interface doit se faire sur un seul, l'Event Dispatch Thread. main() tourne sur un autre fil :
        // invokeLater confie donc la création de la fenêtre à l'EDT, qui l'exécutera dès qu'il sera prêt.
        SwingUtilities.invokeLater(() -> {
            // le thème (Look and Feel) s'installe avant de créer le moindre composant. Le clair d'abord : la fenêtre
            // passera au sombre elle-même si le joueur l'a choisi (elle seule lit les réglages)
            Theme.installer(false);
            new Fenetre().afficher();
        });
    }
}
