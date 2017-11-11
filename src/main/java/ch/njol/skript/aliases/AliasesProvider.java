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

import org.bukkit.Bukkit;
import org.bukkit.UnsafeValues;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;

/**
 * Provides aliases on Bukkit/Spigot platform.
 */
public class AliasesProvider {
	
	private Map<String,ItemType> aliases;
	@SuppressWarnings("deprecation")
	private UnsafeValues unsafe;
	
	public static class MojangsonElement {
		
		private String key;
		private String value;
		
		public MojangsonElement(String key, String value) {
			this.key = key;
			this.value = value;
		}
		
		public String getKey() {
			return key;
		}
		
		public String getValue() {
			return value;
		}
	}
	
	public static class Variation {
		
		@Nullable
		private String id;
		@Nullable
		private List<MojangsonElement> tags;
		
		public Variation(@Nullable String id, @Nullable List<MojangsonElement> tags) {
			this.id = id;
			this.tags = tags;
		}
		
		@Nullable
		public String getId() {
			return id;
		}
		
		@Nullable
		public List<MojangsonElement> getTags() {
			return tags;
		}
	}
	
	private Map<String, Map<String, Variation>> variations;
	
	@SuppressWarnings({"deprecation", "null"})
	public AliasesProvider() {
		aliases = new HashMap<>(3000);
		unsafe = Bukkit.getUnsafe();
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
	
	private List<MojangsonElement> parseMojangson(String raw) {
		List<MojangsonElement> list = new ArrayList<>();
		String[] split = raw.split(","); // TODO this is lazy way to split a string
		
		for (String part : split) {
			String[] parts = part.split("=");
			String key = parts[0];
			String value = parts[1];
			
			// Make sure that there actually was = in string
			if (key == null || value == null) {
				// TODO error reporting
				continue;
			}
			
			list.add(new MojangsonElement(key, value));
		}
		
		return list;
	}
	
	@Nullable
	public Variation parseVariation(String raw) {
		String[] parts = raw.split(" ");
		String id = parts[0];
		String tags = parts[1];
		if (id == null || tags == null) {
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
			
			vars.put(node.getKey(), parseVariation(((EntryNode) node).getValue()));
		}
	}
	
	private List<String> parseKeyPattern(String key) {
		List<String> versions = new ArrayList<>();
		
		return versions;
	}
	
	private void loadAlias(String pattern, String data) {
		
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
	
}
