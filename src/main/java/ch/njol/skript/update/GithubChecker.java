package ch.njol.skript.update;

import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;

import ch.njol.skript.Updater.ResponseEntry.AssetsEntry;
import ch.njol.skript.Updater.ResponseEntry.Author;

/**
 * Uses Github API to check for updates.
 */
public class GithubChecker implements UpdateChecker {
	
	/**
	 * Github API response for GSON deserialization.
	 */
	@NonNullByDefault(value = false)
	public static class ResponseEntry {
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
	    
	    public static class AssetsEntry {
	    	public int size;
	    	public int download_count;
	    	public String browser_download_url;
	    }
	    
	    public List<AssetsEntry> assets;
	    public String body; // Description of release
	    
	    @Override
	    public String toString() {
	    	return tag_name;
	    }
	    
	    public static class Author {
	    	public String login;
	    	public int id;
	    }
	    
	    public Author author;
	}

	@Override
	public CompletableFuture<UpdateManifest> check(String updateSource, String releaseChannel) {
		return CompletableFuture.completedFuture(null); // TODO
	}
	
	
}
