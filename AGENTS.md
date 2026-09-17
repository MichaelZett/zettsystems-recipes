# AGENTS.md

Projekt-spezifische Anleitung für Coding-Agenten. Globale Defaults stehen in
`~/.codex/AGENTS.md`; hier nur Abweichungen und projektspezifische Fakten.

## Was dieses Repo ist

OpenRewrite-Recipe-Bibliothek mit **einer einzigen Recipe**: `UseToList`
(`de.zettsystems:zettsystems-recipes`). Ersetzt
`stream.collect(Collectors.toUnmodifiableList())` durch `stream.toList()`.
Optionaler Boolean-Parameter `alsoChangeCollectorsToList` wandelt zusätzlich
`Collectors.toList()` mit (Achtung: ändert dabei modifiable → unmodifiable).

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

## Wenn die Recipe-ID umbenannt wird

`de.zettsystems.UseToList` ist hardcoded an folgenden Stellen referenziert:

- `src/main/resources/META-INF/rewrite/rewrite.yml`
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
