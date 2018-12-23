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

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.UnsafeValues;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Maps;
import com.google.gson.Gson;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.BukkitUnsafe;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.bukkitutil.block.BlockCompat;
import ch.njol.skript.bukkitutil.block.BlockValues;
import ch.njol.skript.localization.Message;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.SingleItemIterator;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;

@SuppressWarnings("deprecation")
public class ItemData implements Cloneable, YggdrasilExtendedSerializable {
	
	static {
		Variables.yggdrasil.registerSingleClass(ItemData.class, "NewItemData");
		Variables.yggdrasil.registerSingleClass(OldItemData.class, "ItemData");
	}
	
	/**
	 * Represents old ItemData (before aliases rework and MC 1.13).
	 */
	public static class OldItemData {
		
		int typeid = -1;
		public short dataMin = -1;
		public short dataMax = -1;
	}

	@SuppressWarnings("null")
	static final ItemFactory itemFactory = Bukkit.getServer().getItemFactory();
	
	static final MaterialRegistry materialRegistry;
	
	// Load or create material registry
	static {
		Path materialsFile = Paths.get(Skript.getInstance().getDataFolder().getAbsolutePath(), "materials.json");
		if (Files.exists(materialsFile)) {
			String content = null;
			try {
				content = new String(Files.readAllBytes(materialsFile), StandardCharsets.UTF_8);
			} catch (IOException e) {
				Skript.exception(e, "Loading material registry failed!");
			}
			if (content != null)
				materialRegistry = new MaterialRegistry(new Gson().fromJson(content, Material[].class));
			else
				materialRegistry = new MaterialRegistry();
		} else {
			materialRegistry = new MaterialRegistry();
			String content = new Gson().toJson(materialRegistry.getMaterials());
			try {
				Files.write(materialsFile, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
			} catch (IOException e) {
				Skript.exception(e, "Saving material registry failed!");
			}
		}
	}
	
	private final static Message m_named = new Message("aliases.named");
	
	/**
	 * ItemStack, which is used for everything but serialization.
	 */
	transient ItemStack stack;
	
	/**
	 * Type of the item as Bukkit material. Serialized manually.
	 */
	transient Material type;
	
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
	
	/**
	 * Whether this represents an item (that definitely cannot have
	 * block states) or a block, which might have them.
	 */
	boolean itemForm;
	
	boolean strictEquality;
	
	boolean isAlias;
	
	public ItemData(Material type, @Nullable String tags) {
		this.type = type;
		
		this.stack = new ItemStack(type);
		this.blockValues = BlockCompat.INSTANCE.getBlockValues(stack);
		if (tags != null)
			BukkitUnsafe.modifyItemStack(stack, tags);
		assert stack != null; // Yeah nope; modifyItemStack is not THAT Unsafe
		
		// Grab item meta (may be null)
		assert stack != null;
	}
	
	public ItemData(Material type, int amount) {
		this.type = type;
		
		this.stack = new ItemStack(type, Math.abs(amount));
		this.blockValues = BlockCompat.INSTANCE.getBlockValues(stack);
	}
	
	public ItemData(Material type) {
		this(type, 1);
	}
	
	@SuppressWarnings("null") // clone() always returns stuff
	public ItemData(ItemData data) {
		this.stack = data.stack.clone();
		this.type = data.type;
		this.blockValues = data.blockValues;
		this.isAlias = data.isAlias;
	}
	
	public ItemData(ItemStack stack, @Nullable BlockValues values) {
		this.stack = stack;
		this.type = stack.getType();
		this.blockValues = values;
	}
	
	public ItemData(ItemStack stack) {
		this(stack, null);
		this.itemForm = true;
	}
	
	public ItemData(BlockState block) {
		this.type = block.getType();
		this.stack = new ItemStack(type);
		this.blockValues = BlockCompat.INSTANCE.getBlockValues(block);
	}
	
	public ItemData(Block block) {
		this(block.getState());
	}
	
	/**
	 * Only to be used for serialization.
	 */
	@SuppressWarnings("null") // Yeah, only for internal use
	public ItemData() {}
	
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
	
	/**
	 * Returns <code>Aliases.{@link Aliases#getMaterialName(ItemData, boolean) getMaterialName}(ItemData, boolean)</code>
	 * called with this object and relevant plurarily setting.
	 */
	@Override
	public String toString() {
		return toString(false, false);
	}
	
	public String toString(final boolean debug, final boolean plural) {
		StringBuilder builder = new StringBuilder(Aliases.getMaterialName(this, plural));
		ItemMeta meta = stack.getItemMeta();
		if (meta != null && meta.hasDisplayName()) {
			builder.append(" ").append(m_named).append(" ");
			builder.append(meta.getDisplayName());
		}
		return builder.toString();
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
		
		ItemData other = (ItemData) obj;
		if (isAnything || other.isAnything) // First, isAnything check
			return true;
		
		BlockValues values = blockValues;
		if (type == other.type) {
			if (itemForm && other.blockValues != null)
				return other.blockValues.isDefault();
			if (other.itemForm && blockValues != null)
				return blockValues.isDefault();
		}
		
		if (strictEquality) {
			// The two blocks are not exactly same (even though normally they might be same enough to match)
			if (!Objects.equals(values, other.blockValues))
				return false;
			// The two blocks are same, but aliases differ when it comes to item equality
			if (values != null && other.blockValues != null
					&& other.blockValues.isDefault() != values.isDefault())
				return false;
		}
		if (values != null && !itemForm && !other.itemForm)
			return values.equals(other.blockValues);
		
		if (!type.equals(other.type))
			return false; // Types are not equal
		
		// Check the item meta, unless either ItemData is alias
		if (!isAlias && !other.isAlias) {
			return Objects.equals(getItemMeta(), other.getItemMeta());
		} else { // Even for aliases, do a few checks
			// REMIND follow bug reports closely to see which checks are needed
		}

		return true; // All equality checks passed
	}
	
	@Override
	public int hashCode() {
		int hash = type.hashCode(); // Has collisions, but probably not too many of them
		if (blockValues == null || (blockValues != null && blockValues.isDefault()))
			hash = hash * 37 + 1;
		return hash;
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
		
		// TODO implement meta intersection
		return this;
	}
	
	/**
	 * Returns the ItemStack backing this ItemData.
	 * It is not a copy, so please be careful.
	 * @return Item stack.
	 */
	public ItemStack getStack() {
		return stack;
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
	
	public ItemMeta getItemMeta() {
		ItemMeta meta = stack.getItemMeta();
		if (meta == null) { // AIR has null item meta!
			meta = itemFactory.getItemMeta(Material.STONE);
		}
		assert meta != null;
		return meta;
	}
	
	public void setItemMeta(ItemMeta meta) {
		stack.setItemMeta(meta);
		isAlias = false; // This is no longer exact alias
	}
	
	public int getDurability() {
		return ItemUtils.getDamage(stack);
	}
	
	public void setDurability(int durability) {
		ItemUtils.setDamage(stack, durability);
		isAlias = false; // Change happened
	}

	@Override
	public Fields serialize() throws NotSerializableException {
		Fields fields = new Fields(this); // ItemStack is transient, will be ignored
		fields.putPrimitive("id", materialRegistry.getId(type));
		fields.putObject("meta", stack.getItemMeta());
		return fields;
	}

	@Override
	public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
		this.type = materialRegistry.getMaterial(fields.getAndRemovePrimitive("id", int.class));
		ItemMeta meta = fields.getAndRemoveObject("meta", ItemMeta.class);
		fields.setFields(this); // Everything but ItemStack and Material
		
		// Initialize ItemStack
		this.stack = new ItemStack(type);
		stack.setItemMeta(meta); // Just set meta to it
	}
	
	/**
	 * Creates a plain copy of this ItemData. It will have same material,
	 * amount of 1 and same block values. Tags will also be copied, with
	 * following exceptions:
	 * <ul>
	 * <li>Damage: 1.13 tag-damage is only used for actual durability
	 * <li>Name: custom names made with anvil do not change item type
	 * </ul>
	 * @return A modified copy of this item data.
	 */
	public ItemData aliasCopy() {
		ItemData data = new ItemData();
		data.stack = new ItemStack(type, 1);
		
		if (stack.hasItemMeta()) {
			ItemMeta meta = stack.getItemMeta(); // Creates a copy
			meta.setDisplayName(null); // Clear display name
			data.stack.setItemMeta(meta);
		}
		ItemUtils.setDamage(data.stack, 0); // Set to undamaged
		
		data.type = type;
		data.blockValues = blockValues;
		data.itemForm = itemForm;
		return data;
	}

	/**
	 * Applies an item meta to this item. Currently, it copies the following,
	 * provided that they exist in given meta:
	 * <ul>
	 * <li>Lore
	 * <li>Display name
	 * <li>Enchantments
	 * <li>Item flags
	 * </ul>
	 * @param meta Item meta.
	 */
	public void applyMeta(ItemMeta meta) {
		ItemMeta our = stack.getItemMeta();
		if (meta.hasLore())
			our.setLore(meta.getLore());
		if (meta.hasDisplayName())
			our.setDisplayName(meta.getDisplayName());
		if (meta.hasEnchants()) {
			for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
				our.addEnchant(entry.getKey(), entry.getValue(), true);
			}
		}
		for (ItemFlag flag : meta.getItemFlags()) {
			our.addItemFlags(flag);
		}
		setItemMeta(meta);
	}
	
}
