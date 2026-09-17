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
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.util.List;

/**
 * Turns a discarded {@code asInstanceOf(throwable(X.class))} into {@code isInstanceOf(X.class)}.
 */
public class FixAssertJThrowableInstanceOf extends Recipe {
    private static final MethodMatcher AS_INSTANCE_OF = new MethodMatcher(
            "org.assertj.core.api.Assert asInstanceOf(org.assertj.core.api.InstanceOfAssertFactory)", true);
    private static final MethodMatcher THROWABLE_FACTORY = new MethodMatcher(
            "org.assertj.core.api.InstanceOfAssertFactories throwable(java.lang.Class)");

    /**
     * Creates the recipe; it takes no options.
     */
    public FixAssertJThrowableInstanceOf() {
    }

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Replace a discarded `asInstanceOf(throwable(X.class))` with `isInstanceOf(X.class)`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "`asInstanceOf(...)` does not assert anything — it narrows the assertion type and returns a new "
                + "assertion object for the caller to continue on. Used as a statement, as OpenRewrite's "
                + "`JUnitToAssertj` recipe sometimes produces it, the assertion silently never runs. "
                + "`isInstanceOf(X.class)` is the check that was meant. Only statements whose result is discarded "
                + "are rewritten; a chained `asInstanceOf(...)` is left alone.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesMethod<>(AS_INSTANCE_OF), new JavaVisitor<ExecutionContext>() {

            @Override
            public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                Expression select = method.getSelect();
                if (select == null || !AS_INSTANCE_OF.matches(method) || !isDiscardedStatement()) {
                    return super.visitMethodInvocation(method, ctx);
                }
                if (!(method.getArguments().get(0) instanceof J.MethodInvocation factory)
                        || !THROWABLE_FACTORY.matches(factory)) {
                    return super.visitMethodInvocation(method, ctx);
                }
                Expression exceptionType = factory.getArguments().get(0);
                // Swapping name and argument on the existing invocation keeps the author's
                // line breaks and needs no JavaTemplate — a template would have to parse an
                // AssertJ snippet, and AssertJ is not on this recipe's runtime classpath.
                // The name identifier has to carry the very same JavaType.Method instance as
                // the invocation, otherwise OpenRewrite rejects the tree as mistyped.
                maybeRemoveImport("org.assertj.core.api.InstanceOfAssertFactories.throwable");
                JavaType.Method isInstanceOf = isInstanceOfType(method.getMethodType(), exceptionType);
                return method
                        .withName(method.getName().withSimpleName("isInstanceOf").withType(isInstanceOf))
                        .withArguments(List.of(exceptionType))
                        .withMethodType(isInstanceOf);
            }

            // Both methods take one argument and return an assertion, so renaming and
            // retyping the single parameter is all the signature needs.
            private JavaType.@Nullable Method isInstanceOfType(JavaType.@Nullable Method asInstanceOf,
                                                               Expression exceptionType) {
                if (asInstanceOf == null) {
                    return null;
                }
                JavaType.Method renamed = asInstanceOf.withName("isInstanceOf")
                        .withParameterNames(List.of("type"));
                JavaType argumentType = exceptionType.getType();
                return argumentType == null ? renamed : renamed.withParameterTypes(List.of(argumentType));
            }

            // The result is dropped exactly when the invocation is a statement of a block.
            private boolean isDiscardedStatement() {
                return getCursor().getParentTreeCursor().getValue() instanceof J.Block;
            }
        });
    }
}
