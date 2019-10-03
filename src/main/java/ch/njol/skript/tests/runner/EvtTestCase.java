package ch.njol.skript.tests.runner;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;

public class EvtTestCase extends SkriptEvent {
	
	static {
		if (TestMode.ENABLED)
			Skript.registerEvent("Test Case", EvtTestCase.class, PlayerEditBookEvent.class, "test %string%")
				.description("Contents represent one test case.")
				.examples("")
				.since("INSERT VERSION");
	}
	
	@SuppressWarnings("null")
	private Expression<String> name;
	
	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
		name = (Expression<String>) args[0];
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		String n = name.getSingle(e);
		if (n == null) {
			return false;
		}
		TestTracker.testStarted(n);
		return true;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (e != null)
			return "test " + name.getSingle(e);
		return "test case";
	}
}
