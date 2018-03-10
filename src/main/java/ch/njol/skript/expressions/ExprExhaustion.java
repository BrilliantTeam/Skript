package ch.njol.skript.expressions;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.classes.Changer;
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
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Player Exhaustion")
@Description("The exhaustion of a player. This is mainly used to determine the rate of hunger depletion.")
@Examples("set exhaustion of all players to 1")
@Since("2.2-dev35")
public class ExprExhaustion extends SimplePropertyExpression<Player, Number>{
	
	static {
		register(ExprExhaustion.class, Number.class, "exhaustion", "players");
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "exhaustion";
	}

	@Override
	@Nullable
	public Number convert(Player player) {
		return player.getExhaustion();
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		return CollectionUtils.array(Number.class);
	}
	
	@SuppressWarnings("null")
	@Override
	public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		Player player = getExpr().getSingle(event);
		float exhaustion = player.getExhaustion();
		switch (mode) {
			case ADD:
				player.setExhaustion(exhaustion + ((Number)delta[0]).floatValue());
				break;
			case REMOVE:
				player.setExhaustion(exhaustion - ((Number)delta[0]).floatValue());
				break;
			case SET:
				player.setExhaustion(((Number)delta[0]).floatValue());
				break;
			case DELETE:
			case REMOVE_ALL:
			case RESET:
				player.setExhaustion(0);
				break;
		}
	}

}