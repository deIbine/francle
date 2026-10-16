package fr.francle.jeu;

// ce que le Bonus répond à une réponse du joueur, comme Reponse pour la Partie : « sealed », un switch peut traiter
// les deux cas sans « default »
public sealed interface ReponseBonus {

    // la réponse a compté, juste ou fausse : on passe au département suivant
    record Jouee(Tentative tentative) implements ReponseBonus {}

    // la saisie n'est pas un département (ou pas un numéro, quand on demande un numéro) : elle ne coûte rien
    record Inconnue() implements ReponseBonus {}
}
