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
package ch.njol.skript.expressions;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Last Death")
@Description({"The time of the last death of a player.", 
				"A change to this value can't be negative, so it will be reset or remain unchanged."})
@Examples("send \"Your last death was %last death of player%!\" to player")
@Since("INSERT VERSION")
public class ExprLastDeath extends SimplePropertyExpression<Player, Date> {

	private final Statistic LAST_DEATH = Statistic.TIME_SINCE_DEATH;

	static {
		Skript.registerExpression(ExprLastDeath.class, Date.class, ExpressionType.PROPERTY, 
				"[the] [time of [the]] last death of %players%",
				"[the] [time of [the]] %players%'[s] last death"
		);
		register(ExprLastDeath.class, Date.class, "[the] last death", "players");
	}

	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<Player>) exprs[0]);
		return true;
	}

	@Nullable
	@Override
	public Date convert(Player player) {
		Date date = new Date();
		date.subtract(Timespan.fromTicks_i(player.getStatistic(LAST_DEATH)));
		return date;
	}

	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.DELETE || mode == ChangeMode.REMOVE_ALL)
			return null;
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Date.class);
		return CollectionUtils.array(Timespan.class);
	}

	@SuppressWarnings("null")
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null && mode != ChangeMode.RESET)
			return;
		Date now = new Date();
		for (Player player : getExpr().getArray(e)) {
			if (mode == ChangeMode.ADD) {

				Date deathTime = convert(player);
				long add = ((Timespan) delta[0]).getTicks_i();
				deathTime.add(Timespan.fromTicks_i(add));
				player.setStatistic(LAST_DEATH, (int) deathTime.difference(now).getTicks_i());

			} else if (mode == ChangeMode.REMOVE) {

				Date deathTime = convert(player);
				long remove = ((Timespan) delta[0]).getTicks_i();
				deathTime.subtract(Timespan.fromTicks_i(remove));
				player.setStatistic(LAST_DEATH, (int) deathTime.difference(now).getTicks_i());

			} else if (mode == ChangeMode.SET) {

				/*
				 * Since the statistic is actually the time since the last death of the player,
				 * it needs to be set to the difference between now and the new date.
				 * For example, if we were setting the player's last death to 1 day ago,
				 * the statistic would need to be 24 hours, and that's what this would give us.
				 * 
				 * If the new date is in the future, the statistic will remain unchanged.
				 */
				Date newDate = ((Date) delta[0]);
				if (newDate.compareTo(now) < 1)
					player.setStatistic(LAST_DEATH, (int) ((Date) delta[0]).difference(now).getTicks_i());

			} else if (mode == ChangeMode.RESET) {

				player.setStatistic(LAST_DEATH, 0);

			}
		}
	}

	@Override
	public Class<? extends Date> getReturnType() {
		return Date.class;
	}

	@Override
	protected String getPropertyName() {
		return "last death";
	}

}
