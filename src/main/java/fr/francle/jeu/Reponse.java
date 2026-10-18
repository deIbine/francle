package fr.francle.jeu;

import fr.francle.donnees.RegionFr;

/*
Ce que la Partie répond à une saisie du joueur : exactement l'un de ces cinq cas.

« sealed » ferme la liste : aucune autre classe ne peut implémenter Reponse. Un switch sur une Reponse peut donc
traiter tous les cas sans « default », et le compilateur refuse un switch qui en oublierait un (quand la phase 11 a
ajouté AutreEpoque, il a montré tous les endroits à compléter).
 */
public sealed interface Reponse {

    // la saisie a été jouée : un essai de plus
    record Jouee(Essai essai) implements Reponse {}

    // la saisie n'est pas une région reconnue, ou pas dans le mode joué : elle ne coûte pas d'essai
    record Inconnue() implements Reponse {}

    // la saisie n'est pas un nom complet, et elle évoque plusieurs régions (« nor » chez les anciennes régions :
    // Nord-Pas-de-Calais, Basse- et Haute-Normandie) : mieux vaut demander de choisir que d'en jouer une au hasard
    // et faire perdre un essai. Repris de la version web
    record Plusieurs() implements Reponse {}

    // la saisie est le nom d'une région de l'autre époque (« Alsace » en nouvelles régions, « Grand Est » en
    // anciennes) : elle existe, mais ne peut pas être la réponse. Refusée sans coûter d'essai, avec une explication
    // (RegionFr.autreEpoque dit à quelles régions elle correspond). Repris de la version web
    record AutreEpoque(RegionFr region) implements Reponse {}

    // la région a déjà été proposée dans cette partie : refusée, sans coûter d'essai (le joueur s'est sûrement
    // trompé, et elle ne lui apprendrait rien de plus)
    record DejaProposee(RegionFr region) implements Reponse {}
}
