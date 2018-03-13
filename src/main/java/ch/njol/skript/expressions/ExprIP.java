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

import org.bukkit.World;
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
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.Time;
import ch.njol.util.Kleenean;

@Name("IP")
@Description("The IP address of player(s).")
@Examples({"IP-ban the player # is equal to the next line",
		"ban the IP-address of the player",
		"broadcast \"Banned the IP %IP of player%\""})
@Since("1.4, 2.2-dev35 (Converted to PropertyExpression)")
public class ExprIP extends PropertyExpression<Player, String> {
	
	static {
		register(ExprIP.class, String.class, "IP[s][( |-)address[es]]", "players");
	}
    
    @Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends Player>) exprs[0]);
		return true;
	}

	@Override
	protected String[] get(Event event, Player[] source) {
		return get(source, new Getter<String, Player>() {
			@SuppressWarnings("null")
			@Override
			public String get(final Player player) {
				if (event instanceof PlayerLoginEvent && ((PlayerLoginEvent)event).getPlayer().equals(player))
					return ((PlayerLoginEvent) event).getAddress().getHostAddress();
				if (player.getAddress() == null)
					return "unknown";
				//player.getAddress() is the socket address
				InetAddress address = player.getAddress().getAddress();
				return (address == null) ? "unknown" : address.getHostAddress();
			}
		});
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "IP address " + " of " + getExpr().toString(event, debug);
	}
	
}
