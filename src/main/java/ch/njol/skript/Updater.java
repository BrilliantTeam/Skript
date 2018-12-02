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
package ch.njol.skript;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import org.bukkit.plugin.java.JavaPlugin;

import ch.njol.skript.update.ReleaseChannel;
import ch.njol.skript.update.ReleaseManifest;
import ch.njol.skript.update.UpdateChecker;
import ch.njol.skript.update.UpdateManifest;
import ch.njol.skript.update.UpdaterState;

/**
 * Skript's updater checker.
 */
public class Updater {
	
	/**
	 * Release that is currently in use.
	 */
	private final ReleaseManifest currentRelease;
	
	/**
	 * Update checker used by this build.
	 */
	private final UpdateChecker updateChecker;
	
	/**
	 * Current state of the updater.
	 */
	private volatile UpdaterState state;
	
	Updater() {
		String manifest;
		try (InputStream is = Skript.getInstance().getResource("release-manifest.json");
				Scanner s = new Scanner(is)) {
			s.useDelimiter("\\A");
			manifest = s.next();
		} catch (IOException e) {
			throw new IllegalArgumentException("Skript is missing release-manifest.json!");
		}
		assert manifest != null;
		this.currentRelease = ReleaseManifest.load(manifest);
		this.updateChecker = currentRelease.createUpdateChecker();
		this.state = UpdaterState.NOT_STARTED;
	}

	public CompletableFuture<UpdateManifest> checkUpdates() {
		String channel = SkriptConfig.releaseChannel.value();
		if (channel.equals("release")) {
			return updateChecker.check(currentRelease, new ReleaseChannel((update)
					-> !update.contains("-"), channel));
		}
		// Just check that channel name is in update name
		return updateChecker.check(currentRelease, new ReleaseChannel((update)
				-> update.contains(channel), channel));
	}
	
	public void setCheckFrequency(long ticks) {
		// TODO implement scheduled update checks
	}
	
	public UpdaterState getState() {
		return state;
	}
}
