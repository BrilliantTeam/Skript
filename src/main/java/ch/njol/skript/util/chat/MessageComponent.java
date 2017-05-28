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

import java.lang.reflect.Type;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.VariableString;

/**
 * Component for chat messages. This can be serialized with GSON and then
 * sent to client.
 */
public class MessageComponent {
	
	public String text = "";
	
	/**
	 * Tells that this component is supposed to reset styling.
	 */
	public boolean reset = false;
	
	/**
	 * Makes text <b>bold</b>.
	 */
	public boolean bold = false;
	
	/**
	 * Makes text <i>italic</i>.
	 */
	public boolean italic = false;
	
	/**
	 * Makes texxt <u>underlined</u>.
	 */
	public boolean underlined = false;
	
	/**
	 * Makes text <s>strikethrough</s>
	 */
	public boolean strikethrough = false;
	
	/**
	 * Makes text obfuscated, i.e. each tick the client will scramble
	 * all letters with random ones.
	 */
	public boolean obfuscated = false;
	
	/**
	 * Color of this text. Defaults to reseting it.
	 */
	public @Nullable String color;
	
	/**
	 * Value of this, if present, will appended on what player is currently
	 * writing to chat.
	 */
	@Nullable
	public String insertion;
	
	@Nullable
	public ClickEvent clickEvent;
	
	public static class ClickEvent {
		
		public ClickEvent(ClickEvent.Action action, String value, @Nullable VariableString var) {
			this.action = action;
			this.value = value;
			this.var = var;
		}
		
		public static enum Action  {
			
			open_url,
			
			run_command,
			
			suggest_command,
			
			change_page
		}
		
		public ClickEvent.Action action;
		
		public String value;
		
		@Nullable
		public transient VariableString var;
	}
	
	public static class HoverEvent {
		
		public HoverEvent(HoverEvent.Action action, String value, @Nullable VariableString var) {
			this.action = action;
			this.value = value;
			this.var = var;
		}
		
		public static enum Action {
			
			show_text,
			
			show_item,
			
			show_entity,
			
			show_achievement
		}
		
		public HoverEvent.Action action;
		
		public String value;
		
		@Nullable
		public transient VariableString var;
	}
	
	@Nullable
	public HoverEvent hoverEvent;
	
	public static class BooleanSerializer implements JsonSerializer<Boolean> {

		@Override
		public @Nullable JsonElement serialize(@Nullable Boolean src, @Nullable Type typeOfSrc, @Nullable JsonSerializationContext context) {
			return src ? new JsonPrimitive(true) : null;
		}
	}

	@SuppressWarnings("null")
	public void variableUpdate(Event event) {
		if (clickEvent != null && clickEvent.var != null) {
			clickEvent.value = clickEvent.var.getSingle(event);
		}
		if (hoverEvent != null && hoverEvent.var != null) {
			hoverEvent.value = hoverEvent.var.getSingle(event);
		}
	}
}
