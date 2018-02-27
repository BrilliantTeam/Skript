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

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Flight Mode")
@Description("Whether the player is allowed to fly. Use <a href=effects.html#EffMakeFly>Make Fly</a> effect to force a player fly.")
@Examples({"set flight mode of player to true", "send \"%flying state of all players%\""})
@Since("2.2-dev34")
public class ExprFlightMode extends PropertyExpression<Player, Boolean> {

    static {
        PropertyExpression.register(ExprFlightMode.class, Boolean.class, "fl(y[ing]|ight) (mode|state)", "players");
    }

    @SuppressWarnings({"null", "unchecked"})
	@Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<? extends Player>) exprs[0]);
        return true;
    }

    @Override
    protected Boolean[] get(Event e, Player[] source) {
        return get(source, (player) -> player.getAllowFlight());
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "flight mode of " + getExpr().toString(e, debug);
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(Changer.ChangeMode mode) {

        if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.RESET) {
            return CollectionUtils.array(Boolean.class);
        }
        return null;
    }

    @Override
    @SuppressWarnings("null")
    public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {

        Boolean state = mode == Changer.ChangeMode.RESET || delta[0] == null ? false : (Boolean) delta[0];

        for (Player player : getExpr().getArray(e)) {
            player.setAllowFlight(state);
        }
    }

    @Override
    public Class<? extends Boolean> getReturnType() {
        return Boolean.class;
    }


}
