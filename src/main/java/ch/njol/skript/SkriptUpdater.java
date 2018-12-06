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
import ch.njol.skript.update.Updater;
import ch.njol.skript.update.UpdaterState;

/**
 * Skript's updater checker.
 */
public class SkriptUpdater extends Updater {
	
	SkriptUpdater() {
		super(loadManifest());
	}
	
	/**
	 * Loads the release manifest from Skript jar.
	 * @return Release manifest.
	 */
	private static ReleaseManifest loadManifest() {
		String manifest;
		try (InputStream is = Skript.getInstance().getResource("release-manifest.json");
				Scanner s = new Scanner(is)) {
			s.useDelimiter("\\A");
			manifest = s.next();
		} catch (IOException e) {
			throw new IllegalStateException("Skript is missing release-manifest.json!");
		}
		assert manifest != null;
		return ReleaseManifest.load(manifest);
	}
}
