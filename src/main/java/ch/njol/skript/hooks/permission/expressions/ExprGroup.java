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
package ch.njol.skript.hooks.permission.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.hooks.VaultHook;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Name("Group")
@Description("The primary group or all groups of a player. This expression requires Vault and a compatible permissions plugin to be installed.")
@Examples({"on join:",
            "broadcast \"%group of player%\" # this is the player's primary group",
            "broadcast \"%groups of player%\" # this is all of the player's groups"})
@Since("INSERT VERSION")
public class ExprGroup extends PropertyExpression<Player, String> {

    static {
        register(ExprGroup.class, String.class, "group[(1¦s)]", "players");
    }

    private boolean primary;

    @SuppressWarnings("null")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<Player>) exprs[0]);
        primary = parseResult.mark == 0;
        return true;
    }

    @SuppressWarnings("null")
    @Override
    protected String[] get(Event e, Player[] players) {
        List<String> groups = new ArrayList<>();
        for (Player player : players) {
            if (primary)
                groups.add(VaultHook.permission.getPrimaryGroup(player));
            else
                Collections.addAll(groups, VaultHook.permission.getPlayerGroups(player));
        }
        return groups.toArray(new String[groups.size()]);
    }

    @SuppressWarnings("null")
    @Override
    public boolean isSingle() {
        return super.isSingle() && primary;
    }

    @SuppressWarnings("null")
    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @SuppressWarnings("null")
    @Override
    public String toString(Event e, boolean debug) {
        return "group" + (primary ? "" : "s") + " of " + getExpr().toString(e, debug);
    }
 
}
