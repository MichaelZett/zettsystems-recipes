# demo-gradle

Runnable example of the [`UseToList` recipe](../) with the `org.openrewrite.rewrite` plugin.

```bash
./gradlew rewriteRun
```

`src/main/java/de/zettsystems/exercise/StudentManagement.java` uses `Collectors.toUnmodifiableList()`,
`Collectors.toList()` and a locale-dependent `toLowerCase()`. After the run the first two are `Stream.toList()` and the
third carries `Locale.ROOT`. Restore the sample with `git checkout -- src`.

[`build.gradle`](build.gradle) activates `de.zettsystems.UseToListTrue` and `de.zettsystems.UseLocaleWithCaseConversion`,
next to `org.openrewrite.java.migrate.UpgradeToJava17` — so the diff shows text blocks and pattern matching as well.
