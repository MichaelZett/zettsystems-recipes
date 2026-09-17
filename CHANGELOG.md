# Changelog

Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), versioning follows
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased

## 1.0.0 - 2026-09-17

### Added

- `de.zettsystems.UseLocaleWithCaseConversion` — adds `Locale.ROOT` to `String.toLowerCase()` and
  `String.toUpperCase()`. Under a Turkish locale `"I".toLowerCase()` yields a dotless `ı`, which breaks lookup keys
  and protocol tokens.
- `de.zettsystems.UseFloorMod` — `Math.abs(x) % n` → `Math.floorMod(x, n)`. `Math.abs(Integer.MIN_VALUE)` overflows
  back to a negative number, so the old form can return a negative bucket index. The two expressions differ for
  negative input, so this is a fix rather than a refactoring.
- `de.zettsystems.FixAssertJThrowableInstanceOf` — rewrites a discarded `asInstanceOf(throwable(X.class))` into
  `isInstanceOf(X.class)` and drops the then-unused `throwable` static import. `asInstanceOf` narrows the assertion
  type and returns a new assertion; used as a statement the check never runs.
  `org.openrewrite.java.testing.assertj.JUnitToAssertj` produces this shape.
- `de.zettsystems.ZettSystemsRecipes` — runs the three recipes that are pure refactorings. `UseToListTrue` and
  `UseFloorMod` stay out of it, because both change behaviour.

### Breaking

- The declarative recipe bundled with the artifact is now named `de.zettsystems.UseToListTrue`. It used to carry the
  same name as the Java recipe it referenced, `de.zettsystems.UseToList`, which shadowed that recipe with a
  self-referencing one. A local `rewrite.yml` defining `UseToListTrue` is no longer needed and should be removed.
- The artifact is compiled for Java 17 rather than Java 8; it needs a build running on JDK 17 or newer.
- `logback-classic` and a bundled `logback.xml` are gone. The library no longer drags a logging backend and its
  configuration into consumer builds.

### Fixed

- The rewritten `toList()` call kept the method type of `collect(Collector)`, leaving behind an invalid syntax tree.
- A chained stream call lost the line break before `.toList()`; the original formatting is preserved now.
- `new UseToList(null)` threw a `NullPointerException`. OpenRewrite passes `null` for an optional option that was not
  configured; it now means `false`.
- Two `UseToList` instances with the same option never compared equal, because `equals` fell through to the
  identity-based implementation of `Recipe`.
- `collect(...)` without a receiver is skipped instead of producing a broken replacement.

### Changed

- Released through the [Maven Central Portal](https://central.sonatype.com). The OSSRH staging endpoint the previous
  release used was shut down in June 2025.
- The build runs ErrorProne with NullAway, SpotBugs and JaCoCo, and has a SonarQube configuration. NullAway found the
  `Boolean` unboxing bug listed above.
- Dependencies updated: OpenRewrite recipe BOM 3.37.0, Lombok 1.18.48, JUnit 6.1.3, Gradle 9.7.1.
- Documentation is in English throughout.

## 0.1.0 - 2023-12-20

- Initial release of the `UseToList` recipe.
