/*
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
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.skript;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.command.Commands;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Conditional;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Loop;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.lang.While;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Functions.FunctionData;
import ch.njol.skript.lang.function.ScriptFunction;
import ch.njol.skript.lang.function.Signature;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.parser.ScriptManager;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.PluralizingArgsMessage;
import ch.njol.skript.log.CountingLogHandler;
import ch.njol.skript.log.ErrorDescLogHandler;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Callback;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
final public class ScriptLoader {
	private ScriptLoader() {}
	
	private final static Message m_no_errors = new Message("skript.no errors"),
			m_no_scripts = new Message("skript.no scripts");
	private final static PluralizingArgsMessage m_scripts_loaded = new PluralizingArgsMessage("skript.scripts loaded");
	
	@Nullable
	public static Config currentScript = null;
	
	/**
	 * use {@link #setCurrentEvent(String, Class...)}
	 */
	@Nullable
	private static Class<? extends Event>[] currentEvents = null;
	
	private final static Map<String, ItemType> currentAliases = new HashMap<>();
	final static HashMap<String, String> currentOptions = new HashMap<>();
	
	private static final ScriptManager manager = new ScriptManager();
	
	public static Map<String, ItemType> getScriptAliases() {
		return currentAliases;
	}
	
	/**
	 * must be synchronized
	 */
	private final static ScriptInfo loadedScripts = new ScriptInfo();
	
	public static class ScriptInfo {
		public int files, triggers, commands, functions;
		
		public ScriptInfo() {}
		
		public ScriptInfo(final int numFiles, final int numTriggers, final int numCommands, final int numFunctions) {
			files = numFiles;
			triggers = numTriggers;
			commands = numCommands;
			functions = numFunctions;
		}
		
		public void add(final ScriptInfo other) {
			files += other.files;
			triggers += other.triggers;
			commands += other.commands;
			functions += other.functions;
		}
		
		public void subtract(final ScriptInfo other) {
			files -= other.files;
			triggers -= other.triggers;
			commands -= other.commands;
			functions -= other.functions;
		}
	}
	
//	private final static class SerializedScript {
//		public SerializedScript() {}
//
//		public final List<Trigger> triggers = new ArrayList<Trigger>();
//		public final List<ScriptCommand> commands = new ArrayList<ScriptCommand>();
//	}
	
	private static String indentation = "";
	
	static ScriptInfo loadScripts(final CommandSender viewer) {
		final File scriptsFolder = new File(Skript.getInstance().getDataFolder(), Skript.SCRIPTSFOLDER + File.separator);
		if (!scriptsFolder.isDirectory())
			scriptsFolder.mkdirs();
		
		final ScriptInfo i;
		
		final ErrorDescLogHandler h = SkriptLogger.startLogHandler(new ErrorDescLogHandler(null, null, m_no_errors.toString()));
		try {
			Language.setUseLocal(false);
			
			i = loadScripts(scriptsFolder, viewer);
		} finally {
			Language.setUseLocal(true);
			h.stop();
		}
		
		SkriptEventHandler.registerBukkitEvents();
		
		return i;
	}
	
	/**
	 * Filter for enabled scripts & folders.
	 */
	private final static FileFilter scriptFilter = new FileFilter() {
		@Override
		public boolean accept(final @Nullable File f) {
			return f != null && (f.isDirectory() || StringUtils.endsWithIgnoreCase("" + f.getName(), ".sk")) && !f.getName().startsWith("-");
		}
	};
	
	/**
	 * Loads enabled scripts from the specified directory and it's subdirectories.
	 * 
	 * @param directory
	 * @return Info on the loaded scripts
	 */
	public final static ScriptInfo loadScripts(final File directory, final CommandSender viewer) {
		final boolean wasLocal = Language.setUseLocal(false);
		try {
			final File[] files = directory.listFiles(scriptFilter);
			assert files != null;
			return loadScripts(files, viewer);
		} finally {
			if (wasLocal)
				Language.setUseLocal(true);
		}
	}
	
	/**
	 * Loads the specified scripts.
	 * 
	 * @param files Script files.
	 * @return Empty info for API compatibility.
	 */
	public final static ScriptInfo loadScripts(final File[] files, final CommandSender viewer) {
		final ScriptInfo i = new ScriptInfo();
		manager.loadAndEnable(files, viewer);
		
		return i;
	}
	
	/**
	 * Enables scripts from parser instances.
	 * @param parsed
	 * @param viewer 
	 * @return
	 */
	public final static ScriptInfo enableScripts(final List<ParserInstance> parsed, final CommandSender viewer) {
		final Date start = new Date();
		Skript.debug("Enabling scripts...");
		
		final ScriptInfo i = new ScriptInfo();
		for (ParserInstance pi : parsed) {
			assert pi != null;
			enableScript(pi, i, viewer);
		}
		
		synchronized (loadedScripts) {
			loadedScripts.add(i);
		}
		
		if (i.files == 0)
			Skript.info(viewer, m_no_scripts.toString());
		if (Skript.logNormal() && i.files > 0)
			Skript.info(viewer, m_scripts_loaded.toString(i.files, i.triggers, i.commands, start.difference(new Date())));
		
		SkriptEventHandler.registerBukkitEvents();
		parseThreads.clear(); // Clear so this doesn't memory leak
		
		return i;
	}
	
	/**
	 * Enables scripts from parser instance. This is meant to be ran from main thread.
	 * @param pi Parser instance.
	 * @param i Script info - for statistics, e.g. loaded stuff counts.
	 */
	@SuppressWarnings("null")
	private final static void enableScript(ParserInstance pi, ScriptInfo i, CommandSender viewer) {
		final CountingLogHandler numErrors = SkriptLogger.startLogHandler(new CountingLogHandler(SkriptLogger.SEVERE));
		Skript.debug("Enabling script " + pi.config.getFileName());
		try {
			for (ScriptCommand cmd : pi.commands) { // Register commands
				i.commands++;
				Commands.registerCommand(cmd);
			}
			for (ScriptFunction<?> func : pi.functions) { // Register functions
				i.functions++;
				Functions.functions.put(func.getName(), new FunctionData(func));
			}
			for (Entry<Class<? extends Event>[], Trigger> trigger : pi.triggers.entrySet()) { // Register normal triggers
				i.triggers++;
				SkriptEventHandler.addTrigger(trigger.getKey(), trigger.getValue());
			}
			// "Register" self-registering triggers
			for (Entry<NonNullPair<SkriptEventInfo<?>, SkriptEvent>, Trigger> trigger : pi.selfRegisteringTriggers.entrySet()) {
				((SelfRegisteringSkriptEvent) trigger.getKey().getSecond()).register(trigger.getValue());
				SkriptEventHandler.addSelfRegisteringTrigger(trigger.getValue());
			}
			for (ParseLogHandler log : pi.errorLogs) {
				if (viewer instanceof ConsoleCommandSender) // Console -> normal logging
					log.printError();
				else if (log.hasError()) // Non-console -> ugly hack
					viewer.sendMessage(Skript.SKRIPT_PREFIX + Utils.replaceEnglishChatStyles(log.getError().getMessage()));
				//log.stop();
			}
		} finally {
			numErrors.stop();
		}
		
		i.files++; // Increment script counter
	}
	
	/**
	 * Unloads enabled scripts from the specified directory and its subdirectories.
	 * 
	 * @param folder
	 * @return Info on the unloaded scripts
	 */
	final static ScriptInfo unloadScripts(final File folder) {
		final ScriptInfo r = unloadScripts_(folder);
		Functions.validateFunctions();
		return r;
	}
	
	private final static ScriptInfo unloadScripts_(final File folder) {
		final ScriptInfo info = new ScriptInfo();
		final File[] files = folder.listFiles(scriptFilter);
		for (final File f : files) {
			if (f.isDirectory()) {
				info.add(unloadScripts_(f));
			} else if (f.getName().endsWith(".sk")) {
				info.add(unloadScript_(f));
			}
		}
		return info;
	}
	
	/**
	 * Unloads the specified script.
	 * 
	 * @param script
	 * @return Info on the unloaded script
	 */
	final static ScriptInfo unloadScript(final File script) {
		final ScriptInfo r = unloadScript_(script);
		Functions.clearFunctions(script);
		//Functions.validateFunctions();
		return r;
	}
	
	public final static ScriptInfo unloadScripts(final File[] files) {
		Skript.debug("Unloading (some) scripts...");
		ScriptInfo i = new ScriptInfo();
		for (File f : files) {
			assert f != null;
			ScriptInfo r = unloadScript(f);
			i.commands += r.commands;
			i.files += 1;
			i.functions += r.functions;
			i.triggers += r.functions;
		}
		
		return i;
	}
	
	private final static ScriptInfo unloadScript_(final File script) {
		final ScriptInfo info = SkriptEventHandler.removeTriggers(script);
		synchronized (loadedScripts) {
			loadedScripts.subtract(info);
		}
		return info;
	}
	
	/**
	 * For unit testing (and only that).
	 * 
	 * @param node
	 * @return The loaded Trigger
	 */
	@Nullable
	static Trigger loadTrigger(final SectionNode node) {
		final ParserInstance pi = ParserInstance.DUMMY;
		
		String event = node.getKey();
		if (event == null) {
			assert false : node;
			return null;
		}
		if (event.toLowerCase().startsWith("on "))
			event = "" + event.substring("on ".length());
		
		final NonNullPair<SkriptEventInfo<?>, SkriptEvent> parsedEvent = SkriptParser.parseEvent(pi, event, "can't understand this event: '" + node.getKey() + "'");
		if (parsedEvent == null) {
			assert false;
			return null;
		}
		
		pi.setCurrentEvent("unit test", parsedEvent.getFirst().events);
		try {
			return new Trigger(null, event, parsedEvent.getSecond(), pi.loadItems(node));
		} finally {
			pi.deleteCurrentEvent();
		}
	}
	
	public final static int loadedScripts() {
		synchronized (loadedScripts) {
			return loadedScripts.files;
		}
	}
	
	public final static int loadedCommands() {
		synchronized (loadedScripts) {
			return loadedScripts.commands;
		}
	}
	
	public final static int loadedFunctions() {
		synchronized (loadedScripts) {
			return loadedScripts.functions;
		}
	}
	
	public final static int loadedTriggers() {
		synchronized (loadedScripts) {
			return loadedScripts.triggers;
		}
	}
	
	public final static boolean isCurrentEvent(final @Nullable Class<? extends Event> event) {
		return CollectionUtils.containsSuperclass(getCurrentEvents(), event);
	}
	
	@SafeVarargs
	public final static boolean isCurrentEvent(final Class<? extends Event>... events) {
		return CollectionUtils.containsAnySuperclass(getCurrentEvents(), events);
	}
	
	/**
	 * Use this sparingly; {@link #isCurrentEvent(Class)} or {@link #isCurrentEvent(Class...)} should be used in most cases.
	 */
	@Nullable
	public static Class<? extends Event>[] getCurrentEvents() {
		return parseThreads.get(Thread.currentThread()).getCurrentEvents();
	}
	
	private static final Map<Thread,ParserInstance> parseThreads = new ConcurrentHashMap<>();
	
	/**
	 * Only for internal use.
	 * 
	 * Registers caller thread for given parser, so static event checking methods work.
	 * These methods are slow, however, so this is only for compatibility.
	 */
	public static void registerParseThread(ParserInstance pi) {
		parseThreads.put(Thread.currentThread(), pi);
	}
	
}
