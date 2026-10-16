package fr.francle.stats;

import fr.francle.donnees.RegionFr;
import fr.francle.jeu.ModesDifficiles;

import java.time.LocalDate;
import java.util.List;

// la partie du jour commencée mais pas finie : sa date, les régions déjà proposées, et les modes difficiles fixés au
// premier essai (null dans un fichier d'avant la Phase 9). Enregistrée pour qu'en relançant le jeu, on la retrouve où
// on l'avait laissée (sinon, relancer donnerait des essais en plus)
public record PartieEnCours(LocalDate date, List<RegionFr> propositions, ModesDifficiles modes) {

    public PartieEnCours {
        propositions = List.copyOf(propositions);
    }

    public PartieEnCours(LocalDate date, List<RegionFr> propositions) {
        this(date, propositions, null);
    }
}
