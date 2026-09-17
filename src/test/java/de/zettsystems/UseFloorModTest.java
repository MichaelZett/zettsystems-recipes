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

class UseFloorModTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseFloorMod());
    }

    @Test
    void replacesAbsModuloOnInt() {
        rewriteRun(
          // language=java
          java(
            """
              class Test {
                  int bucket(String key, int buckets) {
                      return Math.abs(key.hashCode()) % buckets;
                  }
              }
              """,
            """
              class Test {
                  int bucket(String key, int buckets) {
                      return Math.floorMod(key.hashCode(), buckets);
                  }
              }
              """
          )
        );
    }

    @Test
    void replacesAbsModuloOnLong() {
        rewriteRun(
          // language=java
          java(
            """
              class Test {
                  long bucket(long id, long buckets) {
                      return Math.abs(id) % buckets;
                  }
              }
              """,
            """
              class Test {
                  long bucket(long id, long buckets) {
                      return Math.floorMod(id, buckets);
                  }
              }
              """
          )
        );
    }

    @Test
    void keepsAbsWithoutModulo() {
        rewriteRun(
          // language=java
          java(
            """
              class Test {
                  int distance(int a, int b) {
                      return Math.abs(a - b);
                  }
              }
              """
          )
        );
    }

    @Test
    void keepsModuloWithoutAbs() {
        rewriteRun(
          // language=java
          java(
            """
              class Test {
                  int bucket(int hash, int buckets) {
                      return hash % buckets;
                  }
              }
              """
          )
        );
    }

    @Test
    void keepsFloatingPointAbs() {
        rewriteRun(
          // language=java
          java(
            """
              class Test {
                  double wrap(double value, double period) {
                      return Math.abs(value) % period;
                  }
              }
              """
          )
        );
    }

    @Test
    void replacesAbsModuloOnShort() {
        rewriteRun(
          // language=java
          java(
            """
              class Test {
                  int bucket(short id, int buckets) {
                      return Math.abs(id) % buckets;
                  }
              }
              """,
            """
              class Test {
                  int bucket(short id, int buckets) {
                      return Math.floorMod(id, buckets);
                  }
              }
              """
          )
        );
    }

    @Test
    void keepsAFloatingPointDivisor() {
        rewriteRun(
          // language=java
          java(
            """
              class Test {
                  double wrap(int value, double period) {
                      return Math.abs(value) % period;
                  }
              }
              """
          )
        );
    }

    @Test
    void keepsAbsUnderOtherOperators() {
        rewriteRun(
          // language=java
          java(
            """
              class Test {
                  int half(int value, int divisor) {
                      return Math.abs(value) / divisor;
                  }
              }
              """
          )
        );
    }

    @Test
    void keepsAbsOnTheRightHandSide() {
        rewriteRun(
          // language=java
          java(
            """
              class Test {
                  int odd(int buckets, int hash) {
                      return buckets % Math.abs(hash);
                  }
              }
              """
          )
        );
    }
}
