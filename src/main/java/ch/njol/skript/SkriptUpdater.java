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

import ch.njol.skript.localization.FormattedMessage;
import ch.njol.skript.localization.Message;
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
	
	public final static Message m_not_started = new Message("updater.not started");
	public final static Message m_checking = new Message("updater.checking");
	public final static Message m_check_in_progress = new Message("updater.check in progress");
	public final static FormattedMessage m_check_error = new FormattedMessage("updater.check error", error);
	public final static Message m_running_latest_version = new Message("updater.running latest version");
	public final static Message m_running_latest_version_beta = new Message("updater.running latest version (beta)");
	public final static FormattedMessage m_update_available = new FormattedMessage("updater.update available", latest, Skript.getVersion());
	public final static FormattedMessage m_downloading = new FormattedMessage("updater.downloading", latest);
	public final static Message m_download_in_progress = new Message("updater.download in progress");
	public final static FormattedMessage m_download_error = new FormattedMessage("updater.download error", error);
	public final static FormattedMessage m_downloaded = new FormattedMessage("updater.downloaded", latest);
	public final static Message m_internal_error = new Message("updater.internal error");
	public final static Message m_custom_version = new Message("updater.custom version");
	
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
