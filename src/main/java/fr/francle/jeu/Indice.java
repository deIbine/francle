package fr.francle.jeu;

import java.util.Optional;

// les indices, dans l'ordre où ils sont révélés : un après chacun des cinq premiers essais ratés
// (le sixième essai raté termine la partie : il n'y a pas d'indice après lui)
public enum Indice {
    SUPERFICIE, SPECIALITE, PERSONNALITE, CAPITALE, MONUMENT;

    // l'indice révélé après l'essai raté numéro "numero" (0 = le premier), rien après le cinquième
    public static Optional<Indice> apresEssai(int numero) {
        if (numero < 0 || numero >= values().length) {
            return Optional.empty();
        }
        return Optional.of(values()[numero]);
    }
}
