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
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class FixAssertJThrowableInstanceOfTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new FixAssertJThrowableInstanceOf())
          .parser(JavaParser.fromJavaVersion().classpath("assertj-core"));
    }

    @Test
    void replacesTheDiscardedNarrowingWithAnAssertion() {
        rewriteRun(
          // language=java
          java(
            """
              import static org.assertj.core.api.Assertions.assertThatThrownBy;
              import static org.assertj.core.api.InstanceOfAssertFactories.throwable;

              class Test {
                  void fails() {
                      assertThatThrownBy(() -> { throw new IllegalStateException(); })
                              .asInstanceOf(throwable(IllegalStateException.class));
                  }
              }
              """,
            """
              import static org.assertj.core.api.Assertions.assertThatThrownBy;

              class Test {
                  void fails() {
                      assertThatThrownBy(() -> { throw new IllegalStateException(); })
                              .isInstanceOf(IllegalStateException.class);
                  }
              }
              """
          )
        );
    }

    @Test
    void keepsAChainedNarrowing() {
        rewriteRun(
          // language=java
          java(
            """
              import static org.assertj.core.api.Assertions.assertThatThrownBy;
              import static org.assertj.core.api.InstanceOfAssertFactories.throwable;

              class Test {
                  void fails() {
                      assertThatThrownBy(() -> { throw new IllegalStateException("boom"); })
                              .asInstanceOf(throwable(IllegalStateException.class))
                              .hasMessage("boom");
                  }
              }
              """
          )
        );
    }

    @Test
    void keepsANarrowingToANonThrowableType() {
        rewriteRun(
          // language=java
          java(
            """
              import static org.assertj.core.api.Assertions.assertThat;
              import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

              class Test {
                  void narrows(Object value) {
                      assertThat(value).asInstanceOf(STRING);
                  }
              }
              """
          )
        );
    }

    @Test
    void keepsAnExistingIsInstanceOf() {
        rewriteRun(
          // language=java
          java(
            """
              import static org.assertj.core.api.Assertions.assertThatThrownBy;

              class Test {
                  void fails() {
                      assertThatThrownBy(() -> { throw new IllegalStateException(); })
                              .isInstanceOf(IllegalStateException.class);
                  }
              }
              """
          )
        );
    }
}
