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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Arithmetic;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.YggdrasilSerializable;

@SuppressWarnings("null")
public enum SkriptColor implements Color {

	BLACK(DyeColor.BLACK, ChatColor.BLACK, org.bukkit.Color.fromRGB(0x191919)),
	DARK_GREY(DyeColor.GRAY, ChatColor.DARK_GRAY, org.bukkit.Color.fromRGB(0x4C4C4C)),
	// DyeColor.LIGHT_GRAY on 1.13, DyeColor.SILVER on earlier
	LIGHT_GREY(DyeColor.getByColor(org.bukkit.Color.fromRGB(0x9D9D97)), ChatColor.GRAY, org.bukkit.Color.fromRGB(0x999999)),
	WHITE(DyeColor.WHITE, ChatColor.WHITE, org.bukkit.Color.fromRGB(0xFFFFFF)),
	
	DARK_BLUE(DyeColor.BLUE, ChatColor.DARK_BLUE, org.bukkit.Color.fromRGB(0x334CB2)),
	BROWN(DyeColor.BROWN, ChatColor.BLUE, org.bukkit.Color.fromRGB(0x664C33)),
	DARK_CYAN(DyeColor.CYAN, ChatColor.DARK_AQUA, org.bukkit.Color.fromRGB(0x4C7F99)),
	LIGHT_CYAN(DyeColor.LIGHT_BLUE, ChatColor.AQUA, org.bukkit.Color.fromRGB(0x6699D8)),
	
	DARK_GREEN(DyeColor.GREEN, ChatColor.DARK_GREEN, org.bukkit.Color.fromRGB(0x667F33)),
	LIGHT_GREEN(DyeColor.LIME, ChatColor.GREEN, org.bukkit.Color.fromRGB(0x7FCC19)),
	
	YELLOW(DyeColor.YELLOW, ChatColor.YELLOW, org.bukkit.Color.fromRGB(0xE5E533)),
	ORANGE(DyeColor.ORANGE, ChatColor.GOLD, org.bukkit.Color.fromRGB(0xD87F33)),
	
	DARK_RED(DyeColor.RED, ChatColor.DARK_RED, org.bukkit.Color.fromRGB(0x993333)),
	LIGHT_RED(DyeColor.PINK, ChatColor.RED, org.bukkit.Color.fromRGB(0xF27FA5)),
	
	DARK_PURPLE(DyeColor.PURPLE, ChatColor.DARK_PURPLE, org.bukkit.Color.fromRGB(0x7F3FB2)),
	LIGHT_PURPLE(DyeColor.MAGENTA, ChatColor.LIGHT_PURPLE, org.bukkit.Color.fromRGB(0xB24CD8));
	
	private org.bukkit.Color bukkit;
	private ChatColor chat;
	private DyeColor dye;
	
	@Nullable
	Adjective adjective;
	
	private SkriptColor(DyeColor dye, ChatColor chat, org.bukkit.Color bukkit) {
		this.bukkit = bukkit;
		this.chat = chat;
		this.dye = dye;
	}
	
	// currently only used by SheepData
	@Override
	public Adjective getAdjective() {
		return adjective;
	}
	
	@Override
	public ChatColor asChatColor() {
		return chat;
	}
	
	@Override
	public org.bukkit.Color asBukkitColor() {
		return bukkit;
	}

	@Override
	public DyeColor asDyeColor() {
		return dye;
	}
	
	@Override
	public String getName() {
		return name();
	}
	
	@Override
	public String getFormattedChat() {
		return "" + chat;
	}
	
	@Deprecated
	@Override
	public byte getWoolData() {
		return dye.getWoolData();
	}
	
	@Deprecated
	@Override
	public byte getDyeData() {
		return (byte) (15 - dye.getWoolData());
	}
	
	final static Map<String, SkriptColor> names = new HashMap<>();
	final static Set<SkriptColor> colors = new HashSet<>();
	public final static String LANGUAGE_NODE = "colors";
	
	static {
		for (SkriptColor color : values())
			colors.add(color);
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				names.clear();
				for (SkriptColor color : values()) {
					String node = LANGUAGE_NODE + "." + color.name();
					color.adjective = new Adjective(node + ".adjective");
					for (String name : Language.getList(node + ".names"))
						names.put(name.toLowerCase(), color);
				}
			}
		});
	}
	
	/**
	 * @param name The name of the color defined by Skript's .lang files.
	 * @return Optional if any Skript Color matched up with the defined name
	 */
	public static Optional<SkriptColor> fromName(String name) {
		return names.entrySet().stream()
				.filter(entry -> entry.getKey().equals(name))
				.map(entry -> entry.getValue())
				.findAny();
	}
	
	/**
	 * @param dye DyeColor to match against a defined Skript Color.
	 * @return Optional if any Skript Color matched up with the defined DyeColor
	 */
	public static Optional<SkriptColor> fromDyeColor(DyeColor dye) {
		return colors.stream()
				.filter(color -> color.asDyeColor().equals(dye))
				.findAny();
	}
	
	/**
	 * @param dye DyeColor to match against a defined Skript Color.
	 * @return Optional if any Skript Color matched up with the defined DyeColor
	 */
	public static Optional<SkriptColor> fromBukkitColor(org.bukkit.Color bukkitColor) {
		return colors.stream()
				.filter(color -> color.matches(bukkitColor))
				.findAny();
	}
	
	/**
	 * @deprecated Magic numbers
	 * @param dye DyeColor to match against a defined Skript Color.
	 * @return Optional if any Skript Color matched up with the defined DyeColor
	 */
	@Deprecated
	public static Optional<SkriptColor> fromDyeData(short data) {
		if (data < 0 || data >= 16)
			return Optional.empty();
		return colors.stream()
				.filter(color -> color.getWoolData() == 15 - data)
				.findAny();
	}
	
	/**
	 * @deprecated Magic numbers
	 * @param dye DyeColor to match against a defined Skript Color.
	 * @return Optional if any Skript Color matched up with the defined DyeColor
	 */
	@Deprecated
	public static Optional<SkriptColor> fromWoolData(short data) {
		if (data < 0 || data >= 16)
			return Optional.empty();
		return colors.stream()
				.filter(color -> color.getWoolData() == data)
				.findAny();
	}
	
	/**
	 * @param color The Bukkit Color to match appropriately against.
	 * @return boolean if the Skript Color does indeed match the Bukkit Color.
	 */
	public boolean matches(org.bukkit.Color color) {
		return bukkit.asBGR() == color.asBGR() && bukkit.asRGB() == color.asRGB();
	}
	
	//The acceptable difference between tones when comparing.
	final static int DIFFERENCE = 10;
	
	public boolean isSimilar(org.bukkit.Color color) {
		return Math.abs(bukkit.getBlue() - color.getBlue()) < DIFFERENCE
				&& Math.abs(bukkit.getRed() - color.getRed()) < DIFFERENCE
				&& Math.abs(bukkit.getGreen() - color.getGreen()) < DIFFERENCE;
	}
	
	@Override
	public String toString() {
		return adjective == null ? "" + name() : adjective.toString(-1, 0);
	}

}
