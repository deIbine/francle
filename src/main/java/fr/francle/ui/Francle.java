package fr.francle.ui;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.SwingUtilities;

public class Francle {

    public static void main(String[] args) {
        // Swing n'est pas fait pour être utilisé depuis plusieurs fils d'exécution (threads) : tout ce qui touche à
        // l'interface doit se faire sur un seul, l'Event Dispatch Thread. main() tourne sur un autre fil :
        // invokeLater confie donc la création de la fenêtre à l'EDT, qui l'exécutera dès qu'il sera prêt.
        SwingUtilities.invokeLater(() -> {
            // le thème (Look and Feel) se choisit avant de créer le moindre composant
            FlatLightLaf.setup();
            new Fenetre().afficher();
        });
    }
}
