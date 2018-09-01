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
		private byte data;

		public MagicBlockValues(BlockState block) {
			this.id = block.getType();
			this.data = block.getRawData(); // Some black magic here, please look away...
		}
		
		public MagicBlockValues(ItemStack stack) {
			this.id = stack.getType();
			this.data = stack.getData().getData(); // And terrible hack again
		}
		
		public MagicBlockValues(Material id, byte data) {
			this.id = id;
			this.data = data;
		}

		@Override
		public void setBlock(Block block, boolean applyPhysics) {
			block.setType(id);
			try {
				setDataMethod.invokeExact(block, data);
			} catch (Throwable e) {
				Skript.exception(e);
			}
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

	@Override
	public BlockValues getBlockValues(ItemStack stack) {
		return new MagicBlockValues(stack);
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

	private Map<String,String> parseState(String state) {
		Map<String,String> parsed = new HashMap<>();
		
		int comma;
		int pos = 0;
		while (pos != -1) { // Loop until we don't have more key=value pairs
			comma = state.indexOf(',', pos); // Find where next key starts
			
			// Get key=value as string
			String pair;
			if (comma == -1) {
				pair = state.substring(pos);
				pos = -1;
			} else {
				pair = state.substring(pos, comma);
				pos = comma + 1;
			}
			
			// Split pair to parts, add them to map
			String[] parts = pair.split("=");
			parsed.put(parts[0], parts[1]);
		}
		
		return parsed;
	}
	
	@Nullable
	@Override
	public BlockValues createBlockValues(Material type, Map<String, String> states) {
//		Map<String, String> states = parseState(state);
		int data = 0;
		
//		for (Map.Entry<String, String> entry : states.entrySet()) {
//			String value = entry.getValue();
//			switch (entry.getKey()) {
//				case "damage": // Anvil
//					int damage = Integer.parseInt(value);
//					if (damage == 1) {
//						data |= 4;
//					} else if (damage == 2) {
//						data |= 8;
//					}
//					break;
//				case "facing": // 4 to 8 possible rotations
//					int facing = 0;
//					
//					switch (type) {
//						case ANVIL: // 4 directions
//						case BED_BLOCK:
//							switch (value) {
//								case "south":
//									// No changes
//									break;
//								case "west":
//									facing = 1;
//									break;
//								case "north":
//									facing = 2;
//									break;
//								case "east":
//									facing = 3;
//									break;
//							}
//							
//							data |= facing;
//							break;
//						case WOOD_BUTTON: // 6 directions
//						case STONE_BUTTON:
//							switch (value) {
//								case "bottom":
//									// No changes
//									break;
//								case "east":
//									facing = 1;
//									break;
//								case "west":
//									facing = 2;
//									break;
//								case "south":
//									facing = 3;
//									break;
//								case "north":
//									facing = 4;
//									break;
//								case "up":
//									facing = 5;
//									break;
//							}
//							
//							data |= facing;
//							break;
//						//$CASES-OMITTED$
//						default:
//							break;
//					}
//					break;
//				case "rotation": // 16 possible rotations
//					int rotation = Integer.parseInt(value);
//					data |= rotation; // No way going to write lookup table for THAT
//					break;
//				case "occupied":
//					if (value.equals("true")) {
//						data |= 4;
//					}
//					break;
//				case "part": // Beds, doors, some plants
//					switch (type) {
//						case BED: // Bed foot or head
//							if (value.equals("head")) {
//								data |= 8;
//							}
//							break;
//						//$CASES-OMITTED$
//						default:
//							break;
//					}
//					break;
//				case "age": // Age of plants or frosted ice
//					int age = Integer.parseInt(value);
//					data |= age;
//					break;
//				case "axis": // 3 possible rotations
//					// TODO investigate/reverse-engineer
//					break;
//				case "bites": // Cake bites eaten
//					int bites = Integer.parseInt(value);
//					data |= bites;
//					break;
//				case "level": // Lava/water level, includes cauldron
//					int level = Integer.parseInt(value);
//					data |= level;
//					break;
//				case "north": // Block rotations in some cases
//					// TODO investigate/reverse engineer
//					break;
//				case "south":
//					break;
//				case "east":
//					break;
//				case "west":
//					break;
//				case "up":
//					break;
//				case "down":
//					break;
//				case "conditional": // Command block mode
//					// TODO
//					break;
//				case "snowy": // Snow on dirt
//					break;
//				case "in_wall": // Fence gate is lowered a bit
//					break;
//				case "open": // Fence gates, doors
//					break;
//				case "powered": // Block gets redstone power
//					break;
//				case "check_decay": // Leaf decay check
//					break;
//				case "decayable": // Leaf decay possibility
//					break;
//			}
//		}
		// FIXME re-enable or figure out something better
		
		return new MagicBlockValues(type, (byte) data);
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
	
}
