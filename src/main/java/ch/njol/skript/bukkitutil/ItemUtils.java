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
package ch.njol.skript.bukkitutil;

import java.lang.invoke.MethodHandle;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import ch.njol.skript.Skript;

/**
 * Miscellaneous static utility methods related to items.
 */
public class ItemUtils {
	
	private ItemUtils() {} // Not to be instanced
	
	private static final boolean damageMeta = Skript.classExists("org.bukkit.inventory.meta.Damageable");
	
	@SuppressWarnings("deprecation")
	public static int getDamage(ItemStack stack) {
		if (damageMeta) {
			ItemMeta meta = stack.getItemMeta();
			if (meta instanceof Damageable)
				return ((Damageable) meta).getDamage();
			return 0; // Not damageable item
		} else {
			return stack.getDurability();
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void setDamage(ItemStack stack, int damage) {
		if (damageMeta) {
			ItemMeta meta = stack.getItemMeta();
			if (meta instanceof Damageable) {
				((Damageable) meta).setDamage(damage);
				stack.setItemMeta(meta);
			}
		} else {
			stack.setDurability((short) damage);
		}
	}
}
