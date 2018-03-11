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

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;

@Name("Inventory Holder/Viewers/Rows")
@Description("Gets the rows/size/viewers/holder of an inventory.")
@Examples({"event-inventory's amount of rows",
		   "holder of player's top inventory",
		   "{_inventory}'s viewers"})
@Since("2.2-dev34")
public class ExprInventoryInfo extends SimplePropertyExpression<Inventory, Object> {

	private final static int HOLDER = 1, VIEWERS = 2, ROWS = 3;
	private int type;
	
	static {
		register(ExprInventoryInfo.class, Object.class, "(" + HOLDER + "¦holder[s]|" + VIEWERS + "¦viewers|" + ROWS + "¦[amount of] rows)", "inventories");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		type = parseResult.mark;
		return init(exprs, matchedPattern, isDelayed, parseResult);
	}
	
	@Override
	public Class<? extends Object> getReturnType() {
		return type == HOLDER ? InventoryHolder.class : type == VIEWERS ? Player.class : Number.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "(" + HOLDER + "¦holder[s]|" + VIEWERS + "¦viewers|" + ROWS + "¦[amount of] rows)";
	}
	
	@SuppressWarnings("null")
	@Override
	public Object convert(final Inventory inventory) {
		switch (type) {
			case HOLDER:
				return new InventoryHolder[] {inventory.getHolder()};
			case VIEWERS:
				List<HumanEntity> players = inventory.getViewers();
				return players.toArray(new Player[players.size()]);
			case ROWS:
				return new Number[] {inventory.getSize() / 9};
		}
		return null;
	}

}
