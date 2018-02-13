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

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.command.ScriptCommandEvent;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.UUID;

@Name("Cooldown Time/Remaining Time/Elapsed Time/Cooldown Bypass Permission")
@Description({"Only usable in command events. Represents the cooldown time, the remaining time, or elapsed time (time since last execution), or the cooldown bypass permission."})
@Examples({
        "command /home:",
        "\tcooldown: 10 seconds",
        "\tcooldown message: You last teleported home %elapsed time% ago, you may teleport home again in %remaining time%",
        "\ttrigger:",
        "\t\tteleport player to {home::%player%}"
})
@Since("2.2-dev33")
public class ExprCmdElapsedRemainingTime extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprCmdElapsedRemainingTime.class, Object.class, ExpressionType.SIMPLE,
                "[the] (0¦remaining|1¦elapsed|2¦cooldown|3¦cooldown bypass perm[ission]) [time][ ][span] [of] [the] [(cooldown|wait)] [((of|for)[the] [current] command)]");
    }

    private int mark;

    @Override
    @Nullable
    protected Object[] get(Event e) {
        if (!(e instanceof ScriptCommandEvent)) {
            return null;
        }
        ScriptCommandEvent event = ((ScriptCommandEvent) e);
        ScriptCommand scriptCommand = event.getSkriptCommand();

        CommandSender sender = event.getSender();
        if (scriptCommand.getCooldown() == null || !(sender instanceof Player)) {
            return null;
        }
        Player player = (Player) event.getSender();
        UUID uuid = player.getUniqueId();

        switch (mark) {
            case 0:
            case 1:
                long ms = mark != 1
                        ? scriptCommand.getRemainingMilliseconds(uuid)
                        : scriptCommand.getElapsedMilliseconds(uuid);
                return new Timespan[]{new Timespan(ms)};
            case 2:
                return new Timespan[]{scriptCommand.getCooldown()};
            case 3:
                return new String[]{scriptCommand.getCooldownBypass()};
        }

        return null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<?> getReturnType() {
        return mark <= 2 ? Timespan.class : String.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "the " + getExpressionName();
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        mark = parseResult.mark;
        if (!ScriptLoader.isCurrentEvent(ScriptCommandEvent.class)) {
            Skript.error("The expression '" + getExpressionName() + " time' can only be used within a command", ErrorQuality.SEMANTIC_ERROR);
            return false;
        }
        return true;
    }

    @Nullable
    private String getExpressionName() {
        switch (mark) {
            case 0:
                return "remaining time";
            case 1:
                return "elapsed time";
            case 2:
                return "cooldown";
            case 3:
                return "cooldown bypass permission";
        }
        return null;
    }
}
