package fr.francle.ui;

import fr.francle.donnees.Departement;
import fr.francle.donnees.RegionFr;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import tools.jackson.databind.json.JsonMapper;

/*
Les contours des départements d'une région, lus dans resources/departements/<région>.json (écrits par
outils/generer_images.py). Les coordonnées sont celles du fichier : un rectangle de largeur × hauteur unités, y vers
le bas comme à l'écran. CarteDepartements les ajuste ensuite à la taille du composant.

Chaque département devient un Path2D : une forme faite de plusieurs tracés fermés (ses îles, ses trous). La règle
« pair-impair » (WIND_EVEN_ODD) dit qu'un point est dans la forme s'il est entouré par un nombre impair de tracés :
un point de l'enclave des Papes est dans le tracé extérieur de la Drôme ET dans son trou, donc deux fois, donc pas
dans la Drôme. C'est cette règle qu'appliquent à la fois le remplissage (fill) et le test du clic (contains).
 */
record Contours(double largeur, double hauteur, Map<Departement, Path2D> formes, Optional<Rectangle2D> zoom) {

    // la forme du fichier JSON. contours : une liste de tracés, chacun « x0, y0, x1, y1… » à plat
    private record Fichier(int largeur, int hauteur, int[] zoom, List<Trace> departements) {}
    private record Trace(String code, List<int[]> contours) {}

    // chaque fichier n'est lu qu'une fois : on garde les contours déjà lus
    private static final Map<String, Contours> DEJA_LUS = new ConcurrentHashMap<>();
    private static final JsonMapper JSON = JsonMapper.builder().build();

    static Contours de(RegionFr region) {
        return DEJA_LUS.computeIfAbsent(region.getFichier(), Contours::lire);
    }

    private static Contours lire(String fichier) {
        String chemin = "/departements/" + fichier + ".json";
        try (InputStream flux = Contours.class.getResourceAsStream(chemin)) {
            if (flux == null) {
                throw new IllegalStateException("Fichier introuvable dans les ressources : " + chemin);
            }
            Fichier lu = JSON.readValue(flux, Fichier.class);
            // LinkedHashMap : l'ordre du fichier est gardé, donc toujours le même ordre de dessin
            Map<Departement, Path2D> formes = new LinkedHashMap<>();
            for (Trace trace : lu.departements()) {
                Departement departement = Departement.parCode(trace.code())
                        .orElseThrow(() -> new IllegalStateException("Département inconnu dans " + chemin + " : " + trace.code()));
                formes.put(departement, forme(trace.contours()));
            }
            Optional<Rectangle2D> zoom = Optional.ofNullable(lu.zoom())
                    .map(z -> new Rectangle2D.Double(z[0], z[1], z[2], z[3]));
            // unmodifiableMap et non Map.copyOf : Map.copyOf ne garde pas l'ordre
            return new Contours(lu.largeur(), lu.hauteur(), Collections.unmodifiableMap(formes), zoom);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // les tracés à plat (x0, y0, x1, y1…) -> une forme : moveTo pour le premier point d'un tracé, lineTo pour les
    // suivants, closePath pour revenir au premier
    static Path2D forme(List<int[]> traces) {
        Path2D forme = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        for (int[] trace : traces) {
            forme.moveTo(trace[0], trace[1]);
            for (int i = 2; i + 1 < trace.length; i += 2) {
                forme.lineTo(trace[i], trace[i + 1]);
            }
            forme.closePath();
        }
        return forme;
    }
}
