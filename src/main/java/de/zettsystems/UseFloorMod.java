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
import org.openrewrite.java.tree.JavaType;

/**
 * Replaces {@code Math.abs(x) % n} with {@code Math.floorMod(x, n)}.
 */
public class UseFloorMod extends Recipe {
    private static final MethodMatcher MATH_ABS_INT = new MethodMatcher("java.lang.Math abs(int)");
    private static final MethodMatcher MATH_ABS_LONG = new MethodMatcher("java.lang.Math abs(long)");

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Use `Math.floorMod(x, n)` instead of `Math.abs(x) % n`";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "`Math.abs(x) % n` is the usual way to derive a bucket index from a hash code, and it is wrong: "
                + "`Math.abs(Integer.MIN_VALUE)` overflows back to `Integer.MIN_VALUE`, so the expression can still "
                + "return a negative index. `Math.floorMod(x, n)` is non-negative for every input. Note that the two "
                + "differ for negative `x` in general — `Math.abs(-7) % 3` is `1`, `Math.floorMod(-7, 3)` is `2` — so "
                + "this recipe is a fix, not a pure refactoring. Apply it where the intent is a bucket index.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        TreeVisitor<?, ExecutionContext> usesMathAbs =
                Preconditions.or(new UsesMethod<>(MATH_ABS_INT), new UsesMethod<>(MATH_ABS_LONG));
        return Preconditions.check(usesMathAbs, new JavaVisitor<ExecutionContext>() {
            private final JavaTemplate floorMod = JavaTemplate.builder("Math.floorMod(#{any()}, #{any()})").build();

            @Override
            public J visitBinary(J.Binary binary, ExecutionContext ctx) {
                Expression dividend = integralAbsArgument(binary);
                if (dividend != null && isIntegral(binary.getRight().getType())) {
                    return floorMod.apply(getCursor(), binary.getCoordinates().replace(),
                            dividend, binary.getRight());
                }
                return super.visitBinary(binary, ctx);
            }

            // Returns the argument of the Math.abs(...) on the left-hand side of a modulo,
            // or null if this binary is not Math.abs(integral) % integral.
            private @Nullable Expression integralAbsArgument(J.Binary binary) {
                if (binary.getOperator() != J.Binary.Type.Modulo
                        || !(binary.getLeft() instanceof J.MethodInvocation abs)
                        || !(MATH_ABS_INT.matches(abs) || MATH_ABS_LONG.matches(abs))) {
                    return null;
                }
                Expression argument = abs.getArguments().get(0);
                return isIntegral(argument.getType()) ? argument : null;
            }

            private boolean isIntegral(@Nullable JavaType type) {
                return type == JavaType.Primitive.Int || type == JavaType.Primitive.Long
                        || type == JavaType.Primitive.Short || type == JavaType.Primitive.Byte;
            }
        });
    }
}
