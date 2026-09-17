# zettsystems-recipes

[![Maven Central](https://img.shields.io/maven-central/v/de.zettsystems/zettsystems-recipes.svg)](https://central.sonatype.com/artifact/de.zettsystems/zettsystems-recipes)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)

An [OpenRewrite](https://docs.openrewrite.org/) recipe that replaces `collect(Collectors.toUnmodifiableList())` with
`Stream.toList()`:

```diff
 list.stream()
     .filter(...)
-    .collect(Collectors.toUnmodifiableList());
+    .toList();
```

Both forms return an unmodifiable list, so the change preserves behaviour. An unused
`java.util.stream.Collectors` import is removed along the way.

## Recipes

| Recipe | Converts |
|---|---|
| `de.zettsystems.UseToList` | `collect(Collectors.toUnmodifiableList())` |
| `de.zettsystems.UseToListTrue` | additionally `collect(Collectors.toList())` |

`de.zettsystems.UseToList` has one optional boolean option, `alsoChangeCollectorsToList` (default `false`).
`de.zettsystems.UseToListTrue` is a ready-made variant with that option set to `true`.

> `Collectors.toList()` returns a *modifiable* list, `Stream.toList()` does not. Code that mutates the result will
> start throwing `UnsupportedOperationException`, so review what `UseToListTrue` changes.

The rewritten code needs Java 16+, which is where `Stream.toList()` was introduced. The artifact itself runs on any
build using JDK 17 or newer.

## Maven

```xml
<plugin>
    <groupId>org.openrewrite.maven</groupId>
    <artifactId>rewrite-maven-plugin</artifactId>
    <version>6.46.1</version>
    <configuration>
        <activeRecipes>
            <recipe>de.zettsystems.UseToList</recipe>
        </activeRecipes>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>de.zettsystems</groupId>
            <artifactId>zettsystems-recipes</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
</plugin>
```

```bash
mvn rewrite:run
```

## Gradle

```groovy
plugins {
    id 'java'
    id 'org.openrewrite.rewrite' version '7.39.0'
}

repositories {
    mavenCentral()
}

dependencies {
    rewrite 'de.zettsystems:zettsystems-recipes:1.0.0'
}

rewrite {
    activeRecipe('de.zettsystems.UseToList')
}
```

```bash
./gradlew rewriteRun
```

## Converting `Collectors.toList()` as well

Activate `de.zettsystems.UseToListTrue` instead of `de.zettsystems.UseToList`. Nothing else changes — since 1.0.0 that
recipe ships inside the artifact, so no local `rewrite.yml` is needed.

## Upgrading from 0.1.0

- The declarative recipe in the artifact was renamed from `de.zettsystems.UseToList` to `de.zettsystems.UseToListTrue`.
  If a local `rewrite.yml` defined `UseToListTrue`, delete it — the artifact provides it now.
- The artifact is compiled for Java 17 instead of Java 8, so it needs a build running on JDK 17 or newer.
- `logback-classic` is no longer pulled in transitively.

See the [changelog](CHANGELOG.md) for the full list.

## Examples

Runnable before/after demos: [`demo-maven/`](demo-maven/) and [`demo-gradle/`](demo-gradle/).

## Background

Written in 2023, when OpenRewrite's catalog did not cover this migration yet. The official recipe set now has
equivalents such as `org.openrewrite.java.migrate.util.UseStreamToListNotCollect`; this project remains useful as a
compact example of a hand-written recipe.

## Building from source

```bash
./gradlew build                 # compile and test (JDK 25 toolchain)
./gradlew publishToMavenLocal   # install into ~/.m2
```

## License

[Apache License 2.0](LICENSE)
