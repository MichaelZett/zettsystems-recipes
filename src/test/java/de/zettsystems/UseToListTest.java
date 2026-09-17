/*
 * Copyright 2023 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.zettsystems;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openrewrite.java.Assertions.java;

class UseToListTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseToList(true));
    }

    @Test
    void replacesToUnmodifiableList() {
        rewriteRun(
          // language=java
          java(
            """
              import java.util.List;
              import java.util.stream.Collectors;
              import java.util.stream.Stream;

              class Test {
                  List<String> names() {
                      return Stream.of("test").collect(Collectors.toUnmodifiableList());
                  }
              }
              """,
            """
              import java.util.List;
              import java.util.stream.Stream;

              class Test {
                  List<String> names() {
                      return Stream.of("test").toList();
                  }
              }
              """
          )
        );
    }

    @Test
    void replacesToListWhenOptionIsSet() {
        rewriteRun(
          // language=java
          java(
            """
              import java.util.List;
              import java.util.stream.Collectors;
              import java.util.stream.Stream;

              class Test {
                  List<String> names() {
                      return Stream.of("test").collect(Collectors.toList());
                  }
              }
              """,
            """
              import java.util.List;
              import java.util.stream.Stream;

              class Test {
                  List<String> names() {
                      return Stream.of("test").toList();
                  }
              }
              """
          )
        );
    }

    @Test
    void keepsToListWhenOptionIsNotSet() {
        rewriteRun(
          spec -> spec.recipe(new UseToList(false)),
          // language=java
          java(
            """
              import java.util.List;
              import java.util.stream.Collectors;
              import java.util.stream.Stream;

              class Test {
                  List<String> names() {
                      return Stream.of("test").collect(Collectors.toList());
                  }
              }
              """
          )
        );
    }

    @Test
    void keepsCollectorsOtherThanToList() {
        rewriteRun(
          // language=java
          java(
            """
              import java.util.Set;
              import java.util.stream.Collectors;
              import java.util.stream.Stream;

              class Test {
                  Set<String> names() {
                      return Stream.of("test").collect(Collectors.toSet());
                  }
              }
              """
          )
        );
    }

    @Test
    void keepsThreeArgumentCollect() {
        rewriteRun(
          // language=java
          java(
            """
              import java.util.ArrayList;
              import java.util.List;
              import java.util.stream.Stream;

              class Test {
                  List<String> names() {
                      return Stream.of("test").collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                  }
              }
              """
          )
        );
    }

    @Test
    void keepsCollectorsToListWhenUsingTheDefaultConstructor() {
        rewriteRun(
          spec -> spec.recipe(new UseToList()),
          // language=java
          java(
            """
              import java.util.List;
              import java.util.stream.Collectors;
              import java.util.stream.Stream;

              class Test {
                  List<String> names() {
                      return Stream.of("test").collect(Collectors.toList());
                  }
              }
              """
          )
        );
    }

    @Test
    void appliesTheDeclarativeUseToListTrueRecipe() {
        rewriteRun(
          spec -> spec.recipeFromResource("/META-INF/rewrite/rewrite.yml", "de.zettsystems.UseToListTrue"),
          // language=java
          java(
            """
              import java.util.List;
              import java.util.stream.Collectors;
              import java.util.stream.Stream;

              class Test {
                  List<String> names() {
                      return Stream.of("test").collect(Collectors.toList());
                  }
              }
              """,
            """
              import java.util.List;
              import java.util.stream.Stream;

              class Test {
                  List<String> names() {
                      return Stream.of("test").toList();
                  }
              }
              """
          )
        );
    }

    @Test
    void keepsTheFormattingOfAChainedCall() {
        rewriteRun(
          // language=java
          java(
            """
              import java.util.List;
              import java.util.stream.Collectors;
              import java.util.stream.Stream;

              class Test {
                  List<String> names() {
                      return Stream.of("test")
                              .filter(s -> !s.isEmpty())
                              .collect(Collectors.toUnmodifiableList());
                  }
              }
              """,
            """
              import java.util.List;
              import java.util.stream.Stream;

              class Test {
                  List<String> names() {
                      return Stream.of("test")
                              .filter(s -> !s.isEmpty())
                              .toList();
                  }
              }
              """
          )
        );
    }

    @Test
    void treatsAnUnsetOptionAsFalse() {
        assertFalse(new UseToList((Boolean) null).isAlsoChangeCollectorsToList());
        assertFalse(new UseToList().isAlsoChangeCollectorsToList());
        assertTrue(new UseToList(true).isAlsoChangeCollectorsToList());
    }
}
