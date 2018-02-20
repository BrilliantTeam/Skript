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
 */
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;


@Name("Highest Solid Block")
@Description("Returns the highest solid block at the x and z coordinates of the world of given location")
@Examples("highest block at location of arg-player")
@Since("2.2-dev34")
public class ExprHighestSolidBlock extends SimpleExpression<Block> {
	static {
		Skript.registerExpression(ExprHighestSolidBlock.class, Block.class, ExpressionType.SIMPLE, "highest [(solid|non-air)] block at %location%");
	}

	@Nullable
	private Expression<Location> loc;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		this.loc = (Expression<Location>) exprs[0];
		return true;
	}

	@SuppressWarnings("null")
	@Override
	@Nullable
	protected Block[] get(final Event e) {
		if (loc == null) return null;
		Location loc = this.loc.getSingle(e);
		return new Block[]{loc.getWorld().getHighestBlockAt(loc)};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}

	@SuppressWarnings("null")
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "highest solid block at " + loc.getSingle(e).toString();
	}
}