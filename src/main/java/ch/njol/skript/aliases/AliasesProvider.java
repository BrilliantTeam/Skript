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
 * Copyright 2011-2017 Peter Güttinger and contributors
 * 
 */

package ch.njol.skript.aliases;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.config.Config;

/**
 * Handles aliases backend.
 */
public interface AliasesProvider {
	
	/**
	 * Asks this provider to load aliases from given config file.
	 * @param cfg
	 */
	void load(Config cfg);
	
	/**
	 * Gets an ItemType for alias.
	 * @param name Alias.
	 * @return ItemType, or null if none found.
	 */
	@Nullable
	ItemType getAlias(String alias);
	
	/**
	 * Gets an ItemType for internal Minecraft id.
	 * @param id
	 * @return ItemType, or null if none found.
	 */
	@Nullable
	ItemType getForMinecraftId(String id);
}
