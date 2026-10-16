package fr.francle.donnees;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import tools.jackson.databind.json.JsonMapper;

/*
Le découpage de la France en départements, lu une seule fois dans resources/departements/departements.json :
    "departements" : numéro -> nom, pour les 101 départements
    "regions"      : le nom des fichiers d'une région (celui de son image, sans « .png ») -> les numéros de ses
                     départements. Une région inchangée en 2016 (Bretagne…) n'y est qu'une fois : un seul fichier.

Le fichier est rangé dans le .jar : s'il manque ou ne se lit pas, le jeu est mal construit. Ce n'est pas une erreur
du joueur, à qui l'on pourrait demander de réessayer : on s'arrête net (IllegalStateException), et les tests le voient.
 */
final class Decoupage {

    // la forme du fichier : Jackson remplit directement ce record (les noms des champs sont ceux du JSON)
    private record Fichier(Map<String, String> departements, Map<String, List<String>> regions) {}

    // l'idiome du « holder » : la classe Charge n'est chargée par Java (et le fichier lu) qu'au premier appel d'une
    // méthode qui s'en sert, pas au démarrage du jeu. Et Java garantit que cela n'arrive qu'une fois, même si deux fils
    // d'exécution le demandaient en même temps
    private static final class Charge {
        private static final Map<String, Departement> PAR_CODE = new LinkedHashMap<>();
        private static final Map<String, List<Departement>> PAR_REGION = new HashMap<>();

        static {
            Fichier fichier = lire();
            fichier.departements().forEach((code, nom) -> PAR_CODE.put(code, new Departement(code, nom)));
            fichier.regions().forEach((region, codes) -> {
                List<Departement> liste = new ArrayList<>();
                for (String code : codes) {
                    Departement departement = PAR_CODE.get(code);
                    if (departement == null) {
                        throw new IllegalStateException("Département inconnu " + code + " dans la région " + region);
                    }
                    liste.add(departement);
                }
                PAR_REGION.put(region, List.copyOf(liste));
            });
        }
    }

    static final String CHEMIN = "/departements/departements.json";

    private Decoupage() {}

    static List<Departement> tous() {
        return List.copyOf(Charge.PAR_CODE.values());
    }

    static Optional<Departement> parCode(String code) {
        return Optional.ofNullable(Charge.PAR_CODE.get(code));
    }

    // les départements d'une région, dans l'ordre du fichier (celui des numéros)
    static List<Departement> de(RegionFr region) {
        List<Departement> liste = Charge.PAR_REGION.get(region.getFichier());
        if (liste == null) {
            throw new IllegalStateException("Pas de départements pour " + region + " dans " + CHEMIN);
        }
        return liste;
    }

    private static Fichier lire() {
        try (InputStream flux = Decoupage.class.getResourceAsStream(CHEMIN)) {
            if (flux == null) {
                throw new IllegalStateException("Fichier introuvable dans les ressources : " + CHEMIN);
            }
            return JsonMapper.builder().build().readValue(flux, Fichier.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
