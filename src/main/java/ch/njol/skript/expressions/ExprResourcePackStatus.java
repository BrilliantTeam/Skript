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

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Resource Pack Status")
@Description("Returns the most recent resource pack status received from a player.")
@Examples("if player's resource pack status is deny or download fail:")
@Since("INSERT VERSION")
@RequiredPlugins("Paper 1.9 or newer")
public class ExprResourcePackStatus extends SimplePropertyExpression<Player, Status> {

	static {
		if (Skript.methodExists(Player.class, "getResourcePackStatus"))
			register(ExprResourcePackStatus.class, Status.class, "resource pack status[es]", "players");
	}

	@Override
	public Status convert(final Player p) {
		return p.getResourcePackStatus();
	}

	@Override
	protected String getPropertyName() {
		return "resource pack status";
	}

	@Override
	public Class<Status> getReturnType() {
		return Status.class;
	}

}
