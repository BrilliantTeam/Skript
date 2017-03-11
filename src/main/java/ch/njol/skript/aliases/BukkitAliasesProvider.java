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

package ch.njol.skript.aliases;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.UnsafeValues;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;

/**
 * Provides aliases on Bukkit/Spigot platform.
 */
public class BukkitAliasesProvider implements AliasesProvider {
	
	private Map<String,ItemType> aliases;
	@SuppressWarnings("deprecation")
	private UnsafeValues unsafe;
	
	@SuppressWarnings({"deprecation", "null"})
	public BukkitAliasesProvider() {
		aliases = new HashMap<>(3000);
		unsafe = Bukkit.getUnsafe();
	}
	
	@Override
	public void load(Config cfg) {
		SectionNode main = cfg.getMainNode();
		
		for (Node node : main) {
			
		}
	}

	@Override
	@Nullable
	public ItemType getAlias(String alias) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Nullable
	public ItemType getForMinecraftId(String id) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
