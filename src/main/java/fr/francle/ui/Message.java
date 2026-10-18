package fr.francle.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

/*
Le message sous la saisie : centré, en gras, et sur deux lignes quand il est trop long pour une seule (« Grand Est :
une région d'après 2016, qui regroupe Alsace, Champagne-Ardenne et Lorraine. »). Un JLabel ordinaire n'écrit qu'une
ligne, et coupe le reste par « … ».

Le texte passe donc en HTML, que Swing sait couper en plusieurs lignes. Mais un JLabel calcule sa hauteur comme si le
texte tenait sur une seule ligne, aussi large qu'il le faut : getPreferredSize demande ici la hauteur du texte coupé à
la largeur réelle du message.
 */
final class Message extends JLabel {

    Message() {
        super(" ", SwingConstants.CENTER);
        setFont(getFont().deriveFont(Font.BOLD));
    }

    // le texte tel qu'on l'écrit (« Bravo ! »), sans HTML : c'est ici qu'il est habillé
    @Override
    public void setText(String texte) {
        // un texte vide garde sa ligne (" "), pour que la fenêtre ne saute pas ; « text-align » centre chaque ligne
        // (l'alignement du JLabel, lui, ne place que le bloc tout entier)
        super.setText(texte == null || texte.isBlank()
                ? " "
                : "<html><div style='text-align: center'>" + echapper(texte) + "</div></html>");
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension taille = super.getPreferredSize();
        // la « vue » HTML que Swing a construite pour ce texte (aucune pour " ")
        View vue = (View) getClientProperty(BasicHTML.propertyKey);
        if (vue != null && getWidth() > 0) {
            Insets bords = getInsets();
            // la vue, mise à la largeur du message, dit la hauteur de ses lignes
            vue.setSize(getWidth() - bords.left - bords.right, 0);
            taille.height = (int) Math.ceil(vue.getPreferredSpan(View.Y_AXIS)) + bords.top + bords.bottom;
        }
        return taille;
    }

    // les trois caractères qui ont un sens en HTML : « < » dans une saisie du joueur ne doit pas devenir une balise
    static String echapper(String texte) {
        return texte.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
