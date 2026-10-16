package fr.francle.jeu;

/*
Les modes difficiles avec lesquels une partie est jouée : la silhouette tournée (🌀) ou cachée (🙈). Comme dans la
version web, ils sont fixés au premier essai : un changement de réglage en pleine partie ne compte que pour la
suivante. Le partage peut ainsi les afficher sans mentir (on ne peut pas cacher la silhouette juste avant de gagner).

Une silhouette cachée ne peut pas être tournée : le constructeur compact le garantit, quel que soit l'appelant.
 */
public record ModesDifficiles(boolean tournee, boolean cachee) {

    public static final ModesDifficiles AUCUN = new ModesDifficiles(false, false);

    public ModesDifficiles {
        tournee = tournee && !cachee;
    }

    // « 🌀 », « 🙈 », ou rien : un défi relevé se voit dans le partage
    public String emojis() {
        return (tournee ? "🌀" : "") + (cachee ? "🙈" : "");
    }
}
