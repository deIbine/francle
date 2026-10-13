package fr.francle.jeu;

import fr.francle.donnees.RegionFr;

// une proposition jouée : la région proposée, sa distance à la cible (km) et le pourcentage de proximité
public record Essai(RegionFr proposition, double distance, int pourcentage) {}
