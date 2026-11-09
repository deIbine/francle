package fr.francle.ui;

import java.awt.Color;

/*
Le contraste entre deux couleurs, au sens des règles d'accessibilité du web (WCAG 2) : le rapport des luminances
relatives, de 1:1 (la même couleur) à 21:1 (noir sur blanc). Les règles demandent au moins 3:1 pour un repère graphique
(le focus du clavier, un trait qui doit se voir) et 4,5:1 pour un texte de taille normale.

La luminance relative d'une couleur : chaque composante, ramenée entre 0 et 1, est « linéarisée » (l'écran ne les
affiche pas en proportion de leur valeur), puis les trois sont pesées selon la sensibilité de l'œil, qui voit bien
mieux le vert que le bleu.
 */
final class Contraste {

    private Contraste() {
    }

    // le rapport de contraste entre deux couleurs, dans n'importe quel ordre : (la plus claire + 0,05) / (la plus
    // foncée + 0,05)
    static double rapport(Color une, Color autre) {
        double a = luminance(une);
        double b = luminance(autre);
        return (Math.max(a, b) + 0.05) / (Math.min(a, b) + 0.05);
    }

    static double luminance(Color couleur) {
        return 0.2126 * lineaire(couleur.getRed()) + 0.7152 * lineaire(couleur.getGreen())
                + 0.0722 * lineaire(couleur.getBlue());
    }

    private static double lineaire(int composante) {
        double c = composante / 255.0;
        return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }
}
