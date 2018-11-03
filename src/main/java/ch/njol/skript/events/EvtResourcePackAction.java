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
package ch.njol.skript.events;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;

public class EvtResourcePackAction extends SkriptEvent {

	static {
		Skript.registerEvent("Resource Pack Request Action", EvtResourcePackAction.class, PlayerResourcePackStatusEvent.class,
				"resource pack [request] action",
				"resource pack [request] %resourcepackactions%")
				.description("Called when a player takes action on a resource pack request sent via the ",
						"<a href='effects.html#EffSendResourcePack'>send resource pack</a> effect. ",
						"The <a href='expressions.html#ExprEventAction'>event action</a> expression can be used ",
						"to get the resource pack action.",
						"",
						"This event will be triggered once when the player accepts or declines the resource pack request, ",
						"and once when the resource pack is successfully installed or failed.")
				.examples("on resource pack request action:",
						"	if the action is decline or download fail:",
						"",
						"on resource pack deny:",
						"	kick the player due to \"You have to install the resource pack to play in this server!\"")
				.since("INSERT VERSION");
	}

	@Nullable
	private Literal<Status> status;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		if (matchedPattern == 1)
			status = (Literal<Status>) args[0];
		return true;
	}

	@Override
	public boolean check(final Event e) {
		if (status != null) {
			return status.check(e, new Checker<Status>() {
				@Override
				public boolean check(final Status m) {
					return ((PlayerResourcePackStatusEvent) e).getStatus().equals(m);
				}
			});
		}
		return true;
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return status != null ? "resource pack " + status.toString(e, debug) : "resource pack status";
	}

}
