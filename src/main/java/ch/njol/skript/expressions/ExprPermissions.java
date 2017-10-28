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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.common.collect.Lists;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * @author Tassu_
 */
@Name("All Permissions")
@Description("Returns all permissions of a player.")
@Since("2.2-dev33")
public class ExprPermissions extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprPermissions.class, String.class, ExpressionType.PROPERTY, "[all] permission[s] of %player%", "%player%['s] [all] permission[s]");
    }

    @SuppressWarnings("null")
    private Expression<Player> player;

    @Override
    @SuppressWarnings({"unchecked", "null"})
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        player = (Expression<Player>)exprs[0];
        return true;
    }

    @Override
    @SuppressWarnings("null")
    protected String[] get(Event e) {
        Player p = player.getSingle(e);

        if (p == null){
            return null;
        }

        List<String> perms = Lists.newArrayList();

        for (PermissionAttachmentInfo permissionAttachmentInfo : p.getEffectivePermissions()) {
            perms.add(permissionAttachmentInfo.getPermission());
        }

        return perms.toArray(new String[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(final @Nullable Event event, boolean debug) {
        return "all permissions of " + player.toString();
    }

}
