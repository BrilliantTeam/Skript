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

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.UnsafeValues;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ch.njol.skript.Skript;
import ch.njol.skript.util.Version;

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
	@Nullable
	private static final MethodHandle unsafeFromInternalNameMethod;
	
	static {
		UnsafeValues values = Bukkit.getUnsafe();
		if (values == null) {
			throw new Error("unsafe values are not available");
		}
		unsafe = values;
		
		MethodHandle mh;
		try {
			mh = MethodHandles.lookup().findVirtual(UnsafeValues.class,
					"getMaterialFromInternalName", MethodType.methodType(String.class, Material.class));
		} catch (NoSuchMethodException | IllegalAccessException e) {
			mh = null;
		}
		unsafeFromInternalNameMethod = mh;
	}
	
	private static final boolean newMaterials = Skript.isRunningMinecraft(1, 13);
	
	/**
	 * Vanilla material names to Bukkit materials.
	 */
	@Nullable
	private static Map<String,Material> materialMap;
	
	/**
	 * If we have material map for this version, using it is preferred.
	 * Otherwise, it can be used as fallback.
	 */
	private static boolean preferMaterialMap;
	
	public static void initialize() {
		if (!newMaterials) {
			try {
				Version version = Skript.getMinecraftVersion();
				boolean mapExists = loadMaterialMap("materials/" + version.getMajor() + "." +  version.getMinor() + ".json");
				if (!mapExists) {
					loadMaterialMap("materials/1.12.json");
					preferMaterialMap = false;
					Skript.warning("Material mappings for " + version + " are not available.");
					Skript.warning("Depending on your server software, some aliases may not work.");
				}
			} catch (IOException e) {
				Skript.exception(e, "Failed to load material mappings. Aliases may not work properly.");
			}
		}
	}
	
	@Nullable
	public static Material getMaterialFromMinecraftId(String id) {
		if (newMaterials) {
			// On 1.13, Vanilla and Spigot names are same
			if (id.length() > 9)
				return Material.matchMaterial(id.substring(10)); // Strip 'minecraft:' out
			else // Malformed material name
				return null;
		} else {
			// If we have correct material map, prefer using it
			if (preferMaterialMap) {
				assert materialMap != null;
				return materialMap.get(id);
			}
			
			// Otherwise, hacks
			Material type;
			try {
				assert unsafeFromInternalNameMethod != null;
				type = (Material) unsafeFromInternalNameMethod.invokeExact(unsafe, id);
			} catch (Throwable e) {
				throw new RuntimeException(e); // Hmm
			}
			if (type == null || type == Material.AIR) { // If there is no item form, UnsafeValues won't work
				// So we're going to rely on 1.12's material mappings
				assert materialMap != null;
				return materialMap.get(id);
			}
			return type;
		}
	}
	
	private static boolean loadMaterialMap(String name) throws IOException {
		try (InputStream is = Skript.getInstance().getResource(name)) {
			if (is == null) { // No mappings for this Minecraft version
				return false;
			}
			String data = new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8);
			
			Type type = new TypeToken<Map<String,Material>>(){}.getType();
			materialMap = new Gson().fromJson(data, type);
		}
		
		return true;
	}
	
	public static void modifyItemStack(ItemStack stack, String arguments) {
		unsafe.modifyItemStack(stack, arguments);
	}
}
