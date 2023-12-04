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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
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

@Value
@EqualsAndHashCode(callSuper = true)
public class UseToList extends Recipe {
    private static final MethodMatcher STREAM_COLLECT = new MethodMatcher("java.util.stream.Stream collect(java.util.stream.Collector)");
    private static final MethodMatcher COLLECTORS_UNMOD_LIST = new MethodMatcher("java.util.stream.Collectors toUnmodifiableList()");
    private static final MethodMatcher COLLECTORS_LIST = new MethodMatcher("java.util.stream.Collectors toList()");
    private static final String STREAM_STREAM = "java.util.stream.Stream";

    @Option(displayName = "Also change `collect(Collectors.toList())`.",
            description = "When set to `true` `collect(Collectors.toList())` gets changed as well,"
                    + "changing implementation of List from modifiable to unmodifiable.",
            required = false)
    Boolean alsoChangeCollectorsToList;


    @Override
    public String getDisplayName() {
        //language=markdown
        return "Use `toList()` instead of `collect(Collectors.toUnmodifiableList())`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Prefer the more modern API like this.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        final TreeVisitor<?, ExecutionContext> check = new UsesMethod<>(STREAM_COLLECT);
        return Preconditions.check(check,
                new JavaVisitor<ExecutionContext>() {
                    private final JavaTemplate toList = JavaTemplate
                            .builder("toList()")
                            .imports(STREAM_STREAM)
                            .build();

                    @Override
                    public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
                        if (STREAM_COLLECT.matches(method)) {
                            List<Expression> arguments = method.getArguments();
                            if (arguments.size() == 1) {
                                Expression arg = arguments.get(0);
                                if (arg instanceof J.MethodInvocation) {
                                    J.MethodInvocation methodArg = (J.MethodInvocation) arg;
                                    boolean collectorsToList = Boolean.TRUE.equals(UseToList.this.alsoChangeCollectorsToList);
                                    if (COLLECTORS_UNMOD_LIST.matches(methodArg) || (collectorsToList && COLLECTORS_LIST.matches(methodArg))) {
                                        maybeRemoveImport("java.util.stream.Collectors");
                                        return toList.apply(getCursor(), method.getCoordinates().replaceMethod());
                                    }
                                }
                            }
                        }
                        return super.visitMethodInvocation(method, executionContext);
                    }
                }
        );
    }
}
