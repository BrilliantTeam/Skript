package ch.njol.skript.bukkitutil;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;

import ch.njol.skript.Skript;

/**
 * Maps enchantments to their ids in Minecraft 1.12.
 */
public class EnchantmentIds {
	
	public static final Map<Enchantment,Integer> ids = new HashMap<>();
	public static final Enchantment[] enchantments = new Enchantment[71];
	
	static {
		ids.put(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
		ids.put(Enchantment.PROTECTION_FIRE, 1);
		ids.put(Enchantment.PROTECTION_FALL, 2);
		ids.put(Enchantment.PROTECTION_EXPLOSIONS, 3);
		ids.put(Enchantment.PROTECTION_PROJECTILE, 4);
		ids.put(Enchantment.OXYGEN, 5);
		ids.put(Enchantment.WATER_WORKER, 6);
		ids.put(Enchantment.THORNS, 7);
		ids.put(Enchantment.DEPTH_STRIDER, 8);
		ids.put(Enchantment.FROST_WALKER, 9);
		ids.put(Enchantment.DAMAGE_ALL, 16);
		ids.put(Enchantment.DAMAGE_UNDEAD, 17);
		ids.put(Enchantment.DAMAGE_ARTHROPODS, 18);
		ids.put(Enchantment.KNOCKBACK, 19);
		ids.put(Enchantment.FIRE_ASPECT, 20);
		ids.put(Enchantment.LOOT_BONUS_MOBS, 21);
		ids.put(Enchantment.DIG_SPEED, 32);
		ids.put(Enchantment.SILK_TOUCH, 33);
		ids.put(Enchantment.DURABILITY, 34);
		ids.put(Enchantment.LOOT_BONUS_BLOCKS, 35);
		ids.put(Enchantment.ARROW_DAMAGE, 48);
		ids.put(Enchantment.ARROW_KNOCKBACK, 49);
		ids.put(Enchantment.ARROW_FIRE, 50);
		ids.put(Enchantment.ARROW_INFINITE, 51);
		ids.put(Enchantment.LUCK, 61);
		ids.put(Enchantment.LURE, 62);
		ids.put(Enchantment.MENDING, 70);
		
		if (Skript.isRunningMinecraft(1, 11)) {
			ids.put(Enchantment.BINDING_CURSE, 10);
			ids.put(Enchantment.VANISHING_CURSE, 71);
			ids.put(Enchantment.SWEEPING_EDGE, 22); // Technically 1.11.1, but who runs 1.11 anymore?
		}
		
		for (Map.Entry<Enchantment, Integer> entry : ids.entrySet()) {
			enchantments[entry.getValue()] = entry.getKey();
		}
	}
}
