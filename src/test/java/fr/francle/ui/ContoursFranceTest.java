package fr.francle.ui;

import fr.francle.donnees.Epoque;
import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;
import fr.francle.donnees.Zone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

// Les contours de la carte de France de la fin de partie (resources/france.json)
class ContoursFranceTest {

    private static final ContoursFrance FRANCE = ContoursFrance.de();

    private static Set<RegionFr> regionsDe(Zone zone) {
        return Arrays.stream(RegionFr.values()).filter(region -> region.getZone() == zone).collect(Collectors.toSet());
    }

    @Test
    @DisplayName("Chaque région de métropole a sa forme, chaque région d'outre-mer son encart, et rien d'autre")
    void toutesLesRegions() {
        assertEquals(regionsDe(Zone.METROPOLE), FRANCE.metropole().keySet());
        assertEquals(regionsDe(Zone.OUTRE_MER), FRANCE.outreMer().keySet());
    }

    @Test
    @DisplayName("Chaque forme tient dans le rectangle annoncé par le fichier (à l'arrondi près)")
    void dansLeCadre() {
        FRANCE.metropole().forEach((region, forme) ->
                assertDans(forme, FRANCE.largeur(), FRANCE.hauteur(), region));
        FRANCE.outreMer().forEach((region, encart) ->
                assertDans(encart.forme(), encart.largeur(), encart.hauteur(), region));
    }

    private static void assertDans(Path2D forme, double largeur, double hauteur, RegionFr region) {
        Rectangle2D cadre = forme.getBounds2D();
        assertFalse(cadre.isEmpty(), region.name());
        assertTrue(cadre.getMinX() >= -1 && cadre.getMaxX() <= largeur + 1, region.name());
        assertTrue(cadre.getMinY() >= -1 && cadre.getMaxY() <= hauteur + 1, region.name());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(ModeRegions.class)
    @DisplayName("Une partie montre les régions de métropole de son mode : 13 nouvelles, ou 22 anciennes")
    void regionsDuMode(ModeRegions mode) {
        List<RegionFr> attendues = RegionFr.tirables(mode).stream()
                .filter(region -> region.getZone() == Zone.METROPOLE).toList();

        assertEquals(Set.copyOf(attendues), FRANCE.regions(mode).keySet());
        assertEquals(mode == ModeRegions.NOUVELLES ? 13 : 22, attendues.size());
    }

    // des points répartis sur toute la carte, tous les 8 unités (environ 60 000)
    private static final int PAS = 8;

    @ParameterizedTest(name = "{0}")
    @EnumSource(ModeRegions.class)
    @DisplayName("Les régions d'un même mode ne se chevauchent pas (à quelques points près, sur leurs bords)")
    void sansChevauchement(ModeRegions mode) {
        Map<RegionFr, Path2D> regions = FRANCE.regions(mode);
        int dedans = 0;
        int enDouble = 0;
        for (int y = 0; y < FRANCE.hauteur(); y += PAS) {
            for (int x = 0; x < FRANCE.largeur(); x += PAS) {
                int px = x;
                int py = y;
                long combien = regions.values().stream().filter(forme -> forme.contains(px, py)).count();
                if (combien > 0) {
                    dedans++;
                }
                if (combien > 1) {
                    enDouble++;
                }
            }
        }
        // les régions sont simplifiées chacune de son côté : leurs bords communs ne tombent pas exactement au même
        // endroit, d'où quelques points comptés deux fois, jamais plus d'un sur deux cents
        assertTrue(dedans > 10_000, "la carte doit être remplie : " + dedans);
        assertTrue(enDouble * 200 < dedans, enDouble + " points dans deux régions sur " + dedans);
    }

    static List<RegionFr> anciennes() {
        return Arrays.stream(RegionFr.values()).filter(region -> region.getEpoque() == Epoque.ANCIENNE).toList();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("anciennes")
    @DisplayName("Une ancienne région est dessinée à l'intérieur de la nouvelle qui la contient (vu par les départements)")
    void anciennesDansLesNouvelles(RegionFr region) {
        // le lien entre les époques vient des départements (RegionFr.autreEpoque), et non des contours : le test
        // vérifie que les deux sources disent la même chose
        RegionFr nouvelle = region.autreEpoque().get(0);
        Path2D ancienne = FRANCE.metropole().get(region);
        Path2D contenante = FRANCE.metropole().get(nouvelle);
        int dedans = 0;
        int aussiDansLaNouvelle = 0;
        for (int y = 0; y < FRANCE.hauteur(); y += PAS) {
            for (int x = 0; x < FRANCE.largeur(); x += PAS) {
                if (ancienne.contains(x, y)) {
                    dedans++;
                    if (contenante.contains(x, y)) {
                        aussiDansLaNouvelle++;
                    }
                }
            }
        }
        assertTrue(dedans > 0, region.name());
        assertTrue(aussiDansLaNouvelle * 100 >= dedans * 98,
                region.name() + " : " + aussiDansLaNouvelle + " points sur " + dedans + " dans " + nouvelle.name());
    }
}
