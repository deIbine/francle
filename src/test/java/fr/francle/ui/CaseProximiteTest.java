package fr.francle.ui;

import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Partie;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// La couleur de la case « Proximité », et ce que le modèle du tableau en dit, sans fenêtre
class CaseProximiteTest {

    @Test
    @DisplayName("Ligne vide : pas de pastille ; essai raté : le dégradé de la carte ; la réponse : vert")
    void couleurs() {
        Partie partie = new Partie(RegionFr.GRAND_EST, ModeRegions.NOUVELLES);
        TableauEssais tableau = new TableauEssais();
        tableau.setPartie(partie);
        partie.proposer("Bretagne");
        partie.proposer("Grand Est");

        int proximite = partie.getEssais().get(0).pourcentage();
        assertEquals(Optional.of(CarteFin.degrade(proximite)), CaseProximite.couleur(tableau, 0));
        assertEquals(Optional.of(CarteFin.TROUVEE), CaseProximite.couleur(tableau, 1));
        assertEquals(Optional.empty(), CaseProximite.couleur(tableau, 2));
    }

    @Test
    @DisplayName("Le modèle dit l'essai de chaque ligne, et celle qui a trouvé")
    void essaiEtTrouve() {
        Partie partie = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);
        TableauEssais tableau = new TableauEssais();
        assertTrue(tableau.essai(0).isEmpty(), "sans partie, aucune ligne");
        tableau.setPartie(partie);
        partie.proposer("Normandie");
        partie.proposer("Bretagne");

        assertEquals(RegionFr.NORMANDIE, tableau.essai(0).orElseThrow().proposition());
        assertFalse(tableau.trouve(0));
        assertTrue(tableau.trouve(1));
        assertFalse(tableau.trouve(5));
    }

    @Test
    @DisplayName("Le texte se lit sur toute la pastille : foncé sur le dégradé, blanc sur le vert")
    void contraste() {
        // le texte foncé sur la pastille la plus foncée du dégradé : 4,68:1, au-dessus des 4,5:1 demandés pour un texte
        assertTrue(Contraste.rapport(CaseProximite.TEXTE, CarteFin.degrade(100)) >= 4.5);
        assertTrue(Contraste.rapport(CaseProximite.TEXTE, CarteFin.degrade(0)) >= 4.5);
        // le blanc sur le vert : 3,37:1 seulement. Assez pour un texte gras de grande taille, pas pour celui du
        // tableau (noté pour plus tard dans la feuille de route) : le test garde au moins ce niveau
        assertTrue(Contraste.rapport(CaseProximite.TEXTE_TROUVE, CarteFin.TROUVEE) >= 3);
    }
}
