# zettsystems-recipes

Custom OpenRewrite Recipe `UseToList` — ersetzt `collect(Collectors.toUnmodifiableList())` durch das modernere
`Stream.toList()` (Java 16+).

## Hintergrund

Geschrieben 2023, als OpenRewrite für diese Migration noch keine Standard-Recipe im offiziellen Katalog hatte.
Mittlerweile gibt es vergleichbare Migrationen im offiziellen Recipe-Set (
`org.openrewrite.java.migrate.util.UseStreamToListNotCollect` u. ä.) — als Hands-on-Beispiel zur Recipe-Engine ist die
Implementierung hier weiterhin nützlich.

## Was die Recipe macht

```java
// vorher
list.stream()
    .

filter(...)
    .

collect(Collectors.toUnmodifiableList());

// nachher
        list.

stream()
    .

filter(...)
    .

toList();
```

Optionaler Parameter `alsoChangeCollectorsToList: true` ersetzt zusätzlich das ältere `Collectors.toList()`.

## Struktur

```
zettsystems-recipes/
├── src/                   Recipe-Definition (Java)
├── build.gradle.kts       Build der Recipe-Bibliothek
├── demo-maven/            Demo-Konsument via Maven (mvn rewrite:run)
└── demo-gradle/           Demo-Konsument via Gradle (gradlew rewriteRun)
```

## Recipe lokal bauen und veröffentlichen

```bash
./gradlew publishToMavenLocal
# oder ./gradlew pTML
```

Veröffentlicht nach `~/.m2/repository`. Anschließend kann die Recipe in den beiden Demo-Projekten oder in eigenen
Projekten verwendet werden.

## Recipe in Maven verwenden

```xml

<plugin>
    <groupId>org.openrewrite.maven</groupId>
    <artifactId>rewrite-maven-plugin</artifactId>
    <version>5.12.0</version>
    <configuration>
        <activeRecipes>
            <recipe>de.zettsystems.UseToList</recipe>
        </activeRecipes>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>de.zettsystems</groupId>
            <artifactId>zettsystems-recipes</artifactId>
            <version>0.1.0</version>
        </dependency>
    </dependencies>
</plugin>
```

Vollständiges Beispiel inkl. Vorher/Nachher: [`demo-maven/`](demo-maven/).

## Recipe in Gradle verwenden

Anders als Maven muss Gradle explizit konfiguriert werden, damit die Recipe aus Maven Local aufgelöst wird:

```groovy
plugins {
    id("java")
    id("org.openrewrite.rewrite") version("latest.release")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    rewrite("de.zettsystems:zettsystems-recipes:latest.integration")
}

rewrite {
    activeRecipe("de.zettsystems.UseToList")
}
```

Vollständiges Beispiel inkl. Vorher/Nachher: [`demo-gradle/`](demo-gradle/).

## `Collectors.toList()` mitwandeln

Ein eigenes `rewrite.yaml` im Projekt-Root anlegen:

```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: de.zettsystems.UseToListTrue
recipeList:
  - de.zettsystems.UseToList:
      alsoChangeCollectorsToList: true
```

Und als aktive Recipe verwenden:

```xml
<configuration>
    <activeRecipes>
        <recipe>de.zettsystems.UseToListTrue</recipe>
    </activeRecipes>
</configuration>
```

## Stack

Java 25+, OpenRewrite, Gradle Kotlin DSL.
