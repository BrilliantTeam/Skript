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
import org.bukkit.material.MaterialData;
import org.eclipse.jdt.annotation.Nullable;

import com.bekvon.bukkit.residence.commands.command;
import com.google.gson.Gson;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.BukkitUnsafe;
import ch.njol.skript.bukkitutil.block.BlockCompat;
import ch.njol.skript.bukkitutil.block.BlockValues;
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
	private static final ArgsMessage m_invalid_minecraft_id = new ArgsMessage("aliases.invalid minecraft id");
	private static final Message m_empty_alias = new Message("aliases.empty alias");
	
	/**
	 * All aliases that are currently loaded by this provider.
	 */
	private Map<String, ItemType> aliases;
	
	/**
	 * Material names for aliases this provider has.
	 */
	private Map<ItemData, MaterialName> materialNames;
	
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
			
			String id = this.id;
			assert id != null;
			if (insertPoint == -1) // No place where to insert
				return inserted != null ? inserted : id;
			
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
			
			return new Variation(id, insertPoint != -1 ? insertPoint : other.insertPoint, mergedTags, mergedStates);
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
	private Map<String, VariationGroup> variations;
	
	/**
	 * Subtypes of materials.
	 */
	private Map<ItemData, Set<ItemData>> subtypes;
	
	/**
	 * Maps item datas back to Minecraft ids.
	 */
	private Map<ItemData, String> minecraftIds;
	
	/**
	 * Contains condition functions to determine when aliases should be loaded.
	 */
	private Map<String, Function<String,Boolean>> conditions;
	
	/**
	 * Constructs a new aliases provider with no data.
	 */
	public AliasesProvider() {
		aliases = new HashMap<>(3000);
		materialNames = new HashMap<>(3000);
		variations = new HashMap<>(500);
		subtypes = new HashMap<>(1000);
		minecraftIds = new HashMap<>(3000);
		conditions = new HashMap<>();
		
		gson = new Gson();
	}
	
	/**
	 * Loads aliases from a section node.
	 * @param root Root section node for us to load.
	 */
	public void load(SectionNode root) {
		Skript.debug("Loading aliases node " + root.getKey() + " from " + root.getConfig().getFileName());
		long start = System.currentTimeMillis();
		for (Node node : root) {
			// Get key and make sure it exists
			String key = node.getKey();
			if (key == null) {
				Skript.error(m_empty_name.toString());
				continue;
			}
			
			// Section nodes are for variations
			if (node instanceof SectionNode) {
				VariationGroup vars = loadVariations((SectionNode) node);
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
			
			// Check for conditions
			if (conditions.containsKey(key)) {
				boolean success = conditions.get(key).apply(((EntryNode) node).getValue());
				if (!success) { // Failure causes ignoring rest in this section node
					Skript.debug("Condition " + key + " was NOT met; not loading more");
					return;
				}
				continue; // Do not interpret this as alias
			}
			
			// Get value (it always exists)
			String value = ((EntryNode) node).getValue();
			
			loadAlias(key, value);
		}
		
		long time = System.currentTimeMillis() - start;
		Skript.debug("Finished loading " + root.getKey() + " in " + (time / 1000000) + "ms");
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
	
	private Map<String, String> parseBlockStates(String input) {
		Map<String,String> parsed = new HashMap<>();
		
		int comma;
		int pos = 0;
		while (pos != -1) { // Loop until we don't have more key=value pairs
			comma = input.indexOf(',', pos); // Find where next key starts
			
			// Get key=value as string
			String pair;
			if (comma == -1) {
				pair = input.substring(pos);
				pos = -1;
			} else {
				pair = input.substring(pos, comma);
				pos = comma + 1;
			}
			
			// Split pair to parts, add them to map
			String[] parts = pair.split("=");
			parsed.put(parts[0], parts[1]);
		}
		
		return parsed;
	}
	
	/**
	 * Parses a single variation from a string.
	 * @param item Raw variation info.
	 * @return Variation instance.
	 */
	private Variation parseVariation(String item) {
		String trimmed = item.trim();
		assert trimmed != null;
		item = trimmed; // These could mess up following check among other things
		int firstBracket = item.indexOf('{');
		
		String id; // Id or alias
		Map<String, Object> tags;
		if (firstBracket == -1) {
			id = item;
			tags = new HashMap<>();
		} else {
			if (firstBracket == 0) {
				throw new AssertionError("missing space between id and tags in " + item);
			}
			id = item.substring(0, firstBracket - 1);
			String json = item.substring(firstBracket);
			assert json != null;
			tags = parseMojangson(json);
		}
		
		// Separate block state from id
		String typeName;
		Map<String, String> blockStates;
		int stateIndex = id.indexOf('[');
		if (stateIndex != -1) {
			if (stateIndex == 0) {
				throw new AssertionError("missing id or - in " + id);
			}
			typeName = id.substring(0, stateIndex); // Id comes before block state
			blockStates = parseBlockStates(id.substring(stateIndex + 1, id.length() - 1));
		} else { // No block state, just the id
			typeName = id;
			blockStates = new HashMap<>();
		}
		
		// Variations don't always need an id
		if (typeName.equals("-")) {
			typeName = null;
		}
		
		return new Variation(typeName, typeName == null ? -1 : typeName.indexOf('-'), tags, blockStates);
	}
	
	/**
	 * Loads variations from a section node.
	 * @param root Root node for this variation.
	 * @return Group of variations.
	 */
	@Nullable
	private VariationGroup loadVariations(SectionNode root) {
		String name = root.getKey();
		assert name != null; // Better be so
		if (!name.startsWith("{") || !name.endsWith("}")) {
			// This is not a variation section!
			return null;
		}
		
		VariationGroup vars = new VariationGroup();
		for (Node node : root) {
			String pattern = node.getKey();
			assert pattern != null;
			List<String> keys = parseKeyPattern(pattern);
			Variation var = parseVariation(((EntryNode) node).getValue());
			if (var.getId() == null && var.getTags().isEmpty() && var.getBlockStates().isEmpty()) {
				// Useless variation, basically
				Skript.warning(m_useless_variation.toString());
			}
			
			// Put var there for all keys it matches with
			for (String key : keys) {
				assert key != null;
				if (key.equals("{default}"))
					key = "";
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
		for (int i = 0; i < name.length();) {
			int c = name.codePointAt(i);
			
			if (c == '[') { // Optional part: versions with and without it
				int end = name.indexOf(']', i);
				versions.addAll(parseKeyPattern(name.substring(0, i) + name.substring(i + 1, end) + name.substring(end + 1)));
				versions.addAll(parseKeyPattern(name.substring(0, i) + name.substring(end + 1)));
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
						versions.addAll(parseKeyPattern(name.substring(0, i) + name.substring(last + 1, j) + name.substring(end + 1)));
						last = j;
					}
				}
				if (!hasParts) {
					Skript.error(m_brackets_error.toString());
					return versions;
				}
				versions.addAll(parseKeyPattern(name.substring(0, i) + name.substring(last + 1, end) + name.substring(end + 1)));
				simple = false; // Not simple, has choice group
			}
			
			i += Character.charCount(c);
		}
		if (simple)
			versions.add(name);
		
		return versions;
	}
	
	private static class PatternSlot {
		
		public final String content;
		
		public PatternSlot(String content) {
			this.content = content;
		}
	}
	
	private static class VariationSlot extends PatternSlot {
		
		/**
		 * Variation group.
		 */
		public final VariationGroup vars;
		
		private int counter;
		
		public VariationSlot(VariationGroup vars) {
			super("");
			this.vars = vars;
		}
		
		public String getName() {
			return vars.keys.get(counter);
		}
		
		public Variation getVariation() {
			return vars.values.get(counter);
		}
		
		public boolean increment() {
			counter++;
			if (counter == vars.keys.size()) {
				counter = 0;
				return true;
			}
			return false;
		}
	}
	
	/**
	 * Parses all possible variations from given name.
	 * @param name Name which might contain variations.
	 * @return Map of variations.
	 */
	private Map<String, Variation> parseKeyVariations(String name) {
		/**
		 * Variation name start.
		 */
		int varStart = -1;
		
		/**
		 * Variation name end.
		 */
		int varEnd = 0;
		
		/**
		 * Variation slots in this name.
		 */
		List<PatternSlot> slots = new ArrayList<>();
		
		// Compute variation slots
		for (int i = 0; i < name.length();) {
			int c = name.codePointAt(i);
			if (c == '{') { // Found variation name start
				varStart = i;
				slots.add(new PatternSlot(name.substring(varEnd, i)));
			} else if (c == '}') { // Found variation name end
				if (varStart == -1) { // Or just invalid syntax
					Skript.error(m_brackets_error.toString());
					continue;
				}

				// Extract variation name from full name
				String varName = name.substring(varStart, i + 1);
				assert varName != null;
				// Get variations for that id and hope they exist
				VariationGroup vars = variations.get(varName);
				if (vars == null) {
					Skript.error(m_unknown_variation.toString(varName));
					continue;
				}
				slots.add(new VariationSlot(vars));
				
				// Variation name finished
				varStart = -1;
				varEnd = i + 1;
			}
			
			i += Character.charCount(c);
		}
		
		// Handle last non-variation slot
		slots.add(new PatternSlot(name.substring(varEnd)));
		
		if (varStart != -1) { // A variation was not properly finished
			Skript.error(m_brackets_error.toString());
		}
		
		/**
		 * All possible variations by patterns of them.
		 */
		Map<String, Variation> variations = new HashMap<>();
		
		if (slots.size() == 1) {
			// Fast path: no variations
			PatternSlot slot = slots.get(0);
			if (!(slot instanceof VariationSlot)) {
				variations.put(name, new Variation(null, -1, new HashMap<>(), new HashMap<>()));
				return variations;
			}
			// Otherwise we have only one slot, which is variation. Weird, isn't it?
		}
		
		// Create all permutations caused by variations
		while (true) {
			/**
			 * Count of pattern slots in this key pattern.
			 */
			int count = slots.size();
			
			/**
			 * Slot index of currently manipulated variation.
			 */
			int incremented = 0;
			
			/**
			 * This key pattern.
			 */
			StringBuilder pattern = new StringBuilder();
			
			// Variations replace or add to these after each other
			
			/**
			 * Minecraft id. Can be replaced by subsequent variations.
			 */
			String id = null;
			
			/**
			 * Where to insert id of alias that uses this variation.
			 */
			int insertPoint = -1;
			
			/**
			 * Tags by their names. All variations can add and overwrite them.
			 */
			Map<String, Object> tags = new HashMap<>();
			
			/**
			 * Block states. All variations can add and overwrite them.
			 */
			Map<String, String> states = new HashMap<>();
			
			// Construct alias name and variations
			for (int i = 0; i < count; i++) {
				PatternSlot slot = slots.get(i);
				if (slot instanceof VariationSlot) { // A variation
					VariationSlot varSlot = (VariationSlot) slot;
					pattern.append(varSlot.getName());
					Variation var = varSlot.getVariation();
					String varId = var.getId();
					if (varId != null)
						id = varId;
					if (var.getInsertPoint() != -1)
						insertPoint = var.getInsertPoint();
						
					tags.putAll(var.getTags());
					states.putAll(var.getBlockStates());
					
					if (i == incremented) { // This slot is manipulated now
						if (varSlot.increment())
							incremented++; // And it flipped from max to 0 again
					}
				} else { // Just text
					if (i == incremented) // We can't do that
						incremented++; // But perhaps next slot can
					pattern.append(slot.content);
				}
			}
			
			// Put variation to map which we will return
			variations.put(pattern.toString(), new Variation(id, insertPoint, tags, states));
			
			// Check if we're finished with permutations
			if (incremented == count) {
				break; // Indeed, get out now!
			}
		}
		
		return variations;
	}
		
	/**
	 * Loads an alias with given name (key pattern) and data (material id and tags).
	 * @param name Name of alias.
	 * @param data Data of alias.
	 */
	private void loadAlias(String name, String data) {
		//Skript.debug("Loading alias: " + name + " = " + data);
		List<String> patterns = parseKeyPattern(name);
		
		// Create all variations now (might need them many times in future)
		Map<String, Variation> variations = new HashMap<>();
		for (String pattern : patterns) {
			assert pattern != null;
			variations.putAll(parseKeyVariations(pattern));
		}
		
		// Complex list parsing to avoid commas inside tags
		int start = 0; // Start of next substring
		int indexStart = 0; // Start of next comma lookup
		while (start - 1 != data.length()) {
			int comma = data.indexOf(',', indexStart);
			if (comma == -1) { // No more items than this
				if (indexStart == 0) { // Nothing was loaded, so no commas at all
					String item = data.trim();
					assert item != null;
					loadSingleAlias(variations, item);
					break;
				} else {
					comma = data.length();
				}
			}
			
			int bracketOpen = data.indexOf('{', indexStart);
			int bracketClose = data.indexOf('}', bracketOpen);
			if (comma < bracketClose && comma > bracketOpen) {
				// Inside tags, comma lookup goes to end of tags
				indexStart = bracketClose;
				continue;
			}
			
			// Not inside tags, so process the item
			String item = data.substring(start, comma).trim();
			assert item != null;
			loadSingleAlias(variations, item);
			
			// Set up for next item
			start = comma + 1;
			indexStart = start;
		}
	}
	
	private void loadSingleAlias(Map<String, Variation> variations, String item) {
		Variation base = parseVariation(item); // Share parsing code with variations
		
		for (Map.Entry<String, Variation> entry : variations.entrySet()) {
			String name = entry.getKey();
			assert name != null;
			Variation var = entry.getValue();
			assert var != null;
			Variation merged = base.merge(var);
			
			String id = merged.getId();
			// For null ids, we just spit a warning
			// They should have not gotten this far
			if (id == null) {
				Skript.warning(m_empty_alias.toString());
			} else {
				loadReadyAlias(fixName(name), id, merged.getTags(), merged.getBlockStates());
			}
		}
	}
	
	/**
	 * Fixes an alias name by trimming it and removing all extraneous spaces
	 * between the words.
	 * @param name Name to be fixed.
	 * @return Name fixed.
	 */
	private String fixName(String name) {
		StringBuilder fixed = new StringBuilder();
		
		// Trim whitespace at beginning
		int i = 0;
		for (;i < name.length();) {
			int c = name.codePointAt(i);
			if (!Character.isWhitespace(c))
				break;
			
			i += Character.charCount(c);
		}
		
		// Remove extra whitespace
		boolean whitespace = false;
		for (;i < name.length();) {
			int c = name.codePointAt(i);
			if (Character.isWhitespace(c)) {
				if (whitespace) {
					i += Character.charCount(c);
					continue;
				}
				else // First whitespace
					whitespace = true;
			} else {
				whitespace = false;
			}
			
			fixed.appendCodePoint(c);
			
			i += Character.charCount(c);
		}
		
		return fixed.toString();
	}
	
	/**
	 * Applies given tags to an item stack.
	 * @param stack Item stack.
	 * @param tags Tags.
	 */
	public ItemStack applyTags(ItemStack stack, Map<String, Object> tags) {
		Object damage = tags.get("Damage");
		if (damage instanceof Number) { // Set durability manually, not NBT tag before 1.13
			stack = new ItemStack(stack.getType(), 1, ((Number) damage).shortValue());
			// Bukkit makes this work on 1.13+ too, which is nice
			tags.remove("Damage");
		}
		
		// Apply random tags using JSON
		String json = gson.toJson(tags);
		assert json != null;
		BukkitUnsafe.modifyItemStack(stack, json);
		
		return stack;
	}
	
	/**
	 * Loads an alias which does not have variations.
	 * @param name Name of alias without any patterns or variation blocks.
	 * @param id Id of material.
	 * @param tags Tags for material.
	 * @param blockStates Block states.
	 */
	private void loadReadyAlias(String name, String id, @Nullable Map<String, Object> tags, Map<String, String> blockStates) {
		// First, try to find if aliases already has a type with this id
		// (so that aliases can refer to each other)
		ItemType typeOfId = aliases.get(id);
		List<ItemData> datas;
		if (typeOfId != null) { // If it exists, use datas from it
			datas = typeOfId.getTypes();
		} else { // ... but quite often, we just got Vanilla id
			// Prepare and modify ItemStack (using somewhat Unsafe methods)
			Material material = BukkitUnsafe.getMaterialFromMinecraftId(id);
			if (material == null || material == Material.AIR) { // If server doesn't recognize id, do not proceed
				Skript.error(m_invalid_minecraft_id.toString(id));
				return; // Apparently ItemStack constructor on 1.12 can throw NPE
			}
			
			// Parse block state to block values
			BlockValues blockValues = null;
			if (!blockStates.isEmpty()) {
				blockValues = BlockCompat.INSTANCE.createBlockValues(material, blockStates);
				// TODO error reporting if we get null
			}
			
			// Apply (NBT) tags to item stack
			ItemStack stack = new ItemStack(material);
			if (tags != null) {
				stack = applyTags(stack, new HashMap<>(tags));
			}
			
			if (blockValues == null)
				datas = Collections.singletonList(new ItemData(stack));
			else
				datas = Collections.singletonList(new ItemData(stack, blockValues));
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
			
			if (typeOfId == null) // Only when it is Minecraft id, not an alias reference
				minecraftIds.put(data, id); // Register Minecraft id for the data, too
			
			materialNames.put(data, new MaterialName(data.type, forms.getFirst(), forms.getSecond(), 0));
			// TODO gender
		}
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

	public void registerCondition(String name, Function<String, Boolean> condition) {
		conditions.put(name, condition);
	}
}
