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

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Event Action")
@Description("Global action expression for various events.")
@Events("resource pack request action")
@Since("INSERT VERSION")
public class ExprEventAction extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprEventAction.class, Object.class, ExpressionType.SIMPLE, "[the] [event-]action");
	}

	private enum EventType {
		RESOURCE_PACK_STATUS
	}

	@SuppressWarnings("null")
	private EventType event;

	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		if (ScriptLoader.isCurrentEvent(PlayerResourcePackStatusEvent.class)) {
			event = EventType.RESOURCE_PACK_STATUS;
		} else {
			Skript.error("The " + ScriptLoader.getCurrentEventName() + " event doesn't have an action");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected Object[] get(final Event e) {
		switch (event) {
			case RESOURCE_PACK_STATUS:
				return CollectionUtils.array(((PlayerResourcePackStatusEvent) e).getStatus());
		}
		return null;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		switch (event) {
			case RESOURCE_PACK_STATUS:
				return PlayerResourcePackStatusEvent.Status.class;
		}
		throw new AssertionError();
	}

	@Override
	public String toString(@Nullable final Event e, final boolean debug) {
		switch (event) {
			case RESOURCE_PACK_STATUS:
				return "the resource pack request action";
		}
		throw new AssertionError();
	}

}
