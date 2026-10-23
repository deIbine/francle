# Feuille de route

Plan d'amélioration de Francle, établi le 11 octobre 2026.
Chaque phase associe une amélioration du jeu à une notion Java à apprendre.
Les leçons à retenir de chaque phase sont rassemblées dans [lecons.md](lecons.md).

## État d'avancement

| Phase | Sujet | État |
|---|---|---|
| 0.5 | Dépôt Git propre | ✅ Fait (PR #16) |
| 0 | Environnement de développement | ✅ Fait (JDK 21, IntelliJ 2026.2) |
| 1 | Corriger les bugs | ✅ Fait (4 bugs corrigés) |
| 2 | Centraliser les données des régions | ✅ Fait (enum RegionFr, −466 lignes) |
| 3 | Séparer logique et affichage, tests, Maven, packages | ✅ Fait (PR #21, #22 et 3.3) |
| 4 | Interface Swing en une seule fenêtre | ✅ Fait (PR #24 à #28) |
| 5 | Région du jour et statistiques | ✅ Fait (PR #29 et #30) |
| 6 | Anciennes régions complètes, réglages enregistrés, options | ✅ Fait (PR #31) |
| 7 | Version web | ✅ Devenue un projet à part : [deIbine/francle-web](https://github.com/deIbine/francle-web) |
| 8 | Reprendre les nouveautés de la version web | ✅ Fait (PR #32) |
| 9 | Le bonus des départements | ✅ Fait (PR #34) |
| 10 | Le bonus revu : dès la victoire, et le mode C | ✅ Fait (PR #36 à #38) |
| 11 | Les corrections relevées en jouant (avec la version web) | ✅ Fait (PR #40 à #42) |
| 12 | La liste des départements, pendant le bonus | Décidée le 19 octobre 2026, pas commencée |

---

## Phase 0.5 : dépôt Git propre ✅

Fait dans la PR [#16](https://github.com/deIbine/francle/pull/16) :

- `.gitignore` complet (IDE, dossiers de build, fichiers système) ;
- `.gitattributes` : fins de ligne normalisées (le dépôt mélangeait CRLF et LF) ;
- `oldcode/` déplacé dans `archive/projet-universitaire/`, indices dans `docs/indices.md`,
  images renommées sans espaces ni accents, `LICENCE.MD` renommé en `LICENSE` ;
- README réécrit ;
- suppression des 16 branches `patch-*` créées par l'éditeur web de GitHub
  (le contenu utile des PR non fusionnées a été récupéré dans `docs/indices.md`).

**Conventions adoptées pour la suite :**

- messages de commit en français, à l'impératif (« Corrige le calcul de distance ») ;
- une branche par phase, fusionnée dans `main` via une Pull Request ;
- pas de réécriture de l'historique (`rebase` ou `push --force` sur `main`).

---

## Phase 0 : environnement de développement ✅

**Notions :**

- **JRE** : exécute du Java. **JDK** : le JRE plus les outils de développement (`javac`, `jar`…).
- **`PATH`** : les dossiers où Windows cherche une commande comme `javac`.
- **`JAVA_HOME`** : indique aux outils de build (Maven, Gradle) où se trouve le JDK.
- **LTS** (*Long Term Support*) : seules certaines versions de Java sont maintenues plusieurs
  années (17, 21, 25…). Les versions 18 et 19 ne l'étaient que 6 mois.
- **Configuration d'un projet IntelliJ** (`.idea/`, propre à chaque machine) : le *source root*
  (où est le code), les dossiers exclus (ici `archive/`, qui contient une deuxième classe `main`),
  le *SDK* (le JDK utilisé) et la *run configuration* (quelle classe lancer, depuis quel dossier).

**Fait :**

- JDK 21 Temurin installé, avec `PATH` et `JAVA_HOME` configurés ;
- jeu compilé et lancé en ligne de commande
  (`javac -encoding UTF-8 -d out main.java` puis `java -cp out main`) ;
- IntelliJ IDEA 2026.2.3 installé et configuré : le jeu se lance depuis l'IDE.
  JetBrains a arrêté l'édition Community après la version 2025.2 ; la version unifiée
  reste gratuite pour le développement Java, Maven, Gradle et Git.

**Leçons retenues :**

- un installeur lancé en mode `/quiet` ne peut pas afficher la fenêtre UAC : il échoue sans rien dire ;
- `winget` peut rester bloqué à la fin d'un téléchargement (service *Delivery Optimization*) ;
  `curl -C -` permet de reprendre un téléchargement interrompu ;
- toujours vérifier l'empreinte SHA-256 d'un installeur téléchargé.

**Facultatif :** désinstaller IntelliJ 2022.1, PyCharm 2022.1 et les anciens JDK
(`~/.jdks/openjdk-18`, `openjdk-19`, `C:\Program Files\Java\jdk1.8` et `jre1.8`).

---

## Phase 1 : corriger les bugs ✅

Méthode appliquée à chaque bug : **reproduire** (prouver le bug par un test), **corriger**,
puis **revérifier avec le même test**. Un commit par bug.

| Bug | Comment il a été repéré | Correction | Réflexe à retenir |
|---|---|---|---|
| Latitude et longitude inversées dans `verifdistance` : est-ouest surestimé de 50 % (Rennes → Strasbourg à 1050 km au lieu de 700 km) | Comparaison avec des distances réelles connues. L'écart vaut 1 / cos(48°) ≈ 1,5 : un degré de longitude compté 111 km au lieu de 74 km | Formule découpée en variables nommées (`lat1`, `lat2`, `deltaLon`), protection contre `acos(>1)` = `NaN` (3,8 % des cas sur des coordonnées aléatoires) | Vérifier une formule contre une valeur connue ; ne jamais comparer des `double` avec `==` |
| `essai` jamais remis à 0 : une nouvelle partie gardait l'historique de la précédente | Exécution à la main de la variable sur deux parties | Compteur local d'une boucle `for` au lieu d'une variable globale au programme | Une variable doit vivre aussi longtemps que ce qu'elle représente ; corriger la racine plutôt que poser un pansement |
| Dé relancé dans `latitudeetlongitude` en mode « toutes les régions » : Alsace reconnue 1 fois sur 2, y compris comme région à deviner | Méthode appelée 1000 fois (496/1000) | Recherche dans les anciennes régions pour tout mode sauf 1 | Une méthode qui cherche doit être déterministe ; se méfier du copier-coller hors contexte |
| `break` manquant après le didacticiel : message « Retour au menu » en trop | `javac -Xlint:fallthrough` signale la ligne exacte | Ajout du `break` | Les `case` sont des étiquettes, pas des blocs ; écouter les avertissements du compilateur |

La bonne formule (loi des cosinus sphérique) :
`d = R · acos(sin φ1 · sin φ2 + cos φ1 · cos φ2 · cos(λ1 − λ2))`,
avec φ la latitude, λ la longitude et R = 6371 km.

**Ce que tu apprends :** comparer une formule mathématique au code, la portée et la
réinitialisation des variables, le *fall-through* d'un `switch`, les avertissements de `javac`.

**Découvertes à traiter plus tard :**

- ~~**Pourcentage de proximité peu parlant**~~ : réglé en Phase 2 (formule B). Constat d'origine : il était calculé par rapport
  à la région la plus éloignée de la cible, souvent un département d'outre-mer à plus de 9000 km.
  Toute la métropole se retrouve entre 88 % et 98 % (pour la Bretagne : Corse 88 %, Normandie 97 %).
  Options : calcul limité à la métropole, distance maximale fixe, ou autre formule.
- ~~**`distance == 0` signifie « région inconnue »**~~ : réglé en Phase 2 (`Optional`).
- ~~**`parseInt` en double dans le didacticiel**~~ : réglé (petits correctifs après la Phase 2).

---

## Phase 2 : mettre les données au même endroit ✅

Les données des régions étaient copiées à 5 endroits (`latitudeetlongitude`, `choixpossible`,
`lesregionsenint`, `onmetlimage`, `touteslesregions`), avec des nombres magiques (26, 27, 17 + 1, 100).
C'était l'origine des problèmes de tableaux de 26 ou 27 cases notés dans l'ancien README.

**Ce qui a été fait** (un commit par étape, le jeu fonctionne à chaque commit) :

1. **Filet de sécurité** : un test de caractérisation photographie le comportement (recherche dans
   les 3 modes, 1225 distances et pourcentages, régions tirables, images comparées pixel par pixel).
   Après chaque étape, la sortie est comparée à la photo : identique sur 1397 lignes.
2. **`enum RegionFr`** : source unique des 35 régions (nom, coordonnées, époque, superficie, indices,
   image), plus les `enum` `Epoque` (`ANCIENNE`, `NOUVELLE`, `LES_DEUX`) et `ModeRegions`, qui porte
   les règles « quelles régions tirer » et « quelles régions reconnaître ».
3. **Recherche** des propositions : `RegionFr.chercher` renvoie un `Optional` au lieu d'une distance nulle.
4. **Tirage** : `RegionFr.tirer(mode)` ; suppression des 413 lignes de `switch`.
5. **Pourcentage** : un simple maximum au lieu d'une troisième copie des coordonnées et d'un tri.
6. **Images et liste des régions** lues dans l'`enum` (liste triée avec un `Collator` français).
7. **Ménage** : sentinelle `latitude == 0`, `degresversradian` (remplacé par `Math.toRadians`), imports inutiles.

Résultat : `main.java` passe de 1421 à 955 lignes ; ajouter une région = **1 ligne**.

**Bugs découverts et corrigés en chemin :**

- une région ayant la même capitale que la cible (Alsace pour le Grand Est…) était refusée
  comme « saisie non reconnue », car la distance valait exactement 0 ;
- la Bourgogne-Franche-Comté manquait dans la liste des régions (touche `1`) ;
- le tirage en mode « toutes les régions » n'était pas équitable (1/27 ou 1/54 selon les régions).

**Ce que tu apprends :** `enum` avec champs, constructeurs et méthodes, bloc `static`, `HashMap`,
`Optional`, références de méthode, méthodes `static` et de fabrique, `Collator` ; le principe DRY
(une *connaissance* à un seul endroit, pas seulement un texte) ; le refactoring sous filet de sécurité
(construire à côté, migrer, supprimer) ; remplacer les valeurs spéciales par une vraie notion d'absence.

**Ajouts après le refactoring** (changements de comportement volontaires, dans des commits séparés) :

- **Pourcentage de proximité, formule B** : pour une cible en métropole, 0 % correspond à la région
  *métropolitaine* la plus éloignée (Bretagne : Normandie 76 %, Grand Est 35 %, Corse 0 % ; avant :
  97 %, 92 %, 88 %). Une cible en outre-mer garde la référence de toutes les régions. Chaque région
  porte désormais sa `Zone` (métropole, outre-mer). Le pourcentage est arrondi au lieu d'être tronqué
  (une distance de quelques centimètres donnait 99 %) et ne descend jamais sous 0 %.
  Conséquence de B : la référence dépend de la cible, les pourcentages sont plus sévères pour une
  région en bordure (Grand Est) que pour une région centrale.
- **Indices corrigés** : crêpe, tarte Tatin, Orléans, bœuf, huîtres, Napoléon, rhum arrangé,
  poulet boucané ; superficie affichée en km² au lieu de km.

---

## Petits correctifs (entre les phases 2 et 3) ✅

- **Double conversion dans le didacticiel** : une saisie non numérique était remplacée par `"1"` puis reconvertie ;
  le `catch` affecte désormais directement `boucle = 1`, comme le menu principal.
- **Superficie avec séparateur de milliers** (« 27 208 km² ») : une seule méthode, `Message.avecmilliers`,
  pour les deux affichages. Espace insécable U+00A0 imposée, car l'espace fine U+202F du format français
  de Java s'affiche en carré vide avec certaines polices.

---

## Phase 3 : séparer la logique de l'affichage, et ajouter des tests ✅

**Pourquoi.** Tout est encore mélangé dans `main.java` :

- les règles du jeu affichent elles-mêmes des popups : `Region.jeu()` décide si la proposition est bonne
  *et* appelle `JOptionPane`. On ne peut donc pas la tester automatiquement : en Phases 1 et 2, il a fallu
  tester à côté (exécution à la main, comparaison des décisions) ou jouer à la main ;
- deux classes pour une même région : `RegionFr` (les données) et `Region` (l'objet de jeu, une copie
  modifiable). C'est le détour laissé exprès en Phase 2, avec par exemple `pourcentage` qui doit retrouver
  la `RegionFr` à partir de la clé de la `Region` ;
- `superinterface` stocke la distance sous forme de `String`, et l'historique des essais vit dans un tableau
  de 100 cases dont 5 servent ;
- tout est dans un seul fichier, d'où les avertissements `auxiliary class … should not be accessed from
  outside its own source file` ;
- les tests de caractérisation des Phases 1 et 2 vivaient dans un dossier temporaire : ils ne sont ni
  versionnés ni relançables par quelqu'un d'autre.

**Découpage choisi** (le 12 octobre 2026) : **3.1** `Partie` d'abord, testée hors dépôt comme en Phase 2 ;
**3.2** Maven, JUnit (les tests de la 3.1 y sont réécrits) et intégration continue ; **3.3** un fichier par classe
et des packages.

**Ce qu'on ferait :**

1. une classe **`Partie`** qui contient les règles (essais restants, victoire, abandon, indice à révéler),
   sans aucun affichage ; l'interface lui transmet la saisie et affiche ce qu'elle renvoie ;
2. un **`record Essai`** (proposition, distance, pourcentage, indice) à la place de `superinterface`,
   et une `List<Essai>` à la place du tableau de 100 cases ; le jeu manipule directement les `RegionFr` ;
3. **un fichier par classe**, rangées dans des **packages** (`francle.donnees`, `francle.jeu`, `francle.interface`…) ;
4. **Maven ou Gradle** : un fichier `pom.xml` décrit le projet (version de Java, dépendances, images
   dans `src/main/resources`). N'importe quel IDE l'ouvre sans réglage manuel : ça remplace la
   configuration IntelliJ faite à la main en Phase 0 ;
5. de vrais **tests JUnit**, versionnés, qui reprennent les vérifications faites à la main : distances
   connues, recherche dans les 3 modes, pourcentages entre 0 et 100, 5 échecs = défaite… ;
6. une **GitHub Action** qui compile et lance les tests à chaque PR (✅ ou ❌ directement sur la PR).

### 3.1 : la classe `Partie` ✅

- **`Partie`** : les règles (essais, victoire, défaite, abandon), sans aucun affichage. `proposer(saisie)` renvoie
  l'`Essai` joué, ou `Optional.empty()` pour une saisie inconnue (qui ne coûte pas d'essai). L'état est un
  `enum Etat` (`EN_COURS`, `GAGNEE`, `PERDUE`, `ABANDONNEE`) au lieu du `compteur = 5` qui servait à tout.
- **`record Essai`** (proposition, distance, pourcentage) et une `List<Essai>` à la place de `superinterface`
  et de son tableau de 100 cases ; **`enum Indice`** pour l'ordre des indices.
- La distance passe dans `RegionFr` (`a.distance(b)`), le pourcentage dans `Partie`, la simplification des saisies
  dans `RegionFr.simplifier`. **`Region` et `superinterface` sont supprimées.**
- `Message.tour()` demande la saisie, la transmet à `Partie` et affiche la réponse.
- **Filet** : `JOptionPane` et `JFrame` remplacés par des doublures (`--patch-module`), 300 sessions rejouées :
  77 752 lignes identiques. Un mini-banc de 13 vérifications teste `Partie` sans aucun clic.
- **Bugs d'origine corrigés ensuite**, chacun dans son commit : le message d'abandon n'était jamais affiché
  (« C'est raté ! » à la place) ; « 1 pour avoir la liste » ne marchait qu'en mode « nouvelles régions ».

Résultat : `main.java` passe de 978 à 828 lignes.

### 3.2 : Maven, JUnit et intégration continue ✅

- **`pom.xml`** : Java 21, UTF-8, JUnit 6.1.3 (*scope* `test`, via son BOM), versions des plugins fixées,
  `target/francle.jar` exécutable (`java -jar`).
- **Maven Wrapper** (`mvnw`, `mvnw.cmd`) : Maven 3.9.16, vérifié par son empreinte SHA-256 ; fins de ligne des
  scripts imposées dans `.gitattributes`.
- **Arborescence Maven** : `src/main/java`, `src/main/resources` (images), `src/test/java`. Les images sont lues
  avec `getResource` : le jeu se lance depuis n'importe quel dossier, et depuis le `.jar`.
- **70 tests JUnit** (`PartieTest`, `RegionFrTest`, `MessageTest`), dont des tests paramétrés ; vérifiés en
  réintroduisant des bugs (inversion latitude/longitude, 6 essais).
- **Intégration continue** : `.github/workflows/ci.yml` lance `./mvnw -B verify` à chaque PR.
- **Corrigé en chemin** : les distances suivaient la langue du système (`698.45` sur une machine anglaise).
- **IntelliJ** : ouvrir le `pom.xml` remplace la configuration manuelle de la Phase 0.

### 3.3 : un fichier par classe, des packages ✅

- **Ménage** : la classe vide `commentjouer` et les brouillons en commentaire (`interfacerapide`, `Statistiques`…)
  sont supprimés ; leurs idées sont reportées dans les phases suivantes (statistiques en Phase 5, le reste en Phase 6).
- **Un fichier par classe** (11 fichiers), chacun avec ses seuls imports : plus d'avertissement « auxiliary class ».
- **Conventions de nommage** : `main` devient `Francle`, `representation` devient `Representation`.
- **Trois packages**, avec des dépendances dans un seul sens (`ui` → `jeu` → `donnees`) :

  | Package | Classes | Rôle |
  |---|---|---|
  | `fr.francle.donnees` | `RegionFr`, `ModeRegions`, `Epoque`, `Zone` | Les régions et leurs informations |
  | `fr.francle.jeu` | `Partie`, `Essai`, `Indice` | Les règles, sans affichage |
  | `fr.francle.ui` | `Francle`, `Message`, `Parametres`, `Representation` | Les popups et les images |

  (`interface`, prévu à l'origine, est un mot réservé de Java : d'où `ui`, pour *user interface*.)
- **Visibilité réduite au nécessaire** : seul ce qu'un autre package utilise est `public`.
- **Tests rangés dans le même package que leur classe** ; deux nouveaux : `RepresentationTest` (images) et
  `LancementTest`, qui vérifie la classe à lancer déclarée dans le `pom.xml` (un oubli que ni le compilateur ni les
  70 autres tests n'avaient vu : le `.jar` ne se lançait plus).

Résultat de la Phase 3 : 978 lignes dans un seul fichier → 11 fichiers rangés en 3 packages, 72 tests JUnit,
une CI, et un comportement identique au départ (hors corrections volontaires) sur 300 sessions rejouées.

**Comment le penser.** *Séparation des responsabilités* : une classe = une raison de changer (les règles
changent avec le *game design*, l'affichage avec l'interface). *Testabilité* : « si je ne peux pas tester
une règle sans cliquer, c'est qu'elle est mal rangée ».

**Ce que tu apprends :** `record`, packages et visibilité (`public`, `private`, visibilité du package),
JUnit (`@Test`, `assertEquals`), Maven (cycle `compile` / `test` / `package`, `src/main/java` et
`src/test/java`), les ressources (`getResource`) au lieu des chemins relatifs fragiles, l'intégration continue.
C'est la phase la plus formatrice sur l'architecture, et la plus grosse.

---

## Phase 4 : une vraie interface Swing ✅

**Pourquoi.** Le jeu est une cascade de popups (saisir, valider, lire, OK, recommencer) ; l'historique
est un texte dans la question suivante, et l'image s'ouvre dans une fenêtre séparée.

**Ce qu'on ferait :**

- une seule fenêtre : l'image de la région, un champ de saisie, un **tableau des essais** (`JTable`)
  qui se remplit au fur et à mesure ;
- l'**autocomplétion** des noms de régions pendant la frappe ;
- la **flèche de direction**, comme dans Worldle : le brouillon `Partie.fleche()` existe déjà. Il manque la
  conversion des degrés en radians (le même piège qu'en Phase 1), puis le découpage de l'azimut
  en 8 directions (↑ ↗ → ↘ ↓ ↙ ← ↖) ;
- les menus (paramètres, didacticiel) dans la fenêtre, au lieu de taper 0, 1, 2 ou 3.

**Découpage choisi** (le 13 octobre 2026, Claude code et explique) :

1. **4.1** la flèche de direction, en logique pure dans `jeu`, affichée tout de suite dans les popups ;
   les réglages séparés de leurs popups (prérequis des menus) ;
2. **4.2** la fenêtre principale : `JFrame`, image, champ de saisie, `JTable` des essais sur un `TableModel`,
   lancement sur l'EDT, thème **FlatLaf** (première dépendance Maven hors tests) ;
3. **4.3** la barre de menus (nouvelle partie, paramètres en cases à cocher et boutons radio, comment jouer,
   quitter) à la place des boutons ; suppression des popups de `Parametres` ;
4. **4.4** l'autocomplétion, **faite maison** (filtrage des noms par préfixe pendant la frappe) plutôt
   qu'une `JComboBox` éditable.

### 4.1 : la flèche de direction et les réglages ✅

- **`enum Direction`** (`jeu`) : les 8 directions de la rose des vents avec leur flèche (↑ ↗ → ↘ ↓ ↙ ← ↖), plus
  `SUR_PLACE` (●) pour deux régions qui partagent leur capitale (Hauts-de-France et Nord-Pas-de-Calais : Lille).
  `azimut()` calcule le cap initial en degrés, `depuisAzimut()` le range dans un secteur de 45°.
- **Deux bugs du brouillon `Partie.fleche()`** : les degrés passés tels quels à `Math.sin`, et le cap calculé
  **de la cible vers la proposition** (la flèche aurait pointé à l'envers). Le brouillon est supprimé.
- **`record Essai`** gagne un champ `direction` ; la flèche s'affiche après un essai raté et dans l'historique.
- **`Reglages`** (`ui`) : le mode de jeu et deux booléens (images, liste), sans aucun affichage, au lieu des
  trois `int` 0/1/2 de `Parametres`. `Parametres` ne garde que ses popups, et `Francle.main` perd son
  `switch` imbriqué de 45 lignes.
- **26 nouveaux tests** (98 au total) : les limites des secteurs (22,4° → nord, 22,6° → nord-est), un azimut
  vérifié par une seconde méthode indépendante (82,32° de Rennes à Strasbourg), 8 trajets entre capitales.

### 4.2 : la fenêtre principale ✅

Disposition verticale façon Worldle (choisie le 13 octobre 2026) : boutons, image, saisie et message,
tableau des essais, indices. Plus de cascade de popups pour jouer.

- **`Fenetre`** : `JFrame` en `BorderLayout` imbriqués. Elle ne contient aucune règle : chaque bouton (et la touche
  Entrée) est branché par un `ActionListener` qui transmet l'action à `Partie`, puis affiche la réponse.
  Une saisie inconnue reste dans le champ, sélectionnée, avec un message ; elle ne coûte toujours pas d'essai.
- **`TableauEssais`** (`AbstractTableModel`) : la `JTable` lit chaque case dans `partie.getEssais()`. Toujours
  5 lignes (les vides montrent les essais restants) ; flèche agrandie, coche ✓ sur l'essai gagnant.
- **Fin de partie** : l'image bascule sur la **carte de France** où la région est coloriée (`resources/indices/`,
  jamais utilisées jusqu'ici ; 13 régions de métropole, les autres gardent leur silhouette).
- **`Partie.indicesReveles()`** : quels indices sont révélés est une règle, elle passe dans `jeu`.
- **`Textes`** : toutes les phrases de la fenêtre, testables sans écran. Distances arrondies au kilomètre (`912 km`).
- **`Representation`** réécrite : `charger`, `silhouette`, `carte`, `ajuster` (les images remplissent un cadre fixe
  de 400 × 220 sans être déformées), `icone`.
- **`Francle.main`** : crée la fenêtre sur l'**EDT** (`SwingUtilities.invokeLater`), avec le thème **FlatLaf**.
- **`pom.xml`** : FlatLaf 3.7.2 ; **maven-shade-plugin** 3.6.2 pour un `.jar` « tout compris » (sinon `java -jar`
  ne trouverait pas FlatLaf).
- **Supprimés** : `Message` (sa boucle de popups, son historique en texte) et le menu « tapez 0, 1, 2 ou 3 ».
  Les paramètres passent encore par les popups de `Parametres` (bouton « Paramètres »), jusqu'à la 4.3.
- **144 tests** : `TableauEssaisTest` (cases, événements envoyés à la `JTable`), `TextesTest` (remplace
  `MessageTest`), `RepresentationTest` étendu (carte de chaque région, proportions), `indicesReveles`.
- **Vérifié à l'écran** : la vraie fenêtre lancée, des essais joués en cliquant ses boutons par programme, et
  des captures d'écran. Elles ont montré une fenêtre plus haute que l'écran (cadre réduit de 420 × 320 à
  400 × 220) et des flèches trop petites.

### 4.3 : les menus ✅

- **Barre de menus** (à la place de la barre de boutons, qui disparaît : la fenêtre passe de 624 à 593 points de
  haut) :
  - **Jeu** : Nouvelle partie (Ctrl+N), Liste des régions (Ctrl+L), Abandonner, Quitter (Ctrl+Q) ;
  - **Paramètres** : le mode en **boutons radio** (un seul choix), la silhouette et la liste en **cases à cocher** ;
  - **Aide** : Comment jouer (F1).
  Menus ouvrables au clavier (Alt+J, Alt+P, Alt+A).
- **Des `Action`** au lieu d'écouteurs séparés : « Abandonner » est partagée par le bouton et le menu, et
  `setEnabled(false)` grise les deux. En fin de partie, « Nouvelle partie » remplace « Abandonner » à côté de
  « Valider », et devient le bouton de la touche Entrée.
- **Quitter** passe par le même chemin que la croix de la fenêtre (`WINDOW_CLOSING`).
- **Supprimés** : `Parametres` (ses popups « tapez 0, 1 ou 2 ») et `ModeRegions.depuisCode`, devenus inutiles. Le
  jeu n'affiche plus aucune boîte de saisie.
- **Corrigé en chemin** : la ligne de saisie était en `FlowLayout` ; le bouton « Nouvelle partie », plus large,
  passait à la ligne et disparaissait. Elle est maintenant en `BorderLayout`.
- **145 tests** (`Textes.mode`) ; menus vérifiés par programme (raccourcis déclarés, articles grisés, un seul
  bouton radio coché, mode appliqué à la partie suivante) et fenêtre dessinée en mémoire.

### 4.4 : l'autocomplétion ✅

- **`RegionFr.suggestions(saisie, mode)`** (`donnees`) : les régions acceptées dans le mode dont le **nom** commence
  par la saisie (« nor » → Normandie), puis celles dont un **mot** commence par elle (« loire » → Pays de la Loire) ;
  chaque groupe par ordre alphabétique. Mêmes simplifications que `chercher` (accents, majuscules, tirets) ; les
  petits mots (« de », « la », « d' ») sont ignorés.
- **`Autocompletion`** (`ui`), faite maison : une `JList` posée dans le calque des popups de la fenêtre, sous le champ,
  mise à jour par un `DocumentListener` à chaque lettre. ↓ ↑ pour parcourir, Entrée pour choisir (ou valider s'il n'y
  a pas de suggestion surlignée), Échap pour fermer, clic pour choisir. Elle ne connaît ni les régions ni le jeu.
- **Paramètres** : nouvelle case « Suggestions pendant la frappe » ; deux lignes dans « Comment jouer ».
- **161 tests**, dont `AutocompletionTest` : l'autocomplétion se teste **sans écran** (un champ dans un `JRootPane`),
  en suivant les vraies liaisons des touches.

### 4.4.1 : garder la liste pour corriger l'orthographe ✅

Retour de l'utilisateur après la 4.4 : avec « bretagne » tapé en entier mais sans majuscule, la liste disparaissait.
Elle ne se ferme plus que si le nom est écrit **exactement** (`equals` au lieu de `equalsIgnoreCase`, sans `strip`).
Pour « bretagne », « BRETAGNE », « ile de france » ou « Bretagne » suivi d'une espace, elle reste et propose
l'orthographe correcte. 166 tests.

Résultat de la Phase 4 : le jeu se joue dans une seule fenêtre (image, saisie avec suggestions, tableau des essais
avec flèche de direction, indices, carte de France en fin de partie), réglée par des menus. Il n'y a plus aucun popup
de saisie. Le package `ui` est réécrit (827 lignes en 7 classes, dont la moitié de logique testée sans écran) ; `jeu`
et `donnees` n'ont reçu que des ajouts (`Direction`, `indicesReveles`, `suggestions`). 72 → 166 tests.

**Comment le penser.** La *programmation événementielle* : le programme ne déroule plus ses lignes dans
l'ordre, il **réagit** à des événements (`ActionListener`). Le *fil de Swing* (*Event Dispatch Thread*) :
tout ce qui touche à l'interface s'exécute sur un fil précis, première rencontre avec la programmation
concurrente. **La Phase 3 est un prérequis** : avec une classe `Partie` indépendante, la nouvelle
interface ne fait que la brancher ; sans elle, il faudrait réécrire les règles une seconde fois.

**Ce que tu apprends :** les événements, les layouts, `JTable`, l'EDT, la trigonométrie appliquée (azimut).

---

## Phase 5 : la région du jour et les statistiques ✅

**Pourquoi.** Francle s'inspire de Wordle et Worldle, mais il lui manque ce qui fait revenir chaque jour : une même
énigme pour tout le monde, et des statistiques qui racontent ses parties dans la durée.

**Découpage choisi** (le 13 octobre 2026, Claude code et explique) :

1. **5.1 la région du jour** : une région tirée à partir de la date, la même pour tout le monde (parmi les nouvelles
   régions), avec un numéro de partie à la Wordle. Dans le menu Jeu, « Partie du jour » et « Partie libre »
   (illimitée, comme aujourd'hui) ;
2. **5.2 les statistiques** : l'historique des parties du jour, enregistré en **JSON avec Jackson** dans le dossier de
   l'utilisateur ; parties jouées, % de victoires, série en cours et meilleure série, répartition par nombre
   d'essais ; une fenêtre de statistiques en fin de partie du jour ; la partie du jour ne se rejoue pas, même après
   avoir relancé le jeu. Seules les parties du jour comptent.

**Comment le penser.** *Déterminisme* : « la même région pour tout le monde » veut dire qu'un calcul doit donner le
même résultat sur toutes les machines, sans se parler. Le piège est subtil : la graine du hasard ne suffit pas,
l'ordre de la liste fait partie du résultat. *Persistance* : jusqu'ici, tout disparaissait à la fermeture du jeu.
Écrire sur le disque, c'est aussi prévoir le fichier absent, illisible ou d'une ancienne version.

**Ce que tu apprends :** `LocalDate` et le temps comme paramètre (pour tester n'importe quel jour), le hasard
reproductible, `java.nio.file`, la sérialisation JSON (Jackson), les tests avec un dossier temporaire (`@TempDir`).

### 5.1 : la région du jour ✅

- **`RegionDuJour`** (`jeu`) : `numero(date)` (n° 1 le 13 octobre 2026) et `pour(date)`. Les jours sont groupés en
  **cycles** de 18 jours ; à chaque cycle, les 18 nouvelles régions sont mélangées avec une graine tirée du numéro du
  cycle. Chaque région revient donc **une fois par cycle**, et **jamais deux jours de suite** (même d'un cycle à
  l'autre, grâce à un échange au besoin).
- Le résultat ne dépend que de la date et des **noms** des régions (tri par nom simplifié avant de mélanger), pas de
  l'ordre des lignes de l'enum. Un « sel » ajouté à la graine évite que le premier cycle commence par l'ordre
  alphabétique.
- **Menu Jeu** : « Partie du jour » (Ctrl+D), qui s'ouvre au lancement, et « Partie libre » (Ctrl+N), qui garde le mode
  des réglages. La partie du jour se joue toujours avec les nouvelles régions. Si on y revient le même jour, on la
  retrouve **telle qu'on l'a laissée** (essais, ou fin de partie) ; le lendemain, une nouvelle commence.
- **Titre** de la fenêtre : « Francle n° 1 », comme « Wordle 1234 » ; « Francle · partie libre ».
- **L'horloge est un paramètre** de `Fenetre` (`Clock`) : la vérification a lancé la vraie fenêtre au 1er janvier
  2030, puis l'a avancée d'un jour.
- **176 tests** (`RegionDuJourTest` : valeurs de référence figées sur des dates passées, une fois par cycle,
  jamais deux jours de suite sur 4 000 jours, uniquement les nouvelles régions).

### 5.2 : les statistiques ✅

- **Nouveau package `fr.francle.stats`**, sans Swing ni écran :
  - `Resultat` (une partie du jour terminée : date, région, propositions, état) et `PartieEnCours` ;
  - `Historique` : les résultats et la partie du jour en cours. Une partie est reconstruite en **rejouant ses
    propositions** dans une nouvelle `Partie` : ce sont les règles qui recalculent distances, indices et état ;
  - `Statistiques` : jouées, % de victoires (arrondi), série en cours, meilleure série, répartition des victoires
    par nombre d'essais. Calculées à chaque fois, jamais enregistrées ;
  - `Sauvegarde` : l'historique en **JSON avec Jackson 3.2.3** dans `~/.francle/historique.json`. Écriture dans un
    fichier temporaire puis renommage (jamais de fichier à moitié écrit) ; fichier abîmé **mis de côté** (pas
    effacé) et joueur prévenu ; champ inconnu ignoré ; numéro de version dans le fichier.
- **La partie du jour est enregistrée après chaque essai** : relancer le jeu la reprend où on l'avait laissée, et une
  partie terminée ne se rejoue plus. Les parties libres ne sont pas enregistrées.
- **Fenêtre Statistiques** (menu Jeu, Ctrl+T), ouverte d'elle-même à la fin de la partie du jour : quatre chiffres,
  puis les barres de la répartition, **dessinées à la main** (`paintComponent`), celle du jour en couleur.
- **`pom.xml`** : Jackson ; le `.jar` tout compris l'embarque (9,6 Mo).
- **Corrigé en chemin** (trouvé par un test) : deux fichiers abîmés mis de côté dans la même seconde avaient le même
  nom, et le second déplacement échouait.
- **199 tests** (`StatistiquesTest`, `HistoriqueTest`, `SauvegardeTest` avec `@TempDir`) ; parcours complet vérifié
  depuis le `.jar` avec un dossier temporaire à la place de `~/.francle`.

Résultat de la Phase 5 : Francle a sa partie du jour, la même pour tout le monde, avec son numéro ; elle survit à la
fermeture du jeu, ne se rejoue pas, et alimente des statistiques à la Wordle. 166 → 199 tests.

---

## Phase 6 : les anciennes régions, les réglages enregistrés et les options ✅

**Pourquoi.** En mode « anciennes » ou « toutes », un indice d'ancienne région s'affichait vide (« Superficie : 0 km² »),
sans silhouette ni carte : la moitié du jeu était inachevée. Et les réglages disparaissaient à chaque fermeture.

**Décisions** (le 14 octobre 2026, Claude code et explique, **une seule PR** pour toute la phase, un commit par
sous-phase) : données proposées par Claude et validées par l'utilisateur ; toutes les images régénérées par un script ;
mode « toutes » supprimé ; les indices variés reportés (voir « Idées en réserve »).

**Comment le penser.** *Rendre l'erreur impossible plutôt que d'y penser* : une région sans indices ne compile plus.
*Une seule règle quand deux disent la même chose* : sans le mode « toutes », « peut tirer » et « reconnaît » étaient
identiques. *Stocker dans une unité, convertir à l'affichage* : le jeu compte en kilomètres, les miles ne sont qu'une
façon de l'écrire.

**Ce que tu apprends :** générer ses ressources par un script, les génériques (`Sauvegarde<T>`), le *Look and Feel*
Swing et le changement de thème à chaud, `Graphics2D` et les transformations (rotation), le hasard reproductible
(encore), la compatibilité d'un fichier avec les versions passées et futures.

### 6.1 : les anciennes régions ✅

- **Indices des 17 anciennes régions** (tableau dans [indices.md](indices.md)) : superficies de l'INSEE, recoupées
  (la somme des anciennes régions donne la nouvelle, testé) ; personnalités **nées** dans la région.
- **Le constructeur sans indices disparaît** : celui des anciennes régions exige lui aussi les 4 indices et l'image.
- **Le mode « toutes » est supprimé** : on joue les anciennes **ou** les nouvelles régions. `ModeRegions` n'a plus
  qu'une règle, `contient`, pour tirer la région **et** accepter une proposition : en mode « anciennes », « Grand Est »
  est désormais refusé, comme « Alsace » en mode « nouvelles ».
- **Toutes les images régénérées** par `outils/generer_images.py` (Python, matplotlib, Pillow) à partir des contours
  officiels (IGN/INSEE, licence ouverte) : 35 silhouettes, l'image par défaut (la France et un « ? »), 35 cartes de
  fin de partie en deux séries, `cartes/nouvelles/` et `cartes/anciennes/` (les 5 régions inchangées en 2016 ont les
  deux). Fond **transparent**, palette de 256 couleurs : 1,4 Mo au lieu de 5,6 Mo.
- La silhouette s'affiche dans les deux modes (la cacher n'avait de sens qu'en mode « toutes »).

### 6.2 : les réglages enregistrés et le thème sombre ✅

- **`Sauvegarde<T>`** devient générique : le même mécanisme (JSON, écriture sûre, fichier abîmé mis de côté) sert à
  l'historique et aux réglages. Elle reste la seule classe à connaître Jackson.
- **Réglages enregistrés** dans `~/.francle/reglages.json` à chaque changement, retrouvés au lancement. Un réglage
  absent (fichier d'une version plus ancienne) garde sa valeur par défaut.
- **Thème sombre** (case du menu Paramètres) : FlatDarkLaf, appliqué **tout de suite** à toute la fenêtre (classe
  `Theme`). Vérifié dans les deux thèmes : fenêtre, tableau, statistiques, suggestions.

### 6.3 : les unités anglo-saxonnes et la silhouette tournée ✅

- **Unités anglo-saxonnes** : distances en miles et superficie en miles carrés, converties **seulement à l'affichage**
  (`Textes`) ; le tableau des essais déjà joués se réaffiche tout de suite.
- **Silhouette tournée au hasard** : un angle quelconque. Partie du jour : `RegionDuJour.angle(date)`, le même pour
  tous et après une relance ; partie libre : un vrai hasard. La carte de fin reste à l'endroit ; la case est grisée
  sans silhouette. `Representation.tourner` (`Graphics2D`) puis `rogner` (les marges transparentes).
- **189 tests** (199 avant la phase : les tests qui parcouraient les 35 régions en mode « toutes » ont disparu avec
  lui).

Résultat de la Phase 6 : les deux époques sont jouables avec leurs indices et leurs images, le jeu se souvient des
réglages, et trois options changent l'expérience (thème sombre, miles, silhouette tournée).

### Idées en réserve

- **Indices variés**, tirés parmi les personnalités alternatives de [indices.md](indices.md) : une `List` dans
  l'enum, et un tirage fait à la création de la partie. Piège : la partie du jour doit tirer les mêmes indices pour
  tout le monde et après une relance (graine tirée de la date, comme `RegionDuJour.angle`).
- **Comparaison proximité / taille de la région** (idée du code d'origine).

---

## Phase 7 : la version web ✅ (projet à part)

Prévue d'abord avec Spring Boot (un serveur et une page HTML), elle a été **réorientée le 14 octobre 2026** : un
**nouveau projet**, dans un **nouveau dépôt**, [deIbine/francle-web](https://github.com/deIbine/francle-web), en
TypeScript (Vite + React), sans serveur, inspiré de Worldle, jouable par tous sur
[deibine.github.io/francle-web](https://deibine.github.io/francle-web/). Son plan, ses décisions et ses leçons sont
dans ce dépôt-là (`docs/plan.md`, `docs/lecons.md`).

Ce projet Java lui a servi de **référence** : le web donne la même région du jour, au bit près, et ses tests
comparent distances, directions, pourcentages et suggestions à des valeurs produites par le Java
(`outils/ReferenceJava.java` du dépôt web).

**Comment le penser.** Une bonne architecture se mesure au coût d'une nouvelle interface. Ici, la troisième
(après les popups et la fenêtre Swing) a même changé de langage : ce qui a été repris, ce sont les règles et les
données, pas le code. C'est le signe qu'elles étaient bien séparées de l'affichage.

---

## Phase 8 : reprendre les nouveautés de la version web ✅

**Pourquoi.** La version web a fait évoluer le jeu : un essai et un indice de plus, des indices corrigés, une région
proposée deux fois qui ne coûte plus d'essai, l'anglais. Le jeu de bureau était resté en arrière : deux versions du
même jeu qui ne suivent plus les mêmes règles, c'est un joueur qui s'y perd, et une référence qui n'en est plus une.

**Décisions** (le 16 octobre 2026) : l'utilisateur a choisi de tout reprendre ; Claude code et explique ; une seule PR,
un commit par nouveauté. Restent propres au web : le partage en carrés emoji, le compte à rebours, les crédits.

- **Indices corrigés** : à Mayotte, « le mataba » et Zéna M'Déré ; en Nouvelle-Aquitaine, Montaigne. Les anciens
  passent dans les personnalités alternatives d'[indices.md](indices.md).
- **Une région déjà proposée** est refusée avec un message, **sans coûter d'essai**. `Partie.proposer` renvoie une
  `Reponse` **scellée** (`sealed`) : `Jouee`, `Inconnue` ou `DejaProposee`, traitée par un `switch` sur les motifs de
  records. Une partie du jour finie le reste quand on la rejoue, même si les règles ont changé depuis.
- **Le monument**, cinquième indice, révélé après le cinquième essai raté ; donc **6 essais**, comme Worldle et le web.
  La répartition des statistiques va de 1 à 6 ; la fenêtre gagne une ligne de tableau et une ligne d'indice
  (633 points de haut : elle tient toujours sur un écran portable).
- **L'anglais** : noms des régions et indices dans les deux langues (`Texte`, `Langue`), la saisie accepte les deux
  noms (« Brittany ») ; les textes de la fenêtre dans deux fichiers `ResourceBundle` (`textes_fr.properties`,
  `textes_en.properties`) et l'aide dans `aide_fr.txt`, `aide_en.txt`. Sous-menu **Langue** (celle du système par
  défaut), changement immédiat, même en pleine partie ; **miles par défaut en anglais** tant que le joueur n'a pas
  choisi lui-même son unité.
- **203 tests** (189 avant la phase).

**Ce que tu apprends :** les interfaces scellées et le *pattern matching* de Java 21, l'internationalisation
(`ResourceBundle`, `Locale`, `Collator`, `String.format`), la compatibilité des données enregistrées quand les règles
changent.

**Pour la suite.** Le dépôt web compare ses suggestions à celles du Java. Le Java cherche désormais aussi les noms
anglais : si l'on régénère un jour `reference-java.json` côté web, il faudra que le test web cherche lui aussi les
deux noms (son paramètre `nomsCherches`).

Écarts qui restent voulus avec le web : 8 directions en flèches (et non en emoji), pas de partage, des menus Swing au
lieu d'onglets. Les messages d'erreur de fichier illisible (`SauvegardeIllisible`) restent en français.

---

## Phase 9 : le bonus des départements ✅

**Pourquoi.** Idée du 16 octobre 2026 : après une victoire, en bonus, faire deviner **tous les départements** de la
région trouvée. Le bonus est fait **d'abord ici**, puis le sera dans la version web (son étape web.7, plan dans
`docs/plan.md` de [deIbine/francle-web](https://github.com/deIbine/francle-web)). Le Java a servi à trancher les
questions ouvertes ; ses réponses (plus bas) s'appliqueront au web.

**Décisions** (le 16 octobre 2026, communes aux deux projets) :

- **Après chaque victoire**, en partie du jour **comme en partie libre** (d'abord prévu pour la partie du jour
  seulement, changé après le test du jeu, voir plus bas). Le bonus d'une partie libre n'est pas enregistré.
- **Tous les départements** de la région, dans un ordre tiré de la date (le même pour tous).
- Deux modes programmés : **A. cliquer sur la carte** (« Cliquez sur : Finistère ») et **B. nommer le département
  colorié** (saisie avec suggestions). **A est le mode des joueurs** ; B se choisit dans le code
  (`Bonus.MODE = Question.NOM`), sans le montrer aux joueurs.
- **Île-de-France** : un **encart zoomé** sur Paris et la petite couronne (75, 92, 93, 94).
- Le bonus est **enregistré** et compte dans les **statistiques** (pas de partage dans le Java).
- **Contours** : le script écrit **un fichier de contours par région** (`resources/departements/<région>.json`),
  chaque département étant un tracé à part, avec son numéro.

**Réponses aux questions ouvertes** (le 16 octobre 2026, avant de commencer) :

1. **Essais par département** : **un seul** ; juste ou faux, on montre la réponse (vert ou rouge) et on passe au
   suivant.
2. **Régions à un seul département** (outre-mer) : on demande **le numéro** (« Quel est le numéro de : Guadeloupe ? »).
3. **Où se joue le bonus** : dans la **fenêtre principale**, à la place de la silhouette.
4. **Passer le bonus** : un bouton **« Passer »**. Un bonus **commencé** (au moins une réponse) puis passé ou quitté,
   même en fermant le jeu, **compte comme joué** avec son score partiel, **sans reprise**. Passé avant toute réponse,
   il n'a pas commencé : il reste à jouer.
5. **Les statistiques** : le **score moyen** (moyenne des pourcentages de départements trouvés) et le nombre de bonus.
6. **Choisir A ou B de notre côté** : une **constante dans le code** (`Bonus.MODE`).
7. **Mode B** : le **nom ou le numéro** sont acceptés (« 29 »).
8. **Anciennes régions** : leurs contours sont **générés dès maintenant** (même script).
9. (Propre au web) **Le partage** : un **score** (« 🗺️ 9/12 »).

**Ce qui a été fait** (PR [#34](https://github.com/deIbine/francle/pull/34), un commit par étape) :

1. **Les données** : `resources/departements/departements.json` (101 départements, ceux de chaque région), lu par le
   Java (`Departement`, `Decoupage`, `RegionFr.getDepartements()`) **et** par le script Python, qui n'a plus sa propre
   liste.
2. **Les contours** : `outils/generer_images.py` écrit un fichier par région (35), simplifié par Douglas-Peucker,
   trous compris, et le cadre du zoom de l'Île-de-France.
3. **Les règles** : `jeu.Bonus` (ordre tiré de la date, une réponse par département, passer, score ; questions
   `CARTE`, `NOM`, `NUMERO`), `ReponseBonus`, `Tentative`.
4. **L'affichage** : `ui.CarteDepartements` (dessin des `Path2D`, `AffineTransform`, clic par `contains`, survol,
   encart de Paris, bulles d'aide une fois fini), `ui.Contours` ; dans `Fenetre`, un `CardLayout`, les boutons
   « Passer » et « Bonus », Ctrl+B, un `Timer` après chaque réponse. Le bonus commence à la fermeture de la fenêtre
   des statistiques d'une victoire du jour.
5. **Enregistrement et statistiques** : `Resultat.bonus` (numéros donnés, `null` si pas joué), enregistré à chaque
   réponse ; les anciens fichiers restent lisibles ; score moyen dans la fenêtre des statistiques.
6. **Les finitions** : textes FR et EN, aide, README, [leçons](lecons.md#phase-9--le-bonus-des-départements).

**Retour du test et nouveautés du web** (le 16 octobre 2026, dans la même PR) :

- **Le bonus est obligatoire après n'importe quelle victoire** : il commence de lui-même aussi en partie libre,
  dans un ordre au hasard (`Bonus.auHasard`), avec les départements de l'époque jouée. « Passer » reste possible.
  Le bonus d'une partie libre n'est ni enregistré ni compté, comme la partie libre elle-même.
- **Les améliorations de la version web**, reprises après comparaison des deux projets :
  1. **Entrée sur un début de nom** joue la seule région qu'il évoque (« bret » -> Bretagne) ; s'il en évoque
     plusieurs, un message demande de choisir, sans coûter d'essai (`Reponse.Plusieurs`).
  2. **Confirmation avant d'abandonner** (Oui / Non, dans la langue du jeu).
  3. **Partage du résultat du jour** en carrés, copié dans le presse-papiers (menu Jeu, Ctrl+Maj+C, ou bouton
     « Partager » des statistiques), avec 🌀 / 🙈 et le score du bonus (🗺️ 3/4).
  4. **Modes difficiles fixés au premier essai** (`ModesDifficiles`), enregistrés avec la partie du jour ; avant le
     premier essai, un changement de silhouette compte tout de suite.
  5. **Compte à rebours** jusqu'à la région suivante, dans la fenêtre des statistiques.
  6. **Changement de jour à minuit**, même en pleine partie, si la partie du jour est à l'écran.
  7. **Liste des régions cliquable** : un clic propose la région ; celles déjà proposées sont barrées.
  8. **« Comment jouer » au premier lancement**, avec un exemple calculé par les règles et les crédits.
- Laissé de côté, propre au navigateur : la synchronisation entre onglets, le thème « automatique », les directions
  en emoji dans le tableau (elles le sont dans le partage).
- Un test vérifie maintenant que chaque clé de texte écrite dans le code existe dans les fichiers de textes.

**Côté web** : ces réponses sont reportées dans le plan de [deIbine/francle-web](https://github.com/deIbine/francle-web)
(étape web.7.0.1, le 17 octobre 2026), y compris le bonus après **chaque** victoire ; reste à y faire le bonus.

**Un écart voulu entre les deux versions : la reprise** (décidé le 17 octobre 2026). Ici, un bonus commencé puis
quitté compte comme joué avec son score partiel, **sans reprise**. Dans la version web, un bonus du jour commencé
puis quitté (page fermée, onglet rechargé) **reprend là où il en était** au retour : dans un navigateur, fermer un
onglet par erreur arrive souvent. « Passer » un bonus commencé le termine dans les deux versions. Le Java reste tel
quel.

**Trois autres choix du web** (notés le 17 octobre 2026, à la phase 10) : la version web n'a jamais eu de minuterie
entre deux questions ; en mode B, Entrée sur un début de nom y joue le seul département évoqué ; un bonus pas commencé
y attend son bouton jusqu'à minuit. Les trois valent maintenant ici aussi : la minuterie a disparu avec le mode C,
Entrée sur un début de nom marche en mode C, et le bonus pas commencé attendait déjà son bouton.

**Ce que tu as appris :** dessiner soi-même en Swing (`paintComponent`, `Graphics2D`, `Path2D`), les clics de
souris (`MouseListener`) et le test « ce point est-il dans cette forme ? » (`Shape.contains`), le zoom avec
`AffineTransform`, lire des contours en JSON avec Jackson, et faire évoluer un fichier enregistré sans casser les
anciens.

---

## Phase 10 : le bonus revu ✅

**Pourquoi.** Retours des joueurs de la version web, le 17 octobre 2026 : (1) le bonus doit commencer **juste après
la victoire**, au lieu d'attendre la fermeture des statistiques ; (2) plutôt que de chercher sur la carte un
département nommé, on **clique sur un département, puis on écrit son nom**, comme on écrit une région. Le web l'a fait
d'abord (étapes web.7.5 à web.7.7, plan dans `docs/plan.md` de
[deIbine/francle-web](https://github.com/deIbine/francle-web)) ; le Java reprend les mêmes décisions.

**Les décisions** (celles du web, le 17 octobre 2026, et leurs réponses propres au Java) :

- Après une victoire, du jour ou libre, **plus de fenêtre** : le bonus prend tout de suite la place de la silhouette.
  Les résultats s'ouvrent **à la fin du bonus**, après une pause d'environ 1,5 s (le temps de voir la couleur de la
  dernière réponse), ou tout de suite quand on le passe. Un bonus passé avant toute réponse reste à jouer (bouton
  « Bonus »).
- Sous le bonus : **« Résultats »** pour la partie du jour, **« Partie libre »** pour une partie libre (qui abandonne
  le bonus sans rien demander, rien n'en étant compté).
- **Partie libre** : le même enchaînement. Comme sur le web, une **fenêtre de fin** s'ouvre (fin du bonus, défaite
  ou abandon) : le message de fin et un bouton « Partie libre ».
- Au lancement, si la partie du jour est déjà finie, ses **résultats s'ouvrent**, comme sur le web. Le Java garde
  son écart : **pas de reprise** d'un bonus commencé.
- **Le mode C** : on clique un département (bleu foncé ; on peut changer d'avis), puis on écrit son nom ou son
  numéro. Suggestions parmi les **101 départements** ; une seule réponse par département ; un département **déjà
  trouvé** est refusé sans rien coûter ; un département d'une autre région est une réponse fausse ; Entrée sur un
  début de nom joue le seul département qu'il évoque ; aucune sélection automatique après une réponse. Outre-mer :
  toujours la question du numéro. A et B restent dans le code, cachés (`Bonus.MODE`).
- **La saisie** est grisée avant le choix, avec « Cliquez d'abord la carte » ; elle reçoit le focus au choix.
- **Le clavier** (réponse propre au Java) : Tab jusqu'à la carte, les **flèches** d'un département à l'autre, Entrée
  pour le choisir. À la victoire, le focus va sur la carte.
- **L'enregistrement** : des **paires** (demandé, donné) à la place des seuls numéros donnés ; l'ancien format se relit
  avec l'ordre du jour.
- Les textes du Java restent au **vouvoiement**. **Trois PR** empilées, une par étape.

**Ce qui a été fait :**

1. **10.1 L'enchaînement** (`Fenetre`) : une victoire lance `jouerBonus()` ; `montrerResultats()` est l'unique entrée
   vers les résultats (statistiques du jour, ou fenêtre de fin d'une partie libre), appelée à la fin du bonus par un
   `Timer` de 1,5 s, quand on le passe, après une défaite ou un abandon, et au lancement.
2. **10.2 Les règles** (`Bonus`, `ReponseBonus`, `Resultat`) : la question `CHOIX`, `repondre(choisi, donne)` et
   `repondre(choisi, saisie)`, `restants()`, `trouve()`, `relire()` ; deux cas de plus dans `ReponseBonus`
   (`Plusieurs`, `DejaTrouve`) ; `Resultat.paires`. `MODE` est resté `CARTE` jusqu'à 10.3, pour que le jeu reste
   jouable entre les deux.
3. **10.3 L'affichage** : `CarteDepartements` choisit au lieu de répondre, se parcourt au clavier et dessine le focus ;
   la minuterie entre deux questions disparaît ; `MODE` passe à `CHOIX` ; aide et textes en français et en anglais ;
   [leçons](lecons.md#phase-10--le-bonus-revu).

**Ce que tu as appris :** déplacer un moment du jeu sans le dupliquer (une seule méthode pour « ouvrir les
résultats »), lire deux formats et n'en écrire qu'un, laisser le compilateur montrer les `switch` à compléter,
rendre un composant dessiné à la main utilisable au clavier (`InputMap`, `ActionMap`, focus), et garder un jeu
jouable entre deux commits.

---

## Phase 11 : les corrections relevées en jouant ✅

**Faite le 18 octobre 2026**, en trois PR empilées : deIbine/francle#40 (11.1), #41 (11.2), #42 (11.3). Ce qui a été
fait, en plus des décisions plus bas :

- **11.1** : l'autocomplétion reçoit une action qui dit si la saisie est acceptée (`BooleanSupplier`) ; refusée, la
  liste se rouvre. Un champ vide ne fait rien (`Fenetre.jouer()`).
- **11.2** : `Reponse.AutreEpoque`, cherchée **avant** les suggestions (sinon « Bourgogne » jouait
  Bourgogne-Franche-Comté) ; `RegionFr.autreEpoque()` déduit le lien des départements ; le Centre, seulement renommé,
  a sa phrase (« aujourd'hui appelée Centre-Val de Loire ») ; tous les indices une fois la partie finie, dès la fin
  (abandon compris), et l'aide le dit. Le message de « Grand Est » était coupé : une classe `Message` le passe sur
  deux lignes quand il le faut.
- **11.3** : une phrase par département d'outre-mer (`bonus.numero.971`…) ; `Departement.nomEtNumero()` dans les
  suggestions, relu par `Departement.chercher`.

Les leçons sont dans [lecons.md](lecons.md#phase-11--les-corrections-relevées-en-jouant). Le point 12 (la carte de
fin) reste à faire.

**Pourquoi.** Le 18 octobre 2026, la version web a été testée de bout en bout (tests, jeu dans le navigateur, sur
ordinateur et à la taille d'un téléphone). Chaque problème trouvé a été comparé au Java, en lisant son code et en
exécutant ses règles dans `jshell` (`jshell --class-path "target/classes:<jars Jackson>"`, puis
`new Partie(...).proposer("")`). Ceux qui suivent existent **aussi dans le Java**. La liste complète, avec ceux
propres au web, est dans `docs/plan.md` de [deIbine/francle-web](https://github.com/deIbine/francle-web), section
« Les corrections relevées le 18 octobre 2026 ».

**Découpée et décidée le 18 octobre 2026, pas commencée.** Les décisions sont **celles du web**, validées le même
jour (section « Les décisions (validées le 18 octobre 2026) » du `docs/plan.md` du web), adaptées au Java plus bas.
Le web et le Java se font **en même temps, dans deux conversations différentes** (décidé le 18 octobre 2026) : celle
du Java part de cette phase, sans attendre le web. Les numéros entre parenthèses sont ceux de la liste du web.

### Pour reprendre (conversation du Java)

1. Vérifier que `main` est à jour (`git pull`) et que la PR de ce plan (phase 11, deIbine/francle#39) est fusionnée.
2. Lire cette phase, puis `docs/lecons.md`. Les décisions ci-dessous sont **déjà validées** : ne pas les reposer,
   seulement présenter le découpage à l'utilisateur et attendre son accord avant de coder.
3. Une branche et une PR par étape (11.1, 11.2, 11.3), empilées, comme à la phase 10.

### Le découpage (trois PR, comme le web)

1. **11.1 Les bugs** (points 1 et 4) : `Autocompletion`, `Fenetre.jouer()`, `Partie.proposer()`, tests JUnit.
2. **11.2 La saisie et la fin de partie** (points 5 et 6) : une nouvelle réponse `Reponse.AutreEpoque` (le
   compilateur montrera les `switch` à compléter), le lien entre les époques déduit de `Decoupage` (les départements
   de chaque région), `Partie.indicesReveles()` en fin de partie, textes FR et EN, tests.
3. **11.3 Le bonus** (points 7 et 8) : `textes_*.properties` (`bonus.numero`), les suggestions de départements
   avec leur numéro, et la saisie qui les accepte, tests.

Le point 12 (carte de fin) est remis à plus tard, comme sur le web.

### Les décisions (celles du web, adaptées au Java)

| Point | Décision |
|---|---|
| (1) « Plusieurs » | La liste **reste ouverte** sur les régions (ou départements) évoquées, **sans ligne sélectionnée** : deux Entrée de suite ne jouent pas une région au hasard. Dans `Autocompletion.entree()`, ne fermer la liste que si la saisie est acceptée (ou la rouvrir quand la réponse est `Plusieurs`). |
| (4) Saisie vide | **Rien ne se passe**, sans message (ni dans la partie, ni dans le bonus). |
| (5) Autre époque | Refusée **sans coûter d'essai**, sans article, **au vouvoiement** si une phrase en demande : partie du jour, « Alsace : une ancienne région (avant 2016), aujourd'hui dans la région Grand Est. » ; partie libre, anciennes régions, « Grand Est : une région d'après 2016, qui regroupe Alsace, Champagne-Ardenne et Lorraine. » Lien entre les époques **déduit des départements**, sans nouvelle donnée. En anglais aussi. |
| (6) Indices | **Tous montrés** une fois la partie finie (gagnée, perdue ou abandonnée), sans distinction. Le partage ne change pas. |
| (7) Question d'outre-mer | « Bonus · Quel est le numéro de la Guadeloupe / de la Martinique / de la Guyane / de La Réunion / de Mayotte ? » (l'article des cinq départements d'outre-mer, dans les textes) ; en anglais « What is the number of Guadeloupe? ». Le **focus** va déjà dans la saisie : rien à changer. |
| (8) Suggestions | **« Finistère (29) »** partout dans le bonus ; choisir une suggestion répond bien avec ce département. |
| (12) Carte de fin | **Plus tard**, dans sa propre étape. |

Ne concernent que le web : les points 2, 3, 9, 10 et 11 (voir plus bas), et la PR d'outillage des tests.

**Les bugs :**

- **(1) « Choisissez dans la liste », mais la liste est fermée.** En partie libre, anciennes régions, taper « nor »
  puis Entrée : le message dit de choisir dans la liste, qui vient de se fermer (il faut retaper une lettre). Même
  chose dans le bonus avec « corse ». Cause : `Autocompletion.entree()` appelle `fermer()` avant `valider.run()`,
  que la saisie soit acceptée ou non. Piste : garder (ou rouvrir) la liste quand la réponse est `Plusieurs`.
- **(4) Une saisie vide est « une région non reconnue ».** Valider un champ vide affiche « «  » n'est pas une région
  reconnue : réessayez. », avec des guillemets vides (`proposer("")` renvoie `Inconnue`). Piste : ne rien faire, ou
  un message « Écrivez le nom d'une région ».

**Les améliorations :**

- **(5) Une ancienne région en partie du jour.** « Alsace » répond « n'est pas une région reconnue » alors qu'elle
  existe (`proposer("Alsace")` renvoie `Inconnue`, car `ModeRegions.contient` l'écarte). Piste : « L'Alsace est une
  ancienne région, aujourd'hui dans le Grand Est », sans coûter d'essai (une nouvelle `Reponse`, par exemple
  `AutreEpoque`). Et l'inverse en partie libre, anciennes régions.
- **(6) Les indices restent cachés à la fin.** Une partie gagnée au 2e essai ne montre que la superficie
  (`indicesReveles()` vérifié dans `jshell`) : la capitale et le monument ne se voient jamais. Piste : tout révéler
  une fois la partie finie, pour apprendre.
- **(7) La question du numéro, en outre-mer.** « Bonus · Quel est le numéro de : La Réunion ? » : les deux-points
  sont maladroits (`bonus.numero` dans les textes). Piste : « Quel est le numéro de La Réunion ? », ce qui demande
  l'article de chacun des cinq départements d'outre-mer (« de la Guadeloupe », « de la Guyane », « de Mayotte »…) :
  sans doute la raison des deux-points. Le focus, lui, va déjà dans la saisie : rien à changer ici (le web, lui,
  doit le corriger).
- **(8) Les numéros dans les suggestions de départements.** « corse » propose « Corse-du-Sud, Haute-Corse », sans
  2A ni 2B (`Departement::nom` dans `Fenetre.suggestions`). Piste : « Finistère (29) », pour apprendre les numéros en
  passant ; la saisie doit alors accepter la suggestion choisie avec son numéro.
- **(12) La carte de fin.** Elle ne colore que la réponse (des images toutes faites, `Representation.carte`).
  Piste, déjà notée côté web : montrer aussi les régions proposées ; demande de dessiner la carte (comme le web avec
  ses contours), donc une étape plus longue.

**Ce qui ne concerne pas le Java** (vérifié) : le pourcentage des statistiques (le Java passe déjà par
`textes.pourcentage`), la réponse lisible dans le nom de l'image (propre au navigateur), l'affichage sur téléphone et
le « survol » au doigt, et le score du bonus dans les résultats d'une partie libre (`messageDeFin()` l'ajoute déjà).

---

## Phase 12 : la liste des départements

**Décidée le 19 octobre 2026, pas commencée** ; à faire **plus tard** (décidé le 19 octobre 2026), dans la
conversation du Java. Le code attend l'accord de l'utilisateur. **Le web l'a déjà faite** le 19 octobre 2026
(web.7.12, PR deIbine/francle-web#36) : son code et ses leçons (`docs/lecons.md` du web, section web.7.12) peuvent
servir de modèle.

**Pourquoi.** Idée de l'utilisateur, le 19 octobre 2026 : le bonus n'a pas de liste, contrairement à la partie (menu
Jeu, « Liste des régions », Ctrl+L). Pendant le bonus, cette commande ouvrirait donc la **liste des départements**.
Les décisions ont été prises dans la conversation du web (section « La liste des départements (web.7.12) » du
`docs/plan.md` de [deIbine/francle-web](https://github.com/deIbine/francle-web)), et valent pour le Java, adaptées
plus bas. Le web et le Java se font chacun dans sa conversation, sans s'attendre.

### Pour reprendre (conversation du Java)

0. **Le ménage du retour sur le PC : fait le 23 octobre 2026**, par la conversation du web (compte rendu complet
   dans « À faire sur le PC » du `docs/plan.md` de [deIbine/francle-web](https://github.com/deIbine/francle-web),
   PR deIbine/francle-web#39). Ici : étiquette `phase-12.0` posée sur `f16f013` ; la branche du cloud
   `ccr-8009fd8d-mbx5ca` était déjà supprimée sur GitHub (sans doute par une autre session) ; tests verts,
   **403 sur 403**, avec `./mvnw -q test` (Maven n'est pas installé : le projet apporte le sien, le Maven Wrapper).
   Si on retravaille dans le cloud, qui ne peut ni poser d'étiquette ni supprimer de branche, la liste des choses à
   faire ensuite sur le PC se tient de nouveau dans ce plan web.
1. Vérifier que `main` est à jour (`git pull`) et que la PR de ce plan est fusionnée.
2. Lire cette phase, puis `docs/lecons.md`. Les décisions ci-dessous sont **déjà validées** : ne pas les reposer,
   seulement présenter le découpage (une seule PR, 12) et attendre l'accord de l'utilisateur avant de coder.

### Les décisions (celles du web, adaptées au Java)

| Sujet | Décision |
|---|---|
| Contenu | **Les 101 départements**, pas seulement ceux de la région (choix de l'utilisateur : « ça serait trop simple sinon ») : la liste limitée à la région donnerait toutes les réponses. L'**époque ne joue donc aucun rôle** : les départements sont les mêmes dans les deux. |
| Affichage | **Nom et numéro** (« Finistère (29) », comme les suggestions, `Departement.nomEtNumero()`), **triés par nom** (`Collator` de la langue, comme la liste des régions). |
| Un clic | Il **répond pour le département choisi** sur la carte, comme un clic dans la liste des régions propose la région. Tant qu'aucun département n'est choisi, les noms sont **grisés**, avec « Cliquez d'abord sur un département » (vouvoiement). |
| Déjà répondus | **Barrés et grisés**, comme les régions déjà proposées, sans vert ni rouge (ils sont sur la carte). |
| Outre-mer | La question y est le numéro : la liste, avec ses numéros, la donnerait. La commande reste donc **celle des régions** pendant un bonus d'outre-mer. |
| Quand | Les départements **tant que le bonus est affiché**, même fini (tout grisé, avec « Le bonus est fini »). Les régions de nouveau quand la silhouette revient (bonus passé avant toute réponse, nouvelle partie). |
| La commande | **La même** (menu Jeu, Ctrl+L), dont le nom change : « Liste des départements » pendant le bonus (`action.liste` et un nouveau texte). |
| Réglage et aide | La case « Liste des régions » des paramètres (`parametre.liste`, `Reglages.avecListe`) vaut pour les deux listes, son texte le dit ; une phrase dans l'aide. En français et en anglais. |
| Déjà répondus, précisé | (Décidé au découpage du web, le 19 octobre 2026.) Sont barrés **tous les départements dont la place sur la carte est déjà jouée, trouvés comme manqués** : les donner ailleurs ne peut être que faux (ou refusé, pour un département trouvé), et leur nom est déjà sur la carte. Seuls des départements de la région peuvent donc être barrés. |
| Après un clic | (Idem.) La liste se ferme, la réponse est donnée, et le **focus revient là où il était** (sur le web, le bouton 📃 ; en Swing, la fenêtre principale, comme à la fermeture de la liste des régions). |

**Ce que le web a fait, utile pour le Java** (PR deIbine/francle-web#36) :

- Une fonction du domaine décide si le bonus a sa liste (`avecListe` : seulement le mode C, donc pas la question du
  numéro), une autre donne les départements à barrer (`repondus`) : l'équivalent Java irait dans `Bonus`.
- La fenêtre est gardée par le composant du bonus, qui connaît le département choisi ; la réponse passe par la même
  méthode que la saisie (mêmes messages, même refus d'un département déjà trouvé).
- Textes du web, à mettre au vouvoiement : « Liste des départements », « Cliquez d'abord sur un département de la
  carte. », « Cliquez sur le nom du département choisi sur la carte. », « Le bonus est fini. », « déjà répondu ».
- Le piège du web (un compteur de demandes partagé entre deux fenêtres) n'existe pas en Swing : l'action du menu
  regarde l'état du jeu au moment du clic et ouvre la bonne liste. Il faut en revanche **renommer l'action** (et son
  article de menu) quand le bonus commence et finit, comme le web change le nom du bouton.
