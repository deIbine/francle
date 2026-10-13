import java.util.Optional;

// les indices, dans l'ordre où ils sont révélés : un après chacun des quatre premiers essais ratés
enum Indice {
    SUPERFICIE, SPECIALITE, PERSONNALITE, CAPITALE;

    // l'indice révélé après l'essai raté numéro "numero" (0 = le premier), rien après le quatrième
    public static Optional<Indice> apresEssai(int numero) {
        if (numero < 0 || numero >= values().length) {
            return Optional.empty();
        }
        return Optional.of(values()[numero]);
    }
}
