package fr.francle.ui;

import fr.francle.donnees.Departement;
import fr.francle.donnees.Epoque;
import fr.francle.donnees.Langue;
import fr.francle.donnees.ModeRegions;
import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Bonus;
import fr.francle.jeu.Essai;
import fr.francle.jeu.Indice;
import fr.francle.jeu.Partie;
import fr.francle.jeu.RegionDuJour;
import fr.francle.jeu.Reponse;
import fr.francle.jeu.Tentative;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
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

    // l'aide, avec son exemple. Comme dans la version web, l'exemple est un vrai essai, calculé par les règles du jeu
    // plutôt qu'écrit à la main : il reste juste si une règle change (la Normandie proposée pour trouver la Bretagne)
    String aide() {
        return aide.replace("{exemple}", exemple());
    }

    String exemple() {
        Partie partie = new Partie(RegionFr.BRETAGNE, ModeRegions.NOUVELLES);
        Reponse.Jouee jouee = (Reponse.Jouee) partie.proposer(RegionFr.NORMANDIE.getNom());
        Essai essai = jouee.essai();
        // en km, quelle que soit l'unité choisie : c'est une explication, pas un essai
        return texte("aide.exemple", nom(RegionFr.NORMANDIE), distance(essai.distance(), false),
                essai.direction().getSymbole(), pourcentage(essai.pourcentage()), nom(RegionFr.BRETAGNE));
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
        return ligneIndice(indice, valeurIndice(indice, cible, unitesAnglaises));
    }

    // ce que l'indice dit de la région : « 27 208 km² », « la crêpe »…
    String valeurIndice(Indice indice, RegionFr cible, boolean unitesAnglaises) {
        return switch (indice) {
            case SUPERFICIE -> superficie(cible.getSuperficie(), unitesAnglaises);
            case SPECIALITE -> cible.getSpecialite().dans(langue);
            case PERSONNALITE -> cible.getPersonnalite().dans(langue);
            case CAPITALE -> cible.getCapitale();
            case MONUMENT -> cible.getMonument().dans(langue);
        };
    }

    // la ligne de l'indice, autour de ce qui tient lieu de valeur (la valeur elle-même, mise en gras par la fenêtre, ou
    // « après l'essai 3 » pour un indice pas encore révélé). La clé porte le nom de l'indice : « indice.SUPERFICIE »…
    String ligneIndice(Indice indice, String valeur) {
        return texte("indice." + indice.name(), valeur);
    }

    // pour un indice pas encore révélé, quand il le sera : « après l'essai 3 » (le troisième indice vient après le
    // troisième essai raté)
    String indiceCache(Indice indice) {
        return texte("indice.cache", indice.ordinal() + 1);
    }

    // le nom d'un mode de jeu, dans le menu Paramètres
    String mode(ModeRegions mode) {
        return texte("mode." + mode.name());
    }

    String nom(RegionFr region) {
        return region.getNom(langue);
    }

    // le nom au milieu d'une phrase : « le Grand Est », « l'Occitanie », « Mayotte » en français ; le nom seul en
    // anglais, qui n'en met pas devant les régions
    String nomDansUnePhrase(RegionFr region) {
        return langue == Langue.FR ? Articles.FR.getProperty(region.name(), nom(region)) : nom(region);
    }

    // les articles du français (resources/textes/articles_fr.properties), lus une seule fois, au premier besoin
    // (le même « holder » que ContoursFrance). Properties.load lit un flux d'octets en ISO-8859-1, et abîmerait les
    // accents d'un fichier UTF-8 : on lui donne donc le texte déjà décodé (lire), par un Reader
    private static final class Articles {
        static final Properties FR = lireProprietes("/textes/articles_fr.properties");
    }

    static Properties articles() {
        return Articles.FR;
    }

    private static Properties lireProprietes(String chemin) {
        Properties proprietes = new Properties();
        try (Reader lecteur = new StringReader(lire(chemin))) {
            proprietes.load(lecteur);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return proprietes;
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

    // une région de l'autre époque, et ce qu'elle est devenue (ou ce qu'elle regroupe). « Alsace : une ancienne
    // région (avant 2016), aujourd'hui dans la région Grand Est. » ; « Grand Est : une région d'après 2016, qui
    // regroupe Alsace, Champagne-Ardenne et Lorraine. ». Le Centre, lui, a seulement changé de nom : une phrase à part
    String autreEpoque(RegionFr region) {
        List<RegionFr> liees = new ArrayList<>(region.autreEpoque());
        liees.sort(Comparator.comparing(this::nom, Collator.getInstance(langue.getLocale())));
        boolean ancienne = region.getEpoque() == Epoque.ANCIENNE;
        // renommée : une seule région de l'autre côté, avec exactement les mêmes départements
        boolean renommee = liees.size() == 1
                && Set.copyOf(liees.get(0).getDepartements()).equals(Set.copyOf(region.getDepartements()));
        String cle = "autreEpoque." + (ancienne ? "ancienne" : "nouvelle") + (renommee ? ".renommee" : "");
        return texte(cle, nom(region), enumeration(liees.stream().map(this::nom).toList()));
    }

    // « A », « A et B », « A, B et C » (« and » en anglais)
    String enumeration(List<String> noms) {
        if (noms.size() <= 1) {
            return String.join("", noms);
        }
        String debut = String.join(", ", noms.subList(0, noms.size() - 1));
        return texte("enumeration", debut, noms.getLast());
    }

    String fin(Partie partie) {
        // au milieu de la phrase : « La réponse était le Grand Est. »
        String reponse = nomDansUnePhrase(partie.getCible());
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
    // choisi : en mode CHOIX, le joueur a déjà choisi le département à nommer
    String questionBonus(Bonus bonus, boolean choisi) {
        return switch (bonus.getQuestion()) {
            case CARTE -> texte("bonus.carte", bonus.rang(), bonus.total(), bonus.demande().nom());
            case NOM -> texte("bonus.nom", bonus.rang(), bonus.total());
            // pas de question imposée : c'est au joueur de choisir un département, puis de le nommer
            case CHOIX -> texte(choisi ? "bonus.nom" : "bonus.choix", bonus.rang(), bonus.total());
            // une phrase par département d'outre-mer, pour son article : « de la Guadeloupe », « de La Réunion »,
            // « de Mayotte »… Les seuls départements à avoir une question à eux (une région d'un seul département)
            case NUMERO -> texte("bonus.numero." + bonus.demande().code());
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

    // une saisie qui ne désigne rien. Dans la version simple du mode C, elle peut évoquer un département d'une autre
    // région (« cant » en Bretagne) : on dit donc qu'elle n'évoque aucun département de la région
    String bonusInconnu(Bonus bonus, String saisie) {
        String cle = switch (bonus.getQuestion()) {
            case NUMERO -> "bonus.inconnuNumero";
            case CHOIX -> bonus.isDifficile() ? "bonus.inconnu" : "bonus.inconnuRegion";
            case CARTE, NOM -> "bonus.inconnu";
        };
        return texte(cle, saisie.strip());
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

    // une barre de la répartition des victoires, en mots, pour un lecteur d'écran : « 2 essais : 5 victoires. », et
    // pour celle de la victoire du jour, « (la partie du jour) » en plus. Un pluriel par phrase, comme apresEssai
    String barreRepartition(int essais, int victoires, boolean duJour) {
        String combien = essais == 1 ? texte("repartition.essais.un") : texte("repartition.essais.plusieurs", essais);
        String gagnees = victoires == 0 ? texte("repartition.victoires.aucune")
                : victoires == 1 ? texte("repartition.victoires.une") : texte("repartition.victoires.plusieurs", victoires);
        String barre = texte("repartition.barre", combien, gagnees);
        return duJour ? texte("repartition.duJour", barre) : barre;
    }

    // ---- les commandes grisées ----

    // pourquoi la liste (des régions ou des départements) ne s'ouvre pas, pour la bulle d'aide de la commande grisée :
    // le réglage décoché d'abord (le joueur peut y remédier), sinon une question du bonus qui n'a pas de liste (le
    // numéro, en outre-mer). Rien si elle s'ouvre
    Optional<String> listeGrisee(boolean avecListe, boolean questionAvecListe) {
        if (!avecListe) {
            return Optional.of(texte("liste.desactivee"));
        }
        if (!questionAvecListe) {
            return Optional.of(texte("liste.aucune"));
        }
        return Optional.empty();
    }

    // les régions jouables, dans l'ordre alphabétique de leur nom dans la langue (Île-de-France rangée à la lettre I)
    List<RegionFr> regionsTriees(ModeRegions mode) {
        List<RegionFr> regions = new ArrayList<>(RegionFr.tirables(mode));
        regions.sort(Comparator.comparing(this::nom, Collator.getInstance(langue.getLocale())));
        return regions;
    }

    // ces départements (ceux que le bonus propose : la région, ou les 101), dans l'ordre alphabétique de la langue
    // (Hérault à la lettre H). Les noms, eux, restent en français dans les deux langues, comme sur les cartes
    List<Departement> departementsTries(List<Departement> parmi) {
        List<Departement> departements = new ArrayList<>(parmi);
        departements.sort(Comparator.comparing(Departement::nom, Collator.getInstance(langue.getLocale())));
        return departements;
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
