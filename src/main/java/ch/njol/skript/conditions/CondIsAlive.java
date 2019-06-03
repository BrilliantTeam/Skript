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
package ch.njol.skript.conditions;

import org.bukkit.entity.Entity;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Is Alive/Available")
@Description("Checks whether an entity is alive/available. Using 'alive' for non-living entities works too.")
@Examples({"if {villager-buddy::%player's uuid%} is not dead:",
	"",
	"on shoot:",
	"\twhile the projectile is available:"})
@Since("2.0, INSERT VERSION (non-living entity support)")
public class CondIsAlive extends PropertyCondition<Entity> {

	static {
		register(CondIsAlive.class, "(alive|available|valid|1¦dead|1¦unavailable|1¦invalid)", "entities");
	}

	private boolean isNegated;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isNegated = parseResult.mark == 1;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(Entity e) {
		return isNegated == e.isDead();
	}

	@Override
	protected String getPropertyName() {
		return isNegated ? "dead" : "alive";
	}

}
