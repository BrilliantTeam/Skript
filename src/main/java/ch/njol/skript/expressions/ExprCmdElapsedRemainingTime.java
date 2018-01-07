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
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.UUID;

@Name("Elapsed/Remaining Time")
@Description({"Only usable in command events. The elapsed remaining time until the command may be executed again as per the cooldown."})
@Examples({
        "command /home:",
        "\tcooldown: 10 seconds",
        "\tcooldown message: You last teleported home %elapsed time% ago, you may teleport home again in %remaining time%",
        "\ttrigger:",
        "\t\tteleport player to {home::%player%}"
})
@Since("INSERT VERSION")
public class ExprCmdElapsedRemainingTime extends SimpleExpression<Timespan> {

    static {
        Skript.registerExpression(ExprCmdElapsedRemainingTime.class, Timespan.class, ExpressionType.SIMPLE,
                "[the] (0¦remaining|1¦elapsed) time[span] [of] [the] [(cooldown|wait)] [((of|for)[the] [currnet] command)]");
    }

    private boolean remaining;

    @Override
    @Nullable
    protected Timespan[] get(Event e) {
        if (!(e instanceof ScriptCommandEvent)) {
            return null;
        }
        ScriptCommandEvent event = ((ScriptCommandEvent) e);
        ScriptCommand scriptCommand = event.getSkriptCommand();
        if (scriptCommand.getCooldown() == null) {
            return null;
        }
        if (!(event.getSender() instanceof Player)) {
            return null;
        }
        UUID uuid = ((Player) event.getSender()).getUniqueId();
        long ms = remaining ? scriptCommand.getRemainingMilliseconds(uuid) :
                scriptCommand.getElapsedMilliseconds(uuid);
        return new Timespan[]{new Timespan(ms)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Timespan> getReturnType() {
        return Timespan.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "the remaining time";
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (!ScriptLoader.isCurrentEvent(ScriptCommandEvent.class)) {
            Skript.error("The expression 'remaining time' can only be used within a command", ErrorQuality.SEMANTIC_ERROR);
            return false;
        }
        remaining = parseResult.mark == 0;
        return true;
    }
}
