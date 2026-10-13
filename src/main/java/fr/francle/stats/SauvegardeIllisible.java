package fr.francle.stats;

import java.io.IOException;
import java.nio.file.Path;

// le fichier d'historique existe mais ne se lit pas (abîmé, modifié à la main…). Il a été mis de côté, pas effacé :
// le joueur peut le récupérer, et le jeu repart d'un historique vide
public class SauvegardeIllisible extends IOException {

    private final Path deCote;

    public SauvegardeIllisible(Path fichier, Path deCote, Throwable cause) {
        super("L'historique " + fichier + " est illisible ; il a été mis de côté sous le nom " + deCote.getFileName() + ".",
                cause);
        this.deCote = deCote;
    }

    public Path getDeCote() { return deCote; }
}
