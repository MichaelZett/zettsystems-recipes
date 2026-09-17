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

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

/**
 * Adds an explicit {@code Locale.ROOT} to {@code String.toLowerCase()} and
 * {@code String.toUpperCase()}.
 */
public class UseLocaleWithCaseConversion extends Recipe {
    private static final MethodMatcher TO_LOWER_CASE = new MethodMatcher("java.lang.String toLowerCase()");
    private static final MethodMatcher TO_UPPER_CASE = new MethodMatcher("java.lang.String toUpperCase()");
    private static final String LOCALE = "java.util.Locale";

    /**
     * Creates the recipe; it takes no options.
     */
    public UseLocaleWithCaseConversion() {
    }

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Use `Locale.ROOT` with `String.toLowerCase()` and `String.toUpperCase()`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "The no-argument case conversions use the default locale of the JVM they happen to run on. Under a "
                + "Turkish locale `\"I\".toLowerCase()` yields a dotless `ı`, which breaks identifier comparisons, "
                + "lookup keys and protocol tokens. Passing `Locale.ROOT` makes the conversion independent of the "
                + "machine. Use a specific locale instead where the result is shown to a person.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        TreeVisitor<?, ExecutionContext> usesCaseConversion =
                Preconditions.or(new UsesMethod<>(TO_LOWER_CASE), new UsesMethod<>(TO_UPPER_CASE));
        return Preconditions.check(usesCaseConversion, new JavaVisitor<ExecutionContext>() {
            private final JavaTemplate toLowerCase = localeTemplate("toLowerCase");
            private final JavaTemplate toUpperCase = localeTemplate("toUpperCase");

            private JavaTemplate localeTemplate(String methodName) {
                return JavaTemplate.builder("#{any(java.lang.String)}." + methodName + "(Locale.ROOT)")
                        .imports(LOCALE)
                        .build();
            }

            @Override
            public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                Expression select = method.getSelect();
                if (select == null) {
                    return super.visitMethodInvocation(method, ctx);
                }
                JavaTemplate template = templateFor(method);
                if (template == null) {
                    return super.visitMethodInvocation(method, ctx);
                }
                maybeAddImport(LOCALE);
                J replacement = template.apply(getCursor(), method.getCoordinates().replace(), select);
                return Selects.restoreFormatting(replacement, method);
            }

            private @Nullable JavaTemplate templateFor(J.MethodInvocation method) {
                if (TO_LOWER_CASE.matches(method)) {
                    return toLowerCase;
                }
                if (TO_UPPER_CASE.matches(method)) {
                    return toUpperCase;
                }
                return null;
            }
        });
    }
}
