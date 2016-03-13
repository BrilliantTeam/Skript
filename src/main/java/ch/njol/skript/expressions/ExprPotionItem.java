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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.PotionEffectUtils;
import ch.njol.util.Kleenean;

/**
 * Simple interface for creating vanilla potions (if supported by server).
 * @author bensku
 */
public class ExprPotionItem extends SimpleExpression<ItemType> {
	
	public static final String POTION_MODS = "[(0¦(regular|normal)|1¦(strong|upgraded|level 2)|2¦(extended|long)) ]";
	
	static {
		if (Skript.classExists("org.bukkit.potion.PotionData")) {
			Skript.registerExpression(ExprPotionItem.class, ItemType.class, ExpressionType.SIMPLE,
					POTION_MODS + "potion of %potioneffecttype%");
		}
	}
	
	@Nullable
	private Expression<PotionEffectType> type;
	private int mod = 0; // 1=upgraded, 2=extended
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		type = (Expression<PotionEffectType>) exprs[0];
		mod = parseResult.mark;
		if (exprs.length == 0) return false;
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	@Nullable
	protected ItemType[] get(final Event e) {
		PotionData potion = new PotionData(PotionEffectUtils.effectToType(type.getSingle(e)), mod == 2, mod == 1);
		ItemStack item = new ItemStack(Material.POTION);
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		meta.setBasePotionData(potion);
		item.setItemMeta(meta);
		
		return new ItemType[] {new ItemType(item)};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "";
	}
}
