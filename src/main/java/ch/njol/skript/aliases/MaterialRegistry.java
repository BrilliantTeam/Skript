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
package ch.njol.skript.aliases;

import org.bukkit.Material;

/**
 * Manages Skript's own number -> Material mappings. They are used to save
 * items as variables.
 */
public class MaterialRegistry {
	
	/**
	 * Materials by their number ids.
	 */
	private Material[] materials;
	
	/**
	 * Ids by ordinals of materials they represent.
	 */
	private int[] ids;
	
	/**
	 * Creates a material registry from existing data.
	 * @param materials Materials by their number ids.
	 */
	public MaterialRegistry(Material[] materials) {
		this.materials = materials;
		this.ids = new int[materials.length];
		for (int i = 0; i < materials.length; i++) {
			Material m = materials[i];
			if (m != null)
				ids[m.ordinal()] = i;
		}
	}
	
	/**
	 * Creates a new material registry.
	 */
	public MaterialRegistry() {
		this(Material.values());
	}
	
	public Material getMaterial(int id) {
		try {
			Material m = materials[id];
			assert m != null;
			return m;
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException("invalid material id");
		}
	}
	
	public int getId(Material material) {
		try {
			return ids[material.ordinal()];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new AssertionError("material registry out-of-date");
		}
	}
	
	public Material[] getMaterials() {
		return materials;
	}
}
