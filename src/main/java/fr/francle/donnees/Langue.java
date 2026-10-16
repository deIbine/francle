package fr.francle.donnees;

import java.util.Locale;

// les deux langues du jeu, reprises de la version web
public enum Langue {
    FR(Locale.FRENCH, "Français"),
    EN(Locale.UK, "English");

    // les conventions de la langue : ordre alphabétique (Collator), écriture des nombres, fichier de textes
    private final Locale locale;
    // le nom de la langue dans la langue elle-même : le joueur qui ne lit pas le français reconnaît « English »
    private final String nomPropre;

    Langue(Locale locale, String nomPropre) {
        this.locale = locale;
        this.nomPropre = nomPropre;
    }

    // la langue du système si le jeu la parle, l'anglais sinon (un Allemand lira plus facilement l'anglais)
    public static Langue duSysteme() {
        return Locale.getDefault().getLanguage().equals(FR.locale.getLanguage()) ? FR : EN;
    }

    public Locale getLocale() { return locale; }
    public String getNomPropre() { return nomPropre; }
}
