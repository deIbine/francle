package fr.francle.donnees;

// un texte dans les deux langues du jeu : le nom d'une région, un indice. Un record : deux textes qui disent la même
// chose sont égaux, et personne ne peut en changer un après coup
public record Texte(String fr, String en) {

    // un texte identique dans les deux langues (la plupart des noms propres)
    public static Texte pareil(String texte) {
        return new Texte(texte, texte);
    }

    public String dans(Langue langue) {
        // un switch-expression sur un enum : sans « default », le compilateur exige un cas par langue. Ajouter une
        // troisième langue au jeu ferait donc échouer la compilation ici, à l'endroit exact à compléter
        return switch (langue) {
            case FR -> fr;
            case EN -> en;
        };
    }
}
