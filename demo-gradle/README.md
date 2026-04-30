# demo-gradle

Gradle-Demo-Konsument für die [`UseToList`-Recipe](../) aus dem übergeordneten `zettsystems-recipes`-Projekt — zeigt die
Anwendung via `org.openrewrite.rewrite`-Plugin.

## Voraussetzung

Recipe einmal lokal veröffentlichen (im Repo-Root):

```bash
./gradlew publishToMavenLocal
```

## Anwendung

Im Verzeichnis `demo-gradle/`:

```bash
./gradlew rewriteRun
```

## Was passiert

In `src/main/java/de/zettsystems/exercise/` befindet sich Beispielcode mit `Collectors.toUnmodifiableList()`. Nach
`./gradlew rewriteRun` ist der Code auf `Stream.toList()` migriert.

## Konfiguration

- `build.gradle` — bindet das `org.openrewrite.rewrite`-Plugin und die lokal veröffentlichte Recipe ein (`mavenLocal()`
  als Repository)
- `rewrite.yml` — aktiviert die `UseToListTrue`-Variante (mit `alsoChangeCollectorsToList: true`)
