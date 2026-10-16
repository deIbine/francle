package fr.francle.jeu;

import fr.francle.donnees.Departement;

// une réponse du bonus : le département demandé, et celui que le joueur a donné (cliqué ou nommé)
public record Tentative(Departement demande, Departement donne) {

    public boolean juste() {
        return demande.equals(donne);
    }
}
