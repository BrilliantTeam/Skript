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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

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
	
	/**
	 * Before 1.13, Vanilla material names were translated using
	 * this + a lookup table.
	 */
	private static final MethodHandle unsafeFromInternalNameMethod;
	
	static {
		UnsafeValues values = Bukkit.getUnsafe();
		if (values == null) {
			throw new Error("unsafe values are not available");
		}
		unsafe = values;
		
		try {
			MethodHandle mh = MethodHandles.lookup().findVirtual(UnsafeValues.class,
					"getMaterialFromInternalName", MethodType.methodType(String.class, Material.class));
			assert mh != null;
			unsafeFromInternalNameMethod = mh;
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new Error(e);
		}
	}
	
	private static final boolean newMaterials = Skript.isRunningMinecraft(1, 13);
	
	@Nullable
	public static Material getMaterialFromMinecraftId(String id) {
		if (newMaterials) {
			// On 1.13 (according to preview API), Vanilla and Spigot names are same
			return Material.getMaterial(id);
		} else {
			Material type;
			try {
				type = (Material) unsafeFromInternalNameMethod.invokeExact(unsafe, id);
			} catch (Throwable e) {
				throw new RuntimeException(e); // Hmm
			}
			if (type == null || type == Material.AIR) { // If there is no item form, UnsafeValues won't work
				type = checkForBuggedType(id);
			}
			return type;
		}
	}
	
	@Nullable
	private static Material checkForBuggedType(String id) {
		// Lookup tables, again?
		switch (id) {
			case "minecraft:powered_repeater":
				return Material.DIODE_BLOCK_OFF;
			case "minecraft:unpowered_repeater":
				return Material.DIODE_BLOCK_ON;
			case "minecraft:piston_head":
				return Material.PISTON_EXTENSION;
			case "minecraft:piston_extension":
				return Material.PISTON_MOVING_PIECE;
			case "minecraft:lit_redstone_lamp":
				return Material.REDSTONE_LAMP_ON;
			case "minecraft:daylight_detector":
				return Material.DAYLIGHT_DETECTOR;
			case "minecraft:daylight_detector_inverted":
				return Material.DAYLIGHT_DETECTOR_INVERTED;
			case "minecraft:redstone_wire":
				return Material.REDSTONE_WIRE;
			case "minecraft:unlit_redstone_torch":
				return Material.REDSTONE_TORCH_OFF;
		}
		return null;
	}
	
	public static void modifyItemStack(ItemStack stack, String arguments) {
		unsafe.modifyItemStack(stack, arguments);
	}
}
