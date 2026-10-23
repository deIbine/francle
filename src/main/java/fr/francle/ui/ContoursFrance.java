package fr.francle.ui;

import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;
import fr.francle.donnees.Zone;

import java.awt.geom.Path2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tools.jackson.databind.json.JsonMapper;

/*
Les contours de la carte de France de la fin de partie, lus dans resources/france.json (écrit par
outils/generer_images.py). Deux sortes de régions :
    metropole  les régions de métropole, nouvelles ET anciennes, toutes dans le même repère (largeur × hauteur
               unités, y vers le bas) : elles se dessinent ensemble, à leur place. Le fichier en contient 30 ; une
               partie n'en montre qu'une moitié, celles de son mode (regions)
    outreMer   les cinq régions d'outre-mer, chacune dans son propre repère : un encart chacune, à son échelle
Le même principe que Contours (les départements du bonus), à une différence près : ici, ce sont des régions, et la
carte est la même pour toutes les parties. Le fichier n'est donc lu qu'une fois, au premier appel de de().
 */
record ContoursFrance(double largeur, double hauteur, Map<RegionFr, Path2D> metropole, Map<RegionFr, Encart> outreMer) {

    // une région d'outre-mer : sa forme, et le rectangle de son propre repère
    record Encart(double largeur, double hauteur, Path2D forme) {}

    // la forme du fichier JSON (les tracés à plat, comme ceux des départements)
    private record Fichier(int largeur, int hauteur, List<Trace> regions, List<TraceEncart> outreMer) {}
    private record Trace(String region, List<int[]> contours) {}
    private record TraceEncart(String region, int largeur, int hauteur, List<int[]> contours) {}

    private static final String CHEMIN = "/france.json";

    // « holder » : la classe imbriquée n'est chargée, et le fichier lu, qu'au premier appel de de(). La JVM garantit
    // qu'un chargement de classe ne se fait qu'une fois, même depuis deux threads : pas besoin de synchronized
    private static final class Lu {
        static final ContoursFrance CONTOURS = lire();
    }

    static ContoursFrance de() {
        return Lu.CONTOURS;
    }

    // les régions de métropole de ce mode, dans l'ordre du fichier : la France que l'on dessine pour une partie
    Map<RegionFr, Path2D> regions(ModeRegions mode) {
        Map<RegionFr, Path2D> choisies = new LinkedHashMap<>();
        List<RegionFr> duMode = RegionFr.tirables(mode);
        metropole.forEach((region, forme) -> {
            if (duMode.contains(region)) {
                choisies.put(region, forme);
            }
        });
        return Collections.unmodifiableMap(choisies);
    }

    private static ContoursFrance lire() {
        try (InputStream flux = ContoursFrance.class.getResourceAsStream(CHEMIN)) {
            if (flux == null) {
                throw new IllegalStateException("Fichier introuvable dans les ressources : " + CHEMIN);
            }
            Fichier lu = JsonMapper.builder().build().readValue(flux, Fichier.class);
            // LinkedHashMap : l'ordre du fichier est gardé, donc toujours le même ordre de dessin
            Map<RegionFr, Path2D> metropole = new LinkedHashMap<>();
            for (Trace trace : lu.regions()) {
                metropole.put(region(trace.region(), Zone.METROPOLE), Contours.forme(trace.contours()));
            }
            Map<RegionFr, Encart> outreMer = new LinkedHashMap<>();
            for (TraceEncart trace : lu.outreMer()) {
                outreMer.put(region(trace.region(), Zone.OUTRE_MER),
                        new Encart(trace.largeur(), trace.hauteur(), Contours.forme(trace.contours())));
            }
            return new ContoursFrance(lu.largeur(), lu.hauteur(), Collections.unmodifiableMap(metropole),
                    Collections.unmodifiableMap(outreMer));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // la région d'après son nom de fichier (« bretagne ») ; une région inconnue, ou dans la mauvaise partie du
    // fichier, est une erreur du fichier : mieux vaut qu'elle se voie tout de suite
    private static RegionFr region(String fichier, Zone zone) {
        return Arrays.stream(RegionFr.values())
                .filter(region -> region.getFichier().equals(fichier) && region.getZone() == zone)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Région inconnue dans " + CHEMIN + " : " + fichier));
    }
}
