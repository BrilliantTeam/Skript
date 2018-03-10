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
package ch.njol.skript.expressions;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Click slot")
@Description("The clicked slot number of an inventory click event.")
@Examples("clicked slot is 1")
@Since("2.2-dev35")
public class ExprClickSlot extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprClickSlot.class, Number.class, ExpressionType.SIMPLE, "[the] clicked [raw] slot");
	}
	
	@SuppressWarnings("null")
	private Boolean raw;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(InventoryClickEvent.class)) {
			Skript.error("The expression 'clicked slot' may only be used in the inventory click events", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		raw = parseResult.expr.contains("raw");
		return true;
	}
	
	@Override
	@Nullable
	protected Number[] get(Event event) {
		return CollectionUtils.array((raw) ? ((InventoryClickEvent)event).getRawSlot() : ((InventoryClickEvent)event).getSlot());
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	public String toString(final @Nullable Event event, final boolean debug) {
		return "the click slot " + ((event != null) ? ": " + ((InventoryClickEvent)event).getSlot() : "");
	}
	
}
