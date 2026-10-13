package fr.francle.stats;

import fr.francle.donnees.RegionFr;

import java.time.LocalDate;
import java.util.List;

// la partie du jour commencée mais pas finie : sa date et les régions déjà proposées. Enregistrée pour qu'en relançant
// le jeu, on la retrouve où on l'avait laissée (sinon, relancer donnerait des essais en plus)
public record PartieEnCours(LocalDate date, List<RegionFr> propositions) {

    public PartieEnCours {
        propositions = List.copyOf(propositions);
    }
}
