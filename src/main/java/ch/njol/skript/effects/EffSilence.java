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
package ch.njol.skript.effects;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

@Name("Silence Entity")
@Description("Makes an entity silent/unsilent.")
@Examples("make target entity silent")
@Since("INSERT VERSION")
public class EffSilence extends Effect {
	
	static {
		Skript.registerEffect(EffSilence.class,
			"silence %entities%",
			"unsilence %entities%",
			"make %entities% silent",
			"make %entities% (not silent|unsilent)");
	}
	
	@SuppressWarnings("null")
	private Expression<Entity> entities;
	private boolean silence;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parser) {
		entities = (Expression<Entity>) exprs[0];
		silence = matchedPattern % 2 == 0;
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		for (Entity entity : entities.getArray(e)) {
			entity.setSilent(silence);
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return (silence ? "silence" : "unsilence") + entities.toString(e, debug);
	}
}
