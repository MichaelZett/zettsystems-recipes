/*
 * Copyright 2021 the original author or authors.
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

class UseToListTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseToList(true))
          .parser(JavaParser.fromJavaVersion()
            .logCompilationWarningsAndErrors(true));
    }

    @Test
    void replaceToUnmodifiableList() {
        rewriteRun(
          spec -> spec
            .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(false)),
          // language=java
          java("""
              import java.util.stream.Collectors;
              import java.util.stream.Stream;
              import java.util.List;
                                  
              class Test {
                 public static void main(String[] args) {
                     List<String> list = Stream.of("test").collect(Collectors.toUnmodifiableList());
                 }
              }
                                  
              """,
            """
              import java.util.stream.Stream;
              import java.util.List;
                                  
              class Test {
                 public static void main(String[] args) {
                     List<String> list = Stream.of("test").toList();
                 }
              }
                                  
              """
          )
        );
    }

    @Test
    void replaceToList() {
        rewriteRun(
          spec -> spec
            .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(false)),
          // language=java
          java("""
              import java.util.stream.Collectors;
              import java.util.stream.Stream;
              import java.util.List;
                                  
              class Test {
                 public static void main(String[] args) {
                     List<String> list = Stream.of("test").collect(Collectors.toList());
                 }
              }
                                  
              """,
            """
              import java.util.stream.Stream;
              import java.util.List;
                                  
              class Test {
                 public static void main(String[] args) {
                     List<String> list = Stream.of("test").toList();
                 }
              }
                                  
              """
          )
        );
    }

    @Test
    void doNotReplaceToList() {
        rewriteRun(
          spec -> {
              final UseToList recipe = new UseToList(false);

              spec.recipe(recipe)
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(false));
          },
          // language=java
          java("""
            import java.util.stream.Collectors;
            import java.util.stream.Stream;
            import java.util.List;
                                
            class Test {
               public static void main(String[] args) {
                   List<String> list = Stream.of("test").collect(Collectors.toList());
               }
            }
                                
            """)
        );
    }
}
