package ch.njol.skript.effects;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

@Name("Unsilence Entity")
@Description("Unsilence an entity.")
@Examples("unsilence target entity")
@Since("INSERT VERSION")
public class EffUnsilence extends Effect {
	
	static {
		Skript.registerEffect(EffSilence.class, "unsilence %entities%", "make %entities% (not silent|unsilent)");
	}
	
	@SuppressWarnings("null")
	private Expression<Entity> entities;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parser) {
		entities = (Expression<Entity>) exprs[0];
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		for (Entity entity : entities.getArray(e)) {
			entity.setSilent(false);
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "unsilence" + entities.toString(e, debug);
	}
}
