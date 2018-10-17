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
	 * Type of update checker to use for this release.
	 */
	public final Class<? extends UpdateChecker> updateCheckerType;
	
	/**
	 * Source where updates for this release can be found,
	 * if there are updates.
	 */
	public final String updateSource;
	
	public ReleaseManifest(String id, String date, String flavor, Class<? extends UpdateChecker> updateCheckerType, String updateSource) {
		this.id = id;
		this.date = date;
		this.flavor = flavor;
		this.updateCheckerType = updateCheckerType;
		this.updateSource = updateSource;
	}
}
