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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.UnsafeValues;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;

/**
 * Contains helpers for Bukkit's not so safe stuff.
 */
@SuppressWarnings("deprecation")
public class BukkitUnsafe {
	
	/**
	 * Bukkit's UnsafeValues allows us to do stuff that would otherwise
	 * require NMS. It has existed for a long time, too, so 1.9 support is
	 * not particularly hard to achieve.
	 */
	private static final UnsafeValues unsafe;
	static {
		UnsafeValues values = Bukkit.getUnsafe();
		if (values == null) {
			throw new Error("unsafe values are not available");
		}
		unsafe = values;
	}
	
	private static final boolean newMaterials = Skript.isRunningMinecraft(1, 13); // REMIND test when 1.13 hits!
	
	@Nullable
	public static Material getMaterialFromMinecraftId(String id) {
		if (newMaterials) {
			// On 1.13 (according to preview API), Vanilla and Spigot names are same
			return Material.getMaterial(id);
		} else {
			return unsafe.getMaterialFromInternalName(id);
		}
	}
	
	public static void modifyItemStack(ItemStack stack, String arguments) {
		unsafe.modifyItemStack(stack, arguments);
	}
}
