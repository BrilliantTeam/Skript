package ch.njol.skript.effects;

import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.FireworkMeta;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Launch firework")
@Description("Leash firework effects at the given location(s).")
@Examples("leash the player to the target entity")
@Since("INSERT VERSION")
public class EffFireworkLaunch extends Effect {
	
	static {
		Skript.registerEffect(EffFireworkLaunch.class, "(launch|deploy) [[a] firework [with effect[s]]] %fireworkeffects% at %locations% [(with duration|timed) %number%]");
	}

	@SuppressWarnings("null")
	private Expression<FireworkEffect> effects;
	@SuppressWarnings("null")
	private Expression<Location> locations;
	@SuppressWarnings("null")
	private Expression<Number> lifetime;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		effects = (Expression<FireworkEffect>) exprs[0];
		locations = (Expression<Location>) exprs[1];
		lifetime = (Expression<Number>) exprs[2];
		return true;
	}

	@Override
	protected void execute(Event e) {
		Number power = lifetime.getSingle(e);
		if (power == null)
			power = 1;
		for (Location location : locations.getArray(e)){
			Firework firework = location.getWorld().spawn(location, Firework.class);
			FireworkMeta meta = firework.getFireworkMeta();
			meta.addEffects(effects.getArray(e));
			meta.setPower(power.intValue());
			firework.setFireworkMeta(meta);
		}
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "Launch firework " + effects.toString(e, debug) + " at " + locations.toString(e, debug);
	}
	
}
