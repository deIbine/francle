package fr.francle.jeu;

import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/*
La région du jour : la même pour tout le monde, calculée à partir de la seule date, sans réseau ni fichier.
Deux machines qui ne se parlent pas doivent trouver la même réponse : le calcul ne dépend que de la date et des noms
des régions, jamais de l'ordre des lignes de l'enum ni de l'heure.

Les jours sont groupés en cycles de N jours (N = nombre de régions) : à chaque cycle, les régions sont mélangées avec
le numéro du cycle pour graine, et le jour k du cycle reçoit la k-ième. Chaque région revient donc exactement une fois
par cycle, et jamais deux jours de suite.
 */
public final class RegionDuJour {

    // le jour de la partie n° 1
    public static final LocalDate PREMIER_JOUR = LocalDate.of(2026, 10, 13);
    // la partie du jour se joue toujours avec les nouvelles régions, quels que soient les réglages : la même pour tous
    public static final ModeRegions MODE = ModeRegions.NOUVELLES;

    private RegionDuJour() {}

    // le numéro de la partie, à la Wordle : 1 le premier jour, 2 le lendemain… (0 ou négatif avant le premier jour)
    public static long numero(LocalDate date) {
        return ChronoUnit.DAYS.between(PREMIER_JOUR, date) + 1;
    }

    public static RegionFr pour(LocalDate date) {
        long jour = numero(date) - 1;          // 0 le premier jour
        int taille = REGIONS.size();
        // floorDiv et floorMod, et non / et % : avant le premier jour, jour est négatif, et -1 % 18 vaut -1 en Java
        // (un index invalide), alors que Math.floorMod(-1, 18) vaut 17 (le dernier jour du cycle précédent)
        long cycle = Math.floorDiv(jour, taille);
        int position = (int) Math.floorMod(jour, taille);
        return ordre(cycle).get(position);
    }

    // l'angle (0 à 359 degrés) de la silhouette tournée, pour la partie du jour : le même pour tout le monde, et le
    // même après une relance du jeu. Comme la région, il ne dépend que de la date : un Random dont on fixe la graine
    // donne toujours la même suite de nombres. Un autre sel que celui des régions : sinon l'angle et le mélange
    // viendraient de la même suite, et l'un trahirait peut-être quelque chose de l'autre
    public static int angle(LocalDate date) {
        return new Random(SEL_ANGLE + numero(date)).nextInt(360);
    }

    // les octets ASCII de « Angle »
    private static final long SEL_ANGLE = 0x416E676C65L;

    // l'ordre des régions pendant un cycle
    static List<RegionFr> ordre(long cycle) {
        List<RegionFr> ordre = melange(cycle);
        // le dernier jour du cycle précédent et le premier de celui-ci ne doivent pas tomber sur la même région :
        // on échange alors les deux premières. (Cet échange ne touche jamais la DERNIÈRE région d'un cycle : il suffit
        // donc de regarder le mélange brut du cycle précédent, sans remonter plus loin.)
        List<RegionFr> precedent = melange(cycle - 1);
        if (ordre.get(0) == precedent.get(precedent.size() - 1)) {
            Collections.swap(ordre, 0, 1);
        }
        return ordre;
    }

    // ajouté à la graine : les octets ASCII de « Francle ». Avec la graine 0 seule, le premier cycle commençait
    // par les deux premières régions de l'ordre alphabétique, un motif qu'un joueur aurait pu croire voulu
    private static final long SEL = 0x4672616E636C65L;

    private static List<RegionFr> melange(long cycle) {
        // une copie : shuffle modifie la liste qu'on lui donne, et REGIONS doit rester intacte
        List<RegionFr> ordre = new ArrayList<>(REGIONS);
        // Random et Collections.shuffle sont décrits précisément par la documentation de Java : avec la même graine,
        // ils donnent le même mélange sur toutes les machines et toutes les versions
        Collections.shuffle(ordre, new Random(SEL + cycle));
        return ordre;
    }

    // les régions de la partie du jour, dans un ordre qui ne dépend que de leurs noms. Calculé une seule fois :
    // le tri appelle getCle(), qui refait toute la simplification du nom à chaque comparaison.
    // List.copyOf : une liste non modifiable, personne ne pourra la mélanger par erreur
    private static final List<RegionFr> REGIONS = List.copyOf(trier(RegionFr.tirables(MODE)));

    private static List<RegionFr> trier(List<RegionFr> regions) {
        List<RegionFr> triees = new ArrayList<>(regions);
        triees.sort(Comparator.comparing(RegionFr::getCle));
        return triees;
    }
}
