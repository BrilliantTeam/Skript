/**
 *  This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.WeatherType;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Nicofisi
 * @author Peter Güttinger
 */
@Name("Player Weather")
@Description("The weather for a player.")
@Examples({"set weather for arg-player to clear",
		"arg-player's weather is rainy"})
@Since("2.2-dev34")
public class ExprPlayerWeather extends PropertyExpression<Player, WeatherType> {
	static {
		Skript.registerExpression(ExprPlayerWeather.class, WeatherType.class, ExpressionType.PROPERTY,
				"[the] weather [(for|of) %player%]", "%player%'[s] weather");
	}

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		setExpr((Expression<Player>) exprs[0]);
		return true;
	}

	@Override
	protected WeatherType[] get(final Event e, final Player[] source) {
		return get(source, new Getter<WeatherType, Player>() {
			@Override
			public WeatherType get(final Player p) {
				return WeatherType.fromPlayer(p);
			}
		});
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the weather for " + getExpr().toString(e, debug);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.RESET || mode == ChangeMode.SET)
			return CollectionUtils.array(WeatherType.class);
		return null;
	}

	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		final WeatherType t = delta == null ? WeatherType.CLEAR : (WeatherType) delta[0];
		for (final Player p : getExpr().getArray(e)) {
			p.setPlayerWeather(t.toBukkit());
		}
	}

	@Override
	public Class<WeatherType> getReturnType() {
		return WeatherType.class;
	}
}