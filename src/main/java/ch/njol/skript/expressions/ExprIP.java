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
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("IP")
@Description("The IP address of player(s).")
@Examples({"IP-ban the player # is equal to the next line",
		"ban the IP-address of the player",
		"broadcast \"Banned the IP %IP of player%\""})
@Since("1.4, 2.2-dev35 (Converted to SimplePropertyExpression)")
public class ExprIP extends SimplePropertyExpression<Player, String> {
	
	static {
		register(ExprIP.class, String.class, "IP[s][( |-)address[es]]", "players");
	}
    
    @Override
	public Class<String> getReturnType() {
		return String.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "IP[s][( |-)address[es]]";
	}
	
	@SuppressWarnings("null")
	@Override
	public String convert(final Player player) {
		if (player.getAddress() == null)
			return "unknown";
		//player.getAddress() is the socket address
		InetAddress address = player.getAddress().getAddress();
		if (address == null)
			return "unknown";
		return address.getHostAddress();
	}
	
}
