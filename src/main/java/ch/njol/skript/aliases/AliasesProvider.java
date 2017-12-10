/*
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
 * Copyright 2011-2016 Peter Güttinger and contributors
 * 
 */

package ch.njol.skript.aliases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

/**
 * Provides aliases on Bukkit/Spigot platform.
 */
public class AliasesProvider {
	
	private Map<String, ItemType> aliases;
	private Map<ItemData, MaterialName> materialNames;
	
	@SuppressWarnings("deprecation")
	private UnsafeValues unsafe;
	
	private Gson gson;
	
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
	
	private Map<String, Map<String, Variation>> variations;
	
	public AliasesProvider() {
		aliases = new HashMap<>(3000);
		materialNames = new HashMap<>(3000);
		variations = new HashMap<>(500);
		gson = new Gson();
		
		@SuppressWarnings("deprecation")
		UnsafeValues values = Bukkit.getUnsafe();
		if (values == null) {
			throw new RuntimeException("unsafe values are not available; cannot initialize aliases backend");
		}
		unsafe = values;
	}
	
	public void load(SectionNode root) {
		for (Node node : root) {
			// Section nodes are for variations
			if (node instanceof SectionNode) {
				loadVariations((SectionNode) node);
				continue;
			}
			
			// Sanity check
			if (!(node instanceof EntryNode)) {
				// TODO error reporting
				continue;
			}
			
			// Get key and value from entry node
			String key = node.getKey();
			String value = ((EntryNode) node).getValue();
			
			
		}
	}
	
	@SuppressWarnings("null")
	private Map<String, Object> parseMojangson(String raw) {
		return (Map<String, Object>) gson.fromJson(raw, Object.class);
	}
	
	@Nullable
	public Variation parseVariation(String raw) {
		int firstSpace = raw.indexOf(' ');
		String id = raw.substring(0, firstSpace);
		if (id.equals("-"))
			id = null;
		
		String tags = raw.substring(firstSpace + 1);
		if (tags.isEmpty()) {
			// TODO error reporting
			return null;
		}
		
		return new Variation(id, parseMojangson(tags));
	}
	
	private void loadVariations(SectionNode root) {
		Map<String, Variation> vars = new HashMap<>();
		for (Node node : root) {
			// Sanity check
			if (!(node instanceof EntryNode)) {
				// TODO error reporting
				continue;
			}
			
			String pattern = node.getKey();
			assert pattern != null;
			List<String> keys = parseKeyPattern(pattern);
			Variation var = parseVariation(((EntryNode) node).getValue());
			
			// Put var there for all keys it matches with
			for (String key : keys) {
				vars.put(key, var);
			}
		}
	}
	
	private List<String> parseKeyPattern(String name) {
		List<String> versions = new ArrayList<>();
		
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			
			if (c == '[') { // Optional part: versions with and without it
				int end = name.indexOf(']', i);
				versions.addAll(parseKeyPattern(Aliases.concatenate(name.substring(0, i), name.substring(i + 1, end), name.substring(end + 1))));
				versions.addAll(parseKeyPattern(Aliases.concatenate(name.substring(0, i), name.substring(end + 1))));
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
					//Skript.error(m_brackets_error.toString());
					// TODO error reporting
					return versions;
				}
				versions.addAll(parseKeyPattern(Aliases.concatenate(name.substring(0, i), name.substring(last + 1, end), name.substring(end + 1))));
			}
		}
		
		return versions;
	}
	
	private void loadAlias(String name, String data) {
		List<String> patterns = parseKeyPattern(name);
		int firstSpace = data.indexOf(' ');
		String id = data.substring(0, firstSpace);
		Map<String, Object> tags = parseMojangson(data.substring(firstSpace + 1));
		
		for (String p : patterns) {
			loadVariedAlias(name, id, tags);
		}
	}
	
	private void loadVariedAlias(String name, String id, Map<String, Object> tags) {
		// Find {variations}
		boolean hasVariations = false;
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			
			// Variation start here
			if (c == '{') {
				hasVariations = true;
				
				int end = name.indexOf('}', i);
				if (end == -1) {
					// TODO error reporting
					continue;
				}
				
				// Take variation name from between brackets
				String varName = name.substring(i + 1, end);
				// Get variations for that id and hope they exist
				Map<String, Variation> vars = variations.get(varName);
				if (vars == null) {
					// TODO error reporting
					continue;
				}
				
				// Iterate over variations
				for (Entry<String, Variation> entry : vars.entrySet()) {
					Map<String, Object> combinedTags = new HashMap<>(tags.size() + entry.getValue().getTags().size());
					combinedTags.putAll(tags);
					combinedTags.putAll(entry.getValue().getTags());
					
					String variedId = entry.getValue().getId();
					if (variedId == null) {
						variedId = id;
					}
					
					loadVariedAlias(entry.getKey(), variedId, combinedTags);
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
	
	private void loadReadyAlias(String name, String id, Map<String, Object> tags) {
		// Prepare and modify ItemStack (using somewhat Unsafe methods)
		ItemStack stack = new ItemStack(unsafe.getMaterialFromInternalName(id));
		unsafe.modifyItemStack(stack, gson.toJson(tags));
		
		// Construct the item type and put it to aliases
		ItemData data = new ItemData(stack);
		ItemType type = new ItemType(data);
		aliases.put(name, type);

		// Material names are filled elsewhere (TODO)
	}

	@Nullable
	public ItemType getAlias(String alias) {
		// TODO Auto-generated method stub
		return null;
	}

	@Nullable
	public ItemType getForMinecraftId(String id) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Nullable
	public MaterialName getMaterialName(ItemData type) {
		return materialNames.get(type);
	}
}
