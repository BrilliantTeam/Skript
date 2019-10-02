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

@Name("Silence Entity")
@Description("Makes an entity silent.")
@Examples("make target entity silent")
@Since("INSERT VERSION")
public class EffSilence extends Effect {
	
	static {
		Skript.registerEffect(EffSilence.class, "silence %entities%", "make %entities% silent");
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
			entity.setSilent(true);
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "silence" + entities.toString(e, debug);
	}
}
