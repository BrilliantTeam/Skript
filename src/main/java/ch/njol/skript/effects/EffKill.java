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
package ch.njol.skript.effects;

import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Kill")
@Description({"Kills an entity.",
		"Note: This effect does not set the entitie's health to 0 (which causes issues), but damages the entity by 100 times its maximum health."})
@Examples({"kill the player",
		"kill all creepers in the player's world",
		"kill all endermen, witches and bats"})
@Since("1.0")
public class EffKill extends Effect {
	static {
		Skript.registerEffect(EffKill.class, "kill %entities%", 
				      "kill %entities% without drops");
	}
	
	// Absolutely make sure it dies
	public static final int DAMAGE_AMOUNT = Integer.MAX_VALUE;
	
	@SuppressWarnings("null")
	private Expression<Entity> entities;
	private boolean withoutDrops;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		entities = (Expression<Entity>) vars[0];
		withoutDrops = matchedPattern == 1;
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		for (Entity entity : entities.getArray(e)) {

 
			if (entity instanceof EnderDragonPart) {
				entity = ((EnderDragonPart) entity).getParent();
			}

			// Some entities cannot take damage but should be killable
			if (withoutDrops || (entity instanceof Vehicle && !(entity instanceof Pig || entity instanceof AbstractHorse)) 
				|| entity instanceof ArmorStand || entity instanceof EnderDragon || !(entity instanceof Damageable)) {
				entity.remove(); // Got complaints in issue tracker, so this is possible... Not sure if good idea, though!
			} else if (entity instanceof Damageable) {
				final boolean creative = entity instanceof Player && ((Player) entity).getGameMode() == GameMode.CREATIVE;
				if (creative) // Set player to survival before applying damage
					((Player) entity).setGameMode(GameMode.SURVIVAL);
				
				assert entity != null;
				HealthUtils.damage((Damageable) entity, HealthUtils.getMaxHealth((Damageable) entity) * 100); // just to make sure that it really dies >:)
				
				if (creative) // Set creative player back to creative
					((Player) entity).setGameMode(GameMode.CREATIVE);
			}
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "kill " + entities.toString(e, debug) + (withoutDrops ? " without drops" : "");
	}
	
}
