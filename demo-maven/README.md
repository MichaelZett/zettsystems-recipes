# demo-maven

Runnable example of the [`UseToList` recipe](../) with the `rewrite-maven-plugin`.

```bash
mvn rewrite:run
```

`src/main/java/de/zettsystems/exercise/StudentManagement.java` uses `Collectors.toUnmodifiableList()` and
`Collectors.toList()`; after the run both are `Stream.toList()`. Restore the sample with `git checkout -- src`.

[`pom.xml`](pom.xml) activates `de.zettsystems.UseToListTrue` — the variant that also converts `Collectors.toList()` —
next to `org.openrewrite.java.migrate.UpgradeToJava17`, so the diff shows text blocks and pattern matching as well.
