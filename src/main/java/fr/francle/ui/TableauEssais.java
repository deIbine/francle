package fr.francle.ui;

import fr.francle.donnees.Langue;
import fr.francle.jeu.Essai;
import fr.francle.jeu.Partie;

import javax.swing.table.AbstractTableModel;

/*
Le modèle du tableau des essais : la JTable ne stocke rien, elle demande chaque case à ce modèle (getValueAt),
qui la lit dans la Partie. Toujours 6 lignes : les lignes vides montrent les essais qui restent.
Aucune fenêtre ici : le modèle se teste sans écran.
 */
class TableauEssais extends AbstractTableModel {

    // les clés des titres de colonnes dans les fichiers de textes
    static final String[] COLONNES = {"colonne.numero", "colonne.region", "colonne.distance", "colonne.direction",
            "colonne.proximite"};
    static final String TROUVE = "✓";

    private Partie partie;
    private boolean unitesAnglaises;
    private Textes textes = new Textes(Langue.FR);

    // le joueur change de langue : les noms des régions se réaffichent. Les titres des colonnes, eux, sont gardés par
    // la JTable une fois créée : c'est la fenêtre qui les met à jour (Fenetre.appliquerLangue)
    void setTextes(Textes textes) {
        this.textes = textes;
        fireTableDataChanged();
    }

    // le joueur change d'unité : les essais déjà joués se réaffichent tout de suite dans la nouvelle. Rien n'est
    // recalculé ni stocké en miles : getValueAt convertit à chaque fois les kilomètres de l'Essai
    void setUnitesAnglaises(boolean unitesAnglaises) {
        this.unitesAnglaises = unitesAnglaises;
        fireTableDataChanged();
    }

    // une nouvelle partie : tout le tableau change
    void setPartie(Partie partie) {
        this.partie = partie;
        fireTableDataChanged();
    }

    // le dernier essai vient d'être joué : seule sa ligne change (le nombre de lignes, lui, ne bouge pas)
    void essaiJoue() {
        int ligne = partie.getEssais().size() - 1;
        fireTableRowsUpdated(ligne, ligne);
    }

    @Override
    public int getRowCount() { return Partie.ESSAIS_MAX; }

    @Override
    public int getColumnCount() { return COLONNES.length; }

    @Override
    public String getColumnName(int colonne) { return textes.texte(COLONNES[colonne]); }

    @Override
    public Object getValueAt(int ligne, int colonne) {
        if (colonne == 0) {
            return String.valueOf(ligne + 1);
        }
        // essai pas encore joué : case vide
        if (partie == null || ligne >= partie.getEssais().size()) {
            return "";
        }
        Essai essai = partie.getEssais().get(ligne);
        boolean trouve = essai.proposition() == partie.getCible();
        switch (colonne) {
            case 1 : return textes.nom(essai.proposition());
            case 2 : return textes.distance(essai.distance(), unitesAnglaises);
            case 3 : return trouve ? TROUVE : essai.direction().getSymbole();
            default : return textes.pourcentage(essai.pourcentage());
        }
    }
}
