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
package ch.njol.skript.bukkitutil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Tracks click events to remove extraneous events for one player click.
 */
public class ClickEventTracker {
	
	private static class TrackedEvent {
		
		/**
		 * The actual event that is tracked.
		 */
		final Cancellable event;
		
		/**
		 * Hand used in event.
		 */
		final EquipmentSlot hand;

		public TrackedEvent(Cancellable event, EquipmentSlot hand) {
			this.event = event;
			this.hand = hand;
		}
		
	}
	
	/**
	 * First events by players during this tick. They're stored by their UUIDs.
	 * This map is cleared once per tick.
	 */
	final Map<UUID, TrackedEvent> firstEvents;
	
	public ClickEventTracker(JavaPlugin plugin) {
		this.firstEvents = new HashMap<>();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,
				() -> firstEvents.clear(), 1, 1);
	}
	
	/**
	 * Processes a click event from a player.
	 * @param player Player who caused it.
	 * @param event The event.
	 * @param hand Slot associated with the event.
	 * @return If the event should be passed to scripts.
	 */
	public boolean checkEvent(Player player, Cancellable event, EquipmentSlot hand) {
		UUID uuid = player.getUniqueId();
		TrackedEvent first = firstEvents.get(uuid);
		if (first != null && first.event != event) { // We've checked an event before, and it is not this one
			// Ignore this, but set its cancelled status based on one set to first event
			event.setCancelled(first.event.isCancelled());
			return false;
		} else { // Remember and run this
			firstEvents.put(uuid, new TrackedEvent(event, hand));
			return true;
		}
	}
}
