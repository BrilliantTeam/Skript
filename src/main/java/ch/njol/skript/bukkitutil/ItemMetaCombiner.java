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
 * Copyright 2011-2016 Peter Güttinger and contributors
 * 
 */

package ch.njol.skript.bukkitutil;

import java.util.List;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Combines two ItemMetas.
 */
public class ItemMetaCombiner {
	
	public void combine(ItemMeta modified, ItemMeta modifier) {
		if (modifier.hasDisplayName())
			modified.setDisplayName(modifier.getDisplayName());
		if (modifier.hasLocalizedName())
			modified.setLocalizedName(modifier.getLocalizedName());
		if (modifier.hasLore()) {
			if (modified.hasLore()) {
				List<String> lore = modified.getLore();
				lore.addAll(modifier.getLore());
				modified.setLore(lore);
			} else {
				modified.setLore(modifier.getLore());
			}
				
		}
		if (modifier.hasEnchants()) {
			for (Map.Entry<Enchantment,Integer> entry : modifier.getEnchants().entrySet()) {
				modified.addEnchant(entry.getKey(), entry.getValue(), true);
			}
		}
		// TODO this method, rest of stuff
	}
}
