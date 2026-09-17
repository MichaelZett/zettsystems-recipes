# demo-maven

Runnable example of the [`UseToList` recipe](../) with the `rewrite-maven-plugin`.

```bash
mvn rewrite:run
```

`src/main/java/de/zettsystems/exercise/StudentManagement.java` uses `Collectors.toUnmodifiableList()`,
`Collectors.toList()` and a locale-dependent `toLowerCase()`. After the run the first two are `Stream.toList()` and the
third carries `Locale.ROOT`. Restore the sample with `git checkout -- src`.

[`pom.xml`](pom.xml) activates `de.zettsystems.UseToListTrue` and `de.zettsystems.UseLocaleWithCaseConversion`,
next to `org.openrewrite.java.migrate.UpgradeToJava17` — so the diff shows text blocks and pattern matching as well.
