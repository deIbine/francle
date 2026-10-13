package fr.francle.jeu;

import fr.francle.donnees.RegionFr;

// une proposition jouée : la région proposée, sa distance à la cible (km), le pourcentage de proximité
// et la direction à suivre pour aller vers la cible
public record Essai(RegionFr proposition, double distance, int pourcentage, Direction direction) {}
