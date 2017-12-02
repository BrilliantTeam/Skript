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
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Maps;
import com.google.gson.Gson;

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
	
	private static final Gson gson;
	
	/**
	 * ItemStack, which is used for everything but serialization.
	 * It should be handled by underlying NMS stack that is probably
	 * modified using UnsafeValues.
	 */
	transient ItemStack stack;
	
	/**
	 * Tags for item in Mojangson (JSON) string representation.
	 */
	String tags;
	
	/**
	 * Type of the item as Bukkit material.
	 */
	Material type;
	
	public ItemData(Material type, String tags) {
		this.type = type;
		this.tags = tags;
		
		stack = new ItemStack(type);
		unsafe.modifyItemStack(stack, tags);
	}
	
	public ItemData(ItemData data) {
		this(data.type, data.tags);
	}
	
	public ItemData() {
		this(Material.AIR, "");
	}
	
	public int getId() {
		// Won't work with 1.13!
		return type.getId();
	}
	
	/**
	 * Tests whether the given item is of this type.
	 * 
	 * @param item
	 * @return Whether the given item is of this type.
	 */
	public boolean isOfType(final @Nullable ItemStack item) {
		if (item == null)
			return type == Material.AIR;
		return item.isSimilar(stack);
	}
	
	@Deprecated
	public boolean isSupertypeOf(final ItemData other) {
		return (typeid == -1 || other.typeid == typeid) && (dataMin == -1 || dataMin <= other.dataMin) && (dataMax == -1 || dataMax >= other.dataMax);
	}
	
	/**
	 * Returns <code>Aliases.{@link Aliases#getMaterialName(int, short, short, boolean) getMaterialName}(typeid, dataMin, dataMax, false)</code>
	 */
	@Override
	public String toString() {
		// TODO aliases provider or something else?
		return Aliases.getMaterialName(typeid, dataMin, dataMax, false);
	}
	
	public String toString(final boolean debug, final boolean plural) {
		return debug ? Aliases.getDebugMaterialName(typeid, dataMin, dataMax, plural) : Aliases.getMaterialName(typeid, dataMin, dataMax, plural);
	}
	
	/**
	 * @return The item's gender or -1 if no name is found
	 */
	public int getGender() {
		return Aliases.getGender(typeid, dataMin, dataMax);
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ItemData))
			return false;
		final ItemData other = (ItemData) obj;
		return other.type == type && other.tags == tags;
	}
	
	@Override
	public int hashCode() {
		return stack.hashCode();
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
		int type = typeid;
		if (type == -1) {
			final Material m = CollectionUtils.getRandom(Material.values(), 1);
			assert m != null;
			type = m.getId();
		}
		if (dataMin == -1 && dataMax == -1) {
			return new ItemStack(type, 1);
		} else {
			return new ItemStack(type, 1, (short) (Utils.random(dataMin, dataMax + 1)));
		}
	}
	
	public Iterator<ItemStack> getAll() {
		if (typeid == -1) {
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
		if (dataMin == dataMax)
			return new SingleItemIterator<ItemStack>(new ItemStack(typeid, 1, dataMin == -1 ? 0 : dataMin));
		return new Iterator<ItemStack>() {
			
			private short data = dataMin;
			
			@Override
			public boolean hasNext() {
				return data <= dataMax;
			}
			
			@Override
			public ItemStack next() {
				if (!hasNext())
					throw new NoSuchElementException();
				return new ItemStack(typeid, 1, data++);
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		};
	}
	
	@Override
	public ItemData clone() {
		return new ItemData(this);
	}
	
	public boolean hasDataRange() {
		return dataMin != dataMax;
	}
	
	public int numItems() {
		return dataMax - dataMin + 1;
	}
	
}
