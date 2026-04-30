# demo-maven

Maven-Demo-Konsument für die [`UseToList`-Recipe](../) aus dem übergeordneten `zettsystems-recipes`-Projekt — zeigt die
Anwendung via `rewrite-maven-plugin`.

## Voraussetzung

Recipe einmal lokal veröffentlichen (im Repo-Root):

```bash
./gradlew publishToMavenLocal
```

## Anwendung

Im Verzeichnis `demo-maven/`:

```bash
mvn rewrite:run
```

## Was passiert

In `src/main/java/de/zettsystems/exercise/` befindet sich Beispielcode mit `Collectors.toUnmodifiableList()`. Nach
`mvn rewrite:run` ist der Code auf `Stream.toList()` migriert.

## Konfiguration

- `pom.xml` — bindet das `rewrite-maven-plugin` mit der lokal veröffentlichten Recipe ein
- `rewrite.yml` — aktiviert die `UseToListTrue`-Variante (mit `alsoChangeCollectorsToList: true`)
