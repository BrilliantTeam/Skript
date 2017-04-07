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
 * Copyright 2011-2017 Peter Güttinger and contributors
 * 
 */

package ch.njol.skript.util.chat;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.soap.Text;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.Debuggable;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;
import ch.njol.skript.localization.Message;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.Color;
import ch.njol.yggdrasil.Fields;
import edu.umd.cs.findbugs.ba.bcp.New;

/**
 * Represents a chat message in JSON format.
 */
public class ChatMessages {
	
	/**
	 * Chat codes, see {@link ChatCode}.
	 */
	static final Map<String,ChatCode> codes = new HashMap<>();
	
	/**
	 * Instance of GSON we use for serialization.
	 */
	static final Gson gson;
	
	/**
	 * How many entries should be in message cache.
	 */
	static final int msgCacheSize = 100;
	
	static final Map<String,String> msgCache = new LinkedHashMap<String,String>() {
		/**
		 * Why is this needed... ?
		 */
		private static final long serialVersionUID = 8780868977339889766L;

		@Override
        protected boolean removeEldestEntry(@Nullable Map.Entry<String, String> eldest) {
			return size() > 100;
		}
	};
	
	public static void registerListeners() {
		// When language changes or server is loaded loop through all chatcodes
		Language.addListener(new LanguageChangeListener() {
			
			@Override
			public void onLanguageChange() {
				codes.clear();
				
				Skript.debug("Parsing message style lang files");
				for (ChatCode code : ChatCode.values()) {
					if (code.colorCode != null) { // Color code!
						for (String name : Language.getList(Color.LANGUAGE_NODE + "." + code.langName + ".names")) {
							codes.put(name, code);
						}
					} else { // Not color code
						for (String name : Language.getList("chat styles." + code.langName)) {
							codes.put(name, code);
						}
					}
				}
			}
		});
	}
	
	static {
		Gson nullableGson = new GsonBuilder().registerTypeAdapter(boolean.class, new MessageComponent.BooleanSerializer()).create();
		assert nullableGson != null;
		gson = nullableGson;
	}
	
	/**
	 * Component list - this is just serialization mapper class.
	 */
	private static class ComponentList {
		
		public ComponentList(List<MessageComponent> components) {
			this.extra = components.toArray(new MessageComponent[0]);
		}
		
		public ComponentList(MessageComponent[] components) {
			this.extra = components;
		}

		/**
		 * DO NOT USE!
		 */
		@SuppressWarnings("unused")
		@Deprecated
		public String text = "";
		
		/**
		 * Actual message data.
		 */
		@SuppressWarnings("unused")
		@Nullable
		public MessageComponent[] extra;
	}
	
	public static List<MessageComponent> parse(String msg) {
		char[] chars = msg.toCharArray();
		
		List<MessageComponent> components = new ArrayList<>();
		MessageComponent current = new MessageComponent();
		components.add(current);
		StringBuilder curStr = new StringBuilder();
		
		char previous = 0;
		int tagStart = 0;
		boolean tagMode = false; // Tagmode: don't read to StringBuilder, we are at a tag
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c == '\\' || previous == '\\') // \ serves as escape character
				continue;
			previous = c;
			
			if (c == '<') { // Tag start
				tagStart = i;
				tagMode = true;
			}
				
			if (!tagMode) // Normal handling if not inside a tag
				curStr.append(c); // Append this char to curStr
			
			if (c == '>') { // Tag end
				String tag = msg.substring(tagStart + 1, i);
				tagMode = false;
				
				String name;
				String param = "";
				VariableString varParam = null;
				if (tag.contains(":")) {
					String[] split = tag.split(":", 2);
					name = split[0];
					param = split[1];
					
					// Check if we need to do VariableString parsing
					if (param.contains("%")) {
						varParam = VariableString.newInstance(param);
					}
				} else {
					name = tag;
				}
				if (name.equals("none")) {
					curStr.append("<none>");
					continue; // FIXME an ugly hack!
				}
				
				ChatCode code = codes.get(name);
				if (code.nextComponent()) { // Next chat component, someone asked for reset
					String text = curStr.toString();
					curStr = new StringBuilder();
					assert text != null;
					current.text = text;
					
					MessageComponent old = current;
					current = new MessageComponent();
					if (code.equals(ChatCode.reset))
						current.reset = true;
					copyStyles(old, current);
					
					components.add(current);
				}
				
				assert param != null;
				if (code.colorCode != null) // Just update color code
					current.color = code.colorCode;
				else
					code.updateComponent(current, param, varParam); // Call ChatCode update
			}
		}
		String text = curStr.toString();
		assert text != null;
		current.text = text;
		
		return components;
	}
	
	@SuppressWarnings("null")
	public static MessageComponent[] parseToArray(String msg) {
		return parse(msg).toArray(new MessageComponent[0]);
	}
	
	public static String toJson(String msg) {
		ComponentList componentList = new ComponentList(parse(msg));
		String json = gson.toJson(componentList);
		assert json != null;
		return json;
	}
	
	public static String toJson(MessageComponent[] components) {
		ComponentList componentList = new ComponentList(components);
		String json = gson.toJson(componentList);
		assert json != null;
		return json;
	}
	
	/**
	 * Copies styles from component to another. Note that this only copies
	 * additional styling, i.e. if text was not bold and is bold, it will remain bold.
	 * @param from
	 * @param to
	 */
	public static void copyStyles(MessageComponent from, MessageComponent to) {
		if (to.reset)
			return;
		
		if (!to.bold)
			to.bold = from.bold;
		if (!to.italic)
			to.italic = from.italic;
		if (!to.underlined)
			to.underlined = from.underlined;
		if (!to.strikethrough)
			to.strikethrough = from.strikethrough;
		if (!to.obfuscated)
			to.obfuscated = from.obfuscated;
		if (to.color.equals("reset"))
			to.color = from.color;
		
		if (to.clickEvent == null)
			to.clickEvent = from.clickEvent;
		if (to.insertion == null)
			to.insertion = from.insertion;
		if (to.hoverEvent == null)
			to.hoverEvent = from.hoverEvent;
	}
}