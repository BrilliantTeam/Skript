/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.util;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.UnparsedLiteral;

import java.util.Arrays;
import java.util.Objects;

/**
 * A class that contains methods based around
 * making it easier to deal with {@link UnparsedLiteral}
 * objects.
 */
public class LiteralUtils {

    /**
     * Checks an {@link Expression} for {@link UnparsedLiteral} objects
     * and converts them if found.
     *
     * @param expr The expression to check for {@link UnparsedLiteral} objects
     * @param <T>  {@code expr}'s type
     * @return {@code expr} without {@link UnparsedLiteral} objects
     * @author Bryan Terce
     */
    @SuppressWarnings("unchecked")
    public static <T> Expression<T> defendExpression(Expression<?> expr) {
        if (expr instanceof UnparsedLiteral) {
            Literal<?> parsed = ((UnparsedLiteral) expr).getConvertedExpression(Object.class);
            return (Expression<T>) (parsed == null ? expr : parsed);
        } else if (expr instanceof ExpressionList) {
            Expression[] exprs = ((ExpressionList) expr).getExpressions();
            for (int i = 0; i < exprs.length; i++) {
                exprs[i] = defendExpression(exprs[i]);
            }
        }
        return (Expression<T>) expr;
    }

    /**
     * Checks if an Expression contains {@link UnparsedLiteral}
     * objects.
     *
     * @param expr The Expression to check for {@link UnparsedLiteral} objects
     * @return Whether or not {@code expr} contains {@link UnparsedLiteral} objects
     * @author Bryan Terce
     */
    public static boolean hasUnparsedLiteral(Expression<?> expr) {
        return expr instanceof UnparsedLiteral ||
                (expr instanceof ExpressionList &&
                        Arrays.stream(((ExpressionList) expr).getExpressions())
                                .anyMatch(UnparsedLiteral.class::isInstance));
    }

    /**
     * Checks if the passed Expressions are non-null
     * and do not contain {@link UnparsedLiteral} objects.
     *
     * @param expressions The expressions to check for {@link UnparsedLiteral} objects
     * @return Whether or not the passed expressions contain {@link UnparsedLiteral} objects
     * @author Bryan Terce
     */
    public static boolean canInitSafely(Expression<?>... expressions) {
        return Arrays.stream(expressions)
                .filter(Objects::nonNull)
                .noneMatch(LiteralUtils::hasUnparsedLiteral);
    }

}
