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
package ch.njol.skript.conditions;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptCommand;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;

@Name("Is Script Loaded")
@Description("Check if the current script or another script, is current loaded")
@Examples({"script is loaded","script \"example.sk\" is loaded"})
@Since("2.2-dev31")
public class CondScriptLoaded extends Condition{
	static {
		Skript.registerCondition(CondScriptLoaded.class, "script[s] [%-strings%] (is|are) loaded", "script[s] [%-strings%] (isn't|is not|aren't|are not) loaded");
	}
	@SuppressWarnings("null")
	private Expression<String> scripts;
	@Nullable
	private File currentScriptFile;
	
	@Override
	public boolean check(Event e) {
		if (scripts == null) {
			return ScriptLoader.getLoadedFiles().contains(currentScriptFile);
		}
		return scripts.check(e, new Checker<String>() {
			@Override
			public boolean check(String scriptName) {
				return ScriptLoader.getLoadedFiles().contains(SkriptCommand.getScriptFromName(scriptName));
			}
		}, isNegated());
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		String scriptName = scripts == null ? "script" : (scripts.isSingle() ? "script" : "scripts" + " " + scripts.toString(e, debug));
		boolean isSingle = scripts == null || scripts.isSingle();
		return scriptName + " " + (isSingle ? (isNegated() ? "isn't" : "is") : (isNegated() ? "aren't" : "are")) + " loaded";
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		assert ScriptLoader.currentScript != null;
		currentScriptFile = ScriptLoader.currentScript.getFile();
		scripts = (Expression<String>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}
}
