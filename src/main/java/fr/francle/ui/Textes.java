package fr.francle.ui;

import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Indice;
import fr.francle.jeu.Partie;
import fr.francle.jeu.RegionDuJour;

import java.text.Collator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// les textes affichés par la fenêtre, construits sans rien afficher : on peut donc les tester
final class Textes {

    static final String REGLES = """
            Devinez la région française cachée en 5 essais au plus.

            Partie du jour : la même région pour tout le monde, une seule partie par jour,
            toujours parmi les nouvelles régions. Partie libre : autant que vous voulez,
            avec les régions choisies dans le menu Paramètres.

            Après chaque essai, le tableau indique :
              • la distance entre la capitale de votre proposition et celle de la région cachée ;
              • la direction à suivre pour aller vers la région cachée (↑ nord, → est…) ;
              • le pourcentage de proximité (100 % : vous y êtes).

            Chaque essai raté révèle un indice : la superficie, la spécialité culinaire,
            une personnalité, puis la capitale régionale.

            Les accents, majuscules, espaces et tirets sont ignorés : « ile de france » est accepté.
            Pendant la frappe, des suggestions s'affichent : ↓ et ↑ pour en choisir une, Entrée pour la reprendre,
            puis Entrée encore pour valider (désactivables dans le menu Paramètres).""";

    // des titres courts : avec FlatLaf sous Windows, la barre de menus est intégrée à la barre de titre, et un titre
    // plus long (« Francle — Partie du jour n° 1 · 13 octobre 2026 ») y était coupé
    static final String TITRE_LIBRE = "Francle · partie libre";

    // que des méthodes static : on n'a jamais besoin d'un objet Textes
    private Textes() {}

    // « Francle n° 1 », comme « Wordle 1234 »
    static String titreDuJour(LocalDate date) {
        return "Francle n° " + RegionDuJour.numero(date);
    }

    // 27208 -> "27 208" : séparateur de milliers à la française. On impose l'espace insécable (U+00A0),
    // car l'espace fine (U+202F) du format français de Java s'affiche en carré vide avec certaines polices.
    static String milliers(long nombre) {
        DecimalFormatSymbols symboles = new DecimalFormatSymbols(Locale.FRENCH);
        symboles.setGroupingSeparator(' ');
        return new DecimalFormat("#,##0", symboles).format(nombre);
    }

    // 912.37 -> "912 km" : au kilomètre près, c'est bien assez pour jouer
    static String distance(double kilometres) {
        return milliers(Math.round(kilometres)) + " km";
    }

    static String indice(Indice indice, RegionFr cible) {
        switch (indice) {
            case SUPERFICIE : return "Superficie : " + milliers(cible.getSuperficie()) + " km²";
            case SPECIALITE : return "Spécialité culinaire : " + cible.getSpecialite();
            case PERSONNALITE : return "Personnalité : " + cible.getPersonnalite();
            default : return "Capitale régionale : " + cible.getCapitale();
        }
    }

    // le nom d'un mode de jeu, dans le menu Paramètres
    static String mode(ModeRegions mode) {
        switch (mode) {
            case NOUVELLES : return "Nouvelles régions (depuis 2016)";
            case ANCIENNES : return "Anciennes régions (avant 2016)";
            default : return "Toutes les régions";
        }
    }

    // le message sous la saisie, après un essai raté
    static String apresEssai(Partie partie) {
        int restants = Partie.ESSAIS_MAX - partie.getEssais().size();
        return "C'est raté ! Encore " + restants + (restants == 1 ? " essai." : " essais.");
    }

    static String nonReconnue(String saisie) {
        return "« " + saisie.strip() + " » n'est pas une région reconnue : réessayez.";
    }

    static String fin(Partie partie) {
        switch (partie.getEtat()) {
            case GAGNEE :
                int essais = partie.getEssais().size();
                return "Bravo ! Trouvé en " + essais + (essais == 1 ? " essai." : " essais.");
            case ABANDONNEE : return "Vous avez abandonné. La réponse était " + partie.getCible().getNom() + ".";
            case PERDUE : return "C'est raté ! La réponse était " + partie.getCible().getNom() + ".";
            default : throw new IllegalStateException("La partie n'est pas terminée");
        }
    }

    // les noms des régions jouables, dans l'ordre alphabétique français (Île-de-France rangée à la lettre I)
    static String listeDesRegions(ModeRegions mode) {
        List<String> noms = new ArrayList<>();
        for (RegionFr region : RegionFr.tirables(mode)) {
            noms.add(region.getNom());
        }
        noms.sort(Collator.getInstance(Locale.FRENCH));
        return String.join("\n", noms);
    }
}
