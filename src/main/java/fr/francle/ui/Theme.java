package fr.francle.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

/*
Le thème de la fenêtre, clair ou sombre, fourni par FlatLaf.

Un « Look and Feel » Swing est une table de couleurs, de polices et de bordures (UIManager) dans laquelle chaque
composant puise au moment où il est créé. Changer de thème à chaud se fait donc en deux temps : installer la
nouvelle table, puis demander à tous les composants déjà créés de s'y réabonner (FlatLaf.updateUI).
C'est pourquoi le jeu ne contient (presque) aucune couleur écrite en dur : une couleur écrite dans le code ne
change pas avec le thème. Les images, elles, sont sur fond transparent : elles vont avec les deux thèmes.
 */
final class Theme {

    private Theme() {}

    // avant de créer le moindre composant (au lancement)
    static void installer(boolean sombre) {
        if (sombre) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
    }

    // pendant que le jeu tourne : toutes les fenêtres ouvertes sont redessinées avec le nouveau thème
    static void changer(boolean sombre) {
        installer(sombre);
        FlatLaf.updateUI();
    }
}
