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
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;

/**
 * Tracks click events to remove extraneous events for one player click.
 */
public class ClickEventTracker {
	
	/**
	 * First events by players during this tick. They're stored by their UUIDs.
	 * This map is cleared once per tick.
	 */
	final Map<UUID, Cancellable> firstEvents;
	
	public ClickEventTracker(JavaPlugin plugin) {
		this.firstEvents = new HashMap<>();
		Bukkit.getPluginManager().registerEvent(ServerTickEndEvent.class, new Listener() {}, EventPriority.MONITOR, new EventExecutor() {
			
			@Override
			public void execute(Listener listener, Event event) throws EventException {
				firstEvents.clear(); // Clear all tracked data at end of tick
			}
		}, plugin);
	}
	
	/**
	 * Processes a click event from a player.
	 * @param player Player who caused it.
	 * @param event The event.
	 * @return If the event should be passed to scripts.
	 */
	public boolean checkEvent(Player player, Cancellable event) {
		UUID uuid = player.getUniqueId();
		Cancellable first = firstEvents.get(uuid);
		if (first != null && first != event) { // We've checked an event before, and it is not this one
			// So ignore this, but set its cancelled status based on one set to first event
			event.setCancelled(first.isCancelled());
			return false;
		} else { // Remember and run this
			firstEvents.put(uuid, event);
			return true;
		}
	}
}
