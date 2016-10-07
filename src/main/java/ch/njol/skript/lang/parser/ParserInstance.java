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
 * Copyright 2011-2016 Peter Güttinger and contributors
 * 
 */

package ch.njol.skript.lang.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptCommand;
import ch.njol.skript.SkriptEventHandler;
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
import ch.njol.skript.lang.While;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.ScriptFunction;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Callback;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;

/**
 * Instance of Skript parser. Runs asynchronously.
 */
public class ParserInstance implements Runnable, Comparable<ParserInstance> {
	
	/**
	 * Dummy parser instance. Can be used for legacy code.
	 * All log handles submitted to this will print their contents
	 * immediately.
	 */
	public static final ParserInstance DUMMY = new ParserInstance() {
		@Override
		public void submitErrorLog(final ParseLogHandler log) {
			log.printError();
		}
	};
	
	private Config config;
	private ScriptManager manager;
	public final Map<String, ItemType> aliases;
	public final Map<String, String> options;
	
	private int numCommands;
	private int numFunctions;
	private int numTriggers;
	
	public final List<ScriptCommand> commands;
	public final List<Trigger> selfRegisteringTriggers;
	public final Map<Class<? extends Event>[],Trigger> triggers;
	public final List<ScriptFunction<?>> functions;
	public final List<ParseLogHandler> errorLogs;
	
	private String fileName;
	
	private String indentation = "";
	
	private Kleenean hasDelayBefore = Kleenean.FALSE;
	
	/**
	 * use {@link #setCurrentEvent(String, Class...)}
	 */
	@Nullable
	private Class<? extends Event>[] currentEvents = null;
	
	/**
	 * use {@link #setCurrentEvent(String, Class...)}
	 */
	@Nullable
	private String currentEventName = null;
	
	@SuppressWarnings("null") // Note: only for dummy object
	ParserInstance() {
		aliases = null;
		options = null;
		commands = null;
		selfRegisteringTriggers = null;
		triggers = null;
		functions = null;
		errorLogs = null;
	}
	
	public ParserInstance(String fileName, Config config, ScriptManager manager) {
		this.fileName = fileName;
		this.config = config;
		this.manager = manager;
		this.aliases = new HashMap<>();
		this.options = new HashMap<>();
		this.commands = new ArrayList<>();
		this.selfRegisteringTriggers = new ArrayList<>();
		this.triggers = new HashMap<>();
		this.functions = new ArrayList<>();
		this.errorLogs = new ArrayList<>();
	}
	
	/**
	 * Call {@link #deleteCurrentEvent()} after parsing.
	 * 
	 * @param name
	 * @param events
	 */
	@SafeVarargs
	public final void setCurrentEvent(final String name, final @Nullable Class<? extends Event>... events) {
		currentEventName = name;
		currentEvents = events;
		hasDelayBefore = Kleenean.FALSE;
	}
	
	public void deleteCurrentEvent() {
		currentEventName = null;
		currentEvents = null;
		hasDelayBefore = Kleenean.FALSE;
	}
	
	public String replaceOptions(final String s) {
		final String r = StringUtils.replaceAll(s, "\\{@(.+?)\\}", new Callback<String, Matcher>() {
			@Override
			@Nullable
			public String run(final Matcher m) {
				final String option = options.get(m.group(1));
				if (option == null) {
					Skript.error("undefined option " + m.group());
					return m.group();
				}
				return option;
			}
		});
		assert r != null;
		return r;
	}
	
	public List<TriggerItem> loadItems(final SectionNode node) {
		
		if (Skript.debug())
			indentation += "    ";
		
		final ArrayList<TriggerItem> items = new ArrayList<>();
		
		Kleenean hadDelayBeforeLastIf = Kleenean.FALSE;
		
		for (final Node n : node) {
			SkriptLogger.setNode(n);
			if (n instanceof SimpleNode) {
				final SimpleNode e = (SimpleNode) n;
				final String s = replaceOptions("" + e.getKey());
				if (!SkriptParser.validateLine(s))
					continue;
				final Statement stmt = Statement.parse(s, "Can't understand this condition/effect: " + s);
				if (stmt == null)
					continue;
				if (Skript.debug() || n.debug())
					Skript.debug(indentation + stmt.toString(null, true));
				items.add(stmt);
				if (stmt instanceof Delay)
					hasDelayBefore = Kleenean.TRUE;
			} else if (n instanceof SectionNode) {
				String name = replaceOptions("" + n.getKey());
				if (!SkriptParser.validateLine(name))
					continue;
				
				if (StringUtils.startsWithIgnoreCase(name, "loop ")) {
					final String l = "" + name.substring("loop ".length());
					final RetainingLogHandler h = SkriptLogger.startRetainingLog();
					Expression<?> loopedExpr;
					try {
						loopedExpr = new SkriptParser(this, l).parseExpression(Object.class);
						if (loopedExpr != null)
							loopedExpr = loopedExpr.getConvertedExpression(Object.class);
						if (loopedExpr == null) {
							h.printErrors("Can't understand this loop: '" + name + "'");
							continue;
						}
						h.printLog();
					} finally {
						h.stop();
					}
					if (loopedExpr.isSingle()) {
						Skript.error("Can't loop " + loopedExpr + " because it's only a single value");
						continue;
					}
					if (Skript.debug() || n.debug())
						Skript.debug(indentation + "loop " + loopedExpr.toString(null, true) + ":");
					final Kleenean hadDelayBefore = hasDelayBefore;
					items.add(new Loop(loopedExpr, (SectionNode) n));
					if (hadDelayBefore != Kleenean.TRUE && hasDelayBefore != Kleenean.FALSE)
						hasDelayBefore = Kleenean.UNKNOWN;
				} else if (StringUtils.startsWithIgnoreCase(name, "while ")) {
					final String l = "" + name.substring("while ".length());
					final Condition c = Condition.parse(this, l, "Can't understand this condition: " + l);
					if (c == null)
						continue;
					if (Skript.debug() || n.debug())
						Skript.debug(indentation + "while " + c.toString(null, true) + ":");
					final Kleenean hadDelayBefore = hasDelayBefore;
					items.add(new While(c, (SectionNode) n));
					if (hadDelayBefore != Kleenean.TRUE && hasDelayBefore != Kleenean.FALSE)
						hasDelayBefore = Kleenean.UNKNOWN;
				} else if (name.equalsIgnoreCase("else")) {
					if (items.size() == 0 || !(items.get(items.size() - 1) instanceof Conditional) || ((Conditional) items.get(items.size() - 1)).hasElseClause()) {
						Skript.error("'else' has to be placed just after an 'if' or 'else if' section");
						continue;
					}
					if (Skript.debug() || n.debug())
						Skript.debug(indentation + "else:");
					final Kleenean hadDelayAfterLastIf = hasDelayBefore;
					hasDelayBefore = hadDelayBeforeLastIf;
					((Conditional) items.get(items.size() - 1)).loadElseClause((SectionNode) n);
					hasDelayBefore = hadDelayBeforeLastIf.or(hadDelayAfterLastIf.and(hasDelayBefore));
				} else if (StringUtils.startsWithIgnoreCase(name, "else if ")) {
					if (items.size() == 0 || !(items.get(items.size() - 1) instanceof Conditional) || ((Conditional) items.get(items.size() - 1)).hasElseClause()) {
						Skript.error("'else if' has to be placed just after another 'if' or 'else if' section");
						continue;
					}
					name = "" + name.substring("else if ".length());
					final Condition cond = Condition.parse(this, name, "can't understand this condition: '" + name + "'");
					if (cond == null)
						continue;
					if (Skript.debug() || n.debug())
						Skript.debug(indentation + "else if " + cond.toString(null, true));
					final Kleenean hadDelayAfterLastIf = hasDelayBefore;
					hasDelayBefore = hadDelayBeforeLastIf;
					((Conditional) items.get(items.size() - 1)).loadElseIf(cond, (SectionNode) n);
					hasDelayBefore = hadDelayBeforeLastIf.or(hadDelayAfterLastIf.and(hasDelayBefore.and(Kleenean.UNKNOWN)));
				} else {
					if (StringUtils.startsWithIgnoreCase(name, "if "))
						name = "" + name.substring(3);
					final Condition cond = Condition.parse(this, name, "can't understand this condition: '" + name + "'");
					if (cond == null)
						continue;
					if (Skript.debug() || n.debug())
						Skript.debug(indentation + cond.toString(null, true) + ":");
					final Kleenean hadDelayBefore = hasDelayBefore;
					hadDelayBeforeLastIf = hadDelayBefore;
					items.add(new Conditional(cond, (SectionNode) n));
					hasDelayBefore = hadDelayBefore.or(hasDelayBefore.and(Kleenean.UNKNOWN));
				}
			}
		}
		
		for (int i = 0; i < items.size() - 1; i++)
			items.get(i).setNext(items.get(i + 1));
		
		SkriptLogger.setNode(node);
		
		if (Skript.debug())
			indentation = "" + indentation.substring(0, indentation.length() - 4);
		
		return items;
	}
	
	/**
	 * Submits a parse log handler. Errors will be displayed
	 * when enabling scripts, which allows them to be ordered.
	 * 
	 * It is not recommended to write anything to log after submitting it.
	 * @param log Log handler.
	 */
	public void submitErrorLog(ParseLogHandler log) {
		errorLogs.add(log);
	}
	
	@Override
	public void run() {
		for (final Node cnode : config.getMainNode()) {
			if (!(cnode instanceof SectionNode)) {
				Skript.error("invalid line - all code has to be put into triggers");
				continue;
			}
			
			final SectionNode node = ((SectionNode) cnode);
			String event = node.getKey();
			if (event == null)
				continue;
			
			if (event.equalsIgnoreCase("aliases")) {
				node.convertToEntries(0, "=");
				for (final Node n : node) {
					if (!(n instanceof EntryNode)) {
						Skript.error("invalid line in aliases section");
						continue;
					}
					final ItemType t = Aliases.parseAlias(((EntryNode) n).getValue());
					if (t == null)
						continue;
					aliases.put(((EntryNode) n).getKey().toLowerCase(), t);
				}
				continue;
			} else if (event.equalsIgnoreCase("options")) {
				node.convertToEntries(0);
				for (final Node n : node) {
					if (!(n instanceof EntryNode)) {
						Skript.error("invalid line in options");
						continue;
					}
					options.put(((EntryNode) n).getKey(), ((EntryNode) n).getValue());
				}
				continue;
			} else if (event.equalsIgnoreCase("variables")) {
				// TODO allow to make these override existing variables
				node.convertToEntries(0, "=");
				for (final Node n : node) {
					if (!(n instanceof EntryNode)) {
						Skript.error("Invalid line in variables section");
						continue;
					}
					String name = ((EntryNode) n).getKey().toLowerCase(Locale.ENGLISH);
					if (name.startsWith("{") && name.endsWith("}"))
						name = "" + name.substring(1, name.length() - 1);
					final String var = name;
					name = StringUtils.replaceAll(name, "%(.+)?%", new Callback<String, Matcher>() {
						@Override
						@Nullable
						public String run(final Matcher m) {
							if (m.group(1).contains("{") || m.group(1).contains("}") || m.group(1).contains("%")) {
								Skript.error("'" + var + "' is not a valid name for a default variable");
								return null;
							}
							final ClassInfo<?> ci = Classes.getClassInfoFromUserInput("" + m.group(1));
							if (ci == null) {
								Skript.error("Can't understand the type '" + m.group(1) + "'");
								return null;
							}
							return "<" + ci.getCodeName() + ">";
						}
					});
					if (name == null) {
						continue;
					} else if (name.contains("%")) {
						Skript.error("Invalid use of percent signs in variable name");
						continue;
					}
					if (Variables.getVariable(name, null, false) != null)
						continue;
					Object o;
					final ParseLogHandler log = SkriptLogger.startParseLogHandler();
					try {
						o = Classes.parseSimple(((EntryNode) n).getValue(), Object.class, ParseContext.SCRIPT);
						if (o == null) {
							log.printError("Can't understand the value '" + ((EntryNode) n).getValue() + "'");
							continue;
						}
						log.printLog();
					} finally {
						log.stop();
					}
					@SuppressWarnings("null")
					final ClassInfo<?> ci = Classes.getSuperClassInfo(o.getClass());
					if (ci.getSerializer() == null) {
						Skript.error("Can't save '" + ((EntryNode) n).getValue() + "' in a variable");
						continue;
					} else if (ci.getSerializeAs() != null) {
						final ClassInfo<?> as = Classes.getExactClassInfo(ci.getSerializeAs());
						if (as == null) {
							assert false : ci;
							continue;
						}
						o = Converters.convert(o, as.getC());
						if (o == null) {
							Skript.error("Can't save '" + ((EntryNode) n).getValue() + "' in a variable");
							continue;
						}
					}
					Variables.setVariable(name, o, null, false);
				}
				continue;
			}
			
			if (!SkriptParser.validateLine(event))
				continue;
			
			if (event.toLowerCase().startsWith("command ")) {
				
				setCurrentEvent("command", CommandEvent.class);
				
				final ScriptCommand c = Commands.loadCommand(node, this);
				if (c != null) {
					numCommands++;
					commands.add(c);
				}
				
				deleteCurrentEvent();
				
				continue;
			} else if (event.toLowerCase().startsWith("function ")) {
				
				setCurrentEvent("function", FunctionEvent.class);
				
				final Function<?> func = Functions.loadFunction(node, this);
				if (func != null) {
					numFunctions++;
				}
				
				if (func instanceof ScriptFunction)
					functions.add((ScriptFunction<?>) func);
				
				deleteCurrentEvent();
				
				continue;
			}
			
			if (Skript.logVeryHigh() && !Skript.debug())
				Skript.info("loading trigger '" + event + "'");
			
			if (StringUtils.startsWithIgnoreCase(event, "on "))
				event = "" + event.substring("on ".length());
			
			event = replaceOptions(event);
			
			final NonNullPair<SkriptEventInfo<?>, SkriptEvent> parsedEvent = SkriptParser.parseEvent(this, event, "can't understand this event: '" + node.getKey() + "'");
			if (parsedEvent == null)
				continue;
			
			if (Skript.debug() || node.debug())
				Skript.debug(event + " (" + parsedEvent.getSecond().toString(null, true) + "):");
			
			setCurrentEvent("" + parsedEvent.getFirst().getName().toLowerCase(Locale.ENGLISH), parsedEvent.getFirst().events);
			final Trigger trigger;
			try {
				trigger = new Trigger(config.getFile(), event, parsedEvent.getSecond(), loadItems(node));
			} finally {
				deleteCurrentEvent();
			}
			
			if (parsedEvent.getSecond() instanceof SelfRegisteringSkriptEvent) {
				((SelfRegisteringSkriptEvent) parsedEvent.getSecond()).register(trigger);
				selfRegisteringTriggers.add(trigger);
			} else {
				triggers.put(parsedEvent.getFirst().events, trigger);
			}
			
			numTriggers++;
		}
	}

	@Override
	public int compareTo(@Nullable ParserInstance o) {
		assert o != null;
		return fileName.compareTo(o.fileName);
	}
	
}
