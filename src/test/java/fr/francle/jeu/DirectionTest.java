package fr.francle.jeu;

import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

// La flèche de direction : d'abord le découpage en 8 secteurs, puis le calcul de l'azimut sur de vraies villes
class DirectionTest {

    @ParameterizedTest(name = "{0}° → {1}")
    @CsvSource({
            "0, NORD", "22.4, NORD", "22.6, NORD_EST", "45, NORD_EST", "90, EST", "135, SUD_EST",
            "180, SUD", "225, SUD_OUEST", "270, OUEST", "315, NORD_OUEST", "337.4, NORD_OUEST", "337.6, NORD", "359.9, NORD"
    })
    @DisplayName("Chaque direction couvre 45°, centrée sur son angle")
    void secteurs(double azimut, Direction attendue) {
        assertEquals(attendue, Direction.depuisAzimut(azimut));
    }

    @Test
    @DisplayName("Rennes → Strasbourg : azimut connu d'avance, 82,32°")
    void azimutConnu() {
        // valeur calculée à part avec une autre méthode (vecteurs en 3D projetés sur le plan tangent à Rennes) :
        // deux calculs indépendants qui tombent d'accord, c'est une vraie vérification
        assertEquals(82.32, Direction.azimut(RegionFr.BRETAGNE, RegionFr.GRAND_EST), 0.01);
    }

    @ParameterizedTest(name = "{0} → {1} : {2}")
    @CsvSource({
            "BRETAGNE, GRAND_EST, EST",
            "GRAND_EST, BRETAGNE, OUEST",
            "PROVENCE_ALPES_COTE_D_AZUR, HAUTS_DE_FRANCE, NORD",
            "HAUTS_DE_FRANCE, PROVENCE_ALPES_COTE_D_AZUR, SUD",
            "PAYS_DE_LA_LOIRE, HAUTS_DE_FRANCE, NORD_EST",
            "ILE_DE_FRANCE, CORSE, SUD_EST",
            "GRAND_EST, NOUVELLE_AQUITAINE, SUD_OUEST",
            "PROVENCE_ALPES_COTE_D_AZUR, BRETAGNE, NORD_OUEST"
    })
    @DisplayName("La flèche pointe de la proposition vers la cible")
    void directionsEntreRegions(RegionFr depuis, RegionFr vers, Direction attendue) {
        assertEquals(attendue, Direction.entre(depuis, vers));
    }

    @Test
    @DisplayName("Deux régions à la même capitale (Lille) : pas de direction")
    void surPlace() {
        assertEquals(Direction.SUR_PLACE, Direction.entre(RegionFr.NORD_PAS_DE_CALAIS, RegionFr.HAUTS_DE_FRANCE));
        assertEquals(Direction.SUR_PLACE, Direction.entre(RegionFr.BRETAGNE, RegionFr.BRETAGNE));
    }

    @Test
    @DisplayName("Chaque essai joué porte la direction vers la cible")
    void essaiAvecDirection() {
        Partie partie = new Partie(RegionFr.GRAND_EST, ModeRegions.NOUVELLES);

        Essai essai = assertInstanceOf(Reponse.Jouee.class, partie.proposer("Bretagne")).essai();

        assertEquals(Direction.EST, essai.direction());
        assertEquals("→", essai.direction().getSymbole());
    }
}
