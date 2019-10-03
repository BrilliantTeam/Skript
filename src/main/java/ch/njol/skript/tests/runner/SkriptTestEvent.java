package ch.njol.skript.tests.runner;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Raised by Skript when tests are run.
 */
public class SkriptTestEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
}
