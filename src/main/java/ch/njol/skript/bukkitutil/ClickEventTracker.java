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
package ch.njol.skript.bukkitutil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import ch.njol.skript.Skript;
import ch.njol.skript.effects.EffCancelEvent;
import ch.njol.skript.util.Task;

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
		@SuppressWarnings("unused")
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
	
	/**
	 * Events that have been cancelled with {@link EffCancelEvent}.
	 */
	private final Set<Cancellable> modifiedEvents;
	
	public ClickEventTracker(JavaPlugin plugin) {
		this.firstEvents = new HashMap<>();
		this.modifiedEvents = new HashSet<>();
		new Task(Skript.getInstance(), 1, 1) {
			@Override
			public void run() {
				firstEvents.clear();
				modifiedEvents.clear();
			}
		};
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
			if (!modifiedEvents.contains(first.event)) {
				// Do not modify cancellation status of event, Skript did not touch it
				// This avoids issues like #2389
				return false;
			}
			
			// Ignore this, but set its cancelled status based on one set to first event
			if (event instanceof PlayerInteractEvent) { // Handle use item/block separately
				// Failing to do so caused issue SkriptLang/Skript#2303
				PlayerInteractEvent firstClick = (PlayerInteractEvent) first.event;
				PlayerInteractEvent click = (PlayerInteractEvent) event;
				click.setUseInteractedBlock(firstClick.useInteractedBlock());
				click.setUseItemInHand(firstClick.useItemInHand());
			} else {
				event.setCancelled(first.event.isCancelled());
			}
			return false;
		} else { // Remember and run this
			firstEvents.put(uuid, new TrackedEvent(event, hand));
			return true;
		}
	}
	
	/**
	 * Records that given event was cancelled or uncancelled.
	 * @param event The event.
	 */
	public void eventModified(Cancellable event) {
		modifiedEvents.add(event);
	}
}
