package fr.francle.jeu;

import fr.francle.donnees.Departement;

// ce que le Bonus répond à une réponse du joueur, comme Reponse pour la Partie : « sealed », un switch peut traiter
// tous les cas sans « default » (et le compilateur a montré les switch à compléter quand le mode C en a ajouté deux)
public sealed interface ReponseBonus {

    // la réponse a compté, juste ou fausse : on passe au département suivant
    record Jouee(Tentative tentative) implements ReponseBonus {}

    // la saisie n'est pas un département (ou pas un numéro, quand on demande un numéro) : elle ne coûte rien
    record Inconnue() implements ReponseBonus {}

    // mode CHOIX : la saisie évoque plusieurs départements (« haute ») ; mieux vaut demander de choisir, comme pour
    // les régions (Reponse.Plusieurs)
    record Plusieurs() implements ReponseBonus {}

    // mode CHOIX : ce département a déjà été trouvé ailleurs sur la carte. Refusé sans rien coûter
    record DejaTrouve(Departement departement) implements ReponseBonus {}
}
