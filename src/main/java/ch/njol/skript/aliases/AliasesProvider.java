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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.UnsafeValues;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.Noun;
import ch.njol.util.NonNullPair;

/**
 * Provides aliases on Bukkit/Spigot platform.
 */
public class AliasesProvider {
	
	private static final Message m_empty_name = new Message("aliases.empty name");
	private static final ArgsMessage m_invalid_variation_section = new ArgsMessage("aliases.invalid variation section");
	private static final Message m_unexpected_section = new Message("aliases.unexpected section");
	private static final Message m_useless_variation = new Message("aliases.useless variation");
	private static final Message m_brackets_error = new Message("aliases.brackets error");
	private static final ArgsMessage m_unknown_variation = new ArgsMessage("aliases.unknown variation");
	
	/**
	 * All aliases that are currently loaded by this provider.
	 */
	private Map<String, ItemType> aliases;
	
	/**
	 * Material names for aliases this provider has.
	 */
	private Map<ItemData, MaterialName> materialNames;
	
	/**
	 * Bukkit's UnsafeValues allows us to do stuff that would otherwise
	 * require NMS. It has existed for a long time, too, so 1.9 support is
	 * not particularly hard to achieve.
	 */
	@SuppressWarnings("deprecation")
	private UnsafeValues unsafe;
	
	/**
	 * Tags are in JSON format. We may need GSON when merging tags
	 * (which might be done if variations are used).
	 */
	private Gson gson;
	
	/**
	 * Represents a variation of material. It could, for example, define one
	 * more tag or change base id, but keep tag intact.
	 */
	public static class Variation {
		
		@Nullable
		private String id;
		private Map<String, Object> tags;
		
		public Variation(@Nullable String id, Map<String, Object> tags) {
			this.id = id;
			this.tags = tags;
		}
		
		@Nullable
		public String getId() {
			return id;
		}
		
		public Map<String, Object> getTags() {
			return tags;
		}
	}
	
	/**
	 * Contains all variations. {@link #loadVariedAlias} uses this.
	 */
	private Map<String, Map<String, Variation>> variations;
	
	/**
	 * Subtypes of materials.
	 */
	private Map<ItemData, Set<ItemData>> subtypes;
	
	private Map<ItemData, String> minecraftIds;
	
	/**
	 * Constructs a new aliases provider with no data.
	 */
	public AliasesProvider() {
		aliases = new HashMap<>(3000);
		materialNames = new HashMap<>(3000);
		variations = new HashMap<>(500);
		subtypes = new HashMap<>(1000);
		minecraftIds = new HashMap<>(3000);
		gson = new Gson();
		
		@SuppressWarnings("deprecation")
		UnsafeValues values = Bukkit.getUnsafe();
		if (values == null) {
			throw new RuntimeException("unsafe values are not available; cannot initialize this aliases provider");
		}
		unsafe = values;
	}
	
	/**
	 * Loads aliases from a section node.
	 * @param root Root section node for us to load.
	 */
	public void load(SectionNode root) {
		Skript.debug("Loading aliases node: " + root.getKey() + " from " + root.getConfig().getFileName());
		for (Node node : root) {
			// Get key and make sure it exists
			String key = node.getKey();
			if (key == null) {
				Skript.error(m_empty_name.toString());
				continue;
			}
			
			// Section nodes are for variations
			if (node instanceof SectionNode) {
				Map<String, Variation> vars = loadVariations((SectionNode) node);
				if (vars != null) {
					variations.put(node.getKey(), vars);
				} else {
					Skript.error(m_invalid_variation_section.toString(key));
				}
				continue;
			}
			
			// Sanity check
			if (!(node instanceof EntryNode)) {
				Skript.error(m_unexpected_section.toString());
				continue;
			}
			
			// Get value (it always exists)
			String value = ((EntryNode) node).getValue();
			
			loadAlias(key, value);
		}
	}
	
	/**
	 * Uses GSON to parse Mojang's JSON format to a map.
	 * @param raw Raw JSON.
	 * @return String,Object map.
	 */
	@SuppressWarnings({"null", "unchecked"})
	private Map<String, Object> parseMojangson(String raw) {
		return (Map<String, Object>) gson.fromJson(raw, Object.class);
	}
	
	/**
	 * Parses a single variation from a string.
	 * @param raw Raw variation info.
	 * @return Variation instance.
	 */
	@Nullable
	private Variation parseVariation(String raw) {
		int firstSpace = raw.indexOf(' ');
		String id = raw.substring(0, firstSpace);
		if (id.equals("-")) {
			id = null;
		}
		
		String tags = raw.substring(firstSpace + 1);
		if (tags.isEmpty()) {
			if (id == null) { // Useless variation, does not do anything
				Skript.warning(m_useless_variation.toString());
			}
			return new Variation(id, new HashMap<>()); // No tags, but id
		}
		
		return new Variation(id, parseMojangson(tags));
	}
	
	/**
	 * Loads variations from a section node.
	 * @param root Root node for this variation.
	 * @return Map of variations by their names.
	 */
	@Nullable
	private Map<String, Variation> loadVariations(SectionNode root) {
		String name = root.getKey();
		assert name != null; // Better be so
		if (!name.startsWith("{") && !name.endsWith("}")) {
			// This is not a variation section!
			return null;
		}
		
		Map<String, Variation> vars = new HashMap<>();
		for (Node node : root) {
			String pattern = node.getKey();
			assert pattern != null;
			List<String> keys = parseKeyPattern(pattern);
			Variation var = parseVariation(((EntryNode) node).getValue());
			
			// Put var there for all keys it matches with
			for (String key : keys) {
				vars.put(key, var);
			}
		}
		
		return vars;
	}
	
	/**
	 * Parses alias key pattern using some black magic.
	 * @param name Key/name of alias.
	 * @return All strings that match aliase with this pattern.
	 */
	private List<String> parseKeyPattern(String name) {
		List<String> versions = new ArrayList<>();
		
		boolean simple = true; // Simple patterns are used as-is
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			
			if (c == '[') { // Optional part: versions with and without it
				int end = name.indexOf(']', i);
				versions.addAll(parseKeyPattern(Aliases.concatenate(name.substring(0, i), name.substring(i + 1, end), name.substring(end + 1))));
				versions.addAll(parseKeyPattern(Aliases.concatenate(name.substring(0, i), name.substring(end + 1))));
				simple = false; // Not simple, has optional group
			} else if (c == '(') { // Choose one part: versions for multiple options
				int end = name.indexOf(')', i);
				int n = 0;
				int last = i;
				boolean hasParts = false;
				for (int j = i + 1; j < end; j++) {
					char x = name.charAt(j);
					if (x == '(') {
						n++;
					} else if (x == ')') {
						n--;
					} else if (x == '|') {
						if (n > 0)
							continue;
						hasParts = true;
						versions.addAll(parseKeyPattern(Aliases.concatenate(name.substring(0, i), name.substring(last + 1, j), name.substring(end + 1))));
						last = j;
					}
				}
				if (!hasParts) {
					Skript.error(m_brackets_error.toString());
					return versions;
				}
				versions.addAll(parseKeyPattern(Aliases.concatenate(name.substring(0, i), name.substring(last + 1, end), name.substring(end + 1))));
				simple = false; // Not simple, has choice group
			}
		}
		if (simple)
			versions.add(name);
		
		return versions;
	}
	
	/**
	 * Loads an alias with given name (key pattern) and data (material id and tags).
	 * @param name Name of alias.
	 * @param data Data of alias.
	 */
	private void loadAlias(String name, String data) {
		Skript.debug("Loading alias: " + name + " = " + data);
		List<String> patterns = parseKeyPattern(name);
		Skript.debug("Patterns: " + patterns);
		
		for (String item : data.split(",")) { // All aliases may be lists of items or just other aliases
			item = item.trim(); // These could mess up following check among other things
			int firstSpace = item.indexOf(' ');
			
			String id; // Id or alias
			Map<String, Object> tags;
			if (firstSpace == -1) {
				id = item;
				tags = null;
			} else {
				id = item.substring(0, firstSpace);
				tags = parseMojangson(item.substring(firstSpace + 1));
			}
			assert id != null;
			
			for (String p : patterns) {
				assert p != null; // No nulls in this list
				loadVariedAlias(p, id, tags);
			}
		}
	}
	
	/**
	 * Loads alias which may contain variations.
	 * @param name Alias name without patterns, which still might contain
	 * variation blocks.
	 * @param id Base id of material that this alias represents.
	 * @param tags Base tags of material that this alias represents.
	 */
	private void loadVariedAlias(String name, String id, @Nullable Map<String, Object> tags) {
		// Find {variations}
		boolean hasVariations = false;
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			
			// Variation start here
			if (c == '{') {
				hasVariations = true;
				
				int end = name.indexOf('}', i);
				if (end == -1) {
					Skript.error(m_brackets_error.toString());
					continue;
				}
				
				// Take variation name, including the brackets
				String varName = name.substring(i, end + 1);
				// Get variations for that id and hope they exist
				Map<String, Variation> vars = variations.get(varName);
				if (vars == null) {
					Skript.error(m_unknown_variation.toString(varName));
					continue;
				}
				
				// Iterate over variations
				for (Entry<String, Variation> entry : vars.entrySet()) {
					Map<String, Object> combinedTags;
					if (tags != null) {
						combinedTags = new HashMap<>(tags.size() + entry.getValue().getTags().size());
						combinedTags.putAll(tags);
						combinedTags.putAll(entry.getValue().getTags());
					} else {
						combinedTags = entry.getValue().getTags();
					}
					
					String variedId = entry.getValue().getId();
					if (variedId == null) {
						variedId = id;
					}
					
					String aliasName = name.replace(varName, entry.getKey());
					assert aliasName != null;
					assert variedId != null;
					loadVariedAlias(aliasName, variedId, combinedTags);
				}
				
				// Move to end of this variation
				i = end;
			}
		}
		
		// Enough recursion! No more variations, just alias
		if (!hasVariations) {
			loadReadyAlias(name, id, tags);
		}
	}
	
	/**
	 * Loads an alias which does not have variations.
	 * @param name Name of alias without any patterns or variation blocks.
	 * @param id Id of material.
	 * @param tags Tags for material.
	 */
	@SuppressWarnings("deprecation")
	private void loadReadyAlias(String name, String id, @Nullable Map<String, Object> tags) {
		// First, try to find if aliases already has a type with this id
		// (so that aliases can refer to each other)
		ItemType typeOfId = aliases.get(id);
		List<ItemData> datas;
		if (typeOfId != null) { // If it exists, use datas from it
			datas = typeOfId.getTypes();
		} else { // ... but quite often, we just got Vanilla id
			// Prepare and modify ItemStack (using somewhat Unsafe methods)
			ItemStack stack = new ItemStack(unsafe.getMaterialFromInternalName(id));
			if (tags != null) {
				unsafe.modifyItemStack(stack, gson.toJson(tags));
			}
			
			datas = Collections.singletonList(new ItemData(stack));
		}
		
		// Create plural form of the alias (warning: I don't understand it either)
		NonNullPair<String, Integer> plain = Noun.stripGender(name, name); // Name without gender and its gender token
		NonNullPair<String, String> forms = Noun.getPlural(plain.getFirst()); // Singular and plural forms
		
		// Check if there is item type with this name already, create otherwise
		ItemType type = aliases.get(name);
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
			Set<ItemData> subs = subtypes.get(data);
			if (subs == null) {
				subs = new HashSet<>(datas.size());
				subtypes.put(data, subs);
			}
			subs.addAll(datas); // Add all datas (the ones we have here)
			
			minecraftIds.put(data, id); // Register Minecraft id for the data, too
		}

		// TODO fill material name
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
	
	@Nullable
	public Set<ItemData> getSubtypes(ItemData supertype) {
		return subtypes.get(supertype);
	}
}
