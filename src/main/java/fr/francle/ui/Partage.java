package fr.francle.ui;

import fr.francle.jeu.Bonus;
import fr.francle.jeu.Direction;
import fr.francle.jeu.Essai;
import fr.francle.jeu.ModesDifficiles;
import fr.francle.jeu.Partie;

import java.util.ArrayList;
import java.util.List;

/*
Le résultat de la partie du jour à partager, repris de la version web (domaine/partage.ts), à la Worldle :

    #Francle n° 12 3/6 🌀          (« #Francle #12 3/6 » en anglais ; 🌀 et 🙈 pour les modes difficiles)
    🟩🟩🟨⬜⬜↗️
    🟩🟩🟩🟩🟨⬅️
    🟩🟩🟩🟩🟩🎉
    🗺️ 3/4                         (le score du bonus des départements, s'il a été joué)
    https://deibine.github.io/francle-web/

Ni les noms des régions, ni les indices : on ne gâche pas la réponse à ceux qui n'ont pas encore joué. Les carrés
s'inspirent de generateSquareCharacters de Worldle (Copyright (c) 2022 teuteuf, licence MIT) : un vert par tranche
de 20 % de proximité, un jaune s'il reste au moins 10 %, et du blanc pour compléter.

Le texte est construit sans rien afficher : on peut le tester. C'est la fenêtre qui le copie dans le presse-papiers.
 */
final class Partage {

    // l'adresse du jeu en ligne : celui qui reçoit le résultat peut y jouer, sans rien installer
    static final String ADRESSE = "https://deibine.github.io/francle-web/";
    private static final int CARRES = 5;

    private Partage() {}

    static String texte(Partie partie, long numero, ModesDifficiles modes, Bonus bonus, Textes textes) {
        List<String> lignes = new ArrayList<>();
        String score = (partie.getEtat() == Partie.Etat.GAGNEE ? String.valueOf(partie.getEssais().size()) : "X")
                + "/" + Partie.ESSAIS_MAX;
        String titre = textes.texte("partage.titre", numero) + " " + score;
        lignes.add(modes.emojis().isEmpty() ? titre : titre + " " + modes.emojis());
        for (Essai essai : partie.getEssais()) {
            lignes.add(carres(essai.pourcentage()) + emoji(essai.direction()));
        }
        if (bonus != null && !bonus.enCours()) {
            lignes.add("🗺️ " + bonus.score() + "/" + bonus.total());
        }
        lignes.add(ADRESSE);
        return String.join("\n", lignes);
    }

    // 87 % -> 🟩🟩🟩🟩⬜ (4 × 20 = 80, il reste 7 : moins de 10, pas de jaune)
    static String carres(int proximite) {
        int verts = proximite / 20;
        int jaunes = proximite - verts * 20 >= 10 ? 1 : 0;
        return "🟩".repeat(verts) + "🟨".repeat(jaunes) + "⬜".repeat(CARRES - verts - jaunes);
    }

    // les flèches en emoji, comme sur le web : ↑ et → n'ont pas d'emoji, et s'afficheraient différemment d'une
    // messagerie à l'autre. Un switch sans default : une direction ajoutée un jour ne compilerait pas sans son emoji
    static String emoji(Direction direction) {
        return switch (direction) {
            case NORD -> "⬆️";
            case NORD_EST -> "↗️";
            case EST -> "➡️";
            case SUD_EST -> "↘️";
            case SUD -> "⬇️";
            case SUD_OUEST -> "↙️";
            case OUEST -> "⬅️";
            case NORD_OUEST -> "↖️";
            case SUR_PLACE -> "🎉";
        };
    }
}
