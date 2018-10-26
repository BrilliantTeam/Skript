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
package ch.njol.skript.util;

import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;

import ch.njol.skript.localization.Adjective;
import ch.njol.yggdrasil.YggdrasilSerializable;

public interface Color extends YggdrasilSerializable {
	
	//TODO add references to notes.
	
	/**
	 * @return The Bukkit Color of this Color.
	 */
	//AKA getBukkitColor - Needs a name change
	org.bukkit.Color asBukkitColor();
	
	/**
	 * @return The Skript Adjective if one has been set.
	 */
	Adjective getAdjective();
	
	/**
	 * @return The Bukkit ChatColor of this Color.
	 */
	ChatColor asChatColor();
	
	/**
	 * @return The Bukkkit DyeColor of this Color.
	 */
	//AKA getWoolColor - Needs a name change
	DyeColor asDyeColor();
	
	/**
	 * @return The name of the Skript Color
	 */
	String getName();
	
	/**
	 * @deprecated Bytes contain magic values and is subject to removal by Spigot.
	 * @return The wool byte data of this color
	 */
	//AKA getWool - Needs a name change
	@Deprecated
	byte getWoolData();
	
	/**
	 * @deprecated Bytes contain magic values and is subject to removal by Spigot.
	 * @return The dye byte of this color
	 */
	//AKA getDye - Needs a name change
	@Deprecated
	byte getDyeData();

	/**
	 * @return The ChatColor but formated. Must be returned as a ChatColor String.
	 */
	String getFormattedChat();
	
	
	
	

	//public final static String LANGUAGE_NODE = "colors";
	
	/*private final DyeColor wool;
	private final ChatColor chat;
	private final org.bukkit.Color bukkit;
	
	@Nullable
	Adjective adjective;
	
	private Color(final DyeColor wool, final ChatColor chat, final org.bukkit.Color bukkit) {
		this.wool = wool;
		this.chat = chat;
		this.bukkit = bukkit;
	}
	
	private final static Color[] byWool = new Color[16];
	static {
		for (final Color c : values()) {
			byWool[c.wool.getWoolData()] = c;
		}
	}
	
	final static Map<String, Color> byName = new HashMap<>();
	final static Map<String, Color> byEnglishName = new HashMap<>();
	static {
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				final boolean english = byEnglishName.isEmpty();
				byName.clear();
				for (final Color c : values()) {
					final String[] names = Language.getList(LANGUAGE_NODE + "." + c.name() + ".names");
					for (final String name : names) {
						byName.put(name.toLowerCase(), c);
						if (english)
							byEnglishName.put(name.toLowerCase(), c);
					}
					c.adjective = new Adjective(LANGUAGE_NODE + "." + c.name() + ".adjective");
				}
			}
		});
	}
	
	public byte getWool() {
		return wool.getWoolData();
	}
	
	public String getChat() {
		return "" + chat.toString();
	}
	
	// currently only used by SheepData
	public Adjective getAdjective() {
		return adjective;
	}
	
	@Override
	public String toString() {
		final Adjective a = adjective;
		return a == null ? "" + name() : a.toString(-1, 0);
	}
	
	@Nullable
	public static Color byName(final String name) {
		return byName.get(name.toLowerCase());
	}
	
	@Nullable
	public static Color byEnglishName(final String name) {
		return byEnglishName.get(name.toLowerCase());
	}
	
	@Nullable
	public static Color byWool(final short data) {
		if (data < 0 || data >= 16)
			return null;
		return byWool[data];
	}
	
	@Nullable
	public static Color byDye(final short data) {
		if (data < 0 || data >= 16)
			return null;
		return byWool[15 - data];
	}
	
	public static Color byWoolColor(final DyeColor color) {
		return byWool(color.getWoolData());
	}
	*/
	
}
