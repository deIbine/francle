package fr.francle.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Le message sous la saisie, sans fenêtre : le texte habillé en HTML, et sa hauteur sur une ou deux lignes
class MessageTest {

    @Test
    @DisplayName("Le texte est habillé en HTML, centré ; un texte vide garde sa ligne")
    void habillage() {
        Message message = new Message();

        message.setText("Bravo !");
        assertEquals("<html><div style='text-align: center'>Bravo !</div></html>", message.getText());

        message.setText("");
        assertEquals(" ", message.getText());
    }

    @Test
    @DisplayName("Une saisie du joueur avec « < » ou « & » reste du texte, pas du HTML")
    void echapper() {
        assertEquals("« &lt;b&gt;A &amp; B » : réessayez.", Message.echapper("« <b>A & B » : réessayez."));
    }

    @Test
    @DisplayName("Trop long pour sa largeur : le message prend deux lignes de haut")
    void deuxLignes() {
        Message message = new Message();
        message.setText("Bravo !");
        message.setSize(300, 20);
        int uneLigne = message.getPreferredSize().height;

        message.setText("Grand Est : une région d'après 2016, qui regroupe Alsace, Champagne-Ardenne et Lorraine.");

        assertTrue(message.getPreferredSize().height > uneLigne * 3 / 2,
                "hauteur " + message.getPreferredSize().height + " pour une ligne de " + uneLigne);
    }
}
