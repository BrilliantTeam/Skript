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

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Language;
import ch.njol.yggdrasil.YggdrasilSerializable;

public enum Color implements YggdrasilSerializable {
	
	BLACK(DyeColor.BLACK, ChatColor.BLACK),
	DARK_GREY(DyeColor.GRAY, ChatColor.DARK_GRAY),
	// DyeColor.LIGHT_GRAY on 1.13, DyeColor.SILVER on earlier
	LIGHT_GREY(DyeColor.getByColor(org.bukkit.Color.fromRGB(0x9D9D97)), ChatColor.GRAY),
	WHITE(DyeColor.WHITE, ChatColor.WHITE),
	
	DARK_BLUE(DyeColor.BLUE, ChatColor.DARK_BLUE),
	BROWN(DyeColor.BROWN, ChatColor.BLUE),
	DARK_CYAN(DyeColor.CYAN, ChatColor.DARK_AQUA),
	LIGHT_CYAN(DyeColor.LIGHT_BLUE, ChatColor.AQUA),
	
	DARK_GREEN(DyeColor.GREEN, ChatColor.DARK_GREEN),
	LIGHT_GREEN(DyeColor.LIME, ChatColor.GREEN),
	
	YELLOW(DyeColor.YELLOW, ChatColor.YELLOW),
	ORANGE(DyeColor.ORANGE, ChatColor.GOLD),
	
	DARK_RED(DyeColor.RED, ChatColor.DARK_RED),
	LIGHT_RED(DyeColor.PINK, ChatColor.RED),
	
	DARK_PURPLE(DyeColor.PURPLE, ChatColor.DARK_PURPLE),
	LIGHT_PURPLE(DyeColor.MAGENTA, ChatColor.LIGHT_PURPLE);
	
	public static final String LANGUAGE_NODE = "colors";
	
	private DyeColor wool;
	private ChatColor chat;
	@Nullable
	private Adjective adjective;
	
	
	Color(final DyeColor wool, final ChatColor chat) {
		this.wool = wool;
		this.chat = chat;
	}
	private static final Map<DyeColor, Color> BY_WOOL = new HashMap<>();
	
	static {
		for (Color c : values())
			BY_WOOL.put(c.getWoolColor(), c);
		
	}
	
	final static Map<String, Color> BY_NAME = new HashMap<>();
	final static Map<String, Color> BY_ENGLISH_NAME = new HashMap<>();
	
	static {
		Language.addListener(() -> {
			boolean english = BY_ENGLISH_NAME.isEmpty();
			BY_NAME.clear();
			
			for (Color c : values()) {
				String[] names = Language.getList(LANGUAGE_NODE + "." + c.name() + ".names");
				for (final String name : names) {
					BY_NAME.put(name.toLowerCase(), c);
					if (english)
						BY_ENGLISH_NAME.put(name.toLowerCase(), c);
				}
				c.setAdjective(new Adjective(LANGUAGE_NODE + "." + c.name() + ".adjective"));
			}
			
		});
	}
	public DyeColor getWoolColor() {
		return wool;
	}
	
	public String getChat() {
		return "" + chat.toString();
	}
	
	public ChatColor asChatColor() {
		return chat;
	}
	
	public final org.bukkit.Color getBukkitColor() {
		return wool.getColor();
	}
	
	// currently only used by SheepData
	@Nullable
	public Adjective getAdjective() {
		return adjective;
	}
	
	public void setAdjective(Adjective adjective) {
		this.adjective = adjective;
	}
	
	@Nullable
	public static Color byName(final String name) {
		return BY_NAME.get(name.toLowerCase());
	}
	
	@Nullable
	public static Color byEnglishName(final String name) {
		return BY_ENGLISH_NAME.get(name.toLowerCase());
	}
	
	
	public static Color byWoolColor(final DyeColor color) {
		return BY_WOOL.get(color);
	}
	
	@Override
	public String toString() {
		final Adjective a = adjective;
		return a == null ? "" + name() : a.toString(-1, 0);
	}
	
}
