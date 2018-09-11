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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;

/**
 * Block compatibility implemented with magic numbers. No other choice until
 * Spigot 1.13.
 */
public class MagicBlockCompat implements BlockCompat {
	
	private static final MethodHandle setRawDataMethod;
	private static final MethodHandle getBlockDataMethod;
	static final MethodHandle setDataMethod;
	
	static {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		try {
			MethodHandle mh = lookup.findVirtual(BlockState.class, "setRawData",
					MethodType.methodType(void.class, byte.class));
			assert mh != null;
			setRawDataMethod = mh;
			mh = lookup.findVirtual(FallingBlock.class, "getBlockData",
					MethodType.methodType(byte.class));
			assert mh != null;
			getBlockDataMethod = mh;
			mh = lookup.findVirtual(Block.class, "setData",
					MethodType.methodType(void.class, byte.class));
			assert mh != null;
			setDataMethod = mh;
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new Error(e);
		}
	}
	
	@SuppressWarnings({"deprecation", "null"})
	private class MagicBlockValues extends BlockValues {

		private Material id;
		short data;

		public MagicBlockValues(BlockState block) {
			this.id = block.getType();
			this.data = block.getRawData(); // Some black magic here, please look away...
		}
		
		public MagicBlockValues(Material id, short data) {
			this.id = id;
			this.data = data;
		}

		@Override
		public boolean equals(@Nullable Object other) {
			if (!(other instanceof MagicBlockValues))
				return false;
			MagicBlockValues magic = (MagicBlockValues) other;
			return id == magic.id && data == magic.data;
		}

		@Override
		public int hashCode() {
			// FindBugs reports "Scariest" bug when done with just ordinal << 8 | data
			// byte -> int widening seems to be a bit weird in Java
			return (id.ordinal() << 8) | (data & 0xff);
		}
	}
	
	private static class MagicBlockSetter implements BlockSetter {

		public MagicBlockSetter() {}

		@Override
		public void setBlock(Block block, Material type, @Nullable BlockValues values, int flags) {
			block.setType(type);
			
			if (values != null) {
				MagicBlockValues ourValues = (MagicBlockValues) values;
				try {
					setDataMethod.invokeExact(block, (byte) ourValues.data);
				} catch (Throwable e) {
					Skript.exception(e);
				}
			}
		}
		
		
	}

	@Override
	public BlockValues getBlockValues(BlockState block) {
		return new MagicBlockValues(block);
	}

	@SuppressWarnings("deprecation")
	@Override
	public BlockState fallingBlockToState(FallingBlock entity) {
		BlockState state = entity.getWorld().getBlockAt(0, 0, 0).getState();
		state.setType(entity.getMaterial());
		try {
			setRawDataMethod.invokeExact(state, getBlockDataMethod.invokeExact(entity));
		} catch (Throwable e) {
			Skript.exception(e);
		}
		return state;
	}
	
	@Nullable
	@Override
	public BlockValues createBlockValues(Material type, Map<String, String> states) {
		return new MagicBlockValues(type, (byte) 0); // TODO maybe support block states?
	}

	@Override
	public boolean isEmpty(Material type) {
		return type == Material.AIR;
	}

	@Override
	public boolean isLiquid(Material type) {
		// TODO moving water and lava
		return type == Material.WATER || type == Material.LAVA;
	}

	@SuppressWarnings("deprecation")
	@Override
	@Nullable
	public BlockValues getBlockValues(ItemStack stack) {
		short data = stack.getDurability();
		if (data != 0)
			return new MagicBlockValues(stack.getType(), data);
		return null;
	}

	@Override
	public BlockSetter getSetter() {
		return new MagicBlockSetter();
	}
	
}
