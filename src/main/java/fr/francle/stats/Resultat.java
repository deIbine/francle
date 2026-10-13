package fr.francle.stats;

import fr.francle.donnees.RegionFr;
import fr.francle.jeu.Partie;

import java.time.LocalDate;
import java.util.List;

// une partie du jour terminée : sa date, la région à trouver, les régions proposées dans l'ordre, et comment elle a fini
public record Resultat(LocalDate date, RegionFr region, List<RegionFr> propositions, Partie.Etat etat) {

    // constructeur compact : il s'exécute avant de remplir les champs. List.copyOf fait une copie non modifiable,
    // pour que personne ne puisse changer un résultat après coup (et refuse une liste absente ou avec des trous)
    public Resultat {
        propositions = List.copyOf(propositions);
    }

    public boolean victoire() {
        return etat == Partie.Etat.GAGNEE;
    }
}
