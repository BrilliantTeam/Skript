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
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.command;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.command.Commands.CommandAliasHelpTopic;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.Verbosity;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.EmptyStacktraceException;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.MessageComponent;
import ch.njol.skript.variables.Variables;
import ch.njol.util.StringUtils;
import ch.njol.util.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.Plugin;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.script.Script;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * This class is used for user-defined commands.
 */
public class ScriptCommand implements TabExecutor {

	public final static Message m_executable_by_players = new Message("commands.executable by players");
	public final static Message m_executable_by_console = new Message("commands.executable by console");
	private static final String DEFAULT_PREFIX = "skript";

	final String name;
	private final String label;
	private final List<String> aliases;
	private List<String> activeAliases;
	private String permission;
	private final VariableString permissionMessage;
	private final String description;
	private final String prefix;
	@Nullable
	private final Timespan cooldown;
	private final Expression<String> cooldownMessage;
	private final String cooldownBypass;
	@Nullable
	private final Expression<String> cooldownStorage;
	final CommandUsage usage;

	private final Trigger trigger;

	private final String pattern;
	private final List<Argument<?>> arguments;

	public final static int PLAYERS = 0x1, CONSOLE = 0x2, BOTH = PLAYERS | CONSOLE;
	final int executableBy;

	private transient PluginCommand bukkitCommand;

	private Map<UUID,Date> lastUsageMap = new HashMap<>();

	//<editor-fold default-state="collapsed" desc="public ScriptCommand(... String usage ...)">
	/**
	 * Creates a new ScriptCommand.
	 * Prefer using the CommandUsage class for the usage parameter.
	 *
	 * @param name /name
	 * @param pattern the Skript pattern used to parse the input into arguments.
	 * @param arguments the list of Arguments this command takes
	 * @param description description to display in /help
	 * @param prefix the prefix of the command
	 * @param usage message to display if the command was used incorrectly
	 * @param aliases /alias1, /alias2, ...
	 * @param permission permission or null if none
	 * @param permissionMessage message to display if the player doesn't have the given permission
	 * @param node the node to parse and load into a Trigger
	 */
	public ScriptCommand(
		Script script, String name, String pattern, List<Argument<?>> arguments,
		String description, @Nullable String prefix, String usage, List<String> aliases,
		String permission, @Nullable VariableString permissionMessage, @Nullable Timespan cooldown,
		@Nullable VariableString cooldownMessage, String cooldownBypass,
		@Nullable VariableString cooldownStorage, int executableBy, SectionNode node
	) {
		this(script, name, pattern, arguments, description, prefix, new CommandUsage(null, usage),
				aliases, permission, permissionMessage, cooldown, cooldownMessage, cooldownBypass,
				cooldownStorage, executableBy, node);
	}
	//</editor-fold>

	/**
	 * Creates a new ScriptCommand.
	 *
	 * @param name /name
	 * @param pattern the Skript pattern used to parse the input into arguments.
	 * @param arguments the list of Arguments this command takes
	 * @param description description to display in /help
	 * @param prefix the prefix of the command
	 * @param usage message to display if the command was used incorrectly
	 * @param aliases /alias1, /alias2, ...
	 * @param permission permission or null if none
	 * @param permissionMessage message to display if the player doesn't have the given permission
	 * @param node the node to parse and load into a Trigger
	 */
	public ScriptCommand(
		Script script, String name, String pattern, List<Argument<?>> arguments,
		String description, @Nullable String prefix, CommandUsage usage, List<String> aliases,
		String permission, @Nullable VariableString permissionMessage, @Nullable Timespan cooldown,
		@Nullable VariableString cooldownMessage, String cooldownBypass,
		@Nullable VariableString cooldownStorage, int executableBy, SectionNode node
	) {
		Validate.notNull(name, pattern, arguments, description, usage, aliases, node);
		this.name = name;
		label = "" + name.toLowerCase(Locale.ENGLISH);
		this.permission = permission;
		if (permissionMessage == null) {
			VariableString defaultMsg = VariableString.newInstance(Language.get("commands.no permission message"));
			assert defaultMsg != null;
			this.permissionMessage = defaultMsg;
		} else {
			this.permissionMessage = permissionMessage;
		}

		if (prefix != null) {
			for (char c : prefix.toCharArray()) {
				if (Character.isWhitespace(c)) {
					Skript.warning("command /" + name + " has a whitespace in its prefix. Defaulting to '" + ScriptCommand.DEFAULT_PREFIX + "'.");
					prefix = ScriptCommand.DEFAULT_PREFIX;
					break;
				}
				// char 167 is §
				if (c == 167) {
					Skript.warning("command /" + name + " has a section character in its prefix. Defaulting to '" + ScriptCommand.DEFAULT_PREFIX + "'.");
					prefix = ScriptCommand.DEFAULT_PREFIX;
					break;
				}
			}
		} else {
			prefix = DEFAULT_PREFIX;
		}
		this.prefix = prefix;

		this.cooldown = cooldown;
		this.cooldownMessage = cooldownMessage == null
				? new SimpleLiteral<>(Language.get("commands.cooldown message"),false)
				: cooldownMessage;
		this.cooldownBypass = cooldownBypass;
		this.cooldownStorage = cooldownStorage;

		// remove aliases that are the same as the command
		aliases.removeIf(label::equalsIgnoreCase);
		this.aliases = aliases;
		activeAliases = new ArrayList<>(aliases);

		this.description = Utils.replaceEnglishChatStyles(description);
		this.usage = usage;

		this.executableBy = executableBy;

		this.pattern = pattern;
		this.arguments = arguments;

		trigger = new Trigger(script, "command /" + name, new SimpleEvent(), ScriptLoader.loadItems(node));
		trigger.setLineNumber(node.getLine());

		bukkitCommand = setupBukkitCommand();
	}

	private PluginCommand setupBukkitCommand() {
		try {
			final Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			c.setAccessible(true);
			final PluginCommand bukkitCommand = c.newInstance(name, Skript.getInstance());
			bukkitCommand.setAliases(aliases);
			bukkitCommand.setDescription(description);
			bukkitCommand.setLabel(label);
			bukkitCommand.setPermission(permission);
			// We can only set the message if it's simple (doesn't contains expressions)
			if (permissionMessage.isSimple())
				bukkitCommand.setPermissionMessage(permissionMessage.toString(null));
			bukkitCommand.setUsage(usage.getUsage());
			bukkitCommand.setExecutor(this);
			return bukkitCommand;
		} catch (final Exception e) {
			Skript.outdatedError(e);
			throw new EmptyStacktraceException();
		}
	}

	@Override
	public boolean onCommand(final @Nullable CommandSender sender, final @Nullable Command command, final @Nullable String label, final @Nullable String[] args) {
		if (sender == null || label == null || args == null)
			return false;
		execute(sender, label, StringUtils.join(args, " "));
		return true;
	}

	public boolean execute(final CommandSender sender, final String commandLabel, final String rest) {
		if (sender instanceof Player) {
			if ((executableBy & PLAYERS) == 0) {
				sender.sendMessage("" + m_executable_by_console);
				return false;
			}
		} else {
			if ((executableBy & CONSOLE) == 0) {
				sender.sendMessage("" + m_executable_by_players);
				return false;
			}
		}

		final ScriptCommandEvent event = new ScriptCommandEvent(ScriptCommand.this, sender, commandLabel, rest);

		if (!checkPermissions(sender, event))
			return false;

		cooldownCheck : {
			if (sender instanceof Player && cooldown != null) {
				Player player = ((Player) sender);
				UUID uuid = player.getUniqueId();

				// Cooldown bypass
				if (!cooldownBypass.isEmpty() && player.hasPermission(cooldownBypass)) {
					setLastUsage(uuid, event, null);
					break cooldownCheck;
				}

				if (getLastUsage(uuid, event) != null) {
					if (getRemainingMilliseconds(uuid, event) <= 0) {
						if (!SkriptConfig.keepLastUsageDates.value())
							setLastUsage(uuid, event, null);
					} else {
						String msg = cooldownMessage.getSingle(event);
						if (msg != null)
							sender.sendMessage(msg);
						return false;
					}
				}
			}
		}

		Runnable runnable = () -> {
			// save previous last usage date to check if the execution has set the last usage date
			Date previousLastUsage = null;
			if (sender instanceof Player)
				previousLastUsage = getLastUsage(((Player) sender).getUniqueId(), event);

			// execute the command - may modify the last usage date
			execute2(event, sender, commandLabel, rest);

			if (sender instanceof Player && !event.isCooldownCancelled()) {
				Date lastUsage = getLastUsage(((Player) sender).getUniqueId(), event);
				// check if the execution has set the last usage date
				// if not, set it to the current date. if it has, we leave it alone so as not to affect the remaining/elapsed time (#5862)
				if (Objects.equals(lastUsage, previousLastUsage))
					setLastUsage(((Player) sender).getUniqueId(), event, new Date());
			}
		};
		if (Bukkit.isPrimaryThread()) {
			runnable.run();
		} else {
			// must not wait for the command to complete as some plugins call commands in such a way that the server will deadlock
			new Task(Skript.getInstance(), 1) {
				@Override
				public void run() {
					runnable.run();
				}
			};
		}

		return true; // Skript prints its own error message anyway
	}

	boolean execute2(final ScriptCommandEvent event, final CommandSender sender, final String commandLabel, final String rest) {
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			final boolean ok = SkriptParser.parseArguments(rest, ScriptCommand.this, event);
			if (!ok) {
				final LogEntry e = log.getError();
				if (e != null)
					sender.sendMessage(ChatColor.DARK_RED + e.toString());
				sender.sendMessage(usage.getUsage(event));
				log.clear();
				return false;
			}
			log.clearError();
		} finally {
			log.stop();
		}

		if (Skript.log(Verbosity.VERY_HIGH))
			Skript.info("# /" + name + " " + rest);
		final long startTrigger = System.nanoTime();

		if (!trigger.execute(event))
			sender.sendMessage(Commands.m_internal_error.toString());

		if (Skript.log(Verbosity.VERY_HIGH))
			Skript.info("# " + name + " took " + 1. * (System.nanoTime() - startTrigger) / 1000000. + " milliseconds");
		return true;
	}

	public boolean checkPermissions(CommandSender sender, String commandLabel, String arguments) {
		return checkPermissions(sender, new ScriptCommandEvent(this, sender, commandLabel, arguments));
	}

	public boolean checkPermissions(CommandSender sender, Event event) {
		if (!permission.isEmpty() && !sender.hasPermission(permission)) {
			if (sender instanceof Player) {
				List<MessageComponent> components =
					permissionMessage.getMessageComponents(event);
				((Player) sender).spigot().sendMessage(BungeeConverter.convert(components));
			} else {
				sender.sendMessage(permissionMessage.getSingle(event));
			}
			return false;
		}
		return true;
	}

	public void sendHelp(final CommandSender sender) {
		if (!description.isEmpty())
			sender.sendMessage(description);
		sender.sendMessage(ChatColor.GOLD + "Usage" + ChatColor.RESET + ": " + usage.getUsage());
	}

	/**
	 * Gets the arguments this command takes.
	 *
	 * @return The internal list of arguments. Do not modify it!
	 */
	public List<Argument<?>> getArguments() {
		return arguments;
	}

	public String getPattern() {
		return pattern;
	}

	@Nullable
	private transient Command overridden = null;
	private transient Map<String, Command> overriddenAliases = new HashMap<>();

	public void register(SimpleCommandMap commandMap, Map<String, Command> knownCommands, @Nullable Set<String> aliases) {
		synchronized (commandMap) {
			overriddenAliases.clear();
			overridden = knownCommands.put(label, bukkitCommand);
			if (aliases != null)
				aliases.remove(label);
			final Iterator<String> as = activeAliases.iterator();
			while (as.hasNext()) {
				final String lowerAlias = as.next().toLowerCase(Locale.ENGLISH);
				if (knownCommands.containsKey(lowerAlias) && (aliases == null || !aliases.contains(lowerAlias))) {
					as.remove();
					continue;
				}
				overriddenAliases.put(lowerAlias, knownCommands.put(lowerAlias, bukkitCommand));
				if (aliases != null)
					aliases.add(lowerAlias);
			}
			bukkitCommand.setAliases(activeAliases);
			commandMap.register(prefix, bukkitCommand);
		}
	}

	public void unregister(SimpleCommandMap commandMap, Map<String, Command> knownCommands, @Nullable Set<String> aliases) {
		synchronized (commandMap) {
			knownCommands.remove(label);
			knownCommands.remove(prefix + ":" + label);
			if (aliases != null)
				aliases.removeAll(activeAliases);
			for (final String alias : activeAliases) {
				knownCommands.remove(alias);
				knownCommands.remove(prefix + ":" + alias);
			}
			activeAliases = new ArrayList<>(this.aliases);
			bukkitCommand.unregister(commandMap);
			bukkitCommand.setAliases(this.aliases);
			if (overridden != null) {
				knownCommands.put(label, overridden);
				overridden = null;
			}
			for (final Entry<String, Command> e : overriddenAliases.entrySet()) {
				if (e.getValue() == null)
					continue;
				knownCommands.put(e.getKey(), e.getValue());
				if (aliases != null)
					aliases.add(e.getKey());
			}
			overriddenAliases.clear();
		}
	}

	private transient Collection<HelpTopic> helps = new ArrayList<>();

	public void registerHelp() {
		helps.clear();
		final HelpMap help = Bukkit.getHelpMap();
		final HelpTopic t = new GenericCommandHelpTopic(bukkitCommand);
		help.addTopic(t);
		helps.add(t);
		final HelpTopic aliases = help.getHelpTopic("Aliases");
		if (aliases instanceof IndexHelpTopic) {
			aliases.getFullText(Bukkit.getConsoleSender()); // CraftBukkit has a lazy IndexHelpTopic class (org.bukkit.craftbukkit.help.CustomIndexHelpTopic) - maybe its used for aliases as well
			try {
				final Field topics = IndexHelpTopic.class.getDeclaredField("allTopics");
				topics.setAccessible(true);
				@SuppressWarnings("unchecked")
				final ArrayList<HelpTopic> as = new ArrayList<>((Collection<HelpTopic>) topics.get(aliases));
				for (final String alias : activeAliases) {
					final HelpTopic at = new CommandAliasHelpTopic("/" + alias, "/" + getLabel(), help);
					as.add(at);
					helps.add(at);
				}
				Collections.sort(as, HelpTopicComparator.helpTopicComparatorInstance());
				topics.set(aliases, as);
			} catch (final Exception e) {
				Skript.outdatedError(e);//, "error registering aliases for /" + getName());
			}
		}
	}

	public void unregisterHelp() {
		Bukkit.getHelpMap().getHelpTopics().removeAll(helps);
		final HelpTopic aliases = Bukkit.getHelpMap().getHelpTopic("Aliases");
		if (aliases != null && aliases instanceof IndexHelpTopic) {
			try {
				final Field topics = IndexHelpTopic.class.getDeclaredField("allTopics");
				topics.setAccessible(true);
				@SuppressWarnings("unchecked")
				final ArrayList<HelpTopic> as = new ArrayList<>((Collection<HelpTopic>) topics.get(aliases));
				as.removeAll(helps);
				topics.set(aliases, as);
			} catch (final Exception e) {
				Skript.outdatedError(e);//, "error unregistering aliases for /" + getName());
			}
		}
		helps.clear();
	}

	public String getName() {
		return name;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getLabel() {
		return label;
	}

	@Nullable
	public Timespan getCooldown() {
		return cooldown;
	}

	@Nullable
	private String getStorageVariableName(Event event) {
		assert cooldownStorage != null;
		String variableString = cooldownStorage.getSingle(event);
		if (variableString == null)
			return null;
		if (variableString.startsWith("{"))
			variableString = variableString.substring(1);
		if (variableString.endsWith("}"))
			variableString = variableString.substring(0, variableString.length() - 1);
		return variableString;
	}

	@Nullable
	public Date getLastUsage(UUID uuid, Event event) {
		if (cooldownStorage == null) {
			return lastUsageMap.get(uuid);
		} else {
			String name = getStorageVariableName(event);
			assert name != null;
			Object variable = Variables.getVariable(name, null, false);
			if (!(variable instanceof Date)) {
				Skript.warning("Variable {" + name + "} was not a date! You may be using this variable elsewhere. " +
						"This warning is letting you know that this variable is now overridden for the command storage.");
				return null;
			}
			return (Date) variable;
		}
	}

	public void setLastUsage(UUID uuid, Event event, @Nullable Date date) {
		if (cooldownStorage != null) {
			// Using a variable
			String name = getStorageVariableName(event);
			assert name != null;
			Variables.setVariable(name, date, null, false);
		} else {
			// Use the map
			if (date == null)
				lastUsageMap.remove(uuid);
			else
				lastUsageMap.put(uuid, date);
		}
	}

	public long getRemainingMilliseconds(UUID uuid, Event event) {
		Date lastUsage = getLastUsage(uuid, event);
		if (lastUsage == null)
			return 0;
		Timespan cooldown = this.cooldown;
		assert cooldown != null;
		long remaining = cooldown.getMilliSeconds() - getElapsedMilliseconds(uuid, event);
		if (remaining < 0)
			remaining = 0;
		return remaining;
	}

	public void setRemainingMilliseconds(UUID uuid, Event event, long milliseconds) {
		Timespan cooldown = this.cooldown;
		assert cooldown != null;
		long cooldownMs = cooldown.getMilliSeconds();
		if (milliseconds > cooldownMs)
			milliseconds = cooldownMs;
		setElapsedMilliSeconds(uuid, event, cooldownMs - milliseconds);
	}

	public long getElapsedMilliseconds(UUID uuid, Event event) {
		Date lastUsage = getLastUsage(uuid, event);
		return lastUsage == null ? 0 : new Date().getTimestamp() - lastUsage.getTimestamp();
	}

	public void setElapsedMilliSeconds(UUID uuid, Event event, long milliseconds) {
		Date date = new Date();
		date.subtract(new Timespan(milliseconds));
		setLastUsage(uuid, event, date);
	}

	public String getCooldownBypass() {
		return cooldownBypass;
	}

	public List<String> getAliases() {
		return aliases;
	}

	public List<String> getActiveAliases() {
		return activeAliases;
	}

	public PluginCommand getBukkitCommand() {
		return bukkitCommand;
	}

	@Nullable
	public Script getScript() {
		return trigger.getScript();
	}

	@Nullable
	@Override
	public List<String> onTabComplete(@Nullable CommandSender sender, @Nullable Command command, @Nullable String alias, @Nullable String[] args) {
		assert args != null;
		int argIndex = args.length - 1;
		if (argIndex >= arguments.size())
			return Collections.emptyList(); // Too many arguments, nothing to complete
		Argument<?> arg = arguments.get(argIndex);
		Class<?> argType = arg.getType();
		if (argType.equals(Player.class) || argType.equals(OfflinePlayer.class))
			return null; // Default completion

		return Collections.emptyList(); // No tab completion here!
	}

}
