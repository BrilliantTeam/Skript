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

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.eclipse.jdt.annotation.Nullable;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
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
@Description({"Returns the IP address of the connected player in a " +
		"<a href='events.html#connect>connect</a> event or the IP address of the pinger in a " +
		"<a href='events.html#server_list_ping'>server list ping</a> event."})
@Examples({"on connect:",
		"log \"[%now%] %player% (%IP%) joined the server.\"",
		"",
		"on server list ping:",
		"\tsend \"%IP-address%\" to the console"})
@Since("INSERT VERSION")
public class ExprIP extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprIP.class, String.class, ExpressionType.SIMPLE, "IP[( |-)address]");
	}

	private static final boolean PAPER_EVENT_EXISTS = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent");

	private boolean isConnectEvent;

	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isConnectEvent = ScriptLoader.isCurrentEvent(PlayerLoginEvent.class);
		boolean isServerPingEvent = ScriptLoader.isCurrentEvent(ServerListPingEvent.class) ||
				(PAPER_EVENT_EXISTS && ScriptLoader.isCurrentEvent(PaperServerListPingEvent.class));
		if (!isConnectEvent && !isServerPingEvent) {
			Skript.error("The IP expression can't be used outside of a connect or server list ping event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event e) {
		InetAddress address;
		if (isConnectEvent)
			address = ((PlayerLoginEvent) e).getAddress();
		else
			address = ((ServerListPingEvent) e).getAddress();
		return CollectionUtils.array(address == null ? "unknown" : address.getHostAddress());
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
		return "the IP-address";
	}

}
