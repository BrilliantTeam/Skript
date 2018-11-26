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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.BukkitUnsafe;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.bukkitutil.block.BlockCompat;
import ch.njol.skript.bukkitutil.block.BlockValues;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.Noun;
import ch.njol.util.NonNullPair;

/**
 * Provides aliases on Bukkit/Spigot platform.
 */
public class AliasesProvider {
	
	/**
	 * All aliases that are currently loaded by this provider.
	 */
	private final Map<String, ItemType> aliases;
	
	/**
	 * Material names for aliases this provider has.
	 */
	private final Map<ItemData, MaterialName> materialNames;
	
	/**
	 * Tags are in JSON format. We may need GSON when merging tags
	 * (which might be done if variations are used).
	 */
	private final Gson gson;
	
	/**
	 * Represents a variation of material. It could, for example, define one
	 * more tag or change base id, but keep tag intact.
	 */
	public static class Variation {
		
		@Nullable
		private final String id;
		private final int insertPoint;
		
		private final Map<String, Object> tags;
		private final Map<String, String> states;
		
		public Variation(@Nullable String id, int insertPoint, Map<String, Object> tags, Map<String, String> states) {
			this.id = id;
			this.insertPoint = insertPoint;
			this.tags = tags;
			this.states = states;
		}
		
		@Nullable
		public String getId() {
			return id;
		}
		
		public int getInsertPoint() {
			return insertPoint;
		}
		
		@Nullable
		public String insertId(@Nullable String inserted) {
			if (id == null) // Inserting to nothing
				return inserted;
			if (inserted == null)
				return id;
			inserted = inserted.substring(0, inserted.length() - 1); // Strip out -
			
			String id = this.id;
			assert id != null;
			if (insertPoint == -1) // No place where to insert
				return inserted;
			
			// Insert given string to in middle of our id
			String before = id.substring(0, insertPoint);
			String after = id.substring(insertPoint + 1);
			return before + inserted + after;
		}
		
		public Map<String, Object> getTags() {
			return tags;
		}


		public Map<String,String> getBlockStates() {
			return states;
		}


		public Variation merge(Variation other) {
			// Merge tags and block states
			Map<String, Object> mergedTags = new HashMap<>(other.tags);
			mergedTags.putAll(tags);
			Map<String, String> mergedStates = new HashMap<>(other.states);
			mergedStates.putAll(states);
			
			// Potentially merge ids
			String id = insertId(other.id);
			
			return new Variation(id, -1, mergedTags, mergedStates);
		}
	}
	
	public static class VariationGroup {
		
		public final List<String> keys;
		
		public final List<Variation> values;
		
		public VariationGroup() {
			this.keys = new ArrayList<>();
			this.values = new ArrayList<>();
		}
		
		public void put(String key, Variation value) {
			keys.add(key);
			values.add(value);
		}
	}
	
	/**
	 * Contains all variations. {@link #loadVariedAlias} uses this.
	 */
	private final Map<String, VariationGroup> variations;
	
	/**
	 * Maps item datas back to Minecraft ids.
	 */
	private final Map<ItemData, String> minecraftIds;
	
	/**
	 * Entities related to the items. Most items won't have any.
	 */
	private final Map<ItemData, EntityData<?>> relatedEntities;
	
	/**
	 * Constructs a new aliases provider with no data.
	 */
	public AliasesProvider(int expectedCount) {
		aliases = new HashMap<>(expectedCount);
		materialNames = new HashMap<>(expectedCount);
		variations = new HashMap<>(expectedCount / 20);
		minecraftIds = new HashMap<>(3000);
		relatedEntities = new HashMap<>(10);
		
		gson = new Gson();
	}
	
	/**
	 * Uses GSON to parse Mojang's JSON format to a map.
	 * @param raw Raw JSON.
	 * @return String,Object map.
	 */
	@SuppressWarnings({"null", "unchecked"})
	public Map<String, Object> parseMojangson(String raw) {
		return (Map<String, Object>) gson.fromJson(raw, Object.class);
	}
	
	/**
	 * Applies given tags to an item stack.
	 * @param stack Item stack.
	 * @param tags Tags.
	 */
	public ItemStack applyTags(ItemStack stack, Map<String, Object> tags) {
		// Hack damage tag into item
		Object damage = tags.get("Damage");
		if (damage instanceof Number) { // Use helper for version compatibility
			ItemUtils.setDamage(stack, ((Number) damage).shortValue());
			tags.remove("Damage");
		}
		
		if (tags.isEmpty()) // No real tags to apply
			return stack;
		
		// Apply random tags using JSON
		String json = gson.toJson(tags);
		assert json != null;
		BukkitUnsafe.modifyItemStack(stack, json);
		
		return stack;
	}
	
	/**
	 * Gets singular and plural forms for given name. This might work
	 * slightly differently from {@link Noun#getPlural(String)}, to ensure
	 * it meets specification of aliases.
	 * @param name Name to get forms from.
	 * @return Singular form, plural form.
	 */
	public NonNullPair<String, String> getAliasPlural(String name) {
		int marker = name.indexOf('¦');
		if (marker == -1) { // No singular/plural forms
			String trimmed = name.trim();
			assert trimmed != null;
			return new NonNullPair<>(trimmed, trimmed);
		}
		int pluralEnd = -1;
		for (int i = marker; i < name.length(); i++) {
			int c = name.codePointAt(i);
			if (Character.isWhitespace(c)) {
				pluralEnd = i;
				break;
			}
			
			i += Character.charCount(c);
		}
		
		// No whitespace after marker, so creating forms is simple
		if (pluralEnd == -1) {
			String singular = name.substring(0, marker);
			String plural = singular + name.substring(marker + 1);
			
			singular = singular.trim();
			plural = plural.trim();
			assert singular != null;
			assert plural != null;
			return new NonNullPair<>(singular, plural);
		}
		
		// Need to stitch both singular and plural together
		String base = name.substring(0, marker);
		String singular = base + name.substring(pluralEnd);
		String plural = base + name.substring(marker + 1);
		
		singular = singular.trim();
		plural = plural.trim();
		assert singular != null;
		assert plural != null;
		return new NonNullPair<>(singular, plural);
	}
	
	/**
	 * Adds an alias to this provider.
	 * @param name Name of alias without any patterns or variation blocks.
	 * @param id Id of material.
	 * @param tags Tags for material.
	 * @param blockStates Block states.
	 */
	public void addAlias(String name, String id, @Nullable Map<String, Object> tags, Map<String, String> blockStates) {
		// First, try to find if aliases already has a type with this id
		// (so that aliases can refer to each other)
		ItemType typeOfId = aliases.get(id);
		EntityData<?> related = null;
		List<ItemData> datas;
		if (typeOfId != null) { // If it exists, use datas from it
			datas = typeOfId.getTypes();
		} else { // ... but quite often, we just got Vanilla id
			// Prepare and modify ItemStack (using somewhat Unsafe methods)
			Material material = BukkitUnsafe.getMaterialFromMinecraftId(id);
			if (material == null) { // If server doesn't recognize id, do not proceed
				throw new InvalidMinecraftIdException(id);
			}
			
			// Hacky: get related entity from block states
			String entityName = blockStates.remove("relatedEntity");
			if (entityName != null) {
				related = EntityData.parse(entityName);
			}
			
			// Parse block state to block values
			BlockValues blockValues = BlockCompat.INSTANCE.createBlockValues(material, blockStates);
			
			// Apply (NBT) tags to item stack
			ItemStack stack = new ItemStack(material);
			if (tags != null) {
				stack = applyTags(stack, new HashMap<>(tags));
			}
			
			ItemData data = new ItemData(stack, blockValues);
			data.isAlias = true;
			datas = Collections.singletonList(data);
		}
		
		// Create plural form of the alias (warning: I don't understand it either)
		NonNullPair<String, Integer> plain = Noun.stripGender(name, name); // Name without gender and its gender token
		NonNullPair<String, String> forms = getAliasPlural(plain.getFirst()); // Singular and plural forms
		
		// Check if there is item type with this name already, create otherwise
		ItemType type = aliases.get(forms.getFirst());
		if (type == null)
			type = aliases.get(forms.getSecond());
		if (type == null) {
			type = new ItemType();
			aliases.put(forms.getFirst(), type); // Singular form
			aliases.put(forms.getSecond(), type); // Plural form
		}
		
		// Add item datas we got earlier to the type
		assert datas != null;
		type.addAll(datas);
		
		// Make datas subtypes of the type we have here and handle Minecraft ids
		for (ItemData data : type.getTypes()) { // Each ItemData in our type is supertype
			data.strictEquality = true;
			if (typeOfId == null) // Only when it is Minecraft id, not an alias reference
				minecraftIds.put(data, id); // Register Minecraft id for the data, too
			
			// Material name, including both singular and plural forms
			materialNames.putIfAbsent(data, new MaterialName(data.type, forms.getFirst(), forms.getSecond(), plain.getSecond()));
			
			// Related entity type
			if (related != null)
				relatedEntities.put(data, related);
			
			data.strictEquality = false;
		}
	}
	
	public void addVariationGroup(String name, VariationGroup group) {
		variations.put(name, group);
	}
	
	@Nullable
	public VariationGroup getVariationGroup(String name) {
		return variations.get(name);
	}

	@Nullable
	public ItemType getAlias(String alias) {
		return aliases.get(alias);
	}

	@Nullable
	public String getMinecraftId(ItemData data) {
		return minecraftIds.get(data);
	}
	
	@Nullable
	public MaterialName getMaterialName(ItemData type) {
		return materialNames.get(type);
	}

	public void setMaterialName(ItemData data, MaterialName materialName) {
		materialNames.put(data, materialName);
	}

	public void clearAliases() {
		aliases.clear();
		materialNames.clear();
		variations.clear();
	}

	public int getAliasCount() {
		return aliases.size();
	}
	
	@Nullable
	public EntityData<?> getRelatedEntity(ItemData type) {
		return relatedEntities.get(type);
	}

}
