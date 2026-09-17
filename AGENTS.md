# AGENTS.md

Projekt-spezifische Anleitung für Coding-Agenten. Globale Defaults stehen in
`~/.codex/AGENTS.md`; hier nur Abweichungen und projektspezifische Fakten.

## Was dieses Repo ist

OpenRewrite-Recipe-Bibliothek (`de.zettsystems:zettsystems-recipes`) mit vier
Java-Recipes und zwei deklarativen:

| Recipe | Zweck |
|---|---|
| `UseToList` | `collect(Collectors.toUnmodifiableList())` → `toList()`. Option `alsoChangeCollectorsToList` nimmt zusätzlich `Collectors.toList()` mit (modifiable → unmodifiable). |
| `UseLocaleWithCaseConversion` | `toLowerCase()`/`toUpperCase()` → mit `Locale.ROOT`. |
| `UseFloorMod` | `Math.abs(x) % n` → `Math.floorMod(x, n)`. |
| `FixAssertJThrowableInstanceOf` | verworfenes `asInstanceOf(throwable(X.class))` → `isInstanceOf(X.class)`. |
| `UseToListTrue` (YAML) | `UseToList` mit Option auf `true`. |
| `ZettSystemsRecipes` (YAML) | die drei reinen Refactorings. |

**`UseFloorMod` und `UseToListTrue` ändern Verhalten** und gehören deshalb
bewusst *nicht* in `ZettSystemsRecipes`.

Die drei neuen Recipes schließen Lücken im offiziellen Katalog: weder
`rewrite-static-analysis` 2.41.1 noch `rewrite-migrate-java` kennen
Locale-auf-Case-Conversion oder `Math.floorMod`; die AssertJ-Recipe räumt
hinter `org.openrewrite.java.testing.assertj.JUnitToAssertj` auf (die
Fehlübersetzung steht in `~/.codex/AGENTS.md` als „manuell korrigieren").

Außerdem zwei Demo-Konsumenten unter `demo-maven/` und `demo-gradle/`, die die
Recipe gegen `StudentManagement.java` laufen lassen — dienen nur als
Hands-on-Demo, sind kein Teil des Library-Builds.

**Nutzerdoku ist englisch** (`README.md`, `CHANGELOG.md`, die beiden
Demo-READMEs). Diese Datei bleibt deutsch, sie ist agentenintern.

## Build & Test

```bash
./gradlew build                                          # Library bauen + Tests
./gradlew test                                           # nur Tests
./gradlew test --tests "UseToListTest.replacesToListWhenOptionIsSet"
./gradlew sonar                                          # zentrale lokale Instanz
./gradlew publishToMavenLocal     # === ./gradlew pTML   # für Demos nötig
./gradlew dependencyUpdates                              # ben-manes-Report
./gradlew dependencyUpdates -Pmajor -Punstable           # inkl. Major + RC/M
```

Demos ziehen die Recipe aus Maven Central. Für eine noch nicht veröffentlichte
Version vorher `pTML` im Root — `demo-gradle` hat `mavenLocal()` als erstes
Repository, `demo-maven` findet `~/.m2` ohnehin.

```bash
cd demo-maven  && mvn rewrite:run       # Maven-Demo
cd demo-gradle && ./gradlew rewriteRun  # Gradle-Demo
```

Beide Demos **schreiben `StudentManagement.java` um** — danach
`git checkout -- src` im jeweiligen Demo-Verzeichnis.

## Architektur-Eigenheiten

- **Build**: Gradle 9.7.1 mit Groovy-DSL (`build.gradle`/`settings.gradle`),
  Java-Toolchain 25.
- **`compileJava` setzt `options.release = 17`.** Das Artefakt muss von jedem
  Build ladbar sein, der auf JDK 17+ läuft; mit Class-File-Version 25 wäre es
  für die meisten Konsumenten unbrauchbar. Tests laufen weiter auf 25.
- **Versionen zentral in `gradle.properties`** (inklusive `group` und
  `version` des Projekts) und im `build.gradle` per `"${xxxVersion}"`
  referenziert. Keine Version inline im plugins-/dependencies-Block hart
  kodieren.
- **OpenRewrite-Parser-Module**: `rewrite-java-17`, `-21`, `-25` als
  `testRuntimeOnly` — sie werden nur von den Test-Fixtures gebraucht, nicht
  vom veröffentlichten Artefakt.
- **Nichts Überflüssiges ins Jar.** `logback-classic` und `logback.xml` liegen
  im Test-Scope (`src/test/resources/`). Eine Bibliothek, die eine
  Logging-Konfiguration mitliefert, überschreibt die des Konsumenten.
- **Recipe-Implementierung** (`src/main/java/de/zettsystems/UseToList.java`):
  - `UsesMethod`-Precondition auf `Stream.collect` filtert irrelevante Dateien
    raus, bevor der Visitor läuft.
  - `MethodMatcher`-basierter Match auf `collect(Collector)` + Argument-Check
    auf `Collectors.toUnmodifiableList()` (immer) bzw. `Collectors.toList()`
    (nur wenn `alsoChangeCollectorsToList=true`).
  - Ersetzt wird die **ganze** Invocation über
    `JavaTemplate("#{any(java.util.stream.Stream)}.toList()")` plus
    `replace()`. `replaceMethod()` behält den Methodentyp von
    `collect(Collector)` und hinterlässt ein LST mit „argument count mismatch"
    — genau daran sind die Tests nach dem Versionsupdate hochgegangen.
  - Danach wird der originale `JRightPadded`-Select zurückgesetzt, sonst
    verliert eine Stream-Kette den Zeilenumbruch vor `.toList()`.
  - `maybeRemoveImport("java.util.stream.Collectors")` für den Cleanup.
  - Der `Boolean`-Konstruktor muss `null` vertragen — OpenRewrite übergibt
    `null` für eine nicht gesetzte optionale Option.
  - `@EqualsAndHashCode(callSuper = false)`: `Recipe` erbt `equals` von
    `Object`, mit `callSuper = true` wären zwei gleich konfigurierte Recipes
    nie gleich.
- **Zwei Recipe-IDs**: Die Java-Recipe `de.zettsystems.UseToList` und die
  deklarative YAML-Recipe `de.zettsystems.UseToListTrue` (in
  `src/main/resources/META-INF/rewrite/rewrite.yml`), die die Variante mit
  `alsoChangeCollectorsToList: true` vorbindet. Bis 1.0.0 hieß die YAML-Recipe
  fälschlich ebenfalls `de.zettsystems.UseToList` und referenzierte sich damit
  selbst.
- **Lombok** ist im Build (`@Getter`, `@EqualsAndHashCode`).
- **`Selects.restoreFormatting`** teilen sich alle Recipes, die eine ganze
  Invocation per Template ersetzen. Ohne den Aufruf verliert eine Kette den
  Zeilenumbruch vor dem ersetzten Aufruf.
- **`FixAssertJThrowableInstanceOf` nutzt bewusst kein `JavaTemplate`.** Ein
  Template müsste ein AssertJ-Snippet parsen und dafür
  `JavaParser…classpath("assertj-core")` setzen — das schaut auf den
  *Runtime-Classpath der Recipe*, und AssertJ ist hier nur `testImplementation`.
  Im Unit-Test fällt das nicht auf, beim Konsumenten schlägt es fehl. Statt
  dessen werden Name, Argument und `JavaType.Method` direkt am bestehenden
  Knoten getauscht. Dabei muss der **Name-Identifier dieselbe
  `JavaType.Method`-Instanz** tragen wie die Invocation, sonst lehnt
  OpenRewrite den Baum als fehltypisiert ab.
  Verifiziert wurde das end-to-end gegen das veröffentlichte Jar (Projekt ohne
  AssertJ auf dem `rewrite`-Classpath, anschließend `compileTestJava`) —
  bei Änderungen an dieser Recipe wieder so prüfen, Unit-Tests reichen nicht.

## Code-Qualität

Stack nach `~/.codex/AGENTS.md`, auf Bibliotheksgröße zugeschnitten:

- **ErrorProne + NullAway** auf `compileJava`, Severity `ERROR`,
  `annotatedPackages = de.zettsystems`, `package-info.java` mit
  `@NullMarked`. JSpecify ist `compileOnly` — Konsumenten sollen die
  Annotation nicht erben. Auf `compileTestJava` ist ErrorProne **aus**: die
  Test-Fixtures sind Text-Blöcke mit absichtlich unidiomatischem Java.
- **SpotBugs** (`MAX`/`LOW`), `config/spotbugs/exclude.xml`. Der einzige
  strukturelle Filter: `SIC_INNER_SHOULD_BE_STATIC_ANON` auf `getVisitor` —
  die anonyme Visitor-Klasse ist OpenRewrites Idiom.
- **JaCoCo** 95 % Line / 85 % Branch, `check` hängt dran. Die verbleibenden
  ungedeckten Branches sind Defensivprüfungen, die aus gültigem Java nicht
  erreichbar sind (Invocation ohne Receiver, Ausdruck ohne Typ) — nicht
  künstlich abdecken, lieber die Schwelle so lassen.
- **Sonar** (`sonar.projectKey` `zettsystems-recipes`), 0 offene Issues.
  `java:S2699` ist auf `**/*Test.java` stummgeschaltet: `rewriteRun(...)`
  **ist** die Assertion, Sonar kennt die Methode nur nicht.

## Wenn die Recipe-ID umbenannt wird

`de.zettsystems.UseToList` ist hardcoded an folgenden Stellen referenziert:

- `src/main/resources/META-INF/rewrite/rewrite.yml` (beide YAML-Recipes)
- `src/test/java/de/zettsystems/UseToListTest.java`
- `demo-maven/pom.xml` (`<activeRecipes>`)
- `demo-gradle/build.gradle` (`activeRecipe(...)`)
- `README.md`

Ein Rename muss alle Stellen mitziehen, sonst brechen die Demos.

## Tests schreiben

`UseToListTest` nutzt das `RewriteTest`-Interface. Pro Testfall ein
`rewriteRun(java(before, after))` für eine erwartete Transformation, oder
`rewriteRun(java(unchanged))` für „darf nicht angefasst werden".
`spec.recipe(new UseToList(true|false))` setzt den Parameter pro Test,
`spec.recipeFromResource("/META-INF/rewrite/rewrite.yml", "…")` prüft die
deklarative Variante.

## Publishing

- **Maven Central über das Central Portal**, nicht mehr über OSSRH — siehe
  `agent-config/docs/MAVEN-CENTRAL.md` für Konto, Namespace und GPG-Schlüssel.
  Der alte `s01.oss.sonatype.org`-Endpunkt ist seit Juni 2025 tot.
- `com.vanniktech.maven.publish` erledigt Signierung, POM-Prüfung und Upload.
  Lokal signiert `useGpgCmd()` mit dem Schlüsselbund; in der CI greift der
  In-Memory-Key (`signingInMemoryKey`), deshalb wird `useGpgCmd()` nur gesetzt,
  wenn diese Property fehlt.
- Zugangsdaten als Gradle-Properties `mavenCentralUsername` /
  `mavenCentralPassword` (Publisher-Token aus dem Portal, **nicht** das
  Login-Passwort).
- `./gradlew publishToMavenCentral` lädt ein Deployment hoch, gibt es aber
  **nicht** frei — Freigabe von Hand unter
  <https://central.sonatype.com/publishing/deployments>.
  `publishAndReleaseToMavenCentral` gäbe automatisch frei; eine
  veröffentlichte Version ist unwiderruflich.
- CI: `.github/workflows/release.yml` auf Tag `v<version>`, braucht die
  Secrets `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD`, `SIGNING_KEY`,
  `SIGNING_PASSWORD`.
- **Default-Branch ist `master`**, nicht `main`. Die Workflows lagen bis 1.0.0
  auf `main` und liefen deshalb nie.

## Versions-Fallen (Stand 2026-09-17)

Die OpenRewrite-Veröffentlichungen laufen gerade auseinander: `rewrite-recipe-bom`
3.38.0 und das Gradle-Plugin 7.40.0/7.41.0 verweisen auf `rewrite-bom:8.91.0`,
das nicht in Central liegt — beides ist unauflösbar. Deshalb bewusst gepinnt:
BOM **3.37.0**, Gradle-Plugin **7.39.0**, Maven-Plugin **6.46.1**. Vor einem
Update prüfen, ob `rewrite-bom` in der passenden Version wirklich publiziert ist.
