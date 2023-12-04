## Zettsystems-Recipes
For now this is just 1 recipe for OpenRewrite: UseToList (instead of collect(Collectors.toUnmodifiableList())).

## Local Publishing
To do this on the command line, run:
```bash
./gradlew publishToMavenLocal
# or ./gradlew pTML
```
This will publish to your local maven repository, typically under `~/.m2/repository`.

## Usage
```xml
<project>
    <build>
        <plugins>
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
        </plugins>
    </build>
</project>
```

Unlike Maven, Gradle must be explicitly configured to resolve dependencies from Maven local.
The root project of your Gradle build, make your recipe module a dependency of the `rewrite` configuration:

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
    rewrite(de.zettsystems:zettsystems-recipes:latest.integration")
}

rewrite {
    activeRecipe("de.zettsystems.UseToList")
}
```

Now you can run `mvn rewrite:run` or `gradlew rewriteRun` to run your recipe.

## Update Collectors.toList() as well
Write a rewrite.yaml like this:
```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: de.zettsystems.UseToListTrue
recipeList:
- de.zettsystems.UseToList:
  alsoChangeCollectorsToList: true
```
And put it at your project's root.
Then use
```xml

<configuration>
    <activeRecipes>
        <recipe>de.zettsystems.UseToListTrue</recipe>
    </activeRecipes>
</configuration>
```
instead.
