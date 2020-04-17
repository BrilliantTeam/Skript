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

@Name("Last Death Time")
@Description({"The time of the last death of a player.", 
				"Any changes that would result in the death time being in the future are ignored.",
				"This expression affects the 'TIME_SINCE_DEATH' statistic."})
@Examples("send \"Your last death was %last death of player%!\" to player")
@Since("INSERT VERSION")
public class ExprLastDeathTime extends SimplePropertyExpression<Player, Date> {

	static {
		Skript.registerExpression(ExprLastDeathTime.class, Date.class, ExpressionType.PROPERTY, 
				"[the] time of [the] last death of %players%",
				"[the] time of [the] %players%'[s] last death",
				"%players%'[s] last death time",
				"[the] last death time of %players%"
		);
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
		long ticksSinceDeath = player.getStatistic(Statistic.TIME_SINCE_DEATH);
		if (ticksSinceDeath < 0) 
			return null;
		Timespan timeSinceDeath = Timespan.fromTicks_i(ticksSinceDeath);
		return Date.now().minus(timeSinceDeath);
	}

	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		// Use a date for setting (e.g. 'set last death time of player to 1 hour ago')
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Date.class);
		// Use a timespan for adding/remove (e.g 'add 10 seconds to the time of the player's last death')
		if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE)
			return CollectionUtils.array(Timespan.class);
		return null;
	}

	/*
	 * Date#compare(date)
	 * This method returns whether the passed date is
	 * before, the same as, or after the date it's being compared to.
	 * A value less than 0 indicates that the passed date is AFTER the date it's being compared to.
	 * A value of 0 indicates that the passed date is the SAME the date it's being compared to.
	 * A value greater than 0 indicates that passed date is BEFORE the date it's being compared to.
	 * 
	 * We use this method because we don't want the new death date to be in the future
	 */
	@SuppressWarnings("null")
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {

		if (delta == null)
			return;

		Date now = Date.now();

		for (Player player : getExpr().getArray(e)) {
			if (mode == ChangeMode.ADD) {

				// Get the current death time of the player.
				Date deathTime = convert(player);

				deathTime.add((Timespan) delta[0]);

				if (deathTime.compareTo(now) < 1) {
					// Get the timespan representing the new time since death (in ticks).
					long newTimespanInTicks= deathTime.difference(now).getTicks_i();

					/*
					 *  Convert to int.
					 *  If it is greater than the max value for an integer,
					 *  the user probably wants to set it to the max value, right?
					 */
					int newTimeSinceDeath = newTimespanInTicks < Integer.MAX_VALUE ? (int) newTimespanInTicks : Integer.MAX_VALUE;

					player.setStatistic(Statistic.TIME_SINCE_DEATH, newTimeSinceDeath);
				}

			} else if (mode == ChangeMode.REMOVE) {

				// Get the current death time of the player.
				Date deathTime = convert(player);

				deathTime.subtract((Timespan) delta[0]);
				
				// Get the timespan representing the new time since death (in ticks).
				long newTimespanInTicks= deathTime.difference(now).getTicks_i();

				/*
				 *  Convert to int.
				 *  If it is greater than the max value for an integer,
				 *  the user probably wants to set it to the max value, right?
				 */
				int newTimeSinceDeath = newTimespanInTicks < Integer.MAX_VALUE ? (int) newTimespanInTicks : Integer.MAX_VALUE;

				player.setStatistic(Statistic.TIME_SINCE_DEATH, newTimeSinceDeath);

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

				if (newDate.compareTo(now) < 1) {
					long newTimespanInTicks = ((Date) delta[0]).difference(now).getTicks_i();
					int newTimeSinceDeath = newTimespanInTicks < Integer.MAX_VALUE ? (int) newTimespanInTicks : Integer.MAX_VALUE;
					player.setStatistic(Statistic.TIME_SINCE_DEATH, newTimeSinceDeath);
				}

			}
		}
	}

	@Override
	public Class<? extends Date> getReturnType() {
		return Date.class;
	}

	@Override
	protected String getPropertyName() {
		return "last death time";
	}

}
