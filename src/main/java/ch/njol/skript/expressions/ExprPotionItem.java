/*
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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

/**
 * Simple interface for creating vanilla potions (if supported by server).
 * @author bensku
 */
public class ExprPotionItem extends SimpleExpression<ItemStack> {
	
	public static final String potionTypePattern;
	public static final String potionModPattern;
	
	static {
		potionTypePattern = "(1¦(uncraftable|empty)|2¦mundane|3¦thick|4¦awkward|5¦night vision|6¦invisibility|7¦leaping|8¦fire resistance|9¦(swiftness|speed)"
				+ "|10¦slowness|11¦water breathing|12¦(instant health|heal[ing])|13¦(instant damage|harm[ing])|14¦poison|15¦regeneration|16¦strength|17¦weakness|18¦luck)";
		potionModPattern = "(";
		if (Skript.classExists("org.bukkit.potion.PotionData")) {
			Skript.registerExpression(ExprPotionItem.class, ItemStack.class, ExpressionType.PROPERTY,
					potionTypePattern);
		}
	}
	
	private PotionType type = PotionType.WATER;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		return false;
	}
	
	@Override
	@Nullable
	protected ItemStack[] get(final Event e) {
		return null;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "";
	}
}
