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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.hooks;

import java.io.IOException;
import java.util.Collection;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.eclipse.jdt.annotation.Nullable;
import org.inventivetalent.particle.ParticleEffect;
import org.inventivetalent.particle.ParticlePlugin;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.VisualEffect;

/**
 * @author Peter Güttinger
 *
 */
public class ParticlesHook extends Hook<ParticlePlugin> {
	
	public ParticlesHook() throws IOException {}
	
	public static boolean isAvailable = false;
	
	@Override
	protected boolean init() {
		isAvailable = true;
		
		return true;
	}
	
	@Override
	public String getName() {
		return "ParticleAPI";
	}
	
	private static enum Type {
		// Particles
		FIREWORKS_SPARK(ParticleEffect.FIREWORKS_SPARK),
		CRIT(ParticleEffect.CRIT),
		MAGIC_CRIT(ParticleEffect.CRIT_MAGIC),
		POTION_SWIRL(ParticleEffect.SPELL_MOB),
		POTION_SWIRL_TRANSPARENT(ParticleEffect.SPELL_MOB_AMBIENT),
		SPELL(ParticleEffect.SPELL),
		INSTANT_SPELL(ParticleEffect.SPELL_INSTANT),
		WITCH_MAGIC(ParticleEffect.SPELL_WITCH),
		NOTE(ParticleEffect.NOTE),
		PORTAL(ParticleEffect.PORTAL),
		FLYING_GLYPH(ParticleEffect.ENCHANTMENT_TABLE),
		FLAME(ParticleEffect.FLAME),
		LAVA_POP(ParticleEffect.LAVA),
		FOOTSTEP(ParticleEffect.FOOTSTEP),
		SPLASH(ParticleEffect.WATER_SPLASH),
		PARTICLE_SMOKE(ParticleEffect.SMOKE_NORMAL), // Why separate particle... ?
		EXPLOSION_HUGE(ParticleEffect.EXPLOSION_HUGE),
		EXPLOSION_LARGE(ParticleEffect.EXPLOSION_LARGE),
		EXPLOSION(ParticleEffect.EXPLOSION_NORMAL),
		VOID_FOG(ParticleEffect.SUSPENDED_DEPTH),
		SMALL_SMOKE(ParticleEffect.TOWN_AURA),
		CLOUD(ParticleEffect.CLOUD),
		COLOURED_DUST(ParticleEffect.REDSTONE),
		SNOWBALL_BREAK(ParticleEffect.SNOWBALL),
		WATER_DRIP(ParticleEffect.DRIP_WATER),
		LAVA_DRIP(ParticleEffect.DRIP_LAVA),
		SNOW_SHOVEL(ParticleEffect.SNOW_SHOVEL),
		SLIME(ParticleEffect.SLIME),
		HEART(ParticleEffect.HEART),
		ANGRY_VILLAGER(ParticleEffect.VILLAGER_ANGRY),
		HAPPY_VILLAGER(ParticleEffect.VILLAGER_HAPPY),
		LARGE_SMOKE(ParticleEffect.SMOKE_LARGE),
		ITEM_BREAK(ParticleEffect.ITEM_CRACK),
		TILE_BREAK(ParticleEffect.BLOCK_CRACK),
		TILE_DUST(ParticleEffect.BLOCK_DUST);
		
		final ParticleEffect effect;
		
		private Type(final ParticleEffect effect) {
			this.effect = effect;
		}
		
		/**
		 * Converts the data from the pattern to the data required by Bukkit
		 */
		@Nullable
		public Object getData(final @Nullable Object raw, final Location l) {
			assert raw == null;
			return null;
		}
		
		@Nullable
		public static Type getType(VisualEffect.Type type) {
			Type[] types = Type.values();
			for (Type t : types) {
				if (t.name().equals(type.name())) return t;
			}
			
			return null;
		}
	}
	
	public static boolean canCreate(VisualEffect.Type type) {
		if (!isAvailable) return false;
		
		Type t = Type.getType(type);
		if (t != null) return true;
		
		return false;
	}
	
	
	@SuppressWarnings("null")
	public static void playEffect(Collection<Player> receivers, Location loc, double dX, double dY, double dZ, double speed, VisualEffect.Type type, int count, @Nullable ItemStack item) {
		if (item == null) {
			Type.getType(type).effect.send(receivers, loc.getX(), loc.getY(), loc.getZ(), dX, dY, dZ, speed, count);
		} else {
			Type.getType(type).effect.sendData(receivers, loc.getX(), loc.getY(), loc.getZ(), dX, dY, dZ, speed, count, item);
		}
	}
	
	@SuppressWarnings("null")
	public static void playEffect(double radius, Location loc, double dX, double dY, double dZ, double speed, VisualEffect.Type type, int count, @Nullable ItemStack item) {
		if (item == null) {
			Type.getType(type).effect.send(Bukkit.getOnlinePlayers(), loc.getX(), loc.getY(), loc.getZ(), dX, dY, dZ, speed, count, radius);
		} else {
			Type.getType(type).effect.sendData(Bukkit.getOnlinePlayers(), loc.getX(), loc.getY(), loc.getZ(), dX, dY, dZ, speed, count, item);
		}
	}
	
	public static void playEffect(double radius, Location loc, Color color, VisualEffect.Type type, int count) {
		
	}
}
