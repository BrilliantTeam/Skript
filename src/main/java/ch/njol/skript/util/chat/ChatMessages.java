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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains generic utils for generating formatted chat messages.
 */
public class ChatMessages {
	
	private static final Map<String,ChatCode> codes = new HashMap<>();
	
	public static String parseFormatting(String msg) {
		char[] chars = msg.toCharArray();
		
		List<MessageComponent> components = new ArrayList<>();
		MessageComponent current = new MessageComponent();
		StringBuilder curStr = new StringBuilder();
		
		char previous = 0;
		int tagStart = 0;
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c == '\\' || previous == '\\') // \ serves as escape character
				continue;
			previous = c;
			curStr.append(c); // Append this char to curStr
			
			if (c == '<') {
				tagStart = i;
			} else if (c == '>') {
				String tag = msg.substring(tagStart + 1, i);
				
				String name;
				String param = "";
				if (tag.contains(":")) {
					String[] split = tag.split(":");
					name = split[0];
					param = split[1];
				} else {
					name = tag;
				}
				
				ChatCode code = codes.get(name);
				if (code.nextComponent()) { // Next chat component, someone asked for reset
					current.text = curStr.toString();
					current = new MessageComponent();
					components.add(current);
				}
				
				assert param != null;
				if (code.colorCode != null) // Just update color code
					current.color = code.colorCode;
				else
					code.updateComponent(current, param); // Actually update the component...
			}
		}
		
		return "";
	}
}