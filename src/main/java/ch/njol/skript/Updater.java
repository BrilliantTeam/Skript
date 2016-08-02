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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import ch.njol.skript.Updater.ResponseEntry;

/**
 * Skript's new updater, which uses Github API.
 */
public class Updater {
	
	public static final String RELEASES_URL  = "https://api.github.com/repos/bensku/Skript/releases";
	
	private static final Gson gson = new Gson();
	
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
	
	public static List<ResponseEntry> deserialize(String str) {
		@SuppressWarnings("serial")
		TypeToken<List<ResponseEntry>> listType = new TypeToken<List<ResponseEntry>>() {};
		
		return new ArrayList<ResponseEntry>(); // TODO
	}
}
