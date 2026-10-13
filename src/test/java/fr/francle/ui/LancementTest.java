package fr.francle.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

// Le .jar doit savoir quelle classe lancer : elle est déclarée dans le pom.xml, que le compilateur ne vérifie pas
class LancementTest {

    @Test
    @DisplayName("La classe à lancer déclarée dans le pom.xml existe et a une méthode main")
    void classeALancer() throws Exception {
        // Maven lance les tests depuis la racine du projet, là où se trouve le pom.xml
        String pom = Files.readString(Path.of("pom.xml"));
        Matcher m = Pattern.compile("<mainClass>(.+)</mainClass>").matcher(pom);
        assertTrue(m.find(), "pas de <mainClass> dans le pom.xml");

        Class<?> classe = Class.forName(m.group(1));
        Method main = classe.getMethod("main", String[].class);

        assertTrue(Modifier.isStatic(main.getModifiers()), "main doit être static");
        assertEquals(Francle.class, classe);
    }
}
