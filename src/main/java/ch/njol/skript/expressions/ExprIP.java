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

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerLoginEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("IP")
@Description("The IP address in a <a href='events.html#connect'>connect</a> event.")
@Examples({"on connect:",
		"log \"[%now%] %IP% joined the server.\""})
@Since("INSERT VERSION")
public class ExprIP extends SimpleExpression<String> {
	
	static {
		Skript.registerExpression(ExprIP.class, String.class, ExpressionType.SIMPLE, "[the] IP[(-| )address]");
	}
	
	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(PlayerLoginEvent.class)) {
			Skript.error("The 'IP' expression can only be used in a connect event.");
			return false;
		}
		return true;
	}
	
	@Override
	@Nullable
	protected String[] get(Event e) {
		return CollectionUtils.array(((PlayerLoginEvent) e).getAddress().toString());
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the ip";
	}
	
}
