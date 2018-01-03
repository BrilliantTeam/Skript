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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.validate.SectionValidator;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.localization.RegexMessage;
import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.PotionEffectUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.NonNullPair;
import ch.njol.util.Setter;

/**
 * FIXME rename
 * 
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public abstract class Aliases {
	
	private final static boolean newPotions = Skript.isRunningMinecraft(1, 9);
	
	private final static AliasesProvider provider = new AliasesProvider();
	private static AliasesProvider localProvider = new AliasesProvider();
	
	@Nullable
	private final static ItemType getAlias_i(final String s) {
		final ItemType t = ScriptLoader.getScriptAliases().get(s);
		if (t != null)
			return t;
		return provider.getAlias(s);
	}
	
	static String itemSingular = "item";
	static String itemPlural = "items";
	@Nullable
	static String itemGender = null;
	static String blockSingular = "block";
	static String blockPlural = "blocks";
	@Nullable
	static String blockGender = null;
	
	// this is not an alias!
	private final static ItemType everything = new ItemType();
	static {
		everything.setAll(true);
		everything.add(new ItemData());
	}
	
	private final static Message m_brackets_error = new Message("aliases.brackets error");
	private final static ArgsMessage m_invalid_brackets = new ArgsMessage("aliases.invalid brackets");
	private final static ArgsMessage m_empty_alias = new ArgsMessage("aliases.empty alias");
	private final static ArgsMessage m_unknown_variation = new ArgsMessage("aliases.unknown variation");
	private final static Message m_starting_with_number = new Message("aliases.starting with number");
	private final static Message m_missing_aliases = new Message("aliases.missing aliases");
	private final static Message m_empty_string = new Message("aliases.empty string");
	private final static ArgsMessage m_invalid_item_data = new ArgsMessage("aliases.invalid item data");
	private final static ArgsMessage m_invalid_id = new ArgsMessage("aliases.invalid id");
	private final static Message m_invalid_block_data = new Message("aliases.invalid block data");
	private final static ArgsMessage m_invalid_item_type = new ArgsMessage("aliases.invalid item type");
	private final static ArgsMessage m_out_of_data_range = new ArgsMessage("aliases.out of data range");
	private final static Message m_invalid_range = new Message("aliases.invalid range");
	private final static ArgsMessage m_invalid_section = new ArgsMessage("aliases.invalid section");
	private final static ArgsMessage m_section_not_found = new ArgsMessage("aliases.section not found");
	private final static ArgsMessage m_not_a_section = new ArgsMessage("aliases.not a section");
	private final static Message m_unexpected_non_variation_section = new Message("aliases.unexpected non-variation section");
	private final static Message m_unexpected_section = new Message("aliases.unexpected section");
	private final static ArgsMessage m_loaded_x_aliases_from = new ArgsMessage("aliases.loaded x aliases from");
	private final static ArgsMessage m_loaded_x_aliases = new ArgsMessage("aliases.loaded x aliases");
	
	final static class Variations extends HashMap<String, HashMap<String, ItemType>> {
		private final static long serialVersionUID = -139481665727386819L;
	}
	
	private static int nextBracket(final String s, final char closingBracket, final char openingBracket, final int start) {
		int n = 0;
		assert s.charAt(start) == openingBracket;
		for (int i = start + 1; i < s.length(); i++) {
			if (s.charAt(i) == '\\') {
				i++;
				continue;
			} else if (s.charAt(i) == closingBracket) {
				if (n == 0)
					return i;
				n--;
			} else if (s.charAt(i) == openingBracket) {
				n++;
			}
		}
		Skript.error(m_invalid_brackets.toString(openingBracket + "" + closingBracket));
		return -1;
	}
	
	/**
	 * Concatenates parts of an alias's name. This currently 'lowercases' the first character of any part if there's no space in front of it. It also replaces double spaces with a
	 * single one and trims the resulting string.
	 * 
	 * @param parts
	 */
	final static String concatenate(final String... parts) {
		assert parts.length >= 2;
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].isEmpty())
				continue;
			if (b.length() == 0) {
				b.append(parts[i]);
				continue;
			}
			final char c = parts[i].charAt(0);
			if (Character.isUpperCase(c) && b.charAt(b.length() - 1) != ' ') {
				b.append(Character.toLowerCase(c) + parts[i].substring(1));
			} else {
				b.append(parts[i]);
			}
		}
		return "" + b.toString().replace("  ", " ").trim();
	}
	
	@SuppressWarnings("null")
	private final static Pattern numberWordPattern = Pattern.compile("\\d+\\s+.+");
	
	@SuppressWarnings("null")
	public static final String getMaterialName(ItemData type, boolean plural) {
		MaterialName name = provider.getMaterialName(type);
		if (name == null) {
			return "" + type.type;
		}
		return name.toString(plural);
	}
	
	public final static String getDebugMaterialName(ItemData type, boolean plural) {
		final MaterialName n = provider.getMaterialName(type);
		if (n == null) {
			return "" + type.type;
		}
		return n.getDebugName(plural);
	}
	
	/**
	 * @return The ietm's gender or -1 if no name is found
	 */
	public final static int getGender(ItemData item) {
		final MaterialName n = provider.getMaterialName(item);
		if (n != null)
			return n.gender;
		return -1;
	}
	
	/**
	 * @return how many ids are missing an alias, including the 'any id' (-1)
	 */
	final static int addMissingMaterialNames() {
		int r = 0;
		StringBuilder missing = new StringBuilder(m_missing_aliases + " ");
		for (final Material m : Material.values()) {
			assert m != null;
			ItemData data = new ItemData(m);
			if (provider.getMaterialName(data) == null) { // Material name is missing
				provider.setMaterialName(data, new MaterialName(m, "" + m.toString().toLowerCase().replace('_', ' '), "" + m.toString().toLowerCase().replace('_', ' '), 0));
				missing.append(m.getId() + ", ");
				r++;
			}
		}
		if (r > 0) // Give a warning about missing aliases we just worked around
			Skript.warning("" + missing.substring(0, missing.length() - 2));
		return r;
	}
	
	/**
	 * Parses an ItemType to be used as an alias, i.e. it doesn't parse 'all'/'every' and the amount.
	 * 
	 * @param s mixed case string
	 * @return A new ItemType representing the given value
	 */
	@Nullable
	public static ItemType parseAlias(final String s) {
		if (s.isEmpty()) {
			Skript.error(m_empty_string.toString());
			return null;
		}
		if (s.equals("*"))
			return everything;
		
		final ItemType t = new ItemType();
		
		final String[] types = s.split("\\s*,\\s*");
		for (final String type : types) {
			if (type == null || parseType(type, t, true) == null)
				return null;
		}
		
		return t;
	}
	
	private final static RegexMessage p_any = new RegexMessage("aliases.any", "", " (.+)", Pattern.CASE_INSENSITIVE);
	private final static Message m_any = new Message("aliases.any-skp");
	private final static RegexMessage p_every = new RegexMessage("aliases.every", "", " (.+)", Pattern.CASE_INSENSITIVE);
	private final static RegexMessage p_of_every = new RegexMessage("aliases.of every", "(\\d+) ", " (.+)", Pattern.CASE_INSENSITIVE);
	private final static RegexMessage p_of = new RegexMessage("aliases.of", "(\\d+) (?:", " )?(.+)", Pattern.CASE_INSENSITIVE);
	
	/**
	 * Parses an ItemType.
	 * <p>
	 * Prints errors.
	 * 
	 * @param s
	 * @return The parsed ItemType or null if the input is invalid.
	 */
	@Nullable
	public static ItemType parseItemType(String s) {
		if (s.isEmpty())
			return null;
		s = "" + s.trim();
		
		final ItemType t = new ItemType();
		
		Matcher m;
		if ((m = p_of_every.matcher(s)).matches()) {
			t.setAmount(Utils.parseInt("" + m.group(1)));
			t.setAll(true);
			s = "" + m.group(m.groupCount());
		} else if ((m = p_of.matcher(s)).matches()) {
			t.setAmount(Utils.parseInt("" + m.group(1)));
			s = "" + m.group(m.groupCount());
		} else if ((m = p_every.matcher(s)).matches()) {
			t.setAll(true);
			s = "" + m.group(m.groupCount());
		} else {
			final int l = s.length();
			s = Noun.stripIndefiniteArticle(s);
			if (s.length() != l) // had indefinite article
				t.setAmount(1);
		}
		
		final String lc = s.toLowerCase();
		final String of = Language.getSpaced("enchantments.of").toLowerCase();
		int c = -1;
		outer: while ((c = lc.indexOf(of, c + 1)) != -1) {
			final ItemType t2 = t.clone();
			final BlockingLogHandler log = SkriptLogger.startLogHandler(new BlockingLogHandler());
			try {
				if (parseType("" + s.substring(0, c), t2, false) == null)
					continue;
			} finally {
				log.stop();
			}
			if (t2.numTypes() == 0)
				continue;
			final Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
			final String[] enchs = lc.substring(c + of.length(), lc.length()).split("\\s*(,|" + Pattern.quote(Language.get("and")) + ")\\s*");
			for (final String ench : enchs) {
				final EnchantmentType e = EnchantmentType.parse("" + ench);
				if (e == null)
					continue outer;
				enchantments.put(e.getType(), e.getLevel());
			}
			t2.addEnchantments(enchantments);
			return t2;
		}
		
		if (parseType(s, t, false) == null)
			return null;
		
		if (t.numTypes() == 0)
			return null;
		
		return t;
	}
	
	/**
	 * Prints errors.
	 * 
	 * @param s The string holding the type, can be either a number or an alias, plus an optional data part. Case does not matter.
	 * @param t The ItemType to add the parsed ItemData(s) to (i.e. this ItemType will be modified)
	 * @param isAlias Whether this type is parsed for an alias.
	 * @return The given item type or null if the input couldn't be parsed.
	 */
	@Nullable
	private final static ItemType parseType(final String s, final ItemType t, final boolean isAlias) {
		ItemType i;
		final String type = s;
		if (type.isEmpty()) {
			t.add(new ItemData());
			return t;
		} else if (type.matches("\\d+")) {
			// TODO error: numeric ids are not supported anymore
			return null;
		} else if ((i = getAlias(type)) != null) {
			for (ItemData d : i) {
				d = d.clone();
				t.add(d);
			}
			return t;
		}
		if (isAlias)
			Skript.error(m_invalid_item_type.toString(s));
		return null;
	}
	
	/**
	 * Gets an alias from the aliases defined in the config.
	 * 
	 * @param s The alias to get, case does not matter
	 * @return A copy of the ItemType represented by the given alias or null if no such alias exists.
	 */
	@Nullable
	private final static ItemType getAlias(final String s) {
		ItemType i;
		String lc = "" + s.toLowerCase();
		final Matcher m = p_any.matcher(lc);
		if (m.matches()) {
			lc = "" + m.group(m.groupCount());
		}
		if ((i = getAlias_i(lc)) != null)
			return i.clone();
		boolean b;
		if ((b = lc.endsWith(" " + blockSingular)) || lc.endsWith(" " + blockPlural)) {
			if ((i = getAlias_i("" + s.substring(0, s.length() - (b ? blockSingular.length() : blockPlural.length()) - 1))) != null) {
				i = i.clone();
				for (int j = 0; j < i.numTypes(); j++) {
					final ItemData d = i.getTypes().get(j);
					if (d.getType().isBlock()) {
						i.remove(d);
						j--;
					}
				}
				if (i.getTypes().isEmpty())
					return null;
				return i;
			}
		} else if ((b = lc.endsWith(" " + itemSingular)) || lc.endsWith(" " + itemPlural)) {
			if ((i = getAlias_i("" + s.substring(0, s.length() - (b ? itemSingular.length() : itemPlural.length()) - 1))) != null) {
				for (int j = 0; j < i.numTypes(); j++) {
					final ItemData d = i.getTypes().get(j);
					if (!d.isAnything && d.getType().isBlock()) {
						i.remove(d);
						j--;
					}
				}
				if (i.getTypes().isEmpty())
					return null;
				return i;
			}
		}
		return null;
	}
	
	public static void clear() {
		provider.clearAliases();
	}
	
	public static void load() {
		File aliasesFolder = new File(Skript.getInstance().getDataFolder(), "aliases");
		try {
			loadDirectory(aliasesFolder);
		} catch (IOException e) {
			Skript.exception(e);
		}
	}
	
	public static void loadDirectory(File dir) throws IOException {
		for (File f : dir.listFiles()) {
			if (f.isDirectory())
				loadDirectory(f);
			else
				load(f);
		}
	}
	
	public static void load(File f) throws IOException {
		Config config = new Config(f, false, false, "=");
		load(config);
	}
	
	public static void load(Config config) {
		for (Node n : config.getMainNode()) {
			if (!(n instanceof SectionNode)) {
				Skript.error("" + m_invalid_section);
				continue;
			}
			
			provider.load((SectionNode) n);
		}
	}
}
