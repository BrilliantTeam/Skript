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

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.UnsafeValues;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Maps;
import com.google.gson.Gson;

import ch.njol.skript.bukkitutil.block.BlockCompat;
import ch.njol.skript.bukkitutil.block.BlockValues;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.SingleItemIterator;
import ch.njol.yggdrasil.YggdrasilSerializable;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public class ItemData implements Cloneable, YggdrasilSerializable {
	
	static {
		Variables.yggdrasil.registerSingleClass(ItemData.class, "ItemData");
	}
	
	@SuppressWarnings("null")
	private static final UnsafeValues unsafe = Bukkit.getUnsafe();
	
	private static final Gson gson = new Gson();
	
	/**
	 * ItemStack, which is used for everything but serialization.
	 * It should be handled by underlying NMS stack that is probably
	 * modified using UnsafeValues.
	 */
	@Nullable
	transient ItemStack stack;
	
	/**
	 * Tags for item in Mojangson (JSON) string representation.
	 */
	@Nullable
	String tags;
	
	/**
	 * Type of the item as Bukkit material.
	 */
	Material type;
	
	/**
	 * If this represents all possible items.
	 */
	boolean isAnything;
	
	/**
	 * When this ItemData represents a block, this contains information to
	 * allow comparing it against other blocks.
	 */
	@Nullable
	transient BlockValues blockValues;
	
	public ItemData(Material type, @Nullable String tags) {
		this.type = type;
		this.tags = tags;
		
		stack = new ItemStack(type);
		unsafe.modifyItemStack(stack, tags);
		assert stack != null; // Yeah nope; modifyItemStack is not THAT Unsafe
		
		// Initialize block values with a terrible hack
		blockValues = BlockCompat.INSTANCE.getBlockValues(stack);
	}
	
	public ItemData(Material type) {
		this.type = type;
		
		stack = new ItemStack(type);
		blockValues = BlockCompat.INSTANCE.getBlockValues(stack);
	}
	
	public ItemData(ItemData data) {
		this(data.type, data.tags);
	}
	
	public ItemData(ItemStack stack) {
		this.stack = stack;
		this.type = stack.getType();
		blockValues = BlockCompat.INSTANCE.getBlockValues(stack);
	}
	
	public ItemData(Block block) {
		this.type = block.getType();
		this.blockValues = BlockCompat.INSTANCE.getBlockValues(block);
	}
	
	public ItemData() {
		this.type = Material.AIR; // Fake type, but we have a good reason to not allow null there
		this.isAnything = true;
	}
	
	/**
	 * Tests whether the given item is of this type.
	 * 
	 * @param item
	 * @return Whether the given item is of this type.
	 */
	public boolean isOfType(@Nullable ItemStack item) {
		if (item == null)
			return type == Material.AIR;
		return item.isSimilar(stack);
	}
	
	public boolean isSupertypeOf(ItemData o) {
		// TODO implement this; how?
		return false;
	}
	
	/**
	 * Returns <code>Aliases.{@link Aliases#getMaterialName(int, short, short, boolean) getMaterialName}(typeid, dataMin, dataMax, false)</code>
	 */
	@Override
	public String toString() {
		return Aliases.getMaterialName(this, false);
	}
	
	public String toString(final boolean debug, final boolean plural) {
		return debug ? Aliases.getDebugMaterialName(this, plural) : Aliases.getMaterialName(this, plural);
	}
	
	/**
	 * @return The item's gender or -1 if no name is found
	 */
	public int getGender() {
		return Aliases.getGender(this);
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ItemData))
			return false;
		
		final ItemData other = (ItemData) obj;
		if (stack != null && other.stack != null) { // Stack check is possible
			return stack.equals(other.stack);
		}
		if (other.type != type)
			return false;
		
		if (blockValues != null) {
			if (other.blockValues != null) {
				return blockValues.equals(other.blockValues);
			} else {
				return true; // Type matched, and we have no more data to compare
			}
		} else if (tags != null) {
			if (other.tags != null) {
				return tags.equals(other.tags);
			} else {
				return true; // Type matched, and we have no more data to compare
			}
		}
		
		return true; // Types are same, no tags or block values
	}
	
	@Override
	public int hashCode() {
		if (stack != null)
			return stack.hashCode();
		else if (blockValues != null)
			return blockValues.hashCode() << 14 | type.hashCode();
		else
			return type.ordinal();
	}
	
	/**
	 * Computes the intersection of two ItemDatas. The data range of the returned item data will be the real intersection of the two data ranges, and the type id will be the one
	 * set if any.
	 * 
	 * @param other
	 * @return A new ItemData which is the intersection of the given types, or null if the intersection of the data ranges is empty or both datas have an id != -1 which are not the
	 *         same.
	 */
	@Nullable
	public ItemData intersection(final ItemData other) {
		if (other.type != type) // Different type, no intersection possible
			return null;
		
		Map<String, Object> myTags = (Map<String, Object>) gson.fromJson(tags, Object.class);
		Map<String, Object> theirTags = (Map<String, Object>) gson.fromJson(other.tags, Object.class);
		Map<String, Object> commonTags = Maps.difference(myTags, theirTags).entriesInCommon();
		if (commonTags.isEmpty()) // No intersection exists
			return null;
		
		ItemData intersection = new ItemData(type, gson.toJson(commonTags));
		return intersection;
	}
	
	public ItemStack getRandom() {
		Material m = type;
		if (isAnything) { // Any material
			m = CollectionUtils.getRandom(Material.values(), 1);
		}
		return new ItemStack(type, 1);
	}
	
	public Iterator<ItemStack> getAll() {
		if (isAnything) {
			return new Iterator<ItemStack>() {
				
				@SuppressWarnings("null")
				private final Iterator<Material> iter = Arrays.asList(Material.values()).listIterator(1); // ignore air
				
				@Override
				public boolean hasNext() {
					return iter.hasNext();
				}
				
				@Override
				public ItemStack next() {
					return new ItemStack(iter.next(), 1);
				}
				
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
				
			};
		}
		
		return new SingleItemIterator<>(getRandom());
	}
	
	@Override
	public ItemData clone() {
		return new ItemData(this);
	}
	
	public Material getType() {
		return type;
	}
	
	@Nullable
	public BlockValues getBlockValues() {
		return blockValues;
	}
	
}
