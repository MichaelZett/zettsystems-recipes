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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

import java.util.List;

/**
 * Replaces {@code stream.collect(Collectors.toUnmodifiableList())} with {@code stream.toList()}
 * and, on request, {@code stream.collect(Collectors.toList())} as well.
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public class UseToList extends Recipe {
    private static final MethodMatcher STREAM_COLLECT =
            new MethodMatcher("java.util.stream.Stream collect(java.util.stream.Collector)");
    private static final MethodMatcher COLLECTORS_UNMODIFIABLE_LIST =
            new MethodMatcher("java.util.stream.Collectors toUnmodifiableList()");
    private static final MethodMatcher COLLECTORS_LIST =
            new MethodMatcher("java.util.stream.Collectors toList()");
    private static final String COLLECTORS = "java.util.stream.Collectors";

    @Option(displayName = "Also change `collect(Collectors.toList())`",
            description = "When `true`, `collect(Collectors.toList())` is replaced as well. Note that this changes "
                    + "the returned list from modifiable to unmodifiable. Defaults to `false`.",
            example = "true",
            required = false)
    boolean alsoChangeCollectorsToList;

    /**
     * Leaves {@code collect(Collectors.toList())} untouched.
     */
    public UseToList() {
        this(false);
    }

    /**
     * Creates the recipe with an explicit option value.
     *
     * @param alsoChangeCollectorsToList {@code true} to also replace {@code collect(Collectors.toList())};
     *                                   {@code null} is treated as {@code false}, because OpenRewrite passes
     *                                   {@code null} for an optional option that was not configured.
     */
    public UseToList(Boolean alsoChangeCollectorsToList) {
        this.alsoChangeCollectorsToList = Boolean.TRUE.equals(alsoChangeCollectorsToList);
    }

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Use `toList()` instead of `collect(Collectors.toUnmodifiableList())`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Replaces `collect(Collectors.toUnmodifiableList())` with the more concise `Stream.toList()` "
                + "introduced in Java 16. Both return an unmodifiable list, so the replacement is behaviour "
                + "preserving. Set `alsoChangeCollectorsToList` to `true` to convert the older "
                + "`collect(Collectors.toList())` as well.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesMethod<>(STREAM_COLLECT), new JavaVisitor<ExecutionContext>() {
            // Replacing the whole invocation (instead of just the method name) lets the
            // template re-attribute the type; `replaceMethod()` would keep the type of
            // `collect(Collector)` and leave the LST with an argument count mismatch.
            private final JavaTemplate toList =
                    JavaTemplate.builder("#{any(java.util.stream.Stream)}.toList()").build();

            @Override
            public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                if (isReplaceableCollect(method)) {
                    maybeRemoveImport(COLLECTORS);
                    J replacement = toList.apply(getCursor(), method.getCoordinates().replace(), method.getSelect());
                    return restoreSelectFormatting(replacement, method);
                }
                return super.visitMethodInvocation(method, ctx);
            }

            /**
             * The template reformats the receiver, which collapses a chained call onto a
             * single line. Putting the original padded select back keeps the line break.
             */
            private J restoreSelectFormatting(J replacement, J.MethodInvocation original) {
                if (replacement instanceof J.MethodInvocation replaced) {
                    return replaced.getPadding().withSelect(original.getPadding().getSelect());
                }
                return replacement;
            }

            private boolean isReplaceableCollect(J.MethodInvocation method) {
                if (!STREAM_COLLECT.matches(method) || method.getSelect() == null) {
                    return false;
                }
                List<Expression> arguments = method.getArguments();
                return arguments.size() == 1
                        && arguments.get(0) instanceof J.MethodInvocation collector
                        && isReplaceableCollector(collector);
            }

            private boolean isReplaceableCollector(J.MethodInvocation collector) {
                return COLLECTORS_UNMODIFIABLE_LIST.matches(collector)
                        || (alsoChangeCollectorsToList && COLLECTORS_LIST.matches(collector));
            }
        });
    }
}
