# Leçons apprises

Ce qu'il faut retenir de chaque phase d'amélioration de Francle, pour pouvoir le refaire seul.
Chaque leçon répond à trois questions : **pourquoi** c'est important, **comment le faire** concrètement,
et **comment le penser** (la question à se poser la prochaine fois).

Le détail de ce qui a été fait dans le jeu se trouve dans la [feuille de route](feuille-de-route.md).

**Sommaire**

- [Méthode générale](#méthode-générale)
- [Phase 0.5 : un dépôt Git propre](#phase-05--un-dépôt-git-propre)
- [Phase 0 : l'environnement de développement](#phase-0--lenvironnement-de-développement)
- [Phase 1 : corriger des bugs](#phase-1--corriger-des-bugs)
- [Phase 2 : centraliser les données (refactoring)](#phase-2--centraliser-les-données-refactoring)
- [Petits correctifs](#petits-correctifs)
- [Phase 3.1 : séparer les règles de l'affichage](#phase-31--séparer-les-règles-de-laffichage)
- [Phase 3.2 : Maven, JUnit et intégration continue](#phase-32--maven-junit-et-intégration-continue)
- [Phase 3.3 : un fichier par classe, des packages](#phase-33--un-fichier-par-classe-des-packages)
- [Aide-mémoire](#aide-mémoire)

---

## Méthode générale

Les réflexes qui reviennent dans toutes les phases.

### 1. Reproduire, corriger, revérifier avec le même test

Avant de corriger un bug, **prouve-le** avec un test qui échoue. Corrige. Relance **exactement le même test** :
il doit passer. Relance aussi les tests des corrections précédentes (**non-régression**) : une correction
ne doit pas en casser une autre.

> *La question à se poser :* « Comment saurai-je que c'est réglé, et que je n'ai rien cassé à côté ? »

### 2. Vérifier, ne pas supposer

Quand un outil annonce un résultat inattendu (« 8 lignes modifiées » au lieu de 10, une branche « non fusionnée »,
27 cas sur 35 au lieu de 35), **on regarde pourquoi** avant de continuer. C'est souvent là que se cachent les bugs.

### 3. Une modification par commit

Un commit = une idée. Ne mélange pas « réorganiser » et « changer le comportement » : si un problème apparaît,
on doit pouvoir dire quel commit l'a introduit. Messages en français, à l'impératif :
« Corrige le calcul de distance », pas « Update main.java ».

### 4. Écouter les outils

Le compilateur et l'IDE voient ce que l'œil rate, à condition de le leur demander et de lire leurs messages :
`javac -Xlint:all`, les soulignements jaunes et le code grisé d'IntelliJ.

### 5. Si c'est difficile à tester, c'est mal rangé

La difficulté à tester un morceau de code est un **diagnostic** : elle mesure à quel point ses responsabilités
sont mélangées. En Phase 3.1, vérifier l'ancien `jeu()` a demandé de fausses fenêtres et 300 sessions rejouées ;
une fois les règles rangées dans `Partie`, quelques lignes suffisent, sans écran ni clavier.

> *La question à se poser :* « Puis-je vérifier cette règle sans cliquer ? » Si non, sépare d'abord ce qui
> **décide** (les règles) de ce qui **montre** (l'affichage), puis teste ce qui décide.

---

## Phase 0.5 : un dépôt Git propre

### Une branche n'est qu'une étiquette

Une branche Git n'est **pas une copie du code** : c'est un petit fichier qui contient l'identifiant d'un commit.
Supprimer une branche fusionnée retire l'étiquette, pas le travail, qui vit dans `main`.

- **Pourquoi supprimer les branches fusionnées :** une branche ouverte signifie « travail en cours ».
  Si les branches mortes s'accumulent (16 branches `patch-*` au départ), ce signal disparaît, on risque
  de travailler sur une vieille base, et le moindre `git branch` devient du bruit.
- **Comment, sans risque :**
  - merge classique : `git branch --merged main` liste les branches déjà fusionnées, `git branch -d` (minuscule)
    refuse de supprimer une branche non fusionnée ;
  - **squash merge** (le cas de ce projet) : GitHub crée un *nouveau* commit, donc Git croit la branche non
    fusionnée. On compare l'**état des fichiers** :
    `git rev-parse main^{tree} ma-branche^{tree}` ; si les deux empreintes sont identiques, `git branch -D` ;
  - sur GitHub : `git push origin --delete ma-branche`, puis `git fetch --prune` en local.
- **Filets de sécurité :** le bouton *Restore branch* sur la page de la PR, et `git reflog` en local (environ 90 jours).

### Étiquette (tag) contre branche

Une **branche** est un marque-page qui avance avec le travail. Une **étiquette** est une épingle fixe sur un instant précis.
Pose-en une à la fin d'une étape importante, ou avant une modification risquée.

```bash
git tag -a phase-1 f4a6172 -m "Phase 1 : correction de quatre bugs"   # étiquette annotée (avec message)
git push origin phase-1
git tag -n5                       # liste avec les messages
git show phase-1                  # message, commit et modifications
git diff phase-0 phase-1 --stat   # ce qui a changé entre deux étapes
git switch --detach phase-0.5     # revenir temporairement à cette version (puis git switch main)
```

### Ne versionner que ce qui reconstruit le projet

Le `.gitignore` exclut ce qui est **généré** (`.class`, `out/`, `target/`) ou **propre à une machine**
(`.idea/`, `.DS_Store`, `Thumbs.db`).

> *La question à se poser :* « Quelqu'un qui clone le dépôt a-t-il besoin de ce fichier pour construire le projet ? »

### Les fins de ligne

Windows termine les lignes par `\r\n` (CRLF), Linux et macOS par `\n` (LF). Un dépôt qui mélange les deux produit
des diffs illisibles. Le fichier `.gitattributes` (`* text=auto`) stocke tout en LF dans Git et convertit selon
le système de chacun. Après l'avoir ajouté : `git add --renormalize .`, puis vérifier avec
`git diff --cached --ignore-cr-at-eol` que seul le format a changé, pas le contenu.

### Renommer avec Git

`git mv ancien nouveau` plutôt qu'un déplacement à la main : Git comprend que c'est un **renommage** et garde
l'historique du fichier (`git log --follow`).

### Les noms de fichiers

En minuscules, sans espaces ni accents (`bourgogne-franche-comte.png`, pas `Bourgogne Franche-Comté.png`).
Les accents et les espaces posent problème dans les scripts, les URL et les archives.
L'extension doit dire la vérité : du pseudo-code n'est pas un fichier `.java`, sinon les outils essaient de le compiler.

### Les conventions de GitHub

`LICENSE` (et pas `LICENCE.MD`) pour que GitHub reconnaisse la licence. Un bon README contient : ce qu'est le
projet, une image, les règles, **comment le lancer**, la structure, la feuille de route, les crédits et la licence.
Ce n'est pas un bloc-notes : les tâches vont dans la feuille de route ou dans des *issues*.

### Ne pas réécrire l'historique partagé

`rebase` et `push --force` sur `main` cassent les liens vers les anciennes PR et le travail des autres.
On garde le passé tel quel et on fait propre à partir de maintenant.

---

## Phase 0 : l'environnement de développement

### JRE, JDK et versions de Java

- **JRE** (*Runtime Environment*) : exécute les `.class`. **JDK** (*Development Kit*) : le JRE plus les outils de
  développement (`javac`, `jar`, le débogueur…). Pour développer, il faut un JDK.
- **LTS** (*Long Term Support*) : Java sort une version tous les 6 mois, mais seules certaines sont maintenues
  plusieurs années (17, 21, 25…). Préférer une LTS : les versions 18 et 19 n'ont été maintenues que 6 mois.

### Les variables d'environnement

- **`PATH`** : la liste des dossiers où Windows cherche une commande. Il prend le **premier** `java.exe` trouvé :
  l'ordre compte (`where.exe java` pour voir lequel).
- **`JAVA_HOME`** : indique aux outils de build (Maven, Gradle) où se trouve le JDK.
- Un terminal déjà ouvert garde l'**ancien** `PATH` : il faut en ouvrir un nouveau après une installation.

### Compiler et lancer à la main

```bash
javac -encoding UTF-8 -d out main.java   # compile le source en bytecode (.class) dans out/
java -cp out main                        # lance la classe main, en cherchant les .class dans out/ (classpath)
```

`-encoding UTF-8` évite que les accents du code soient mal lus. Les chemins relatifs (comme `regions/`) partent
du **dossier depuis lequel on lance** le programme.

### Ce qu'un IDE doit savoir d'un projet

IntelliJ ne devine rien : il lit sa configuration dans `.idea/`.

- le **source root** : où est le code à compiler ;
- les dossiers **exclus** : ici `archive/`, qui contient une seconde classe `main` (« classe en double ») ;
- le **SDK** : le JDK utilisé ;
- la **run configuration** : quelle classe lancer, et depuis quel **répertoire de travail**.

Avec Maven ou Gradle (Phase 3), tout ça est décrit dans un fichier versionné, et n'importe quel IDE sait ouvrir le projet.

### Installer des logiciels proprement

- Un installeur lancé en mode `/quiet` ne peut pas afficher la fenêtre d'autorisation (UAC) : il **échoue
  sans rien dire** (code 1603). Utiliser `/passive`, ou lancer explicitement en administrateur.
- `curl -C -` **reprend** un téléchargement interrompu au lieu de tout recommencer.
- Toujours **vérifier l'empreinte SHA-256** d'un installeur téléchargé contre celle publiée par l'éditeur.

---

## Phase 1 : corriger des bugs

### Tester avec une valeur connue d'avance

**Bug :** la formule de distance inversait latitude et longitude (Rennes → Strasbourg : 1050 km au lieu de 700).

- **Comment le repérer :** comparer le résultat avec une valeur trouvée ailleurs (une distance réelle).
  Un écart **régulier** signe une erreur de formule : ici +50 % uniquement en est-ouest, car le code comptait
  un degré de longitude pour 111 km au lieu de 111 × cos(48°) ≈ 74 km (1 / cos 48° ≈ 1,5).
- **Comment corriger :** écrire la formule de référence à côté du code et comparer **terme à terme**.
  Découper les longues expressions en **variables nommées** (`lat1`, `lat2`, `deltaLon`) rend les inversions visibles.

> *La question à se poser :* « Quel cas puis-je calculer de tête ou trouver ailleurs pour vérifier mon code ? »

### Les pièges des nombres à virgule (`double`)

- Les arrondis peuvent donner `1.0000000000000002` là où on attend `1` : `Math.acos` renvoie alors `NaN`.
  Protection : `Math.min(1.0, valeur)`. Sur un million de latitudes au hasard, 3,8 % des cas tombaient dans le piège.
- **Ne jamais comparer des `double` avec `==`** : une distance « nulle » peut valoir 0,000095 km.

### La portée d'une variable

**Bug :** une nouvelle partie affichait les essais de la précédente, car la variable `essai` était déclarée
pour tout le programme, et non pour une partie.

- **Comment le repérer :** l'**exécution à la main**, en suivant la variable dans un tableau, tour après tour.
- **Comment corriger :** à la racine (la portée), pas avec un pansement. `essai = 0;` aurait marché, mais le piège
  serait resté. Un compteur de boucle `for (int i = 0; …)` n'existe que dans la boucle : le bug devient impossible.
- **Astuce :** supprimer une variable puis compiler, c'est trouver tous ses usages (`cannot find symbol`).

> *La question à se poser :* « Cette variable vit-elle aussi longtemps que ce qu'elle représente ? »

### Une recherche doit être déterministe

**Bug :** en mode « toutes les régions », un dé était relancé pendant la recherche : l'Alsace n'était reconnue
qu'une fois sur deux.

- **Comment le repérer :** appeler la méthode **1000 fois** avec la même entrée. Un résultat qui varie trahit du hasard caché.
- **Le principe :** une méthode qui *cherche* doit toujours donner la même réponse à la même question.
  Le code venait sans doute d'un copier-coller depuis la méthode de *tirage*, où le dé avait du sens.

### Le *fall-through* du `switch`

**Bug :** un `break` manquant après le didacticiel affichait « Retour au menu » en trop.

- **Le mécanisme :** les `case` sont des **étiquettes**, pas des blocs. Java saute à la bonne étiquette et exécute
  tout ce qui suit, jusqu'au premier `break`.
- **Comment le repérer :** `javac -Xlint:fallthrough` signale la ligne exacte.
- Un fall-through volontaire se signale par un commentaire.

---

## Phase 2 : centraliser les données (refactoring)

### Qu'est-ce qu'un refactoring ?

**Changer la structure du code sans changer son comportement.** Les changements de comportement voulus
(nouvelle formule, corrections de texte) se font dans des commits séparés.

### Le filet de sécurité : le test de caractérisation

Un test de caractérisation ne vérifie pas que le code est *juste* : il **photographie ce qu'il fait aujourd'hui**
(ici : 1397 lignes de recherches, distances, tirages et images), pour prouver ensuite que le nouveau code fait
exactement la même chose.

- Enregistrer la sortie dans un fichier de référence, puis après chaque étape : `diff reference.txt sortie.txt`.
- **Isoler les points de contact** du test avec le code testé (ici, 3 petites méthodes) : on peut alors changer
  toute l'API du programme sans toucher à la « photo ».
- **Quand le test trouve des écarts, les analyser avant de corriger.** À l'étape 3, les 40 écarts révélaient un bug
  de l'*ancien* code : une région de même capitale que la cible (Alsace pour le Grand Est) était refusée comme
  « saisie non reconnue ».

### Construire à côté, migrer, supprimer

1. Créer la nouvelle structure **à côté** de l'ancienne, sans l'utiliser.
2. Brancher les utilisateurs **un par un** (la recherche, puis le tirage, puis le pourcentage…), en testant après chaque étape.
3. Supprimer l'ancien code quand plus personne ne l'appelle (*Safe Delete*).

Le programme fonctionne à chaque commit, et on peut revenir à n'importe quelle étape.

### Les données ne sont pas du code

Un `switch` qui choisit une **valeur** (des coordonnées, un nom de fichier) est un tableau de données déguisé.
Un `switch` qui choisit un **comportement** est légitime.

> *La question à se poser :* « Est-ce que je pourrais écrire ça dans un tableur ? » Si oui, c'est une donnée :
> sa place est un `enum`, une liste ou un fichier.

### DRY : une connaissance à un seul endroit

*Don't Repeat Yourself.* Les coordonnées étaient copiées à 3 endroits, et la liste des régions à 5. Ajouter une
région demandait 6 modifications, et la Bourgogne-Franche-Comté avait été oubliée dans l'une d'elles.

- **Comment repérer une duplication :** chercher une valeur précise (`Ctrl+Shift+F`). Si elle apparaît plusieurs fois,
  se demander : « si l'une change, l'autre doit-elle changer aussi ? »
- **DRY concerne la connaissance, pas le texte.** Lille apparaît deux fois (Hauts-de-France et Nord-Pas-de-Calais) :
  ce n'est pas une duplication, ce sont deux régions qui partagent une capitale.
- **Le test de l'ajout :** « combien d'endroits dois-je modifier pour ajouter un élément ? » Plus d'un, c'est un signal d'alarme.

### Rendre explicite ce qui était implicite

L'« époque » d'une région existait déjà, mais seulement dans la *position* du code (premier ou second `switch`).
En lui donnant un nom (`Epoque.ANCIENNE`, `NOUVELLE`, `LES_DEUX`), les règles se lisent au lieu de se deviner.
Même chose pour le mode de jeu : `verifchoix != 1` est devenu `ModeRegions.reconnait(region)`.

### Les nombres magiques

`26 + 1`, `tab[26]`, `new double[100]` : des nombres que le programmeur doit garder en tête.
**Une taille se déduit des données** (`liste.size()`, `values().length`), elle ne s'écrit pas à la main.

### Les valeurs spéciales (sentinelles)

« Latitude = 0 » et « distance = 0 » voulaient dire « région introuvable ». Mais l'équateur existe, et deux régions
de même capitale sont à 0 km l'une de l'autre.

> *La règle :* dès que « absent » mérite d'être distingué, donne-lui sa propre représentation (`Optional`, `enum`),
> au lieu de l'encoder dans une valeur ordinaire (0, -1, `""`).

### Trouver un maximum sans trier

L'ancien code triait 27 distances pour n'en garder que la plus grande. **Un seul parcours suffit** :

```java
double max = 0;
for (RegionFr autre : RegionFr.values()) {
    max = Math.max(max, distance(cible, autre));
}
```

Avec des `double`, garder le **même ordre d'opérations** que l'ancien code quand on veut un résultat identique :
`(max - d) * 100 / max` et `(max - d) / max * 100` peuvent différer au dernier chiffre.

### Connaître la bibliothèque standard

Avant d'écrire un utilitaire, vérifier qu'il n'existe pas déjà : `Math.toRadians`, `Math.max`, `Math.round`,
`String.join`, `Collator`, `HashMap`… Chaque méthode maison remplacée, c'est du code en moins à maintenir et à tester.

### Trier du texte en français

Le tri par défaut de Java compare les **codes** des caractères : `Î` (206) passe après `Z` (90),
et « Île-de-France » finit en dernier. Un `Collator.getInstance(Locale.FRENCH)` trie comme un dictionnaire.

### Tester une spécification à ses limites, avant de la coder

« Pourcentage par rapport à la métropole » semblait clair, jusqu'à la question : « et si la cible est la Martinique ? ».
Passer chaque règle au crible des cas extrêmes : vide, zéro, maximum, l'élément « différent des autres ».

### Vérifier un changement de comportement volontaire

Même voulu, un changement se vérifie :

1. **seule la partie visée a bougé** (ici : en masquant les pourcentages, 0 différence ailleurs) ;
2. **les invariants tiennent** : un pourcentage est toujours entre 0 et 100, une région face à elle-même vaut 100 % ;
3. ensuite seulement, on remplace la référence du test de caractérisation.

C'est en écrivant ces invariants qu'est apparu un bug ancien : une distance de quelques centimètres donnait 99 %.

### Tronquer n'est pas arrondir

`(int) 99.9999` donne `99` : la conversion **coupe** les décimales. Pour une valeur affichée, utiliser `Math.round`.

### Les caractères d'échappement traversent plusieurs couches

Une commande qui modifie du code Java passe par le shell, puis par l'outil (`sed`), puis par Java.
Un `\n` écrit dans le code peut être compris comme un vrai saut de ligne, et une apostrophe peut fermer une chaîne
du shell. Pour modifier un texte littéral, préférer l'éditeur ou l'outil de remplacement de l'IDE.

### Les notions Java rencontrées

- **`enum` avec champs, constructeurs et méthodes.** En Java, un `enum` est une vraie classe dont les constantes
  sont les seules instances. Un constructeur peut en appeler un autre avec `this(...)`.
- **Le bloc `static { … }`** : exécuté une seule fois au chargement de la classe (ici, pour construire un index).
- **`HashMap`** : retrouver un élément par sa clé directement, au lieu de tester 35 `case`.
- **`Optional`** : « une valeur, ou rien ». `ofNullable`, `filter`, `map` (si la fonction renvoie `null`,
  le résultat devient vide), `orElse`, `isEmpty`, `orElseThrow`.
- **Les références de méthode** : `mode::reconnait`, `Region::depuis`, `RegionFr::getImage`.
- **Les méthodes `static`** : une méthode qui ne dépend d'aucun champ de l'objet n'a pas besoin d'instance
  (`Region.convertisseur`).
- **La méthode de fabrique** : `Region.depuis(regionFr)`, une alternative nommée au constructeur.
- **Les imports** : un import nommé l'emporte sur `*` (`java.util.List` contre `java.awt.List`),
  et `java.lang` (`String`, `Math`…) est importé automatiquement.
- **Un champ plutôt qu'une liste à part** : ajouter la `Zone` dans chaque ligne de l'`enum` oblige chaque future
  région à la préciser, et le compilateur refuse celle qui l'oublie.

> *Note (Phase 3.1) :* la classe `Region` citée ici n'existe plus. `Region.convertisseur` est devenue
> `RegionFr.simplifier`, et `Region.depuis` a disparu avec la classe : le jeu manipule directement les `RegionFr`.
> Les notions, elles, restent valables.

---

## Petits correctifs

### Dire directement ce qu'on veut

Le didacticiel remplaçait une saisie invalide par le texte `"1"`, pour la **reconvertir** ensuite en nombre.
L'intention était « rester dans le didacticiel » : on l'écrit directement, `boucle = 1;` dans le `catch`.

> *La question à se poser :* « Est-ce que je fabrique une donnée intermédiaire juste pour la retransformer ? »
> Si oui, il y a sans doute un chemin plus direct. Et un même problème (une saisie invalide) devrait se traiter
> de la même façon partout dans le programme.

### Tous les espaces ne se valent pas

Un nombre comme « 27 208 » contient un espace **insécable** : il ne doit pas être coupé en fin de ligne.
Unicode en propose plusieurs : U+00A0 (insécable classique) et U+202F (fine insécable, utilisée par le format
français de Java depuis la version 13). Certaines polices affichent U+202F comme un carré vide : on choisit donc
explicitement le séparateur avec `DecimalFormatSymbols`. Et comme la superficie est affichée à deux endroits,
le formatage est écrit **une seule fois**, dans `Message.avecmilliers` (DRY, même pour un petit détail).

### Séparer deux modifications en deux commits

Quand un fichier contient deux corrections indépendantes, on peut les commiter séparément :

- dans IntelliJ, la fenêtre *Commit* permet de cocher les morceaux (*hunks*) à inclure ;
- en ligne de commande : `git add -p` propose chaque morceau, un par un ;
- sans ces outils : mettre de côté le fichier final, repartir de la version commitée (`git checkout fichier`),
  réappliquer la première correction et commiter, puis remettre le fichier final et commiter le reste.
  Vérifier à la fin que le diff total est identique à celui de départ.

Attention : après un `git checkout`, Git réécrit le fichier selon le `.gitattributes`, avec des fins de ligne
CRLF sous Windows. Un script qui cherche des `\n` seuls ne trouvera plus rien.

---

## Phase 3.1 : séparer les règles de l'affichage

### Une classe, une raison de changer

`Region.jeu()` faisait tout à la fois : demander la saisie, chercher la région, calculer la distance, choisir l'indice,
compter les essais **et** afficher les popups. On ne pouvait donc pas vérifier une règle (« 5 échecs = défaite »)
sans cliquer. Désormais :

- **`Partie`** contient les règles et ne connaît pas `JOptionPane` : elle reçoit une saisie (`proposer`) et répond
  (un `Essai`, ou rien si la saisie est inconnue) ;
- **`Message`** demande, transmet et affiche : il ne décide de rien.

Les règles changent avec le *game design* (6 essais ? un indice de plus ?), l'affichage avec l'interface (Swing en
Phase 4, une page web un jour). Deux raisons de changer, deux classes. C'est le **principe de responsabilité unique**.

> *La question à se poser :* « Puis-je tester cette règle sans écran ni clavier ? » Si non, elle est mal rangée.
> Le signe qui ne trompe pas : un `if` de règle du jeu juste à côté d'un `showMessageDialog`.

### Un objet qui protège ses règles (encapsulation)

L'ancien code signalait la fin de partie en forçant `compteur = 5`, que la victoire, la défaite et l'abandon
utilisaient tous les trois. Résultat : impossible de les distinguer ensuite, d'où le bug du message d'abandon.
`Partie` remplace ce nombre magique par un `enum Etat { EN_COURS, GAGNEE, PERDUE, ABANDONNEE }` et protège
ses **invariants** (ce qui doit toujours être vrai) :

- les champs sont `private` : personne ne peut modifier l'état de l'extérieur ;
- `getEssais()` renvoie `Collections.unmodifiableList(essais)` : une vue en lecture seule, l'interface peut lire
  les essais mais pas en ajouter ;
- jouer dans une partie terminée lève une `IllegalStateException` : c'est une **erreur de programmation**, pas une
  saisie du joueur, donc on échoue tout de suite et bruyamment au lieu de continuer dans un état absurde.

> *La question à se poser :* « Qu'est-ce qui doit toujours être vrai pour cet objet, et qui pourrait le casser ? »

### Ranger chaque calcul chez celui qui possède les données

- La **distance** entre deux régions ne dépend que de leurs coordonnées : elle va dans `RegionFr` (`a.distance(b)`).
- Le **pourcentage** est une règle du jeu (la formule B) : il va dans `Partie`.
- La **phrase** « La capitale régionale de la région recherchée est… » est de l'affichage : elle reste dans `Message`.
  `Indice` ne porte que l'**ordre** des indices, qui est une règle.

Le détour de la Phase 2 disparaît : `pourcentage` n'a plus besoin de retrouver la `RegionFr` à partir d'une clé,
puisque le jeu manipule directement les `RegionFr`. `Region` (copie modifiable) et `superinterface` (distance
stockée en `String`, tableau de 100 cases dont 5 servaient) sont supprimées.

### Tester ce qui n'était pas testable : les doublures

Pour photographier l'ancien `jeu()`, plein de popups, on a remplacé `JOptionPane` et `JFrame` par des **doublures**
(*test doubles*) : de fausses classes, de même nom et de mêmes méthodes, qui lisent des saisies écrites à l'avance
et écrivent chaque message dans un fichier. Elles sont glissées à la place des vraies au lancement, sans toucher
au code du jeu :

```
javac --patch-module java.desktop=doublures -d doublures-out doublures/javax/swing/*.java
java  --patch-module java.desktop=doublures-out -Djava.awt.headless=true -Dscript=s1.txt -cp out Lanceur 1
```

Le tirage au hasard est rendu reproductible en fixant la graine du `Random`. 300 sessions générées au hasard
(101 victoires, 225 défaites, abandons, liste, 3 modes) ont été jouées sur l'ancienne et la nouvelle version :
**77 752 lignes identiques**. C'est un moyen de dernier recours : une fois les règles dans `Partie`, un simple
test les vérifie en quelques millisecondes, sans aucune doublure. La difficulté du filet *mesure* le mélange
des responsabilités.

### Le filet révèle des bugs d'origine

En lisant l'ancien code pour le reproduire exactement, deux bugs présents depuis le début sont apparus. Ils ont
été corrigés **après** le refactoring, chacun dans son commit, avec un `diff` qui montre exactement ce qui change :

- le message « Vous avez abandonné » n'a jamais été affiché (le `compteur = 5` sautait la condition) :
  199 fins de partie concernées dans les scénarios ;
- « 1 pour avoir la liste » ne marchait qu'en mode « nouvelles régions » : 18 cas où « Saisie non reconnue »
  devient la liste.

### Le piège de l'ordre d'initialisation `static`

Premier lancement après le déplacement de `simplifier` dans `RegionFr` : `ExceptionInInitializerError`. Les champs
et blocs `static` s'initialisent **dans l'ordre du fichier**. Le bloc `static` qui remplit l'index `PAR_CLE` appelle
`getCle()`, donc `simplifier()`, qui utilise le `Pattern ACCENTS`… déclaré plus bas, et encore `null` à ce moment-là.
Correction : déclarer `ACCENTS` avant le bloc. (Une constante comme `RAYON_TERRE = 6371` n'a pas ce problème :
le compilateur recopie directement sa valeur partout où elle sert.)

> *La question à se poser :* quand un bloc `static` appelle une méthode, « de quels champs `static` dépend-elle,
> et sont-ils déclarés avant ? »

### Les notions Java rencontrées

- **`record`** (Java 16) : `record Essai(RegionFr proposition, double distance, int pourcentage) {}` écrit en une
  ligne le constructeur, les accesseurs (`essai.distance()`), `equals`, `hashCode` et `toString`. Ses champs sont
  `final` : un essai joué ne change plus. C'est la bonne forme pour une donnée qui n'a pas de comportement.
- **`enum` imbriqué** : `Partie.Etat` n'a de sens qu'à l'intérieur d'une partie.
- **`Collections.unmodifiableList`** : une vue en lecture seule (toute modification lève `UnsupportedOperationException`).
- **Les exceptions de programmation** : `IllegalStateException` (l'objet n'est pas dans le bon état),
  `IllegalArgumentException` (un paramètre est invalide).
- **`StringBuilder`** : construire un texte en plusieurs morceaux dans une boucle.
- **`==` entre constantes d'`enum`** : chaque constante existe en un seul exemplaire, `proposition == cible`
  est donc correct (contrairement à deux `String`, à comparer avec `equals`).

---

## Phase 3.2 : Maven, JUnit et intégration continue

### Pourquoi un outil de build

Jusqu'ici, compiler demandait de connaître la bonne commande (`javac -encoding UTF-8 -d out main.java`), et
IntelliJ avait été réglé à la main (dossier de code, dossier exclu, dossier de travail). Rien de tout ça n'était
écrit dans le dépôt. Le **`pom.xml`** décrit le projet une fois pour toutes : version de Java, encodage,
dépendances (JUnit), versions des plugins, classe à lancer. N'importe quel IDE et n'importe quelle machine
(y compris les serveurs de GitHub) en tirent la même construction.

> *La question à se poser :* « Quelqu'un qui clone le dépôt peut-il compiler et tester avec **une seule**
> commande, sans me demander comment ? »

### Les conventions de Maven

Maven préfère les **conventions** à la configuration : si on range les fichiers à l'endroit attendu,
il n'y a rien à déclarer.

| Dossier | Contenu |
|---|---|
| `src/main/java` | le code du jeu |
| `src/main/resources` | les fichiers non Java (images), copiés à côté des classes et dans le `.jar` |
| `src/test/java` | les tests, compilés et lancés à part, jamais embarqués dans le jeu |
| `target/` | tout ce que Maven produit (classes, rapports, `.jar`) ; ignoré par Git, on peut le supprimer |

Le **cycle de vie** enchaîne des phases dans un ordre fixe, et demander une phase lance toutes les précédentes :
`compile` → `test` → `package` (le `.jar`) → `verify`. `.\mvnw.cmd package` compile donc **et teste** avant
d'assembler : un jeu dont les tests échouent n'est pas empaqueté.

**Les dépendances** sont téléchargées depuis Maven Central et gardées dans `~/.m2`. Le *scope* `test` indique
que JUnit ne sert qu'aux tests. Le **BOM** (`junit-bom`) fixe en un seul endroit la version de tous les modules
de JUnit (DRY, appliqué aux dépendances).

**Les versions sont fixées** (plugins, et Maven lui-même via le wrapper) : sans cela, le même `pom.xml` pourrait
construire différemment dans six mois. Ce sont les dernières versions **stables**, pas les bêta.

### Le Maven Wrapper

`mvnw` / `mvnw.cmd` sont de petits scripts versionnés qui téléchargent la version de Maven inscrite dans
`.mvn/wrapper/maven-wrapper.properties`, puis la lancent. Personne n'a besoin d'installer Maven, et tout le monde
utilise la même version. Comme en Phase 0, l'**empreinte SHA-256** du téléchargement est inscrite
(`distributionSha256Sum`) : le wrapper refuse un fichier altéré. Elle a été calculée après avoir vérifié le
fichier avec l'empreinte SHA-512 publiée par Apache.

Piège des fins de ligne : un script shell en CRLF échoue sous Linux (`/bin/sh^M: not found`), un `.cmd` en LF peut
échouer sous Windows. Le `.gitattributes` les impose (`mvnw text eol=lf`, `*.cmd text eol=crlf`), et le bit
exécutable de `mvnw` est enregistré dans Git (`git update-index --chmod=+x mvnw`), car Windows ne le connaît pas.

### Les ressources au lieu des chemins relatifs

`new File("regions/bretagne.png")` part du **dossier courant** : lancé depuis un autre dossier, ou depuis un `.jar`,
le jeu ne trouvait plus ses images. `representation.class.getResource("/regions/bretagne.png")` les cherche dans
le **classpath** : à côté des classes compilées, ou à l'intérieur du `.jar`. Le `/` initial part de la racine du
classpath. `getResource` renvoie `null` si la ressource n'existe pas : un test vérifie que l'image de chaque
région est bien là.

### JUnit

```java
@Test
@DisplayName("Cinq essais ratés font perdre la partie")
void defaite() {
    Partie partie = jouer(RegionFr.BRETAGNE, ModeRegions.NOUVELLES,
            "Corse", "Normandie", "Occitanie", "Grand Est", "Guyane");   // préparer et agir

    assertEquals(Partie.Etat.PERDUE, partie.getEtat());                  // vérifier
}
```

- Un test = une méthode annotée `@Test`, qui vérifie **un** comportement, avec un nom qui dit lequel.
- Structure **Préparer / Agir / Vérifier** (*Arrange / Act / Assert*).
- `assertEquals(attendu, obtenu)` : l'ordre compte pour le message d'erreur. `assertEquals(a, b, tolérance)` pour
  des `double`, `assertThrows` pour une exception attendue. Un dernier paramètre texte précise le cas en échec.
- **Tests paramétrés** : `@ParameterizedTest` avec `@CsvSource` (un tableau de cas) ou `@EnumSource` (une
  exécution par constante d'`enum`) ; un seul test vérifie ainsi les 35 images.
- Les tests sont dans le même package que le code (ici, le package par défaut) : ils voient tout ce qui n'est pas
  `private`.
- Dans IntelliJ : flèche verte dans la marge pour lancer un test, `Ctrl+Shift+F10` pour lancer celui sous le curseur.

**Un test qui ne peut pas échouer ne prouve rien.** Pour le vérifier, on réintroduit volontairement un bug et on
regarde les tests rougir (c'est le principe du *mutation testing*) : l'inversion latitude/longitude de la Phase 1
fait échouer 4 tests, « 6 essais au lieu de 5 » en fait échouer 2. Puis on annule (`git checkout -- fichier`).

### Ce que les tests ont appris en chemin

- **La distance d'une région à elle-même valait 9 cm.** Près de 1, `acos` amplifie énormément les erreurs
  d'arrondi : 10⁻¹⁶ sur le cosinus devient 10⁻⁸ radian, soit des centimètres sur Terre. Sans effet sur le jeu
  (la victoire compare des régions, pas des distances), mais un test sur des `double` doit toujours avoir
  une **tolérance** adaptée au calcul.
- **Les distances dépendaient de la langue du système.** `String.format("%.2f", d)` écrit `698,45` sur un Windows
  français et `698.45` sur un serveur en anglais. On impose `Locale.FRENCH`, et un test simule un système anglais
  avec `Locale.setDefault(Locale.US)`, en restaurant la valeur d'origine dans un `finally` pour ne pas perturber
  les autres tests.

> *La question à se poser :* « Mon code dépend-il de quelque chose qui change d'une machine à l'autre ? »
> (dossier courant, langue, fuseau horaire, fins de ligne, encodage). L'intégration continue, qui tourne sur une
> autre machine, est faite pour le révéler.

### Les anciens réglages de l'IDE entrent en conflit avec Maven

Premier lancement dans IntelliJ après le passage à Maven : `Maven resources compiler: Failed to copy
'…\out\production\francle\docs\feuille-de-route.md'`. La cause : en Phase 0, sans outil de build, la racine du
projet avait été marquée à la main comme dossier de code (dans `.idea/francle.iml`). En ouvrant le `pom.xml`,
IntelliJ a **ajouté** la configuration de Maven mais **gardé** ce réglage manuel. Il traitait donc toute la racine
comme du code et copiait dans `target/classes` tout ce qui n'était pas du Java (`docs/`, et même l'ancien dossier
de compilation `out/`, qui a disparu pendant la copie).

- **Comment le repérer :** un chemin absurde dans l'erreur (`target/classes/out/production/…`, un dossier de
  compilation copié dans un autre) ; puis *File > Project Structure* (`Ctrl+Alt+Shift+S`) → *Modules* → *Sources* :
  la racine apparaît en bleu.
- **Comment corriger :** décocher *Sources* sur la racine, puis *Reload All Maven Projects* dans l'onglet Maven ;
  ou, plus radical, fermer IntelliJ, supprimer `.idea` et rouvrir le `pom.xml` : IntelliJ reconstruit tout depuis
  le `pom.xml`.
- Le dépôt, le build en ligne de commande et la CI n'étaient pas touchés : `.idea/` et `out/` ne sont pas versionnés.

> *Comment le penser :* une connaissance (« où est le code ») ne doit vivre qu'à **un seul endroit, versionné**.
> En adoptant un outil de build, on **supprime** les réglages manuels qu'il remplace au lieu de les empiler.
> C'est DRY, appliqué à la configuration.

### L'intégration continue

`.github/workflows/ci.yml` décrit ce que GitHub exécute à chaque PR et à chaque push sur `main`, sur une machine
Linux neuve : récupérer le code (`actions/checkout`), installer le JDK 21 (`actions/setup-java`, avec cache des
dépendances Maven), puis `./mvnw -B verify`. Le résultat (✅ ou ❌) s'affiche sur la PR, avant la fusion.
La « machine neuve » est le vrai intérêt : ce qui ne marche que chez moi (un fichier oublié dans le commit,
un réglage local) y échoue tout de suite.

---

## Phase 3.3 : un fichier par classe, des packages

### Le code mort se supprime, il ne se range pas

Avant de répartir les classes, on retire ce qui ne sert à rien : la classe vide `commentjouer` (créée dans `main`
mais jamais utilisée) et les brouillons en commentaire (`interfacerapide`, `Statistiques`, `internet ?`).
Ranger du code mort dans des packages, c'est lui donner l'air d'être vivant. Les **idées** qu'il contenait ne sont
pas perdues : elles passent dans la feuille de route, où on les retrouvera (et l'ancien code reste dans `archive/`
et dans l'historique Git).

> *La question à se poser :* « Ce commentaire est-il une explication du code, ou une idée pour plus tard ? »
> Une explication reste dans le code ; une idée va dans la liste des choses à faire.

### Un fichier par classe

Java n'autorise qu'**une classe `public` par fichier**, qui porte le nom du fichier. Les autres classes d'un même
fichier (« classes auxiliaires ») sont tolérées, mais `javac -Xlint:all` les signale, et elles sont introuvables :
qui chercherait `Partie` dans `main.java` ? Un fichier par classe, c'est :

- **trouver** une classe par son nom (`Ctrl+N` dans IntelliJ) ;
- des **diffs lisibles** : un commit qui change les règles ne touche que `Partie.java` ;
- des **imports précis** par fichier : on voit d'un coup d'œil de quoi dépend chaque classe
  (`Partie` n'importe rien de `javax.swing` : la preuve qu'elle n'affiche rien).

Les jokers comme `import javax.swing.*` disparaissent au passage : un import nommé dit exactement ce qu'on utilise.

### Les conventions de nommage

| Élément | Convention | Exemples |
|---|---|---|
| Classe, `enum`, `record` | `UpperCamelCase` | `Partie`, `RegionFr`, `Francle` |
| Méthode, variable | `lowerCamelCase` | `proposer`, `getCible`, `distanceMax` |
| Constante (`static final`) | `MAJUSCULES_SOULIGNEES` | `ESSAIS_MAX`, `RAYON_TERRE` |
| Package | minuscules, à l'envers du nom de domaine | `fr.francle.jeu` |

Ce n'est pas qu'une question de goût : `main` (une classe) se confondait avec `main` (la méthode). Le renommage
est fait dans un **commit séparé** du découpage, pour que chacun reste lisible (`Shift+F6` dans IntelliJ renomme
partout d'un coup, fichier compris). Les méthodes aux noms d'origine (`onmetlimage`, `messagedefin`…) seront
renommées en Phase 4, quand l'interface sera réécrite.

Piège Windows : son système de fichiers ignore la casse (`representation.java` et `Representation.java` sont
le même fichier pour lui, pas pour Linux ni pour Git). Toujours renommer avec `git mv` (ou l'IDE), puis vérifier
avec `git ls-files` que Git a bien enregistré la nouvelle casse.

### Les packages : ranger par responsabilité

Un package est un **dossier** et un **espace de noms** : la classe `Partie` du package `fr.francle.jeu` s'appelle
en entier `fr.francle.jeu.Partie`, et vit dans `src/main/java/fr/francle/jeu/Partie.java`. Chaque fichier commence
par `package fr.francle.jeu;`, et une classe d'un autre package s'utilise avec un `import`.

On a rangé **par responsabilité**, en reprenant la séparation de la 3.1 :

```
fr.francle.ui        Francle, Message, Parametres, Representation
      │ utilise
      ▼
fr.francle.jeu       Partie, Essai, Indice
      │ utilise
      ▼
fr.francle.donnees   RegionFr, ModeRegions, Epoque, Zone
```

Les flèches ne vont **que vers le bas** : les données ignorent qu'il existe un jeu, le jeu ignore qu'il existe des
popups. C'est ce qui permettra en Phase 4 de remplacer tout `ui` par une fenêtre Swing **sans toucher** à `jeu` ni à
`donnees`. Une flèche vers le haut (`RegionFr` qui appellerait `Message`) serait une **dépendance circulaire** :
impossible alors de changer une couche sans toucher l'autre.

> *La question à se poser :* pour chaque `import fr.francle.…` : « Est-ce que ça va dans le bon sens ? »
> Les imports en tête de fichier rendent ces dépendances visibles ; c'est un des intérêts du découpage.

### La visibilité : ouvrir le moins possible

| Mot-clé | Visible depuis |
|---|---|
| `private` | la classe seule |
| *(rien)* : visibilité du package | les classes du même package |
| `protected` | le package, plus les sous-classes (héritage, pas encore utilisé ici) |
| `public` | partout |

Tant que tout était dans le package par défaut, la visibilité du package voulait dire « tout le monde ». Avec
trois packages, il faut choisir. La règle : **commencer fermé, et n'ouvrir que ce qu'un autre package utilise
vraiment**. Concrètement, on a déplacé les fichiers, puis **compilé** : c'est le compilateur qui dit ce qui doit
devenir `public` (ici, l'analyse faite avant était juste : tout a compilé du premier coup).

- `public` : `RegionFr`, `ModeRegions`, `Partie`, `Essai`, `Indice`, `Partie.Etat`… car `ui` et `jeu` s'en servent.
- visibles du **seul package** : `ModeRegions.peutTirer` et `reconnait` (seul `RegionFr` s'en sert),
  `RegionFr.simplifier`, `Partie.pourcentage`, le brouillon `Partie.fleche`, et les classes `Message`,
  `Parametres`, `Representation` (seul `Francle` s'en sert).

Pourquoi fermer ? Tout ce qui est `public` est une **promesse** : d'autres parties du code peuvent s'en servir, donc
on ne peut plus le changer librement. Ce qui reste fermé peut être modifié sans risque hors de son package.

Les **tests** sont rangés dans le **même package** que la classe testée (`src/test/java/fr/francle/jeu/PartieTest.java`) :
ils voient donc ce qui est visible du package, comme `Partie.pourcentage`, sans qu'on ait à l'ouvrir pour eux. Le
test des images a dû déménager de `RegionFrTest` (package `donnees`) vers `RepresentationTest` (package `ui`) :
un test de `donnees` ne doit pas dépendre de l'interface, pour la même raison que le code.

### Ce que le compilateur ne vérifie pas

Après le passage aux packages : compilation OK, 71 tests au vert, `BUILD SUCCESS`… et un `.jar` qui ne se lançait
plus (`impossible de trouver ou de charger la classe principale Francle`). Le `pom.xml` déclarait encore la classe
à lancer sous son ancien nom ; or un nom écrit dans un fichier de configuration (XML, texte) n'est **pas vérifié
par le compilateur**, et aucun test ne lançait le `.jar`.

Méthode habituelle : **reproduire** (lancer le `.jar`), **corriger** (`fr.francle.ui.Francle`), puis écrire un
test qui l'aurait attrapé (`LancementTest` lit le `pom.xml` et vérifie que la classe existe et a une méthode `main`),
et vérifier qu'il **échoue** avec l'ancienne valeur avant de passer avec la nouvelle.

> *La question à se poser :* après un renommage ou un déplacement, « Où ce nom est-il écrit **en dehors du code
> Java** ? » (configuration, scripts, documentation, chaînes de caractères). `Ctrl+Shift+F` (chercher dans tout le
> projet) les trouve ; le refactoring d'IntelliJ propose aussi de les mettre à jour (*Search in comments and strings*).

### Les notions Java rencontrées

- **`package` et `import`** : le nom complet d'une classe, l'arborescence de dossiers qui doit lui correspondre.
- **Les quatre niveaux de visibilité**, et le fait qu'un `enum` ou une classe imbriquée (`Partie.Etat`) a sa propre
  visibilité, à ouvrir elle aussi si un autre package s'en sert.
- **La réflexion** (`Class.forName`, `getMethod`, `Modifier.isStatic`) : manipuler une classe à partir de son nom
  sous forme de texte, utilisée dans `LancementTest`.

---

## Phase 4.1 : la flèche de direction et les réglages

### Un brouillon peut cacher plusieurs bugs

Le brouillon `fleche()` avait un bug annoncé (les degrés passés à `Math.sin` sans conversion, le piège de la
Phase 1) et un bug caché : il calculait le cap **de la cible vers la proposition**. La flèche aurait été juste en
apparence, mais aurait pointé à l'envers. On l'a vu en comparant la formule ligne à ligne avec sa définition :
`atan2(sin Δλ · cos φ2, …)` donne le cap **du point 1 vers le point 2**, donc le point 1 doit être la proposition
(là où est le joueur) et le point 2 la cible (là où il doit aller).

> *La question à se poser :* pour toute fonction à deux arguments du même type (`distance(a, b)`, `azimut(a, b)`,
> `copier(source, destination)`), « Est-ce que l'ordre compte ? » La distance est symétrique, l'azimut ne l'est pas.
> Le nom des paramètres doit le dire (`depuis`, `vers`), et un test doit vérifier les deux sens (Rennes → Strasbourg
> donne l'est, Strasbourg → Rennes l'ouest).

### Découper un problème : calcul d'un côté, classement de l'autre

`Direction` sépare deux étapes, testées chacune à part :

1. `azimut(depuis, vers)` : de la trigonométrie, qui renvoie un nombre (82,32°) ;
2. `depuisAzimut(angle)` : un classement, qui range ce nombre dans l'un des 8 secteurs de 45°.

La deuxième se teste sans aucune région, avec des angles choisis **aux limites** : 22,4° doit donner le nord et
22,6° le nord-est. C'est là que vivent les bugs (un `<` au lieu d'un `<=`, un secteur décalé de 22,5°). Le tour de
passe-passe : `Math.round(angle / 45)` donne le numéro du secteur dont le centre est le plus proche, et `% 8`
ramène le secteur 8 (de 337,5° à 360°) sur le nord.

### Vérifier un calcul par un autre chemin

Pour l'azimut de référence, le test contenait d'abord « ≈ 80° », une estimation de tête ; il a échoué (82,32°). Changer
le test pour qu'il passe aurait été une erreur : on ne sait pas encore qui a tort. On a recalculé la valeur par une
**méthode indépendante** (les deux villes en vecteurs 3D, projetés sur le plan tangent à Rennes) : 82,32° aussi.
Deux méthodes différentes qui tombent d'accord, c'est une vraie preuve ; une estimation de tête n'en est pas une.

> *La question à se poser :* quand un test échoue, « Qui a tort, le code ou la valeur attendue ? » Ne jamais
> recopier dans le test la valeur que donne le code : le test ne vérifierait plus rien.

### Les cas où la réponse n'existe pas

À 0 km, il n'y a pas de direction : `atan2(0, 0)` renvoie 0, soit « nord », ce qui serait faux. Ce cas arrive
vraiment : en mode « toutes les régions », Hauts-de-France et Nord-Pas-de-Calais ont toutes deux Lille pour
capitale. Plutôt qu'un `null` (qui ferait planter l'affichage), on ajoute une valeur explicite `SUR_PLACE` (●).
On compare la distance à 1 km plutôt qu'à 0 : les arrondis des `double` peuvent donner quelques micromètres au
lieu de 0 pile (le piège de la Phase 1).

### Séparer l'état de son affichage

`Parametres` mélangeait deux choses : **ce qui est réglé** (trois `int` valant 0, 1 ou 2) et **comment on le
demande** (des popups). La Phase 4.3 remplacera les popups par des menus ; si l'état restait dedans, il faudrait
tout réécrire. `Reglages` ne garde que l'état, avec les bons types : un `ModeRegions` au lieu de `0/1/2`, des
`boolean` au lieu de « 0 = avec, 1 = sans ». `reglages.isAvecImages()` se lit tout seul,
`getChoiximage() == 0` demandait de connaître la convention. C'est le même raisonnement que `Partie` en
Phase 3.1, appliqué aux réglages : le modèle d'un côté, la vue de l'autre.

### Les notions Java rencontrées

- **Un `enum` avec des champs** (`symbole`) et un **tableau constant** (`ROSE`) qui fixe un ordre.
- **`Math.atan2(y, x)`** : l'angle d'un point, dans le bon quadrant (contrairement à `atan(y / x)`), entre −180° et
  180° ; **`Math.toRadians` / `Math.toDegrees`** ; le modulo pour ramener un angle entre 0° et 360°.
- **Les tests paramétrés avec `@CsvSource`** : JUnit convertit tout seul le texte `"NORD_EST"` en `Direction.NORD_EST`.

---

## Phase 4.2 : la fenêtre principale

### La programmation événementielle

Avec les popups, le programme menait la danse : `showInputDialog` **bloquait** jusqu'à la réponse, puis la ligne
suivante s'exécutait, dans une boucle `while`. Avec une fenêtre, c'est le joueur qui mène : il peut cliquer
« Valider », « Abandonner » ou « Nouvelle partie » dans n'importe quel ordre. Le programme ne déroule plus une
histoire, il **réagit** :

```java
valider.addActionListener(e -> jouer());   // « quand on cliquera sur Valider, appelle jouer() »
```

Cette ligne n'appelle **pas** `jouer()` : elle **inscrit** un écouteur, que Swing appellera plus tard, à chaque
clic. `e -> jouer()` est une *lambda* : une petite fonction sans nom, passée comme une valeur. Conséquence :
plus de boucle de jeu. L'état de la partie ne vit plus dans des variables locales de `main`, mais dans les
**champs** de `Fenetre` (`partie`, les composants), qui survivent entre deux clics.

> *La question à se poser :* « Qu'est-ce qui déclenche ce code, et dans quel état est le programme à ce
> moment-là ? » Par exemple : que se passe-t-il si on clique « Valider » après la fin de la partie ?
> (Réponse : rien, car le bouton est désactivé. Sans ça, `Partie` lèverait son `IllegalStateException`.)

### L'Event Dispatch Thread (EDT)

Un *thread* (fil d'exécution) est une suite d'instructions exécutée par la machine ; un programme peut en avoir
plusieurs **en même temps**. Swing n'est pas prévu pour ça : si deux fils modifient le même composant à la
fois, l'affichage peut se corrompre, sans erreur et de façon imprévisible. D'où la règle : **tout ce qui touche à
l'interface se fait sur un seul fil, l'EDT**. Les écouteurs sont déjà appelés sur l'EDT ; seul `main`, qui tourne
sur le fil principal, doit y confier la création de la fenêtre :

```java
SwingUtilities.invokeLater(() -> new Fenetre().afficher());
```

Corollaire : un écouteur doit être **rapide**. Pendant qu'il s'exécute, l'EDT ne fait rien d'autre et la fenêtre
est figée. Un calcul long (un téléchargement, par exemple) se fait sur un autre fil (`SwingWorker`), qui
renvoie le résultat à l'EDT.

### Les layouts : décrire des règles, pas des positions

On ne place pas les composants au pixel près : on choisit un **gestionnaire de disposition** (*layout manager*) qui
les place selon des règles, et qui suit quand la fenêtre change de taille ou que la police grossit (écran à 150 %).

- `BorderLayout` : cinq zones (`NORTH`, `SOUTH`, `EAST`, `WEST`, `CENTER`) ; le centre prend la place qui reste.
- `FlowLayout` : les composants à la suite, comme des mots dans une ligne (la barre de boutons).

Une interface réelle **imbrique** des panneaux (`JPanel`), chacun avec son layout : la fenêtre est un `BorderLayout`
dont la zone sud est elle-même un `BorderLayout`, qui contient une ligne en `FlowLayout`. `frame.pack()` donne
ensuite à la fenêtre la taille juste nécessaire.

### Modèle et vue : `JTable` et `AbstractTableModel`

Une `JTable` ne stocke **aucune donnée**. Elle demande chaque case à son **modèle** (`getValueAt(ligne, colonne)`),
qui va la lire dans la `Partie`. On a donc trois rôles distincts :

| Rôle | Classe | Sait… |
|---|---|---|
| Les données et les règles | `Partie` | ce qui s'est joué, sans rien afficher |
| Le modèle du tableau | `TableauEssais` | traduire la partie en lignes et colonnes de texte |
| La vue | `JTable` | dessiner des cases, sans savoir ce qu'elles veulent dire |

Le modèle doit **prévenir** la vue quand ses données changent : `fireTableDataChanged()` (tout a pu changer) ou
`fireTableRowsUpdated(1, 1)` (seule la ligne 1). Oublier cet appel est le bug classique : les données sont justes
mais l'écran ne bouge pas. `TableauEssaisTest` le vérifie en branchant son propre écouteur sur le modèle, comme
le ferait la `JTable`. C'est le même mécanisme d'événements que les boutons, dans l'autre sens.

### Le renderer est un tampon, pas une case

Pour dessiner 25 cases, la `JTable` n'utilise pas 25 composants. Elle prend **un seul** `JLabel` (le *renderer*),
le règle pour la case 1, le dessine, le règle pour la case 2, etc., comme un tampon encreur. Conséquence : le
`DefaultTableCellRenderer` remet la police du tableau **à chaque case**. Un `setFont` fait une seule fois serait
écrasé. Pour agrandir les flèches, il faut redéfinir `getTableCellRendererComponent` et changer la police **après**
l'appel à `super`.

### Les composants restent bêtes, la logique se teste à part

`Fenetre` crée des composants, les dispose et branche les écouteurs : rien d'autre. Tout ce qui demande une
réflexion a été sorti dans des classes sans fenêtre, donc testables : les phrases (`Textes`), le tableau
(`TableauEssais`), le choix et la taille des images (`Representation.silhouette`, `carte`, `ajuster`), et la règle
des indices révélés (`Partie.indicesReveles`). La raison est très concrète : sur la machine d'intégration continue,
il n'y a pas d'écran, et `new JFrame()` y lève une `HeadlessException`.

### Vérifier à l'écran, pas seulement avec des tests

Les 144 tests passaient, et pourtant la fenêtre dépassait de l'écran et les flèches étaient minuscules. Des tests
ne voient pas ce qu'un humain voit. La fenêtre a donc été lancée, jouée par programme (les vrais boutons, cliqués
par `doClick()`), puis photographiée (`Robot.createScreenCapture`). Au passage, une notion utile : avec une mise à
l'échelle de 150 %, un écran 1080p n'offre que 672 **points** de haut utilisables. Swing compte en points, pas en
pixels.

### Une dépendance, et un `.jar` qui l'embarque

FlatLaf est la première dépendance du jeu lui-même (JUnit ne sert qu'aux tests, d'où son *scope* `test`). Maven
la télécharge et la met dans le classpath pour compiler et tester. Mais le `.jar` produit par `maven-jar-plugin`
ne contient **que nos classes** : `java -jar target/francle.jar` aurait planté avec `NoClassDefFoundError`.
`maven-shade-plugin` fabrique un `.jar` « tout compris » (*uber-jar*), qui embarque aussi les classes de FlatLaf
(6,9 Mo au lieu de quelques centaines de Ko).

> *La question à se poser :* après avoir ajouté une dépendance, « Le programme livré la trouvera-t-il ? » Les
> tests passent dans Maven, qui fournit le classpath ; l'utilisateur, lui, n'a que le `.jar`.

### Le code mort se supprime (encore)

La fenêtre remplace la boucle de popups : `Message` (tour, messages, historique en texte) n'avait plus d'appelant.
Ce qui servait encore (séparateur de milliers, liste triée) est passé dans `Textes`, avec ses tests. Le reste est
supprimé, pas commenté : Git le garde dans l'historique (`git show 8098abf:src/main/java/fr/francle/ui/Message.java`, 8098abf étant le commit de la 4.1).

### Les notions Java rencontrées

- **Les lambdas** (`e -> jouer()`) et les **références de méthode** (`this::montrer`, `recus::add`).
- **Une classe anonyme** (`new DefaultTableCellRenderer() { @Override … }`) : une sous-classe sans nom, définie
  là où on s'en sert, pour redéfinir une seule méthode.
- **Les blocs de texte** (`"""…"""`, Java 15) pour un texte de plusieurs lignes, comme les règles du jeu.
- **Un constructeur `private`** (`Textes`, `Representation`) : une classe qui n'a que des méthodes `static` ne doit
  pas être instanciée.

---

## Phase 4.3 : les menus

### Une `Action` : une commande, plusieurs déclencheurs

« Abandonner » existe à deux endroits : le bouton à côté de « Valider » et l'article du menu Jeu. Avec deux
écouteurs séparés, il faudrait penser à griser **les deux** en fin de partie, et l'oubli de l'un est un bug qui
attend son heure. Une `Action` réunit en **un seul objet** le nom, le raccourci, l'état actif ou non, et le code
à exécuter. Le bouton et l'article de menu sont construits à partir d'elle (`new JButton(abandonner)`,
`menu.add(abandonner)`) et **l'écoutent** : `abandonner.setEnabled(false)` grise les deux d'un coup.

> *La question à se poser :* « Cette commande peut-elle être déclenchée de plusieurs façons (bouton, menu,
> raccourci) ? » Si oui, une `Action` : l'état est stocké à un seul endroit. C'est encore DRY, appliqué à
> l'interface.

### Les menus Swing

| Classe | Rôle |
|---|---|
| `JMenuBar` | la barre, posée sur la fenêtre par `frame.setJMenuBar(...)` (hors du contenu : elle ne compte pas dans le layout) |
| `JMenu` | un menu déroulant (« Jeu ») |
| `JMenuItem` | un article ordinaire, souvent créé à partir d'une `Action` |
| `JRadioButtonMenuItem` + `ButtonGroup` | des choix **exclusifs** : le groupe décoche les autres quand on en coche un |
| `JCheckBoxMenuItem` | une case à cocher **indépendante** |

Le choix du composant **dit** la règle à l'utilisateur : des ronds pour « un seul mode à la fois », des coches
pour « avec ou sans silhouette ». Les anciens popups demandaient de taper 0, 1 ou 2, ce qui permettait de taper 7
(et il fallait le gérer). Avec des boutons radio, une saisie invalide n'est **plus possible**. C'est la meilleure
validation : celle qu'on n'a pas besoin d'écrire. C'est pourquoi `Parametres` et `ModeRegions.depuisCode` ont pu
disparaître.

### Raccourcis (accélérateurs) et mnémoniques

- Un **accélérateur** (`Ctrl+N`) déclenche la commande directement, menu fermé. Il est affiché à droite de
  l'article : l'utilisateur le découvre en ouvrant le menu.
- Un **mnémonique** (`Alt+J`) ouvre le menu au clavier ; la lettre est soulignée quand on appuie sur Alt.
- `Toolkit.getMenuShortcutKeyMaskEx()` donne la bonne touche selon le système : Ctrl sous Windows et Linux,
  Cmd sur Mac. Écrire `CTRL_DOWN_MASK` en dur aurait été faux sur Mac.
- `F1` pour l'aide et `Ctrl+Q` pour quitter sont des **conventions** : les respecter, c'est ce qui rend une
  application « évidente ».

### Un seul chemin pour quitter

« Quitter » ne fait pas `System.exit(0)` : il envoie à la fenêtre l'événement `WINDOW_CLOSING`, exactement comme un
clic sur la croix. Si un jour on veut demander « Voulez-vous vraiment quitter ? » ou sauvegarder des statistiques
(Phase 5), il n'y aura qu'un seul endroit à modifier.

### Le piège du `FlowLayout`

En fin de partie, « Nouvelle partie » remplace « Abandonner ». Le nouveau bouton est plus large, la ligne
déborde, et `FlowLayout` fait ce qu'il fait toujours : il **passe à la ligne**, comme un mot trop long. Mais la
hauteur du panneau a été fixée au `pack()` : la seconde ligne est coupée, et le bouton est invisible. Il n'y a
aucune erreur, juste un bouton qui manque. `BorderLayout` règle le problème : le champ de saisie au centre prend la
largeur qui **reste**, et les boutons à l'est gardent leur taille.

> *La question à se poser :* « Que devient cette disposition si un composant change de taille (texte plus long,
> police plus grande, bouton qui en remplace un autre) ? » `FlowLayout` convient à ce qui a la place de s'étaler,
> pas à une ligne qui doit rester une ligne.

### Vérifier sans déranger l'utilisateur

Pour tester les raccourcis, le harnais a d'abord envoyé de **vraies touches** (`Robot.keyPress`). Les touches sont
bien arrivées, mais elles vont à la fenêtre active de l'ordinateur, que l'utilisateur était en train d'utiliser :
`Ctrl+N` aurait pu ouvrir une fenêtre dans son navigateur. Une vérification ne doit pas avoir d'effets de bord
sur le reste du système. La seconde version ne touche plus à l'écran :

- les commandes sont déclenchées **par programme** (`article.doClick()`), et les raccourcis lus sur les articles
  (`getAccelerator()`) ;
- la fenêtre n'est jamais affichée : elle est **dessinée dans une image** en mémoire (`racine.paint(g)`). Il faut
  alors appeler `frame.validate()` à la main. Swing ne recalcule la disposition tout seul que pour une fenêtre
  visible, et sans cet appel le bouton « Nouvelle partie » semblait encore absent.

Au passage, un piège de l'EDT : `SwingUtilities.invokeAndWait` **depuis** l'EDT est interdit. L'EDT attendrait
une tâche qu'il est le seul à pouvoir exécuter. Swing préfère lever une `Error` plutôt que de bloquer pour
toujours.

### Les notions Java rencontrées

- **`AbstractAction`**, classe abstraite à compléter : on ne redéfinit que `actionPerformed`.
- **`Runnable`** comme paramètre (`action(nom, raccourci, this::commencer)`) : passer « du code à exécuter plus
  tard » à une méthode.
- **Une lambda qui capture une variable de boucle** (`for (ModeRegions mode : MODES)` puis `e -> reglages.setMode(mode)`) :
  autorisé, car `mode` est une nouvelle variable à chaque tour et n'est jamais modifiée (*effectivement finale*).

---

## Phase 4.4 : l'autocomplétion

### La règle d'un côté, le composant de l'autre

L'autocomplétion a deux moitiés, rangées à deux endroits :

- **quelles régions proposer** : une règle sur les données, donc `RegionFr.suggestions(saisie, mode)` dans
  `donnees`, à côté de `chercher` et de `simplifier`. Elle se teste comme n'importe quelle méthode ;
- **comment les proposer** (une liste sous le champ, les flèches, Entrée) : un comportement d'interface, donc la
  classe `Autocompletion` dans `ui`. Elle reçoit une **fonction** (`Function<String, List<String>>`) qui calcule
  les suggestions, et ne sait rien des régions. On pourrait la brancher demain sur un champ « capitale » sans
  la modifier.

Placer `suggestions` dans `RegionFr` a aussi une raison pratique : `simplifier` n'est visible que du package
`donnees`, et c'est voulu. On n'a pas ouvert `simplifier` pour l'interface : on a ajouté le service dont
l'interface avait besoin.

> *La question à se poser :* « Cette classe pourrait-elle servir ailleurs si on lui passait ses données au lieu
> qu'elle aille les chercher ? » Passer une fonction en paramètre, c'est la forme la plus légère de cette idée
> (*injection de dépendance*).

### Écouter un texte qui change : `DocumentListener`

Un `JTextField` ne stocke pas son texte lui-même : il l'affiche à partir d'un **`Document`**, son modèle, comme
la `JTable` affiche son `TableModel`. Pour réagir à chaque lettre, on écoute le document (`insertUpdate`,
`removeUpdate`) plutôt que le clavier. Ça attrape **toutes** les modifications : frappe, collage, Retour arrière,
et aussi `setText` appelé par le code. Écouter les touches en aurait manqué (un collage à la souris, par exemple).
En contrepartie, `setText(x)` déclenche deux événements (effacer, puis insérer), et le code doit le supporter.

### Les liaisons de touches : `InputMap` et `ActionMap`

Pour que ↓, ↑, Entrée et Échap pilotent la liste **pendant que le champ garde le clavier**, on les relie au champ :

```java
champ.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("DOWN"), "DOWN");   // touche -> nom
champ.getActionMap().put("DOWN", action);                                                  // nom -> action
```

Deux tables plutôt qu'une, pour séparer « quelle touche » de « quoi faire » : plusieurs touches peuvent viser la
même action, et le thème peut changer les touches sans toucher aux actions. Une liaison `WHEN_FOCUSED` passe avant
toutes les autres. C'est ainsi qu'Entrée remplace le comportement normal du champ.

### Pourquoi pas un `JPopupMenu`

Le réflexe serait un menu surgissant. Mais un `JPopupMenu` est fait pour les **menus** : à l'ouverture, il prend en
charge les flèches, Entrée, Échap et les clics pour sa propre navigation, ce qui entre en conflit avec la frappe dans
le champ. La liste est donc une simple `JList` posée dans le **calque des popups** de la fenêtre :

- un `JLayeredPane` empile des calques, et `POPUP_LAYER` est dessiné par-dessus le contenu ;
- la liste est **non focusable** : un clic dessus ne vole pas le clavier au champ ;
- comme ce n'est pas une fenêtre à part, elle se dessine avec le reste. Sa vérification en mémoire marche donc
  comme pour la fenêtre.

### Tester un composant sans écran

`AutocompletionTest` tourne aussi sur la machine d'intégration continue, qui n'a pas d'écran. Ce qui est interdit
sans écran, ce sont les **fenêtres** (`JFrame` lève `HeadlessException`). Les composants légers (`JTextField`,
`JList`, et même `JRootPane`) se créent sans problème. Le test place le champ dans un `JRootPane` et déclenche les
touches **par leurs liaisons** (`InputMap` → `ActionMap`), exactement le chemin d'une vraie touche après sa
réception. Il ne dépend donc ni du focus ni du système.

### Le test qui avait tort, encore

Le test attendait `[Nord-Pas-de-Calais, Normandie]` pour « nor » en mode « toutes ». Le code répondait aussi
Basse-Normandie et Haute-Normandie, car leur mot « Normandie » commence par « nor ». C'est exactement la règle
voulue, et la plus utile au joueur. On a donc corrigé l'attendu, en sachant pourquoi : c'est l'**inverse** de
recopier la sortie du code sans réfléchir.

### Les notions Java rencontrées

- **`Function<T, R>`** : une fonction passée en paramètre (`this::suggestions`), appelée par `source.apply(texte)`.
- **Les adaptateurs** (`MouseAdapter`, `FocusAdapter`, `ComponentAdapter`) : des écouteurs aux méthodes vides, dont on
  ne redéfinit que celles qui servent.
- **`Comparator.comparing(RegionFr::getNom, collator)`** : trier des objets selon une de leurs propriétés, avec un
  ordre de comparaison choisi (ici l'ordre alphabétique français).
- **Les streams** (`stream().map(RegionFr::getNom).toList()`, `anyMatch`) : transformer ou interroger une liste en
  une ligne.

---

## Phase 4.4.1 : un retour d'utilisateur

### « Égal » a plusieurs sens

La liste se fermait quand l'unique suggestion était « déjà écrite ». Le code disait
`suggestion.equalsIgnoreCase(texte.strip())` : égal *sans tenir compte des majuscules ni des espaces autour*. Avec
« bretagne », la liste disparaissait donc alors que le joueur n'avait pas écrit le nom correctement. Il y a en réalité
trois égalités différentes dans ce jeu :

| Égalité | Méthode | Sert à |
|---|---|---|
| même région | `simplifier(a).equals(simplifier(b))` (via `chercher`) | accepter « ile de france » comme proposition |
| même texte, à la casse près | `equalsIgnoreCase` | ne correspond ici à aucun besoin réel |
| même texte, caractère par caractère | `equals` | savoir si le nom est **écrit** correctement |

La bonne question n'était pas « le joueur a-t-il désigné cette région ? » (`chercher` y répond déjà), mais « le
champ contient-il déjà l'orthographe exacte ? ». C'est `equals`.

> *La question à se poser :* devant une comparaison, « égal **au sens de quoi** ? ». `equalsIgnoreCase`, `strip`,
> les arrondis des `double`, la simplification des saisies : chacun définit une égalité différente, à choisir
> d'après ce qu'on veut savoir.

### Reproduire avec un test avant de corriger

Avant de toucher au code, le test a été écrit **à partir du retour** (« bretagne », « BRETAGNE », « Bretagne »
avec une espace, plus deux cas d'accents et de tirets), puis lancé : 3 échecs, exactement les cas signalés. Les
cas d'accents passaient déjà, car `equalsIgnoreCase` ne confond pas « ile » et « Île ». Ensuite seulement, le
correctif d'une ligne, et les 5 cas au vert. C'est la méthode de la Phase 1 (reproduire, corriger, revérifier avec
le même test), appliquée à un retour d'utilisateur plutôt qu'à un bug trouvé en lisant le code.

Détail JUnit : `@CsvSource` retire les espaces autour des valeurs, sauf entre guillemets. Comme son guillemet par
défaut est l'apostrophe (présente dans « d'Azur »), on a choisi `quoteCharacter = '"'` pour pouvoir écrire `"Bretagne "`.

---

## Bilan de la Phase 4

- **Ce qui a vraiment changé, c'est le sens du programme.** Il ne pose plus de questions dans l'ordre : il attend et
  réagit (`ActionListener`, `Action`, `DocumentListener`), sur un seul fil, l'EDT.
- **La Phase 3 a payé.** Les règles (`Partie`) n'ont reçu que des ajouts. Toute l'interface a pu être réécrite
  sans toucher à une seule règle du jeu, et les tests de `jeu` et `donnees` écrits en Phase 3 sont restés au vert du
  début à la fin, sans modification.
- **Garder la logique hors des fenêtres.** Tout ce qui demande une réflexion (textes, tableau, images,
  suggestions, flèche) vit dans des classes testables sans écran ; les fenêtres ne font qu'assembler et brancher.
- **Regarder le résultat.** Plusieurs défauts n'étaient visibles que sur une image : fenêtre trop haute, flèches
  minuscules, bouton disparu, sélection grise. Les tests vérifient ce qu'on a prévu ; regarder montre ce qu'on
  n'avait pas prévu.

---

## Phase 5.1 : la région du jour

### Le même résultat partout, sans se parler

Comme dans Wordle, tout le monde doit avoir la même région le même jour, sans serveur : chaque machine fait le
calcul de son côté. Il faut donc qu'il ne dépende **que** de choses identiques partout : la date, et les noms des
régions. Trois pièges, chacun évité exprès :

1. **L'ordre de la liste.** `Collections.shuffle(liste, new Random(graine))` donne le même mélange pour la même
   graine *et la même liste de départ*. Mélanger `RegionFr.values()`, c'est faire dépendre la réponse de l'ordre des
   lignes de l'enum : réordonner une ligne changerait la région du jour chez tout le monde. On trie donc d'abord par
   nom simplifié.
2. **L'algorithme du hasard.** `Random` et `Collections.shuffle` sont décrits précisément par la documentation de Java :
   même graine, même suite, sur toutes les machines et toutes les versions. Ce n'est pas vrai de tous les générateurs
   (par exemple `Math.random()` ou `ThreadLocalRandom`, sans graine choisie).
3. **L'heure et le fuseau.** `LocalDate.now()` dépend du fuseau de la machine : à minuit à Paris, il est encore
   23 h à Londres. Chacun a donc la région de *son* jour, comme dans Wordle. C'est un choix, pas un oubli.

### Le hasard qui ne se répète pas : mélanger par cycles

Tirer une région par jour (`liste.get(random.nextInt(18))`) donne parfois la même deux jours de suite, et laisse
certaines régions absentes des semaines entières. On mélange plutôt les 18 régions **une fois par cycle de 18
jours** et on les distribue dans l'ordre : chacune revient exactement une fois par cycle. C'est le principe du jeu de
cartes qu'on bat puis qu'on distribue, plutôt que de tirer une carte au hasard et de la remettre.

Reste la jointure entre deux cycles : le dernier jour d'un cycle et le premier du suivant peuvent tomber sur la même
région (une chance sur 18). On échange alors les deux premières régions du nouveau cycle. Le test « jamais deux jours
de suite » a été vérifié en **retirant** cet échange : il échoue (Bourgogne-Franche-Comté deux jours de suite,
1 710 jours avant le lancement). Un test qui ne casse jamais ne prouve rien.

### `floorDiv` et `floorMod` : les nombres négatifs

Avant le premier jour, le numéro du jour est négatif. Or en Java, `-1 % 18` vaut **-1** (le reste garde le signe),
ce qui donnerait un index invalide. `Math.floorMod(-1, 18)` vaut **17**, le dernier jour du cycle précédent, ce qu'on
veut. De même, `-1 / 18` vaut 0 alors que `Math.floorDiv(-1, 18)` vaut -1. Dès qu'un calcul « tourne en rond »
(jours, heures, angles, positions dans un cercle), se demander s'il peut recevoir un nombre négatif.

### Le temps comme paramètre

`LocalDate.now()` au milieu du code rend celui-ci intestable : on ne peut pas vérifier « le lendemain » sans attendre
un jour. Deux réponses, à deux niveaux :

- la **règle** reçoit la date en paramètre (`RegionDuJour.pour(date)`) : le test choisit n'importe quel jour, passé
  ou futur ;
- la **fenêtre** reçoit une horloge (`Clock`), avec `Clock.systemDefaultZone()` par défaut. La vérification lui a
  donné une horloge réglée au 1er janvier 2030, puis l'a avancée d'un jour pour voir la nouvelle partie commencer.

C'est la même idée que la fonction de suggestions passée à `Autocompletion` en 4.4 : ce dont un code dépend, on le
lui **donne** au lieu qu'il aille le chercher (*injection de dépendance*).

### Afficher à partir de l'état

Revenir à la partie du jour après une partie libre doit la montrer **telle qu'on l'a laissée** : sinon, la
recommencer offrirait des essais supplémentaires. Plutôt que de mémoriser ce qui était affiché, `montrerPartie(p)`
reconstruit tout l'affichage à partir de la seule `Partie` (essais, indices, message, image, boutons). Une partie
neuve, en cours ou terminée passent par le même chemin. C'est plus robuste qu'une suite de mises à jour : il n'y a
rien à oublier de remettre à zéro.

### Valeurs de référence (tests « golden »)

Il n'existe pas de « bonne » région du jour en dehors du calcul lui-même. Le test ne peut donc pas en vérifier la
justesse, mais il peut vérifier qu'elle **ne change pas** : quelques dates et leurs régions sont figées dans le test.
S'il casse un jour, c'est que la région du jour a changé pour tout le monde. Ces dates sont **passées** : figer des
dates à venir aurait révélé les réponses des prochaines parties à qui lit le code.

### Mesurer avant d'optimiser

Les tests de la région du jour prenaient 4,4 secondes. La cause : le tri compare les régions par `getCle()`, qui
refait la simplification du nom (normalisation Unicode, expressions régulières) à **chaque comparaison**, et ce tri
était refait à chaque jour testé. La liste triée ne change jamais : elle est maintenant calculée une seule fois
(`static final`, rendue non modifiable par `List.copyOf`). On passe à 0,6 seconde, sans rien changer au résultat (les
valeurs de référence l'ont vérifié).

### Les notions Java rencontrées

- **`java.time`** : `LocalDate`, `ChronoUnit.DAYS.between`, `Clock` (et une sous-classe de `Clock` dans le harnais).
- **`Collections.shuffle(liste, random)`** et **`Collections.swap`**.
- **`List.copyOf`** : une copie non modifiable (toute tentative de modification lève une exception).
- **Plusieurs constructeurs** qui s'enchaînent : `Fenetre()` appelle `this(Clock.systemDefaultZone())`.
- **Un nombre en hexadécimal** (`0x4672616E636C65L`) : ici les codes ASCII de « Francle », écrits deux chiffres par lettre.

---

## Phase 5.2 : les statistiques

### Enregistrer le minimum, recalculer le reste

On aurait pu enregistrer des compteurs (« 12 parties, 9 victoires, série de 4 ») et les mettre à jour à chaque
partie. Le moindre bug, ou un fichier modifié à la main, les aurait désynchronisés pour toujours. On enregistre
plutôt **les faits** (chaque partie du jour : date, région, propositions, état), et les statistiques sont
**recalculées** à partir d'eux à chaque affichage. C'est le même principe que la Phase 2 (une information à un seul
endroit), appliqué aux données enregistrées.

Pour la même raison, une partie n'est pas enregistrée avec ses distances, pourcentages et indices : seulement ses
propositions. Pour la reprendre, `Historique` les **rejoue** dans une nouvelle `Partie`. Ce sont donc les règles du
jeu qui recalculent tout le reste, et rien ne peut contredire les règles.

> *La question à se poser :* avant d'enregistrer une valeur, « Peut-elle se recalculer à partir d'autre chose ? »
> Si oui, on enregistre la source, pas le résultat.

### JSON et Jackson : la sérialisation

*Sérialiser*, c'est transformer des objets en texte (ou en octets) pour les enregistrer ou les envoyer ;
*désérialiser*, c'est l'inverse. Jackson fait les deux à partir des records eux-mêmes : le nom de chaque composant
devient une clé JSON, une `LocalDate` devient `"2030-03-09"` (format ISO), un enum devient son nom (`"BRETAGNE"`),
une `List` un tableau. Aucune ligne de code d'écriture du JSON à écrire.

Ce que ça implique :

- **les noms deviennent un format.** Renommer la constante `BRETAGNE` de l'enum, ou le composant `propositions` du
  record, rendrait illisibles les fichiers déjà enregistrés. D'où le champ `version`, qui permettra un jour de
  reconnaître et convertir un ancien format ;
- **un fichier peut venir d'ailleurs.** On a désactivé `FAIL_ON_UNKNOWN_PROPERTIES` : un champ ajouté par une
  version plus récente est ignoré au lieu de tout rendre illisible.

Jackson existe en deux lignées : 2.x (`com.fasterxml.jackson`) et 3.x (`tools.jackson`, la version actuelle). La
3 lit et écrit déjà les dates `java.time` sans module à ajouter, et ses exceptions (`JacksonException`) sont non
vérifiées. Curiosité : elle dépend de `jackson-annotations` 2.22, resté dans l'ancien groupe `com.fasterxml`. C'est
pourquoi le filtre du plugin *shade* couvre les deux groupes.

### Un fichier n'est jamais fiable

Lire un fichier, c'est accepter qu'il soit absent, abîmé, modifié à la main ou plus récent que le programme. Chaque
cas a sa réponse :

| Cas | Réponse |
|---|---|
| fichier absent | premier lancement : historique vide, sans erreur |
| JSON illisible, région inconnue, fichier « null » | le fichier est **mis de côté** (`historique.json.illisible-…`), pas effacé : le joueur peut le récupérer ; le jeu repart de zéro et le prévient |
| champ inconnu | ignoré |
| échec d'écriture (disque plein, droits) | le joueur est prévenu une fois et peut continuer à jouer |

Et pour l'écriture : on écrit d'abord un **fichier temporaire**, puis on le renomme. Un renommage est quasi
instantané, et même « atomique » quand le système le permet. Si le jeu est coupé pendant l'écriture, l'ancien
historique reste intact au lieu d'être remplacé par un fichier à moitié écrit.

### Les exceptions vérifiées : forcer à traiter l'erreur

`Sauvegarde.charger()` déclare `throws IOException`. C'est une exception *vérifiée* : le compilateur refuse qu'on
l'appelle sans la traiter (`try / catch`) ou la transmettre (`throws`). C'est voulu, car une erreur de fichier n'est
pas un bug du programme, mais un aléa normal qu'il faut prévoir. `SauvegardeIllisible` hérite d'`IOException` et
transporte une information utile, le chemin où le fichier a été mis de côté. À l'inverse, jouer dans une partie
terminée est une erreur de programmation : `Partie` lève une `IllegalStateException`, non vérifiée.

### Les tests ne touchent jamais aux vraies données

Les tests et la vérification ont enregistré des dizaines d'historiques. Aucun n'est allé dans le vrai
`~/.francle` : la `Sauvegarde` reçoit son fichier en paramètre (encore l'injection de dépendance), et les tests
utilisent `@TempDir`, un dossier que JUnit crée avant chaque test et efface après. Un test qui écrirait dans le
dossier du joueur effacerait ses statistiques à chaque `mvnw test`.

Et un test a trouvé un vrai bug : trois fichiers abîmés mis de côté **dans la même seconde** recevaient le même nom
horodaté, et le deuxième déplacement échouait. Le fichier abîmé serait resté en place, et le jeu aurait buté dessus
à chaque lancement. Désormais, un numéro est ajouté si le nom est pris (`-2`, `-3`…).

### Dessiner soi-même : `paintComponent`

Les barres de la répartition ne sont pas des composants : c'est un `JComponent` qui redéfinit `paintComponent`, et
y dessine avec un `Graphics2D` (`fillRoundRect`, `drawString`, `FontMetrics` pour mesurer le texte). Les règles du
dessin Swing :

- c'est **Swing** qui appelle `paintComponent`, quand il le faut (affichage, redimensionnement, fenêtre découverte) :
  on y dessine seulement, à partir des données, sans rien modifier ;
- on travaille sur une **copie** du pinceau (`g.create()`, puis `dispose()`), pour que l'anticrénelage ou la couleur
  choisis ne débordent pas sur le dessin des autres composants ;
- les tailles se calculent à partir de `getWidth()`, `getHeight()` et des marges de la bordure (`getInsets()`).

### Les notions Java rencontrées

- **`java.nio.file`** : `Path`, `Files.readString` / `writeString` / `move` / `createDirectories` / `list`,
  `StandardCopyOption`.
- **try-with-resources** (`try (var fichiers = Files.list(dossier)) { … }`) : `Files.list` ouvre le dossier, et sous
  Windows un dossier resté ouvert ne peut plus être effacé ; le `try` le referme quoi qu'il arrive.
- **Le constructeur compact d'un record** (`public Resultat { propositions = List.copyOf(propositions); }`) : il
  s'exécute avant que les champs soient remplis, pour valider ou copier les valeurs.
- **Une exception personnalisée** (`SauvegardeIllisible extends IOException`) qui garde sa cause (`super(message, cause)`).
- **Les blocs de texte** dans les tests, pour écrire un JSON lisible.

---

## Bilan de la Phase 5

- **Ce qui était nouveau** : le temps (une réponse qui dépend de la date), et la persistance (des données qui
  survivent au programme). Les deux posent la même question : de quoi dépend ce résultat, et que se passe-t-il
  si cette chose change ou disparaît ?
- **Les paramètres plutôt que l'environnement** : la date (`Clock`) et l'emplacement du fichier (`Sauvegarde`) sont
  donnés au code au lieu qu'il aille les chercher. C'est ce qui a permis de tout vérifier en 2030, dans un dossier
  temporaire, sans attendre le lendemain et sans toucher aux vraies statistiques.
- **L'architecture des phases 3 et 4 a encore payé** : `Partie` n'a pas changé d'une ligne. Reprendre une partie,
  c'est rejouer ses propositions à travers les règles existantes.

---

## Aide-mémoire

### Raccourcis IntelliJ

| Raccourci | Action |
|---|---|
| `Ctrl+Shift+F` | Chercher un texte dans tout le projet |
| `Alt+F7` | *Find Usages* : qui appelle cette méthode ? |
| `Shift+F6` | *Rename* : renommer partout d'un coup |
| `Ctrl+N` | Ouvrir une classe par son nom |
| `Ctrl+Alt+M` | *Extract Method* : transformer une sélection en méthode |
| `Alt+Suppr` | *Safe Delete* : supprimer, sauf si c'est encore utilisé |
| `F6` | *Move* : déplacer une méthode `static` vers une autre classe |
| `Ctrl+Alt+O` | *Optimize Imports* : retirer les imports inutiles |
| `Ctrl+Shift+F10` | Lancer le test (ou la classe) sous le curseur |
| Icône 🐞 | Lancer en *Debug* (avec points d'arrêt) |

### Commandes Git

| Commande | Usage |
|---|---|
| `git switch -c nom` | Créer une branche et s'y placer |
| `git mv ancien nouveau` | Renommer en gardant l'historique |
| `git log --oneline main..HEAD` | Les commits de ma branche absents de `main` |
| `git diff --stat` | Résumé des fichiers modifiés |
| `git rev-parse A^{tree} B^{tree}` | Comparer l'état exact des fichiers de deux versions |
| `git branch -d` / `-D` | Supprimer une branche (avec / sans vérification de fusion) |
| `git push origin --delete nom` | Supprimer une branche sur GitHub |
| `git fetch --prune` | Oublier en local les branches supprimées sur GitHub |
| `git tag -a nom -m "…"` | Poser une étiquette annotée |
| `git reflog` | Retrouver un commit « perdu » |

### Compilateur

| Commande | Usage |
|---|---|
| `javac -encoding UTF-8 -d out main.java` | Compiler dans `out/` |
| `javac -Xlint:all …` | Afficher tous les avertissements |
| `java -cp out main` | Lancer le programme |
| `java --patch-module java.desktop=dossier …` | Remplacer des classes du JDK par des doublures (tests) |

### Maven

| Commande | Usage |
|---|---|
| `.\mvnw.cmd test` | Compiler et lancer les tests |
| `.\mvnw.cmd package` | Compiler, tester et produire `target/francle.jar` |
| `java -jar target/francle.jar` | Lancer le jeu empaqueté |
| `.\mvnw.cmd clean` | Supprimer `target/` |
| `.\mvnw.cmd "-Dtest=PartieTest" test` | Lancer une seule classe de tests |

Sous PowerShell, le `.\` est obligatoire : par sécurité, PowerShell ne lance jamais un programme du dossier
courant sans qu'on le désigne explicitement (sinon un faux `notepad.cmd` déposé dans un dossier serait lancé à la
place du vrai). Les guillemets autour de `-Dtest=…` évitent que PowerShell découpe l'argument.
Sous Git Bash, macOS ou Linux : `./mvnw test`.
