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
package ch.njol.skript.expressions;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Andrew Tran
 */
@Name("Chat Format")
@Description("Can be used to get/retrieve the chat format")
@Examples({"set the chat format to \"<yellow>[%s] <green>%s\""})
@Since("INSERT VERSION")
public class ExprChatFormat extends SimpleExpression<String>{
	static {
		Skript.registerExpression(ExprChatFormat.class, String.class, ExpressionType.SIMPLE, "[the] (message|chat) format[ting]");
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.SET){
			return new Class<?>[]{String.class};
		}
		return null;
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(AsyncPlayerChatEvent.class)){
			Skript.error("The expression 'chat format' may only be used in chat events");
			return false;
		}
		return true;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "chat format";
	}
	
	@SuppressWarnings("null")
	@Override
	@Nullable
	protected String[] get(Event e) {
		return new String[]{((AsyncPlayerChatEvent) e).getFormat()};
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		if (delta == null){
			return;
		}
		if (mode == Changer.ChangeMode.SET){
			((AsyncPlayerChatEvent) e).setFormat((String) delta[0]);
		}
	}
}
