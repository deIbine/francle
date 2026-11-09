package fr.francle.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

// Le rapport de contraste des règles d'accessibilité (WCAG 2)
class ContrasteTest {

    @Test
    @DisplayName("De 1:1 pour la même couleur à 21:1 pour noir sur blanc, dans n'importe quel ordre")
    void bornes() {
        assertEquals(21, Contraste.rapport(Color.BLACK, Color.WHITE), 1e-9);
        assertEquals(21, Contraste.rapport(Color.WHITE, Color.BLACK), 1e-9);
        assertEquals(1, Contraste.rapport(Color.GRAY, Color.GRAY), 1e-9);
    }

    @Test
    @DisplayName("Des valeurs connues : #767676 sur blanc fait 4,54:1, #3C628C sur #3F6690 1,06:1")
    void valeursConnues() {
        // #767676, le gris le plus clair qui se lit encore sur blanc (une valeur souvent citée)
        assertEquals(4.54, Contraste.rapport(new Color(0x767676), Color.WHITE), 0.01);
        // la couleur du focus de FlatLaf sur un département, en thème sombre : le défaut de la phase 20
        assertEquals(1.06, Contraste.rapport(new Color(0x3C628C), new Color(0x3F6690)), 0.01);
    }
}
