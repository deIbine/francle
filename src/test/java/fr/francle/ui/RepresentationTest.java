package fr.francle.ui;

import fr.francle.donnees.RegionFr;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

// Le chargement des images depuis les ressources du classpath
class RepresentationTest {

    @ParameterizedTest
    @EnumSource(RegionFr.class)
    @DisplayName("L'image de chaque région existe dans les ressources")
    void imageExiste(RegionFr region) {
        String fichier = region.getImage() == null ? "defaut.png" : region.getImage();
        assertNotNull(Representation.Image("regions/" + fichier), fichier);
    }

    @Test
    @DisplayName("Une image absente donne null, sans exception")
    void imageAbsente() {
        assertNull(Representation.Image("regions/inexistante.png"));
    }
}
