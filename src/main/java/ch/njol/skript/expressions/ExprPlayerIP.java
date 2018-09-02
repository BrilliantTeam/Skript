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

import java.net.InetSocketAddress;

import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Player IP")
@Description({"The IP address of a player.", "",
			"Note: you may use the '<a href='expressions.html#ExprIP'>IP</a>' expression for getting the IP in a " +
			"<a href='events.html#connect>connect</a> event."})
@Examples("ban IP of the player")
@Since("1.4")
public class ExprPlayerIP extends SimplePropertyExpression<Player, String> {

	static {
		register(ExprPlayerIP.class, String.class, "IP[(-| )address[es]]", "players");
	}

	@Nullable
	@Override
	public String convert(final Player player) {
		InetSocketAddress address = player.getAddress();
		return address == null ? "unknown" : address.getAddress().toString();
	}

	@Override
	protected String getPropertyName() {
		return "IP-address";
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
}
