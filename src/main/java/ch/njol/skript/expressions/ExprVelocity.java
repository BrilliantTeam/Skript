package ch.njol.skript.expressions;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;


@Name("Vectors - Velocity")
@Description("Gets, sets, adds or removes velocity to/from/of an entity")
@Examples({"set player's velocity to {_v}"})
@Since("INSERT VERSION")
public class ExprVelocity extends SimplePropertyExpression<Entity, Vector> {
	static {
		Skript.registerExpression(ExprVelocity.class, Vector.class, ExpressionType.PROPERTY, "(velocity|acceleration) of %entity%", "%entity%'s (velocity|acceleration)");
	}

	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		super.init(exprs, matchedPattern, isDelayed, parseResult);
		
		return true;
	}

	@Override
	protected String getPropertyName() {
		return "velocity";
	}

	@Override
	public Class<Vector> getReturnType() {
		return Vector.class;
	}

	@Override
	@SuppressWarnings("null")
	public Class<?>[] acceptChange(final Changer.ChangeMode mode) {
		if ((mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.REMOVE || mode == Changer.ChangeMode.DELETE) && getExpr().isSingle() && Changer.ChangerUtils.acceptsChange(getExpr(), Changer.ChangeMode.SET, Vector.class))
			return new Class[] { Number.class };
		return null;
	}

	@Override
	@SuppressWarnings("null")
	public void change(final Event e, final @Nullable Object[] delta, final Changer.ChangeMode mode) throws UnsupportedOperationException {
		Entity ent = getExpr().getSingle(e);
		if (ent == null)
			return;
		switch(mode){
			case ADD:
				ent.setVelocity(ent.getVelocity().add((Vector) delta[0]));
				break;
			case DELETE:
				ent.setVelocity(new Vector());
				break;
			case REMOVE:
				ent.setVelocity(ent.getVelocity().subtract((Vector) delta[0]));
				break;
			case REMOVE_ALL:
				break;
			case RESET:
				break;
			case SET:
				ent.setVelocity((Vector) delta[0]);
				break;
		}
	}

	@Override
	@Nullable
	public Vector convert(Entity e) {
		return e.getVelocity();
	}

}