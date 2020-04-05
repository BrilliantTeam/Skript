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

import java.util.ArrayList;
import java.util.List;

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
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Time Since")
@Description("The time that has passed since a date. This will return 0 seconds if the given date is in the future.")
@Examples("send \"You died %time since last death of player% ago!\" to player")
@Since("INSERT VERSION")
public class ExprTimeSince extends SimpleExpression<Timespan> {

	static {
		Skript.registerExpression(ExprTimeSince.class, Timespan.class, ExpressionType.SIMPLE, "time since %dates%");
	}

	@SuppressWarnings("null")
	private Expression<Date> dates;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		dates = (Expression<Date>) exprs[0];
		return true;
	}

	@Override
	@Nullable
	protected Timespan[] get(Event e) {
		List<Timespan> timespans = new ArrayList<>();
		Date now = new Date();
		for (Date date : dates.getArray(e))
			if (date.compareTo(now) > 0) {
				timespans.add(new Timespan());
			} else {
				timespans.add(date.difference(now));
			}
		return timespans.toArray(new Timespan[0]);
	}

	@Override
	public boolean isSingle() {
		return dates.isSingle();
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "time since " + dates.toString(e, debug);
	}

}
