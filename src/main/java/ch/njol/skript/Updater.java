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

package ch.njol.skript;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.bind.DatatypeConverter;

import org.bukkit.command.CommandSender;
import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import ch.njol.skript.LegacyUpdater.VersionInfo;
import ch.njol.skript.Updater.ResponseEntry;
import ch.njol.skript.localization.FormattedMessage;
import ch.njol.skript.localization.Message;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.Version;

/**
 * Skript's new updater, which uses Github API.
 */
public class Updater {
	
	public static final String RELEASES_URL  = "https://api.github.com/repos/bensku/Skript/releases";
	
	private static final Gson gson = new Gson();
	
	final static AtomicReference<String> error = new AtomicReference<String>();
	
	public final static List<VersionInfo> infos = new ArrayList<VersionInfo>();
	public final static AtomicReference<VersionInfo> latest = new AtomicReference<VersionInfo>();
	
	// must be down here as they reference 'error' and 'latest' which are defined above
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
	
	/**
	 * Github API response for GSON deserialization.
	 */
	@NonNullByDefault(value = false)
	public class ResponseEntry {
		public String url;
	    public String assets_url;
	    public String upload_url;
	    public String html_url;
	    public int id;
	    public String tag_name;
	    public String target_commitish;
	    public String name;
	    public boolean draft;
	    
	    public boolean prerelease;
	    public String created_at;
	    public String published_at;
	    
	    public class AssetsEntry {
	    	public int size;
	    	public int download_count;
	    	public String browser_download_url;
	    }
	    
	    public List<AssetsEntry> assets;
	    public String body; // Description of release
	}
	
	static List<ResponseEntry> deserialize(String str) {
		assert str != null : "Cannot deserialize null string";
		@SuppressWarnings("serial")
		Type listType = new TypeToken<List<ResponseEntry>>() {}.getType();
		List<ResponseEntry> responses = gson.fromJson(str, listType);
		assert responses != null;
		
		return responses;
	}
	
	static boolean isLatest(List<ResponseEntry> responses) {
		String ver = Skript.getInstance().getDescription().getVersion();
		return ver.endsWith(responses.get(0).tag_name);
	}
	
	@SuppressWarnings("null")
	private static Calendar parseReleaseTime(String date) {
		assert date != null : "Cannot parse null date!";
		return DatatypeConverter.parseDateTime(date);
	}
	
	public class UpdaterThread extends Thread {
		
		private CommandSender sender;
		
		public UpdaterThread(CommandSender sender) {
			this.sender = sender;
		}
		
		public String tryLoadReleases(URL url) throws IOException, SocketTimeoutException {
			Scanner scan = null;
			try {
				scan = new Scanner(url.openStream(), "UTF-8");
				String out = scan.useDelimiter("\\A").next();
				if (out == null)
					throw new IOException("Null output from scanner!");
				return out;
			} finally {
				if (scan != null)
					scan.close();
			}
		}
		
		public boolean performUpdate(List<ResponseEntry> releases) throws IOException, SocketTimeoutException {
			ResponseEntry current = null;
			String ver = Skript.getInstance().getDescription().getVersion();
			for (ResponseEntry release : releases) {
				if (ver.endsWith(release.tag_name)) {
					current = release;
					break;
				}
			}
			if (current == null) { // Non-baseline version. Fail gracefully!
				Skript.info(sender, "" + m_custom_version);
				return true;
			}
			ResponseEntry latest = releases.get(0);
			
			return false;
		}
		
		@Override
		public void run() {
			URL url = null;
			try {
				url = new URL(RELEASES_URL); // Create URL
			} catch (MalformedURLException e) {
				Skript.info(sender, "" + m_internal_error);
				e.printStackTrace();
			}
			assert url != null;
			
			int maxTries = SkriptConfig.updaterDownloadTries.value();
			int tries = 0;
			String response = null;
			while (response == null) {
				try {
					response = tryLoadReleases(url);
				} catch (SocketTimeoutException e) {
					
				} catch (IOException e) {
					error.set(ExceptionUtils.toString(e));
					Skript.info(sender, "" + m_check_error);
				}
				
				tries++;
				if (tries > maxTries && response == null) {
					error.set("Can't reach update server (SocketTimeoutException)");
					Skript.info(sender, "" + m_check_error);
					return;
				}
			}
			assert response != null;
			
			List<ResponseEntry> entries = deserialize(response);
			boolean latest = isLatest(entries);
			
			if (!latest) {
				tries = 0;
				boolean result = false;
				while (!result) {
					try {
					result = performUpdate(entries);
					} catch (SocketTimeoutException e) {
						
					} catch (IOException e) {
					
					}
					if (tries > maxTries && response == null) {
						error.set("Can't reach update server (SocketTimeoutException)");
						Skript.info(sender, "" + m_check_error);
					}
				}
			}
		}
	}
}
