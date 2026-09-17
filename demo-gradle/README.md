# demo-gradle

Runnable example of the [`UseToList` recipe](../) with the `org.openrewrite.rewrite` plugin.

```bash
./gradlew rewriteRun
```

`src/main/java/de/zettsystems/exercise/StudentManagement.java` uses `Collectors.toUnmodifiableList()` and
`Collectors.toList()`; after the run both are `Stream.toList()`. Restore the sample with `git checkout -- src`.

[`build.gradle`](build.gradle) activates `de.zettsystems.UseToListTrue` — the variant that also converts
`Collectors.toList()` — next to `org.openrewrite.java.migrate.UpgradeToJava17`, so the diff shows text blocks and
pattern matching as well.
