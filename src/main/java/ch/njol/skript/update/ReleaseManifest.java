package ch.njol.skript.update;


/**
 * Describes a Skript release.
 */
public class ReleaseManifest {
	
	/**
	 * Release id, for example "2.3".
	 */
	public final String id;
	
	/**
	 * When the release was published.
	 */
	public final String date;
	
	/**
	 * Flavor of the release. For example "github" or "custom".
	 */
	public final String flavor;
	
	/**
	 * URL where updates for this release might be found.
	 */
	public final String updateUrl;
	
	public ReleaseManifest(String id, String date, String flavor, String updateUrl) {
		this.id = id;
		this.date = date;
		this.flavor = flavor;
		this.updateUrl = updateUrl;
	}
}
