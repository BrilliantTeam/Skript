package ch.njol.skript.events.bukkit;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.Config;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * If {@link ScriptLoader#callPreLoadEvent} is true,
 * this event is called before a script starts
 * loading via {@link ScriptLoader#loadScript(Config)}
 * or one of it's overloads.
 */
public class PreScriptLoadEvent extends Event {

    private static HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
