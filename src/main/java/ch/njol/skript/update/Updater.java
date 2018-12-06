package ch.njol.skript.update;

import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.util.Task;

/**
 * Extensible updater system.
 */
public abstract class Updater {
	
	/**
	 * Release that is currently in use.
	 */
	private final ReleaseManifest currentRelease;
	
	/**
	 * Update checker used by this build.
	 */
	private final UpdateChecker updateChecker;
	
	/**
	 * Release channel currently in use.
	 */
	@Nullable
	private volatile ReleaseChannel releaseChannel;
	
	/**
	 * Current state of the updater.
	 */
	private volatile UpdaterState state;
	
	/**
	 * Status of the release.
	 */
	private volatile ReleaseStatus releaseStatus;
	
	/**
	 * How often to check for updates. 0 to not check automatically at all.
	 */
	private volatile long checkFrequency;
	
	/**
	 * Update manifest, if it exists.
	 */
	@Nullable
	private volatile UpdateManifest updateManifest;
	
	protected Updater(ReleaseManifest manifest) {
		this.currentRelease = manifest;
		this.updateChecker = manifest.createUpdateChecker();
		this.state = UpdaterState.NOT_STARTED;
		this.releaseStatus = ReleaseStatus.UNKNOWN;
	}
	
	/**
	 * Fetches the update manifest. Release channel must have been set before
	 * this is done. Note that this will not have side effects to this Updater
	 * instance.
	 * @return Future that will contain update manifest or null if no updates
	 * are available in current channel.
	 */
	public CompletableFuture<UpdateManifest> fetchUpdateManifest() {
		ReleaseChannel channel = releaseChannel;
		if (channel == null) {
			throw new IllegalStateException("release channel must be specified");
		}
		// Just check that channel name is in update name
		return updateChecker.check(currentRelease, channel);
	}
	
	/**
	 * Checks for updates. This mutates the object it is called on.
	 */
	public synchronized void checkUpdates() {
		state = UpdaterState.CHECKING; // We started checking for updates
		fetchUpdateManifest().thenAccept((manifest) -> {
			if (manifest != null) {
				releaseStatus = ReleaseStatus.OUTDATED; // Update available
				updateManifest = manifest;
			} else {
				releaseStatus = ReleaseStatus.LATEST;
				// TODO handle ReleaseStatus.CUSTOM
			}
			
			state = UpdaterState.INACTIVE; // In any case, we finished now
			
			// Call this again later
			long ticks = checkFrequency;
			if (ticks > 0) {
				new Task(Skript.getInstance(), ticks, true) {
					
					@Override
					public void run() {
						checkUpdates();
					}
				};
			}
		}).whenComplete((none, e) -> {
			state = UpdaterState.ERROR;
			Skript.exception(e, "checking for updates failed");
			
		});
		// TODO UpdaterState.ERROR on error
	}
	
	public void setReleaseChannel(ReleaseChannel channel) {
		this.releaseChannel = channel;
	}
	
	/**
	 * Sets update check frequency. This will automatically schedule next
	 * check to happen after given amount of ticks, too.
	 * @param ticks Frequency in ticks.
	 */
	public void setCheckFrequency(long ticks) {
		this.checkFrequency = ticks;
		checkUpdates();
	}
	
	public UpdaterState getState() {
		return state;
	}
	
	public ReleaseStatus getReleaseStatus() {
		return releaseStatus;
	}
	
	@Nullable
	public UpdateManifest getUpdateManifest() {
		return updateManifest;
	}
}
