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

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;

/**
 * 1.13+ block compat.
 */
public class NewBlockCompat implements BlockCompat {

	private static class NewBlockValues extends BlockValues {

		private Material type;
		private BlockData data;
		
		public NewBlockValues(Material type, BlockData data) {
			this.type = type;
			this.data = data;
		}
		
		@Override
		public void setBlock(Block block, boolean applyPhysics) {
			block.setType(type);
			block.setBlockData(data, applyPhysics);
		}

		@Override
		public boolean equals(@Nullable Object other) {
			if (!(other instanceof NewBlockValues))
				return false;
			NewBlockValues n = (NewBlockValues) other;
			return data.equals(n.data) && type.equals(n.type);
		}

		@SuppressWarnings("null")
		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + (data == null ? 0 : data.hashCode());
			result = prime * result + type.hashCode();
			return result;
		}
		
	}
	
	@SuppressWarnings("null")
	@Nullable
	@Override
	public BlockValues getBlockValues(BlockState block) {
		// If block doesn't have useful data, data field of type is MaterialData
		if (BlockData.class.isAssignableFrom(block.getType().data))
			return new NewBlockValues(block.getType(), block.getBlockData());
		return null;
	}

	@Override
	public BlockState fallingBlockToState(FallingBlock entity) {
		BlockState state = entity.getWorld().getBlockAt(0, 0, 0).getState();
		state.setBlockData(entity.getBlockData());
		return state;
	}

	@Override
	@Nullable
	public BlockValues createBlockValues(Material type, Map<String, String> states) {
		if (states.isEmpty())
			return null; // Block values not needed
		
		StringBuilder combined = new StringBuilder("[");
		boolean first = true;
		for (Map.Entry<String, String> entry : states.entrySet()) {
			if (first)
				first = false;
			else
				combined.append(',');
			combined.append(entry.getKey()).append('=').append(entry.getValue());
		}
		combined.append(']');
		
		try {
			BlockData data =  Bukkit.createBlockData(type, combined.toString());
			assert data != null;
			return new NewBlockValues(type, data);
		} catch (IllegalArgumentException e) {
			Skript.error("Parsing block state " + combined + " failed!");
			e.printStackTrace();
			return null;
		}	
	}

	@Override
	public boolean isEmpty(Material type) {
		return type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR;
	}

	@Override
	public boolean isLiquid(Material type) {
		return type == Material.WATER || type == Material.LAVA;
	}

	@Override
	@Nullable
	public BlockValues getBlockValues(ItemStack stack) {
		Material type = stack.getType();
		if (BlockData.class.isAssignableFrom(type.data)) { // Block has data
			// Create default block data for the type
			return new NewBlockValues(type, Bukkit.createBlockData(type));
		}
		return null;
	}
	
}
