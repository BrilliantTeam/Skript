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
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.Nullable;

public class EvtMove extends SkriptEvent {

	private static final boolean HAS_ENTITY_MOVE = Skript.classExists("io.papermc.paper.event.entity.EntityMoveEvent");

	static {
		Class<? extends Event>[] events;
		if (HAS_ENTITY_MOVE)
			events = CollectionUtils.array(PlayerMoveEvent.class, EntityMoveEvent.class);
		else
			events = CollectionUtils.array(PlayerMoveEvent.class);
		Skript.registerEvent("Move / Rotate", EvtMove.class, events,
				"%entitydata% (move|walk|step|rotate:(look[ing] around|rotate))",
				"%entitydata% (move|walk|step) or (look[ing] around|rotate)")
				.description(
						"Called when a player or entity moves or rotates their head.",
						"NOTE: Move event will only be called when the entity/player moves position, not orientation (ie: looking around). Use the keyword 'rotate' instead.",
						"NOTE: These events can be performance heavy as they are called quite often.",
						"If you use these events, and later remove them, a server restart is recommended to clear registered events from Skript.")
				.examples(
						"on player move:",
							"\tif player does not have permission \"player.can.move\":",
								"\t\tcancel event",
						"on skeleton move:",
							"\tif event-entity is not in world \"world\":",
								"\t\tkill event-entity",
						"on player rotate:",
							"send action bar \"You are currently looking around!\" to player")
				.requiredPlugins("Paper 1.16.5+ (entity move)")
				.since("2.6, INSERT VERSION (rotate)");
	}

	private EntityData<?> type;
	private boolean isPlayer;
	private boolean isRotate;
	private Move moveType;

	private enum Move {

		POSITION("move"),
		ORIENTATION("rotate"),
		POSITION_OR_ORIENTATION("move or rotate");

		private final String toString;
		Move(String toString) {
			this.toString = toString;
		}

		public String getString() {
			return toString;
		}

	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		type = ((Literal<EntityData<?>>) args[0]).getSingle();
		if (!HAS_ENTITY_MOVE && !isPlayer) {
			Skript.error("Entity move event requires Paper 1.16.5+");
			return false;
		}
		isPlayer = Player.class.isAssignableFrom(type.getType());
		if (matchedPattern == 1) {
			moveType = Move.POSITION_OR_ORIENTATION;
		} else if (parseResult.hasTag("rotate")) {
			moveType = Move.ORIENTATION;
		} else {
			moveType = Move.POSITION;
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		Location from, to;
		if (isPlayer && event instanceof PlayerMoveEvent) {
			PlayerMoveEvent playerEvent = (PlayerMoveEvent) event;
			from = playerEvent.getFrom();
			to = playerEvent.getTo();
		} else if (HAS_ENTITY_MOVE && event instanceof EntityMoveEvent) {
			EntityMoveEvent entityEvent = (EntityMoveEvent) event;
			from = entityEvent.getFrom();
			to = entityEvent.getTo();
		} else {
			return false;
		}
		switch (moveType) {
			case POSITION:
				return hasChangedPosition(from, to);
			case ORIENTATION:
				return hasChangedOrientation(from, to);
			case POSITION_OR_ORIENTATION:
				return true;
		}
		return false;
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public Class<? extends Event> [] getEventClasses() {
		if (isPlayer) {
			return new Class[] {PlayerMoveEvent.class};
		} else if (HAS_ENTITY_MOVE) {
			return new Class[] {EntityMoveEvent.class};
		}
		return null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return type + " " + moveType.getString();
	}

	private static boolean hasChangedPosition(Location from, Location to) {
		return from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ() || from.getWorld() != to.getWorld();
	}

	private static boolean hasChangedOrientation(Location from, Location to) {
		return from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch();
	}

}
