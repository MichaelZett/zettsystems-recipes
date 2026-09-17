# zettsystems-recipes

[![Maven Central](https://img.shields.io/maven-central/v/de.zettsystems/zettsystems-recipes.svg)](https://central.sonatype.com/artifact/de.zettsystems/zettsystems-recipes)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)

A small set of [OpenRewrite](https://docs.openrewrite.org/) recipes for cleanups the official catalog does not cover.

```diff
 list.stream()
     .filter(...)
-    .collect(Collectors.toUnmodifiableList());
+    .toList();

-return name.toLowerCase();
+return name.toLowerCase(Locale.ROOT);

-return Math.abs(key.hashCode()) % buckets;
+return Math.floorMod(key.hashCode(), buckets);
```

## Recipes

| Recipe | What it does |
|---|---|
| `de.zettsystems.UseToList` | `collect(Collectors.toUnmodifiableList())` → `toList()` |
| `de.zettsystems.UseLocaleWithCaseConversion` | `toLowerCase()` / `toUpperCase()` → the same with `Locale.ROOT` |
| `de.zettsystems.FixAssertJThrowableInstanceOf` | a discarded `asInstanceOf(throwable(X.class))` → `isInstanceOf(X.class)` |
| `de.zettsystems.ZettSystemsRecipes` | the three above in one go |
| `de.zettsystems.UseToListTrue` | like `UseToList`, but converts `Collectors.toList()` too |
| `de.zettsystems.UseFloorMod` | `Math.abs(x) % n` → `Math.floorMod(x, n)` |

The first four are pure refactorings. The last two change behaviour and are therefore not part of
`ZettSystemsRecipes` — read the diff before committing:

- **`UseToListTrue`** — `Collectors.toList()` returns a *modifiable* list, `Stream.toList()` does not. Code that
  mutates the result starts throwing `UnsupportedOperationException`. (`UseToList` also takes this as the option
  `alsoChangeCollectorsToList`, default `false`.)
- **`UseFloorMod`** — the two expressions differ for negative input: `Math.abs(-7) % 3` is `1`, `Math.floorMod(-7, 3)`
  is `2`. That is the point — `Math.abs(Integer.MIN_VALUE)` is still negative, so the old form can return a negative
  bucket index — but it is a fix, not a refactoring.

### What the pure refactorings guarantee

`UseToList` only touches `toUnmodifiableList`, which has the same semantics as `Stream.toList()`; the now-unused
`Collectors` import is removed. `UseLocaleWithCaseConversion` makes the conversion independent of the machine's
default locale, which is what almost every caller already assumed — use a specific locale where the result is shown
to a person. `FixAssertJThrowableInstanceOf` only rewrites statements whose result is discarded, where the assertion
never ran at all; a chained `asInstanceOf(...)` is left alone.

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
            <recipe>de.zettsystems.ZettSystemsRecipes</recipe>
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
    activeRecipe('de.zettsystems.ZettSystemsRecipes')
}
```

```bash
./gradlew rewriteRun
```

Every recipe in the table can be activated individually in the same way; all of them ship inside the artifact, so no
local `rewrite.yml` is needed.

## Upgrading from 0.1.0

- The declarative recipe in the artifact was renamed from `de.zettsystems.UseToList` to `de.zettsystems.UseToListTrue`.
  If a local `rewrite.yml` defined `UseToListTrue`, delete it — the artifact provides it now.
- Three recipes were added; see the table above.
- The artifact is compiled for Java 17 instead of Java 8, so it needs a build running on JDK 17 or newer.
- `logback-classic` is no longer pulled in transitively.

See the [changelog](CHANGELOG.md) for the full list.

## Examples

Runnable before/after demos: [`demo-maven/`](demo-maven/) and [`demo-gradle/`](demo-gradle/).

## Background

`UseToList` was written in 2023, when OpenRewrite's catalog did not cover that migration yet; the official set now has
equivalents such as `org.openrewrite.java.migrate.util.UseStreamToListNotCollect`. The other recipes fill gaps that are
still open: neither `rewrite-static-analysis` nor `rewrite-migrate-java` adds a locale to case conversions or knows
about `Math.floorMod`, and the AssertJ fix cleans up after `org.openrewrite.java.testing.assertj.JUnitToAssertj`.

## Building from source

```bash
./gradlew build                 # compile, test, ErrorProne/NullAway, SpotBugs, JaCoCo
./gradlew publishToMavenLocal   # install into ~/.m2
```

`master` carries a `-SNAPSHOT` version between releases. Snapshots are not published anywhere, so use
`publishToMavenLocal` to try out an unreleased state.

Building needs a JDK 25 toolchain; Gradle fetches one through the foojay resolver if none is installed.

## License

[Apache License 2.0](LICENSE)
