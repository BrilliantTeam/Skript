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
package ch.njol.skript.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import ch.njol.skript.Skript;
import ch.njol.skript.effects.EffTeleport;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Callback;
import ch.njol.util.Checker;
import ch.njol.util.NonNullPair;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.EnumerationIterable;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class.
 * 
 * @author Peter Güttinger
 */
public abstract class Utils {
	
	private Utils() {}
	
	public final static Random random = new Random();
	
	public static String join(final Object[] objects) {
		assert objects != null;
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < objects.length; i++) {
			if (i != 0)
				b.append(", ");
			b.append(Classes.toString(objects[i]));
		}
		return "" + b.toString();
	}
	
	public static String join(final Iterable<?> objects) {
		assert objects != null;
		final StringBuilder b = new StringBuilder();
		boolean first = true;
		for (final Object o : objects) {
			if (!first)
				b.append(", ");
			else
				first = false;
			b.append(Classes.toString(o));
		}
		return "" + b.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> boolean isEither(@Nullable T compared, @Nullable T... types) {
		return CollectionUtils.contains(types, compared);
	}
	
	public static Pair<String, Integer> getAmount(String s) {
		if (s.matches("\\d+ of .+")) {
			return new Pair<>(s.split(" ", 3)[2], Utils.parseInt("" + s.split(" ", 2)[0]));
		} else if (s.matches("\\d+ .+")) {
			return new Pair<>(s.split(" ", 2)[1], Utils.parseInt("" + s.split(" ", 2)[0]));
		} else if (s.matches("an? .+")) {
			return new Pair<>(s.split(" ", 2)[1], 1);
		}
		return new Pair<>(s, Integer.valueOf(-1));
	}
	
//	public final static class AmountResponse {
//		public final String s;
//		public final int amount;
//		public final boolean every;
//
//		public AmountResponse(final String s, final int amount, final boolean every) {
//			this.s = s;
//			this.amount = amount;
//			this.every = every;
//		}
//
//		public AmountResponse(final String s, final boolean every) {
//			this.s = s;
//			amount = -1;
//			this.every = every;
//		}
//
//		public AmountResponse(final String s, final int amount) {
//			this.s = s;
//			this.amount = amount;
//			every = false;
//		}
//
//		public AmountResponse(final String s) {
//			this.s = s;
//			amount = -1;
//			every = false;
//		}
//	}
//
//	public final static AmountResponse getAmountWithEvery(final String s) {
//		if (s.matches("\\d+ of (all|every) .+")) {
//			return new AmountResponse("" + s.split(" ", 4)[3], Utils.parseInt("" + s.split(" ", 2)[0]), true);
//		} else if (s.matches("\\d+ of .+")) {
//			return new AmountResponse("" + s.split(" ", 3)[2], Utils.parseInt("" + s.split(" ", 2)[0]));
//		} else if (s.matches("\\d+ .+")) {
//			return new AmountResponse("" + s.split(" ", 2)[1], Utils.parseInt("" + s.split(" ", 2)[0]));
//		} else if (s.matches("an? .+")) {
//			return new AmountResponse("" + s.split(" ", 2)[1], 1);
//		} else if (s.matches("(all|every) .+")) {
//			return new AmountResponse("" + s.split(" ", 2)[1], true);
//		}
//		return new AmountResponse(s);
//	}

	/**
	 * Loads classes of the plugin by package. Useful for registering many syntax elements like Skript does it.
	 * 
	 * @param basePackage The base package to add to all sub packages, e.g. <tt>"ch.njol.skript"</tt>.
	 * @param subPackages Which subpackages of the base package should be loaded, e.g. <tt>"expressions", "conditions", "effects"</tt>. Subpackages of these packages will be loaded
	 *            as well. Use an empty array to load all subpackages of the base package.
	 * @throws IOException If some error occurred attempting to read the plugin's jar file.
	 * @return This SkriptAddon
	 */
	public static Class<?>[] getClasses(Plugin plugin, String basePackage, String... subPackages) throws IOException {
		assert subPackages != null;
		JarFile jar = new JarFile(getFile(plugin));
		for (int i = 0; i < subPackages.length; i++)
			subPackages[i] = subPackages[i].replace('.', '/') + "/";
		basePackage = basePackage.replace('.', '/') + "/";
		List<Class<?>> classes = new ArrayList<>();
		try {
			List<String> classNames = new ArrayList<>();

			for (JarEntry e : new EnumerationIterable<>(jar.entries())) {
				if (e.getName().startsWith(basePackage) && e.getName().endsWith(".class") && !e.getName().endsWith("package-info.class")) {
					boolean load = subPackages.length == 0;
					for (String sub : subPackages) {
						if (e.getName().startsWith(sub, basePackage.length())) {
							load = true;
							break;
						}
					}

					if (load)
						classNames.add(e.getName().replace('/', '.').substring(0, e.getName().length() - ".class".length()));
				}
			}

			classNames.sort(String::compareToIgnoreCase);

			for (String c : classNames) {
				try {
					classes.add(Class.forName(c, true, plugin.getClass().getClassLoader()));
				} catch (ClassNotFoundException | NoClassDefFoundError ex) {
					Skript.exception(ex, "Cannot load class " + c);
				} catch (ExceptionInInitializerError err) {
					Skript.exception(err.getCause(), "class " + c + " generated an exception while loading");
				}
			}
		} finally {
			try {
				jar.close();
			} catch (IOException e) {}
		}
		return classes.toArray(new Class<?>[classes.size()]);
	}

	/**
	 * The first invocation of this method uses reflection to invoke the protected method {@link JavaPlugin#getFile()} to get the plugin's jar file.
	 * 
	 * @return The jar file of the plugin.
	 */
	@Nullable
	public static File getFile(Plugin plugin) {
		try {
			Method getFile = JavaPlugin.class.getDeclaredMethod("getFile");
			getFile.setAccessible(true);
			return (File) getFile.invoke(plugin);
		} catch (NoSuchMethodException e) {
			Skript.outdatedError(e);
		} catch (IllegalArgumentException e) {
			Skript.outdatedError(e);
		} catch (IllegalAccessException e) {
			assert false;
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		}
		return null;
	}

	private final static String[][] plurals = {
			
			{"fe", "ves"},// most -f words' plurals can end in -fs as well as -ves
			
			{"axe", "axes"},
			{"x", "xes"},
			
			{"ay", "ays"},
			{"ey", "eys"},
			{"iy", "iys"},
			{"oy", "oys"},
			{"uy", "uys"},
			{"kie", "kies"},
			{"zombie", "zombies"},
			{"y", "ies"},
			
			{"h", "hes"},
			
			{"man", "men"},
			
			{"us", "i"},
			
			{"hoe", "hoes"},
			{"toe", "toes"},
			{"o", "oes"},
			
			{"alias", "aliases"},
			{"gas", "gases"},
			
			{"child", "children"},
			
			{"sheep", "sheep"},
			
			// general ending
			{"", "s"},
	};
	
	/**
	 * @param s trimmed string
	 * @return Pair of singular string + boolean whether it was plural
	 */
	@SuppressWarnings("null")
	public static NonNullPair<String, Boolean> getEnglishPlural(final String s) {
		assert s != null;
		if (s.isEmpty())
			return new NonNullPair<>("", Boolean.FALSE);
		for (final String[] p : plurals) {
			if (s.endsWith(p[1]))
				return new NonNullPair<>(s.substring(0, s.length() - p[1].length()) + p[0], Boolean.TRUE);
			if (s.endsWith(p[1].toUpperCase(Locale.ENGLISH)))
				return new NonNullPair<>(s.substring(0, s.length() - p[1].length()) + p[0].toUpperCase(Locale.ENGLISH), Boolean.TRUE);
		}
		return new NonNullPair<>(s, Boolean.FALSE);
	}
	
	/**
	 * Gets the english plural of a word.
	 * 
	 * @param s
	 * @return The english plural of the given word
	 */
	public static String toEnglishPlural(final String s) {
		assert s != null && s.length() != 0;
		for (final String[] p : plurals) {
			if (s.endsWith(p[0]))
				return s.substring(0, s.length() - p[0].length()) + p[1];
		}
		assert false;
		return s + "s";
	}
	
	/**
	 * Gets the plural of a word (or not if p is false)
	 * 
	 * @param s
	 * @param p
	 * @return The english plural of the given word, or the word itself if p is false.
	 */
	public static String toEnglishPlural(final String s, final boolean p) {
		if (p)
			return toEnglishPlural(s);
		return s;
	}
	
	/**
	 * Adds 'a' or 'an' to the given string, depending on the first character of the string.
	 * 
	 * @param s The string to add the article to
	 * @return The given string with an appended a/an and a space at the beginning
	 * @see #A(String)
	 * @see #a(String, boolean)
	 */
	public static String a(final String s) {
		return a(s, false);
	}
	
	/**
	 * Adds 'A' or 'An' to the given string, depending on the first character of the string.
	 * 
	 * @param s The string to add the article to
	 * @return The given string with an appended A/An and a space at the beginning
	 * @see #a(String)
	 * @see #a(String, boolean)
	 */
	public static String A(final String s) {
		return a(s, true);
	}
	
	/**
	 * Adds 'a' or 'an' to the given string, depending on the first character of the string.
	 * 
	 * @param s The string to add the article to
	 * @param capA Whether to use a capital a or not
	 * @return The given string with an appended a/an (or A/An if capA is true) and a space at the beginning
	 * @see #a(String)
	 */
	public static String a(final String s, final boolean capA) {
		assert s != null && s.length() != 0;
		if ("aeiouAEIOU".indexOf(s.charAt(0)) != -1) {
			if (capA)
				return "An " + s;
			return "an " + s;
		} else {
			if (capA)
				return "A " + s;
			return "a " + s;
		}
	}
	
	/**
	 * Gets the collision height of solid or partially-solid blocks at the center of the block.
	 * This is mostly for use in the {@link EffTeleport teleport effect}.
	 * <p>
	 * This version operates on numeric ids, thus only working on
	 * Minecraft 1.12 or older.
	 * 
	 * @param type
	 * @return The block's height at the center
	 */
	public static double getBlockHeight(final int type, final byte data) {
		switch (type) {
			case 26: // bed
				return 9. / 16;
			case 44: // slabs
			case 126:
				return (data & 0x8) == 0 ? 0.5 : 1;
			case 78: // snow layer
				return data == 0 ? 1 : (data % 8) * 2. / 16;
			case 85: // fences & gates
			case 107:
			case 113:
			case 139: // cobblestone wall
				return 1.5;
			case 88: // soul sand
				return 14. / 16;
			case 92: // cake
				return 7. / 16;
			case 93: // redstone repeater
			case 94:
			case 149: // redstone comparator
			case 150:
				return 2. / 16;
			case 96: // trapdoor
				return (data & 0x4) == 0 ? ((data & 0x8) == 0 ? 3. / 16 : 1) : 0;
			case 116: // enchantment table
				return 12. / 16;
			case 117: // brewing stand
				return 14. / 16;
			case 118: // cauldron
				return 5. / 16;
			case 120: // end portal frame
				return (data & 0x4) == 0 ? 13. / 16 : 1;
			case 127: // cocoa plant
				return 12. / 16;
			case 140: // flower pot
				return 6. / 16;
			case 144: // mob head
				return 0.5;
			case 151: // daylight sensor
				return 6. / 16;
			case 154: // hopper
				return 10. / 16;
			default:
				return 1;
		}
	}

	/**
	 * Sends a plugin message using the first player from {@link Bukkit#getOnlinePlayers()}.
	 *
	 * The next plugin message to be received through {@code channel} will be assumed to be
	 * the response.
	 *
	 * @param channel the channel for this plugin message
	 * @param data the data to add to the outgoing message
	 * @return a completable future for the message of the responding plugin message, if there is one.
	 * this completable future will complete exceptionally if no players are online.
	 */
	public static CompletableFuture<ByteArrayDataInput> sendPluginMessage(String channel, String... data) {
		return sendPluginMessage(channel, r -> true, data);
	}

	/**
	 * Sends a plugin message using the from {@code player}.
	 *
	 * The next plugin message to be received through {@code channel} will be assumed to be
	 * the response.
	 *
	 * @param player the player to send the plugin message through
	 * @param channel the channel for this plugin message
	 * @param data the data to add to the outgoing message
	 * @return a completable future for the message of the responding plugin message, if there is one.
	 * this completable future will complete exceptionally if no players are online.
	 */
	public static CompletableFuture<ByteArrayDataInput> sendPluginMessage(Player player, String channel, String... data) {
		return sendPluginMessage(player, channel, r -> true, data);
	}

	/**
	 * Sends a plugin message using the first player from {@link Bukkit#getOnlinePlayers()}.
	 *
	 * @param channel the channel for this plugin message
	 * @param messageVerifier verifies that a plugin message is the response to the sent message
	 * @param data the data to add to the outgoing message
	 * @return a completable future for the message of the responding plugin message, if there is one.
	 * this completable future will complete exceptionally if the player is null.
	 * @throws IllegalStateException when there are no players online
	 */
	public static CompletableFuture<ByteArrayDataInput> sendPluginMessage(String channel,
			Predicate<ByteArrayDataInput> messageVerifier, String... data) throws IllegalStateException {
		Player firstPlayer = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
		if (firstPlayer == null)
			throw new IllegalStateException("There are no players online");
		return sendPluginMessage(firstPlayer, channel, messageVerifier, data);
	}

	/**
	 * Sends a plugin message.
	 *
	 * Example usage using the "GetServers" bungee plugin message channel via an overload:
	 * <code>
	 *     Utils.sendPluginMessage("BungeeCord", r -> "GetServers".equals(r.readUTF()), "GetServers")
	 *     			.thenAccept(response -> Bukkit.broadcastMessage(response.readUTF()) // comma delimited server broadcast
	 *     			.exceptionally(ex -> {
	 *     			 	Skript.warning("Failed to get servers because there are no players online");
	 *     			 	return null;
	 *     			});
	 * </code>
	 *
	 * @param player the player to send the plugin message through
	 * @param channel the channel for this plugin message
	 * @param messageVerifier verifies that a plugin message is the response to the sent message
	 * @param data the data to add to the outgoing message
	 * @return a completable future for the message of the responding plugin message, if there is one.
	 * this completable future will complete exceptionally if the player is null.
	 */
	public static CompletableFuture<ByteArrayDataInput> sendPluginMessage(Player player, String channel,
			Predicate<ByteArrayDataInput> messageVerifier, String... data) {
		CompletableFuture<ByteArrayDataInput> completableFuture = new CompletableFuture<>();

		Skript skript = Skript.getInstance();
		Messenger messenger = Bukkit.getMessenger();

		messenger.registerOutgoingPluginChannel(skript, channel);

		PluginMessageListener listener = (sendingChannel, sendingPlayer, message) -> {
			ByteArrayDataInput input = ByteStreams.newDataInput(message);
			if (channel.equals(sendingChannel) && sendingPlayer == player && !completableFuture.isDone()
					&& !completableFuture.isCancelled() && messageVerifier.test(input)) {
				completableFuture.complete(input);
			}
		};

		messenger.registerIncomingPluginChannel(skript, channel, listener);

		completableFuture.whenComplete((r, ex) -> messenger.unregisterIncomingPluginChannel(skript, channel, listener));

		// if we haven't gotten a response after a minute, let's just assume there wil never be one
		Bukkit.getGlobalRegionScheduler().runDelayed(skript, (ignored) -> {

			if (!completableFuture.isDone())
				completableFuture.cancel(true);

		}, 60 * 20);

		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		Stream.of(data).forEach(out::writeUTF);
		player.sendPluginMessage(Skript.getInstance(), channel, out.toByteArray());

		return completableFuture;
	}
	
	final static ChatColor[] styles = {ChatColor.BOLD, ChatColor.ITALIC, ChatColor.STRIKETHROUGH, ChatColor.UNDERLINE, ChatColor.MAGIC, ChatColor.RESET};
	final static Map<String, String> chat = new HashMap<>();
	final static Map<String, String> englishChat = new HashMap<>();
	
	public final static boolean HEX_SUPPORTED = Skript.isRunningMinecraft(1, 16);
	public final static boolean COPY_SUPPORTED = Skript.isRunningMinecraft(1, 15);
	
	static {
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				final boolean english = englishChat.isEmpty();
				chat.clear();
				for (final ChatColor style : styles) {
					for (final String s : Language.getList("chat styles." + style.name())) {
						chat.put(s.toLowerCase(Locale.ENGLISH), style.toString());
						if (english)
							englishChat.put(s.toLowerCase(Locale.ENGLISH), style.toString());
					}
				}
			}
		});
	}
	
	@Nullable
	public static String getChatStyle(final String s) {
		SkriptColor color = SkriptColor.fromName(s);
		
		if (color != null)
			return color.getFormattedChat();
		return chat.get(s);
	}
	
	private final static Pattern stylePattern = Pattern.compile("<([^<>]+)>");
	
	/**
	 * Replaces &lt;chat styles&gt; in the message
	 * 
	 * @param message
	 * @return message with localised chat styles converted to Minecraft's format
	 */
	public static String replaceChatStyles(final String message) {
		if (message.isEmpty())
			return message;
		String m = StringUtils.replaceAll(Matcher.quoteReplacement("" + message.replace("<<none>>", "")), stylePattern, new Callback<String, Matcher>() {
			@Override
			public String run(final Matcher m) {
				SkriptColor color = SkriptColor.fromName("" + m.group(1));
				if (color != null)
					return color.getFormattedChat();
				final String tag = m.group(1).toLowerCase(Locale.ENGLISH);
				final String f = chat.get(tag);
				if (f != null)
					return f;
				if (HEX_SUPPORTED && tag.startsWith("#")) { // Check for parsing hex colors
					ChatColor chatColor = parseHexColor(tag);
					if (chatColor != null)
						return chatColor.toString();
				}
				return "" + m.group();
			}
		});
		assert m != null;
		// Restore user input post-sanitization
		// Sometimes, the message has already been restored
		if (!message.equals(m)) {
			m = m.replace("\\$", "$").replace("\\\\", "\\");
		}
		m = ChatColor.translateAlternateColorCodes('&', "" + m);
		return "" + m;
	}
	
	/**
	 * Replaces english &lt;chat styles&gt; in the message. This is used for messages in the language file as the language of colour codes is not well defined while the language is
	 * changing, and for some hardcoded messages.
	 * 
	 * @param message
	 * @return message with english chat styles converted to Minecraft's format
	 */
	public static String replaceEnglishChatStyles(final String message) {
		if (message.isEmpty())
			return message;
		String m = StringUtils.replaceAll(Matcher.quoteReplacement(message), stylePattern, new Callback<String, Matcher>() {
			@Override
			public String run(final Matcher m) {
				SkriptColor color = SkriptColor.fromName("" + m.group(1));
				if (color != null)
					return color.getFormattedChat();
				final String tag = m.group(1).toLowerCase(Locale.ENGLISH);
				final String f = englishChat.get(tag);
				if (f != null)
					return f;
				if (HEX_SUPPORTED && tag.startsWith("#")) { // Check for parsing hex colors
					ChatColor chatColor = parseHexColor(tag);
					if (chatColor != null)
						return chatColor.toString();
				}
				return "" + m.group();
			}
		});
		assert m != null;
		// Restore user input post-sanitization
		// Sometimes, the message has already been restored
		if (!message.equals(m)) {
			m = m.replace("\\$", "$").replace("\\\\", "\\");
		}
		m = ChatColor.translateAlternateColorCodes('&', "" + m);
		return "" + m;
	}

	private static final Pattern HEX_PATTERN = Pattern.compile("(?i)#?[0-9a-f]{6}");

	/**
	 * Tries to get a {@link ChatColor} from the given string.
	 * @param hex The hex code to parse.
	 * @return The ChatColor, or null if it couldn't be parsed.
	 */
	@SuppressWarnings("null")
	@Nullable
	public static ChatColor parseHexColor(String hex) {
		if (!HEX_SUPPORTED || !HEX_PATTERN.matcher(hex).matches()) // Proper hex code validation
			return null;
		
		hex = hex.replace("#", "");
		try {
			return ChatColor.of('#' + hex.substring(0, 6));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	/**
	 * Gets a random value between <tt>start</tt> (inclusive) and <tt>end</tt> (exclusive)
	 * 
	 * @param start
	 * @param end
	 * @return <tt>start + random.nextInt(end - start)</tt>
	 */
	public static int random(final int start, final int end) {
		if (end <= start)
			throw new IllegalArgumentException("end (" + end + ") must be > start (" + start + ")");
		return start + random.nextInt(end - start);
	}

	/**
	 * @see #highestDenominator(Class, Class[])
	 */
	public static Class<?> getSuperType(final Class<?>... classes) {
		return highestDenominator(Object.class, classes);
	}

	/**
	 * Searches for the highest common denominator of the given types;
	 * in other words, the first supertype they all share.
	 *
	 * <h3>Arbitrary Selection</h3>
	 * Classes may have <b>multiple</b> highest common denominators: interfaces that they share
	 * which do not extend each other.
	 * This method selects a <b>superclass</b> first (where possible)
	 * but its selection of interfaces is quite random.
	 * For this reason, it is advised to specify a "best guess" class as the first parameter, which will be selected if
	 * it's appropriate.
	 * Note that if the "best guess" is <i>not</i> a real supertype, it can never be selected.
	 *
	 * @param bestGuess The fallback class to guess
	 * @param classes The types to check
	 * @return The most appropriate common class of all provided
	 * @param <Found> The highest common denominator found
	 * @param <Type> The input type spread
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public static <Found, Type extends Found> Class<Found> highestDenominator(Class<? super Found> bestGuess, @NotNull Class<? extends Type> @NotNull ... classes) {
		assert classes.length > 0;
		Class<?> chosen = classes[0];
		outer:
		for (final Class<?> checking : classes) {
			assert checking != null && !checking.isArray() && !checking.isPrimitive() : checking;
			if (chosen.isAssignableFrom(checking))
				continue;
			Class<?> superType = checking;
			do if (superType != Object.class && superType.isAssignableFrom(chosen)) {
				chosen = superType;
				continue outer;
			}
			while ((superType = superType.getSuperclass()) != null);
			for (final Class<?> anInterface : checking.getInterfaces()) {
				superType = highestDenominator(Object.class, anInterface, chosen);
				if (superType != Object.class) {
					chosen = superType;
					continue outer;
				}
			}
			return (Class<Found>) bestGuess;
		}
		if (!bestGuess.isAssignableFrom(chosen)) // we struck out on a type we don't want
			return (Class<Found>) bestGuess;
		// Cloneable is about as useful as object as super type
		// However, it lacks special handling used for Object supertype
		// See #1747 to learn how it broke returning items from functions
		return (Class<Found>) (chosen == Cloneable.class ? bestGuess : chosen == Object.class ? bestGuess : chosen);
	}
	
	/**
	 * Parses a number that was validated to be an integer but might still result in a {@link NumberFormatException} when parsed with {@link Integer#parseInt(String)} due to
	 * overflow.
	 * This method will return {@link Integer#MIN_VALUE} or {@link Integer#MAX_VALUE} respectively if that happens.
	 * 
	 * @param s
	 * @return The parsed integer, {@link Integer#MIN_VALUE} or {@link Integer#MAX_VALUE} respectively
	 */
	public static int parseInt(final String s) {
		assert s.matches("-?\\d+");
		try {
			return Integer.parseInt(s);
		} catch (final NumberFormatException e) {
			return s.startsWith("-") ? Integer.MIN_VALUE : Integer.MAX_VALUE;
		}
	}
	
	/**
	 * Parses a number that was validated to be an integer but might still result in a {@link NumberFormatException} when parsed with {@link Long#parseLong(String)} due to
	 * overflow.
	 * This method will return {@link Long#MIN_VALUE} or {@link Long#MAX_VALUE} respectively if that happens.
	 * 
	 * @param s
	 * @return The parsed long, {@link Long#MIN_VALUE} or {@link Long#MAX_VALUE} respectively
	 */
	public static long parseLong(final String s) {
		assert s.matches("-?\\d+");
		try {
			return Long.parseLong(s);
		} catch (final NumberFormatException e) {
			return s.startsWith("-") ? Long.MIN_VALUE : Long.MAX_VALUE;
		}
	}
	
	/**
	 * Gets class for name. Throws RuntimeException instead of checked one.
	 * Use this only when absolutely necessary.
	 * @param name Class name.
	 * @return The class.
	 */
	public static Class<?> classForName(String name) {
		Class<?> c;
		try {
			c = Class.forName(name);
			return c;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Class not found!");
		}
	}
	
	/**
	 * Finds the index of the last in a {@link List} that matches the given {@link Checker}.
	 *
	 * @param list the {@link List} to search.
	 * @param checker the {@link Checker} to match elements against.
	 * @return the index of the element found, or -1 if no matching element was found.
	 */
	public static <T> int findLastIndex(List<T> list, Checker<T> checker) {
		int lastIndex = -1;
		for (int i = 0; i < list.size(); i++) {
			if (checker.check(list.get(i)))
				lastIndex = i;
		}
		return lastIndex;
	}

	public static boolean isInteger(Number... numbers) {
		for (Number number : numbers) {
			if (Double.class.isAssignableFrom(number.getClass()) || Float.class.isAssignableFrom(number.getClass()))
				return false;
		}
		return true;
	}
	
}
