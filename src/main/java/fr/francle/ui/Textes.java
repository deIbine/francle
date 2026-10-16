package fr.francle.ui;

import fr.francle.donnees.Departement;
import fr.francle.donnees.Langue;
import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Bonus;
import fr.francle.jeu.Indice;
import fr.francle.jeu.Partie;
import fr.francle.jeu.RegionDuJour;
import fr.francle.jeu.Tentative;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/*
Les textes affichés par la fenêtre, dans une langue, construits sans rien afficher : on peut donc les tester.

Les phrases elles-mêmes ne sont pas ici : elles sont dans resources/textes/textes_fr.properties et textes_en.properties,
un fichier par langue avec les mêmes clés. C'est le mécanisme standard de Java pour traduire une application
(ResourceBundle) : ajouter une langue, c'est ajouter un fichier, sans toucher au code. Ici restent les règles
d'écriture qui ne sont pas de simples phrases : nombres, unités, pluriels, ordre alphabétique.
 */
final class Textes {

    // le mile international, défini exactement en mètres depuis 1959. Un mile carré vaut donc 1,609344² km²
    static final double KM_PAR_MILE = 1.609344;

    private final Langue langue;
    private final ResourceBundle phrases;
    private final String aide;

    Textes(Langue langue) {
        this.langue = langue;
        // getNoFallbackControl : sans lui, si un fichier manquait, Java se rabattrait en silence sur la langue du
        // système ; on préfère une erreur franche, qu'un test attrape tout de suite
        this.phrases = ResourceBundle.getBundle("textes.textes", langue.getLocale(),
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
        this.aide = lire("/textes/aide_" + langue.getLocale().getLanguage() + ".txt");
    }

    Langue getLangue() { return langue; }

    // la phrase de cette clé, dans la langue, où %s et %d sont remplacés par les valeurs données
    String texte(String cle, Object... valeurs) {
        return String.format(langue.getLocale(), phrases.getString(cle), valeurs);
    }

    // toutes les clés du fichier : sert au test qui vérifie que les deux langues ont les mêmes
    Set<String> cles() {
        return phrases.keySet();
    }

    String aide() {
        return aide;
    }

    // « Francle n° 1 » ou « Francle #1 », comme « Wordle 1234 ». Des titres courts : avec FlatLaf sous Windows, la
    // barre de menus est intégrée à la barre de titre, et un titre plus long y était coupé
    String titreDuJour(LocalDate date) {
        return texte("titre.duJour", RegionDuJour.numero(date));
    }

    // 27208 -> "27 208" en français, "27,208" en anglais. En français, on impose l'espace insécable (U+00A0), car
    // l'espace fine (U+202F) du format français de Java s'affiche en carré vide avec certaines polices
    String milliers(long nombre) {
        DecimalFormatSymbols symboles = new DecimalFormatSymbols(langue.getLocale());
        if (langue == Langue.FR) {
            symboles.setGroupingSeparator(' ');
        }
        return new DecimalFormat("#,##0", symboles).format(nombre);
    }

    // 912.37 -> "912 km" ou "567 mi" : à l'unité près, c'est bien assez pour jouer. La distance arrive toujours en km
    // (c'est l'unité de tout le jeu) ; on ne convertit qu'ici, au dernier moment, pour l'afficher
    String distance(double kilometres, boolean unitesAnglaises) {
        return unitesAnglaises
                ? milliers(Math.round(kilometres / KM_PAR_MILE)) + " mi"
                : milliers(Math.round(kilometres)) + " km";
    }

    // 27208 -> "27 208 km²" ou "10 505 mi²"
    String superficie(int kilometresCarres, boolean unitesAnglaises) {
        return unitesAnglaises
                ? milliers(Math.round(kilometresCarres / (KM_PAR_MILE * KM_PAR_MILE))) + " mi²"
                : milliers(kilometresCarres) + " km²";
    }

    // « 03:07:09 » : une durée en heures, minutes, secondes, jamais négative (le compte à rebours)
    String duree(Duration duree) {
        long secondes = Math.max(0, duree.toSeconds());
        return String.format("%02d:%02d:%02d", secondes / 3600, secondes / 60 % 60, secondes % 60);
    }

    // « 76 % » en français, « 76% » en anglais
    String pourcentage(int pourcentage) {
        return texte("pourcentage", pourcentage);
    }

    String indice(Indice indice, RegionFr cible, boolean unitesAnglaises) {
        String valeur = switch (indice) {
            case SUPERFICIE -> superficie(cible.getSuperficie(), unitesAnglaises);
            case SPECIALITE -> cible.getSpecialite().dans(langue);
            case PERSONNALITE -> cible.getPersonnalite().dans(langue);
            case CAPITALE -> cible.getCapitale();
            case MONUMENT -> cible.getMonument().dans(langue);
        };
        // la clé porte le nom de l'indice : « indice.SUPERFICIE »…
        return texte("indice." + indice.name(), valeur);
    }

    // le nom d'un mode de jeu, dans le menu Paramètres
    String mode(ModeRegions mode) {
        return texte("mode." + mode.name());
    }

    String nom(RegionFr region) {
        return region.getNom(langue);
    }

    // le message sous la saisie, avant le premier essai ou après un essai raté
    String apresEssai(Partie partie) {
        if (partie.getEssais().isEmpty()) {
            return texte("question");
        }
        int restants = Partie.ESSAIS_MAX - partie.getEssais().size();
        // le pluriel n'est pas une règle universelle (certaines langues en ont plusieurs) : chaque langue a une
        // phrase pour 1 et une pour le reste, plutôt qu'un « s » ajouté par le code
        return restants == 1 ? texte("apresEssai.un") : texte("apresEssai.plusieurs", restants);
    }

    String nonReconnue(String saisie) {
        return texte("nonReconnue", saisie.strip());
    }

    String dejaProposee(RegionFr region) {
        return texte("dejaProposee", nom(region));
    }

    String fin(Partie partie) {
        String reponse = nom(partie.getCible());
        return switch (partie.getEtat()) {
            case GAGNEE -> {
                int essais = partie.getEssais().size();
                yield essais == 1 ? texte("gagnee.un") : texte("gagnee.plusieurs", essais);
            }
            case ABANDONNEE -> texte("abandonnee", reponse);
            case PERDUE -> texte("perdue", reponse);
            case EN_COURS -> throw new IllegalStateException("La partie n'est pas terminée");
        };
    }

    // ---- le bonus des départements ----

    // la question en cours : « Bonus 2/4 · Cliquez sur : Finistère ». Les noms des départements n'ont pas d'article
    // (« le Finistère », « la Somme », « l'Ain », « les Landes »…) : la phrase est tournée pour s'en passer
    String questionBonus(Bonus bonus) {
        String nom = bonus.demande().nom();
        return switch (bonus.getQuestion()) {
            case CARTE -> texte("bonus.carte", bonus.rang(), bonus.total(), nom);
            case NOM -> texte("bonus.nom", bonus.rang(), bonus.total());
            case NUMERO -> texte("bonus.numero", nom);
        };
    }

    // ce que la réponse donnée valait. Ratée en mode CARTE, le bon département est colorié en rouge sur la carte ;
    // dans les autres modes, on écrit la bonne réponse
    String reponseBonus(Bonus bonus, Tentative tentative) {
        Departement demande = tentative.demande();
        if (tentative.juste()) {
            return texte("bonus.juste", demande.nom());
        }
        return bonus.getQuestion() == Bonus.Question.CARTE
                ? texte("bonus.rate.carte", demande.nom())
                : texte("bonus.rate", demande.code(), demande.nom());
    }

    String bonusInconnu(Bonus bonus, String saisie) {
        return texte(bonus.getQuestion() == Bonus.Question.NUMERO ? "bonus.inconnuNumero" : "bonus.inconnu",
                saisie.strip());
    }

    // le bonus est fini : terminé ou passé, avec le score. Pour un seul département, on dit simplement juste ou faux
    String finBonus(Bonus bonus) {
        if (bonus.total() == 1 && !bonus.getTentatives().isEmpty()) {
            Departement departement = bonus.getOrdre().get(0);
            return bonus.score() == 1
                    ? texte("bonus.fin.un.trouve", departement.code())
                    : texte("bonus.fin.un.rate", departement.code());
        }
        String cle = bonus.getEtat() == Bonus.Etat.PASSE ? "bonus.passe" : "bonus.fin";
        return texte(cle, bonus.score(), bonus.total());
    }

    // le score du bonus en quelques caractères, après le message de fin de la partie : « Bonus : 3/4. »
    String resumeBonus(Bonus bonus) {
        return texte("bonus.resume", bonus.score(), bonus.total());
    }

    // les noms des régions jouables, dans l'ordre alphabétique de la langue (Île-de-France rangée à la lettre I)
    String listeDesRegions(ModeRegions mode) {
        List<String> noms = new ArrayList<>(RegionFr.tirables(mode).stream().map(this::nom).toList());
        noms.sort(Collator.getInstance(langue.getLocale()));
        return texte("liste.intro") + "\n" + String.join("\n", noms);
    }

    // un fichier texte rangé dans le .jar, lu en entier
    private static String lire(String chemin) {
        try (InputStream flux = Textes.class.getResourceAsStream(chemin)) {
            if (flux == null) {
                throw new IllegalStateException("Fichier introuvable dans les ressources : " + chemin);
            }
            return new String(flux.readAllBytes(), StandardCharsets.UTF_8).strip();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
