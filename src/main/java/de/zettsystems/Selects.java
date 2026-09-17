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

import org.openrewrite.java.tree.J;

/**
 * Helpers shared by the recipes in this library.
 */
final class Selects {

    private Selects() {
    }

    /**
     * Replacing a whole method invocation through a {@code JavaTemplate} is what gives the
     * result correct type information, but it also reformats the receiver — which collapses
     * a chained call onto a single line. Putting the original padded select back keeps the
     * line break the author wrote.
     *
     * @param replacement the invocation produced by the template
     * @param original    the invocation that was replaced
     * @return the replacement with the original receiver formatting
     */
    static J restoreFormatting(J replacement, J.MethodInvocation original) {
        return ((J.MethodInvocation) replacement).getPadding().withSelect(original.getPadding().getSelect());
    }
}
