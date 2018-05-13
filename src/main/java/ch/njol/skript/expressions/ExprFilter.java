/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Name("Filter")
@Description("Filters a list based on a condition. " +
		"For example, if you ran 'broadcast \"something\" and \"something else\" where [string input is \"something\"]" +
		"only \"something\" would be broadcast as it is the only string that matched the condition.")
@Examples("send \"congrats on being staff!\" to all players where [player input has permission \"staff\"]")
@Since("INSERT VERSION")
@SuppressWarnings({"null", "unchecked"})
public class ExprFilter<T> extends SimpleExpression<T> {

	private static ExprFilter<?> parsing;

	static {
		Skript.registerExpression(ExprFilter.class, Object.class, ExpressionType.COMBINED,
				"%objects% (where|that match) \\[<.+>\\]");
	}

	private ExprFilter<?> source;
	private Object current;
	private List<ExprInput<?>> children = new ArrayList<>();
	private Class<T> superType;
	private Condition condition;
	private String rawCond;
	private Expression<Object> objects;

	public ExprFilter() {
		this(null, (Class<? extends T>) Object.class);
	}

	public ExprFilter(ExprFilter<?> source, Class<? extends T>... types) {
		this.source = source;
		if (source != null) {
			this.condition = source.condition;
			this.rawCond = source.rawCond;
			this.objects = source.objects;
			for (ExprInput<?> child : source.children) {
				child.setParent(this);
			}
		}
		this.superType = (Class<T>) Utils.getSuperType(types);
	}

	public static ExprFilter<?> getParsing() {
		return parsing;
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		parsing = this;
		objects = LiteralUtils.defendExpression(exprs[0]);
		if (objects instanceof ExprFilter) {
			Skript.error("Filters may not be nested");
			return false;
		}
		rawCond = parseResult.regexes.get(0).group();
		condition = Condition.parse(rawCond, "Can't understand this condition: " + rawCond);
		parsing = null;
		return condition != null && LiteralUtils.canInitSafely(objects);
	}

	@Override
	protected T[] get(Event e) {
		List<Object> filtered = new ArrayList<>();
		for (Object object : objects.getArray(e)) {
			current = object;
			if (condition.check(e)) {
				filtered.add(object);
			}
		}
		current = null;
		try {
			return CollectionUtils.convertArray(filtered.toArray(), superType);
		} catch (ClassCastException e1) {
			return null;
		}
	}

	public Object getCurrent() {
		return current;
	}

	public void addChild(ExprInput<?> child) {
		children.add(child);
	}

	@Override
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		return new ExprFilter<>(this, to);
	}

	@Override
	public Expression<?> getSource() {
		return source == null ? this : source;
	}

	@Override
	public Class<? extends T> getReturnType() {
		return superType;
	}

	@Override
	public boolean isSingle() {
		return objects.isSingle();
	}

	@Override
	public String toString(Event e, boolean debug) {
		return String.format("%s where [%s]", objects.toString(e, debug), rawCond);
	}

	@Name("Filter Input")
	@Description("Represents the input in a filter expression. " +
			"For example, if you ran 'broadcast \"something\" and \"something else\" where [string input is \"something\"]" +
			"the condition would be checked twice, using \"something\" and \"something else\" as the inputs.")
	@Examples("send \"congrats on being staff!\" to all players where [player input has permission \"staff\"]")
	@Since("INSERT VERSION")
	@SuppressWarnings({"null", "unchecked"})
	public static class ExprInput<T> extends SimpleExpression<T> {

		static {
			Skript.registerExpression(ExprInput.class, Object.class, ExpressionType.COMBINED,
					"[%-classinfo%] input");
		}

		private ExprInput<?> source;
		private Class<T> superType;
		private ExprFilter<?> parent;
		private Literal<ClassInfo<?>> inputType;

		public ExprInput() {
			this(null, (Class<? extends T>) Object.class);
		}

		public ExprInput(ExprInput<?> source, Class<? extends T>... types) {
			this.source = source;
			if (source != null) {
				this.parent = source.parent;
				this.inputType = source.inputType;
				parent.addChild(this);
			}
			this.superType = (Class<T>) Utils.getSuperType(types);
		}

		@Override
		public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
			if (ExprFilter.getParsing() == null || !(exprs[0] == null || exprs[0] instanceof Literal)) {
				return false;
			}
			parent = ExprFilter.getParsing();
			parent.addChild(this);
			inputType = (Literal<ClassInfo<?>>) exprs[0];
			return true;
		}

		@Override
		protected T[] get(Event e) {
			Object current = parent.getCurrent();
			if (inputType != null && !inputType.getSingle().getC().isInstance(current)) {
				return null;
			}
			try {
				return CollectionUtils.convertArray(new Object[]{current}, superType);
			} catch (ClassCastException e1) {
				return (T[]) Array.newInstance(superType, 0);
			}
		}

		public void setParent(ExprFilter<?> parent) {
			this.parent = parent;
		}

		@Override
		public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
			return new ExprInput<>(this, to);
		}

		@Override
		public Expression<?> getSource() {
			return source == null ? this : source;
		}

		@Override
		public Class<? extends T> getReturnType() {
			return superType;
		}

		@Override
		public boolean isSingle() {
			return true;
		}

		@Override
		public String toString(Event e, boolean debug) {
			return inputType == null ? "input" : String.format("%s input", inputType.toString(e, debug));
		}

	}

}