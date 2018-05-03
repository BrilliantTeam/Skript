package ch.njol.skript.conditions;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;

public class CondStartsWith extends Condition {

	static {
		Skript.registerCondition(CondStartsWith.class,
				"%strings% (start|1¦end)[s] with %string%",
				"%strings% do[es](n't| not) (start|1¦end) with %string%");
	}

	@SuppressWarnings("null")
	private Expression<String> strings;
	@SuppressWarnings("null")
	private Expression<String> prefix;
	private boolean ends;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		strings = (Expression<String>) exprs[0];
		prefix = (Expression<String>) exprs[1];
		ends = parseResult.mark == 1;
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event e) {
		String p = prefix.getSingle(e);

		if (p == null)
			return false;

		return strings.check(e, new Checker<String>() {
			@Override
			public boolean check(String s) {
				return ends ? s.startsWith(p) : s.endsWith(p);
			}
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return strings.toString(e, debug) + (isNegated() ? " don't " : " ") + (ends ? "start" : "end") +  " with " + prefix.toString(e, debug);
	}


}
