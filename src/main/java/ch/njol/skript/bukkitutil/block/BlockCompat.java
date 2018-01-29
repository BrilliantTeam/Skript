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
package ch.njol.skript.bukkitutil.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;

/**
 * Methods which operate with blocks but are not compatible across some
 * Minecraft versions.
 */
public interface BlockCompat {
	
	/**
	 * Instance of BlockCompat for current Minecraft version.
	 */
	static final BlockCompat INSTANCE = new MagicBlockCompat();
	
	/**
	 * Gets block values from a block state. They can be compared to other
	 * values if needed, but cannot be used to retrieve any other data.
	 * @param block Block state to retrieve value from.
	 * @return Block values.
	 */
	BlockValues getBlockValues(BlockState block);
	
	/**
	 * Gets block values from a block. They can be compared to other values
	 * if needed, but cannot be used to retrieve any other data.
	 * @param block Block to retrieve value from.
	 * @return Block values.
	 */
	@SuppressWarnings("null")
	default BlockValues getBlockValues(Block block) {
		return getBlockValues(block.getState());
	}

	/**
	 * Gets block values from a item stack. They can be compared to other values
	 * if needed, but cannot be used to retrieve any other data.
	 * @param stack Item that would be placed as the block
	 * @return Block values.
	 */
	BlockValues getBlockValues(ItemStack stack);
	
	/**
	 * Creates a block state from a falling block.
	 * @param entity Falling block entity
	 * @return Block state.
	 */
	BlockState fallingBlockToState(FallingBlock entity);
	
	default BlockValues getBlockValues(FallingBlock entity) {
		return getBlockValues(fallingBlockToState(entity));
	}
}
