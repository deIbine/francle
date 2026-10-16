package fr.francle.donnees;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/*
Un département : son numéro (« 29 », « 2A », « 971 ») et son nom (« Finistère »). Les noms restent en français dans
les deux langues du jeu, comme sur les cartes anglaises.

La liste des 101 départements, et ceux de chaque région, ne sont pas écrits ici : ils sont dans
resources/departements/departements.json, que lisent à la fois ce code (Decoupage) et le script qui dessine les
cartes (outils/generer_images.py). Une seule liste pour les deux : ils ne peuvent pas se contredire.
 */
public record Departement(String code, String nom) {

    // tous les départements, dans l'ordre de leurs numéros
    public static List<Departement> tous() {
        return Decoupage.tous();
    }

    // le département de ce numéro, s'il existe
    public static Optional<Departement> parCode(String code) {
        return Decoupage.parCode(code);
    }

    // le département que le joueur a voulu dire, par son nom (« finistere ») ou son numéro (« 29 », « 2a », « 1 »)
    public static Optional<Departement> chercher(String saisie) {
        Optional<Departement> parNumero = parCode(numero(saisie));
        if (parNumero.isPresent()) {
            return parNumero;
        }
        String cle = RegionFr.simplifier(saisie);
        return tous().stream().filter(departement -> RegionFr.simplifier(departement.nom).equals(cle)).findFirst();
    }

    // la saisie écrite comme un numéro de département : « 2a » -> « 2A », « 1 » -> « 01 » (les numéros à un chiffre
    // s'écrivent avec un zéro devant). Une saisie qui n'a pas la forme d'un numéro est renvoyée telle quelle
    public static String numero(String saisie) {
        String numero = saisie.strip().toUpperCase();
        return UN_CHIFFRE.matcher(numero).matches() ? "0" + numero : numero;
    }

    private static final Pattern UN_CHIFFRE = Pattern.compile("[1-9]");

    // les départements à proposer pendant la frappe, parmi ceux donnés : d'abord ceux dont le nom commence par la
    // saisie (« haute » -> Haute-Corse), puis ceux dont un mot commence par elle (« corse » -> Corse-du-Sud,
    // Haute-Corse) ; chaque groupe par ordre alphabétique. La même règle que pour les régions (RegionFr.suggestions)
    public static List<Departement> suggestions(String saisie, List<Departement> parmi, Langue langue) {
        String debut = RegionFr.simplifier(saisie);
        List<Departement> parNom = new ArrayList<>();
        List<Departement> parMot = new ArrayList<>();
        if (debut.isEmpty()) {
            return parNom;
        }
        for (Departement departement : parmi) {
            if (RegionFr.simplifier(departement.nom).startsWith(debut)) {
                parNom.add(departement);
            } else if (RegionFr.mots(departement.nom).stream().anyMatch(mot -> mot.startsWith(debut))) {
                parMot.add(departement);
            }
        }
        Comparator<Departement> alphabetique = Comparator.comparing(Departement::nom,
                Collator.getInstance(langue.getLocale()));
        parNom.sort(alphabetique);
        parMot.sort(alphabetique);
        parNom.addAll(parMot);
        return parNom;
    }
}
