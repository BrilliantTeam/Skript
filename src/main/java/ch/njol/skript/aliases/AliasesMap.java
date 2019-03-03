package ch.njol.skript.aliases;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.aliases.AliasesMap.Match;

/**
 * Stores the aliases.
 */
public class AliasesMap {
	
	public static class Match {
		
		private final MatchQuality quality;
		
		@Nullable
		private final AliasData data;
		
		public Match(MatchQuality quality, @Nullable AliasData data) {
			this.quality = quality;
			this.data = data;
		}
		
		public MatchQuality getQuality() {
			return quality;
		}
		
		@Nullable
		public AliasData getData() {
			return data;
		}
	}
	
	public static class AliasData {
		
		/**
		 * The item associated with this alias.
		 */
		private final ItemData item;
		
		/**
		 * Name of this alias.
		 */
		private final MaterialName name;
		
		/**
		 * Minecraft ID of this alias.
		 */
		private final String minecraftId;
		
		public AliasData(ItemData item, MaterialName name, String minecraftId) {
			this.item = item;
			this.name = name;
			this.minecraftId = minecraftId;
		}

		public ItemData getItem() {
			return item;
		}

		public MaterialName getName() {
			return name;
		}

		public String getMinecraftId() {
			return minecraftId;
		}
	}
		
	private static class MaterialEntry {
		
		/**
		 * The default alias for this material.
		 */
		@Nullable
		public AliasData defaultItem;
		
		/**
		 * All different aliases that share this material.
		 */
		public final List<AliasData> items;
		
		public MaterialEntry() {
			this.items = new ArrayList<>();
		}
		
	}
	
	/**
	 * One material entry per material. Ordinal of material is index of entry.
	 */
	private final MaterialEntry[] materialEntries;
	
	public AliasesMap() {
		this.materialEntries = new MaterialEntry[Material.values().length];
		for (int i = 0; i < materialEntries.length; i++) {
			materialEntries[i] = new MaterialEntry();
		}
	}
	
	private MaterialEntry getEntry(ItemData item) {
		MaterialEntry entry = materialEntries[item.getType().ordinal()];
		assert entry != null;
		return entry;
	}
	
	public void addAlias(AliasData data) {
		MaterialEntry entry = getEntry(data.getItem());
		if (data.getItem().isDefault()) {
			// TODO handle overwriting defaults; is it an error?
			entry.defaultItem = data;
		} else {
			entry.items.add(data);
		}
	}
	
	/**
	 * Attempts to get the closest matching alias for given item.
	 * @param item Item to find closest alias for.
	 * @return The match, containing the alias data and match quality.
	 */
	public Match matchAlias(ItemData item) {
		MaterialEntry entry = getEntry(item);
		
		// Special case: no aliases available!
		if (entry.defaultItem == null && entry.items.isEmpty()) {
			return new Match(MatchQuality.DIFFERENT, null);
		}
		
		// Try to find the best match
		MatchQuality maxQuality = MatchQuality.DIFFERENT;
		AliasData bestMatch = null;
		for (AliasData data : entry.items) {
			MatchQuality quality = item.matchAlias(data.getItem());
			if (quality.isBetter(maxQuality)) {
				maxQuality = quality;
				bestMatch = data;
			}
		}
		
		// Check that we found a reasonably good match
		// Just same material id -> default item
		if (maxQuality.isBetter(MatchQuality.SAME_MATERIAL)) {
			assert bestMatch != null; // Re-setting quality sets this too
			return new Match(maxQuality, bestMatch);
		} else { // Try default item
			AliasData defaultItem = entry.defaultItem;
			if (defaultItem != null) { // Just match against it
				return new Match(item.matchAlias(defaultItem.getItem()), defaultItem);
			} else { // No default item, no match
				if (bestMatch != null) { // Initially ignored this, but it is best match
					return new Match(MatchQuality.SAME_MATERIAL, bestMatch);
				}
			}
		}
		
		throw new AssertionError(); // Shouldn't have reached here
	}
}
