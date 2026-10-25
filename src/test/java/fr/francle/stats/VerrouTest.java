package fr.francle.stats;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// Le verrou d'un seul jeu ouvert, dans un dossier temporaire (jamais celui du joueur)
class VerrouTest {

    @TempDir
    Path dossier;

    @Test
    @DisplayName("Le premier lancement prend le verrou, un second le trouve pris")
    void unSeul() throws Exception {
        Path fichier = dossier.resolve("sous-dossier/francle.lock");
        try (Verrou premier = Verrou.prendre(fichier).orElseThrow()) {
            assertNotNull(premier);
            assertEquals(Optional.empty(), Verrou.prendre(fichier));
        }
    }

    @Test
    @DisplayName("Une fois le verrou relâché, il peut être repris")
    void relache() throws Exception {
        Path fichier = dossier.resolve("francle.lock");
        Verrou.prendre(fichier).orElseThrow().close();
        try (Verrou encore = Verrou.prendre(fichier).orElseThrow()) {
            assertNotNull(encore);
        }
    }
}
