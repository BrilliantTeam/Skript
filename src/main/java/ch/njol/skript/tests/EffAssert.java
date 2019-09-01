package ch.njol.skript.tests;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.EffDoIf;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;

@Name("Assert")
@Description("Assert that condition is true.")
@Examples("")
@Since("INSERT VERSION")
public class EffAssert extends Effect  {

	static {
		Skript.registerEffect(EffAssert.class, "assert <.+> with %string%");
	}

	@SuppressWarnings("null")
	private Condition condition;
	
	@SuppressWarnings("null")
	private Expression<String> errorMsg;

	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		String cond = parseResult.regexes.get(0).group();
		condition = Condition.parse(cond, "Can't understand this condition: " + cond);
		errorMsg = (Expression<String>) exprs[1];
		return condition != null;
	}

	@Override
	protected void execute(Event e) {}
	
	@Nullable
	@Override
	public TriggerItem walk(Event e) {
		if (!condition.check(e)) {
			String msg = errorMsg.getSingle(e);
			TestTracker.testFailed(msg != null ? msg : "assertation failed");
			return null;
		}
		return getNext();
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "assert " + condition.toString(e, debug);
	}
}