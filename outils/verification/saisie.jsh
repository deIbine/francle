import fr.francle.donnees.*;
import fr.francle.jeu.*;
import java.util.*;
import java.util.function.*;
import java.text.Normalizer;

String sansAccents(String s) { return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", ""); }
Map<String, UnaryOperator<String>> variantes = new LinkedHashMap<>();
variantes.put("tel quel", s -> s);
variantes.put("minuscules", s -> s.toLowerCase());
variantes.put("MAJUSCULES", s -> s.toUpperCase());
variantes.put("sans accents", s -> sansAccents(s));
variantes.put("espaces/tirets", s -> s.replace("-", " "));
variantes.put("espaces autour", s -> "  " + s + "  ");
variantes.put("tab autour", s -> "\t" + s + "\n");
variantes.put("apostrophe courbe", s -> s.replace("'", "’"));
variantes.put("tout mélangé", s -> "  " + sansAccents(s).toLowerCase().replace("-", " ").replace("'", "’") + " ");
// au-delà de ce que demande le web : à titre d'information
variantes.put("[info] espace insécable", s -> s.replace(" ", " ").replace("-", " "));
variantes.put("[info] espace insécable autour", s -> " " + s + " ");
variantes.put("[info] apostrophe ʼ (U+02BC)", s -> s.replace("'", "ʼ"));
variantes.put("[info] apostrophe ‘ (U+2018)", s -> s.replace("'", "‘"));
variantes.put("[info] tiret insécable (U+2011)", s -> s.replace("-", "‑"));
variantes.put("[info] tiret demi-cadratin –", s -> s.replace("-", "–"));

boolean ModeRegionsContient(ModeRegions m, RegionFr r) { return RegionFr.chercher(r.getNom(Langue.FR), m).isPresent(); }
int total = 0, ko = 0;
List<String> echecs = new ArrayList<>();
for (RegionFr r : RegionFr.values()) {
  for (ModeRegions mode : ModeRegions.values()) {
    boolean dansMode = RegionFr.chercher(r.getNom(Langue.FR)).flatMap(x -> RegionFr.chercher(x.getNom(Langue.FR), mode)).isPresent();
    if (!dansMode) continue;
    for (Langue l : Langue.values()) {
      String nom = r.getNom(l);
      for (var v : variantes.entrySet()) {
        String saisie = v.getValue().apply(nom);
        Optional<RegionFr> trouve = RegionFr.chercher(saisie, mode);
        // et par la vraie partie (comme la fenêtre) : une cible différente pour que ce soit un essai
        RegionFr cible = r == RegionFr.CORSE ? RegionFr.BRETAGNE : RegionFr.CORSE;
        if (!ModeRegionsContient(mode, cible)) cible = RegionFr.BRETAGNE;
        Partie p = new Partie(cible, mode);
        Reponse rep = p.proposer(saisie);
        boolean okPartie = rep instanceof Reponse.Jouee j && j.essai().proposition() == r;
        total++;
        if (trouve.orElse(null) != r || !okPartie) {
          ko++;
          echecs.add(String.format("%-8s %-3s %-32s %-34s chercher=%s partie=%s", mode, l, nom, v.getKey(), trouve.orElse(null), rep));
        }
      }
    }
  }
}
System.out.println("régions : " + RegionFr.values().length + ", saisies essayées : " + total + ", échecs : " + ko);
echecs.forEach(System.out::println);

// collisions de clés : deux régions (ou deux langues) qui donneraient la même clé
Map<String, Set<RegionFr>> parCle = new HashMap<>();
for (RegionFr r : RegionFr.values()) for (Langue l : Langue.values())
  parCle.computeIfAbsent(r.getNom(l).toLowerCase().replaceAll("[\\s\\p{Punct}’]", "").transform(x -> sansAccents(x)), k -> new HashSet<>()).add(r);
parCle.forEach((k, v) -> { if (v.size() > 1) System.out.println("COLLISION " + k + " " + v); });

// les départements, pour information
int dTotal = 0, dKo = 0;
for (Departement d : Departement.tous()) {
  for (String nom : List.of(d.nom(), d.code(), d.nomEtNumero())) {
    for (var v : variantes.entrySet()) {
      if (v.getKey().startsWith("[info]")) continue;
      String s = v.getValue().apply(nom);
      dTotal++;
      if (Departement.chercher(s).orElse(null) != d) { dKo++; System.out.println("DEPT " + nom + " / " + v.getKey() + " -> " + Departement.chercher(s).orElse(null)); }
    }
  }
}
System.out.println("départements : saisies " + dTotal + ", échecs " + dKo);
/exit
