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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Getter;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

/**
 * Used to set saturation of players. Number is used in case something changes in future...
 * @author bensku
 * 
 * Cleaned up by LimeGlass (2.2-dev35)
 */
@Name("Saturation")
@Description("The saturation of player.")
@Examples("set saturation of player to 20 #Full hunger")
@Since("2.2-Fixes-v10, 2.2-dev35 (Made 'saturation' work in player events & changers added)")
public class ExprSaturation extends PropertyExpression<Player, Number> {

	static {
		Skript.registerExpression(ExprSaturation.class, Number.class, ExpressionType.PROPERTY, "[the] saturation [of %players%]", "%players%['s] saturation");
	}
	
	@Override
	public Class<Number> getReturnType() {
		return Number.class;
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		setExpr((Expression<? extends Player>) exprs[0]);
		return true;
	}
	
	@Override
	protected Number[] get(final Event event, final Player[] source) {
		return get(source, new Getter<Float, Player>() {
			@Override
			public Float get(final Player player) {
				return player.getSaturation();
			}
		});
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		return CollectionUtils.array(Number.class);
	}
	
	@SuppressWarnings("null")
	@Override
	public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		float value = ((Number)delta[0]).floatValue();
		switch (mode) {
			case ADD:
				for (Player player : getExpr().getArray(event))
					player.setSaturation(player.getSaturation() + value);
				break;
			case REMOVE:
				for (Player player : getExpr().getArray(event))
					player.setSaturation(player.getSaturation() - value);
				break;
			case SET:
				for (Player player : getExpr().getArray(event))
					player.setSaturation(value);
				break;
			case DELETE:
			case REMOVE_ALL:
			case RESET:
				for (Player player : getExpr().getArray(event))
					player.setSaturation(0);
				break;
		}
	}
	
	@Override
	public String toString(final @Nullable Event event, final boolean debug) {
		return "saturation" + (getExpr().isDefault() ? "" : " of " + getExpr().toString(event, debug));
	}
	
}