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
`compile` → `test` → `package` (le `.jar`) → `verify`. `mvnw.cmd package` compile donc **et teste** avant
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

## Aide-mémoire

### Raccourcis IntelliJ

| Raccourci | Action |
|---|---|
| `Ctrl+Shift+F` | Chercher un texte dans tout le projet |
| `Alt+F7` | *Find Usages* : qui appelle cette méthode ? |
| `Shift+F6` | *Rename* : renommer partout d'un coup |
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
| `mvnw.cmd test` | Compiler et lancer les tests |
| `mvnw.cmd package` | Compiler, tester et produire `target/francle.jar` |
| `java -jar target/francle.jar` | Lancer le jeu empaqueté |
| `mvnw.cmd clean` | Supprimer `target/` |
| `mvnw.cmd -Dtest=PartieTest test` | Lancer une seule classe de tests |
