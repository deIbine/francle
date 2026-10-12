import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Les textes construits par l'affichage, sans ouvrir de popup
class MessageTest {

    @Test
    @DisplayName("Séparateur de milliers : espace insécable U+00A0")
    void avecMilliers() {
        assertEquals("27 208", Message.avecmilliers(27208));
        assertEquals("374", Message.avecmilliers(374));
        assertEquals("0", Message.avecmilliers(0));
    }

    @Test
    @DisplayName("La liste des régions est triée à la française (Île-de-France à la lettre I)")
    void listeTriee() {
        List<String> noms = List.of(Message.listedesregions(ModeRegions.NOUVELLES).split("\n"));

        assertEquals(18, noms.size());
        assertEquals("Auvergne-Rhône-Alpes", noms.get(0));
        assertTrue(noms.indexOf("Hauts-de-France") < noms.indexOf("Île-de-France"));
        assertTrue(noms.indexOf("Île-de-France") < noms.indexOf("La Réunion"));
    }

    @Test
    @DisplayName("Historique d'une partie neuve : 5 lignes vides")
    void historiqueVide() {
        String historique = Message.historique(new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES));

        assertEquals(5, historique.split("\n").length);
        assertTrue(historique.startsWith("Supposition 1 :   km 0% Superficie :  km² \n"));
        assertTrue(historique.endsWith("Supposition 5 :   km 0% \n"));
    }

    @Test
    @DisplayName("Historique : un essai raté révèle son indice, la ligne gagnante n'en a pas")
    void historiqueAvecEssais() {
        Partie partie = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);
        partie.proposer("Corse");
        partie.proposer("Normandie");
        partie.proposer("Bretagne");

        String[] lignes = Message.historique(partie).split("\n");

        assertTrue(lignes[0].startsWith("Supposition 1 : Corse "));
        assertTrue(lignes[0].endsWith("% Superficie : 27 208 km² "));
        assertTrue(lignes[1].startsWith("Supposition 2 : Normandie "));
        assertTrue(lignes[1].endsWith("% Premier indice : la crêpe"));
        assertEquals("Supposition 3 : Bretagne 0 km 100% Deuxième indice : ", lignes[2]);
    }
}
