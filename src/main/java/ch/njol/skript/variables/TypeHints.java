package ch.njol.skript.variables;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This is used to manage local variable type hints.
 * 
 * <ul>
 * <li>EffChange adds then when local variables are set
 * <li>Variable checks them when parser tries to create it
 * <li>ScriptLoader clears hints after each section has been parsed
 * </ul>
 */
public class TypeHints {
	
	private static final Map<String, Class<?>> typeHints = new HashMap<>();
	
	public static void add(String variable, Class<?> hint) {
		if (!hint.equals(Object.class))
			typeHints.put(variable, hint);
	}
	
	@Nullable
	public static Class<?> get(String variable) {
		return typeHints.get(variable);
	}
	
	public static void clear() {
		typeHints.clear();
	}
}
