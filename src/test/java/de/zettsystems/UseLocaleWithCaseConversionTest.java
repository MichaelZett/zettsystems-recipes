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

import static org.openrewrite.java.Assertions.java;

class UseLocaleWithCaseConversionTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseLocaleWithCaseConversion());
    }

    @Test
    void addsLocaleToToLowerCase() {
        rewriteRun(
          // language=java
          java(
            """
              class Test {
                  String key(String name) {
                      return name.toLowerCase();
                  }
              }
              """,
            """
              import java.util.Locale;

              class Test {
                  String key(String name) {
                      return name.toLowerCase(Locale.ROOT);
                  }
              }
              """
          )
        );
    }

    @Test
    void addsLocaleToToUpperCase() {
        rewriteRun(
          // language=java
          java(
            """
              class Test {
                  String shout(String name) {
                      return name.toUpperCase();
                  }
              }
              """,
            """
              import java.util.Locale;

              class Test {
                  String shout(String name) {
                      return name.toUpperCase(Locale.ROOT);
                  }
              }
              """
          )
        );
    }

    @Test
    void keepsAnExplicitLocale() {
        rewriteRun(
          // language=java
          java(
            """
              import java.util.Locale;

              class Test {
                  String label(String name) {
                      return name.toLowerCase(Locale.GERMAN);
                  }
              }
              """
          )
        );
    }

    @Test
    void keepsCaseConversionsOnOtherTypes() {
        rewriteRun(
          // language=java
          java(
            """
              class Test {
                  char upper(char c) {
                      return Character.toUpperCase(c);
                  }
              }
              """
          )
        );
    }

    @Test
    void keepsCaseConversionsOnOtherTypesInTheSameFile() {
        rewriteRun(
          // language=java
          java(
            """
              class Test {
                  String key(String name) {
                      return name.toLowerCase();
                  }

                  char upper(char c) {
                      return Character.toUpperCase(c);
                  }
              }
              """,
            """
              import java.util.Locale;

              class Test {
                  String key(String name) {
                      return name.toLowerCase(Locale.ROOT);
                  }

                  char upper(char c) {
                      return Character.toUpperCase(c);
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
              class Test {
                  String key(String name) {
                      return name.strip()
                              .toLowerCase();
                  }
              }
              """,
            """
              import java.util.Locale;

              class Test {
                  String key(String name) {
                      return name.strip()
                              .toLowerCase(Locale.ROOT);
                  }
              }
              """
          )
        );
    }
}
