package ch.njol.skript.tests.platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test environment information.
 */
public class Environment {
	
	/**
	 * Name of this environment. For example, spigot-1.14.
	 */
	private final String name;
	
	/**
	 * Resource that needs to be downloaded for the environment.
	 */
	public static class Resource {
		
		/**
		 * Where to get this resource.
		 */
		private String source;
		
		/**
		 * Path under platform root where it should be placed.
		 * Directories created as needed.
		 */
		private String target;

		public Resource(String url, String target) {
			this.source = url;
			this.target = target;
		}
		
		public String getSource() {
			return source;
		}

		
		public String getTarget() {
			return target;
		}
		
	}
	
	/**
	 * Resources that need to be copied.
	 */
	private final List<Resource> resources;
	
	/**
	 * Resources that need to be downloaded.
	 */
	private final List<Resource> downloads;
	
	/**
	 * Where Skript should be placed under platform root.
	 * Directories created as needed.
	 */
	private final String skriptTarget;
	
	/**
	 * Added after platform's own JVM flags.
	 */
	private final String[] commandLine;
	
	public Environment(String name, List<Resource> resources, List<Resource> downloads, String skriptTarget, String... commandLine) {
		this.name = name;
		this.resources = resources;
		this.downloads = downloads;
		this.skriptTarget = skriptTarget;
		this.commandLine = commandLine;
	}
	
	public String getName() {
		return name;
	}

	public void initialize(Path dataRoot, Path runnerRoot, boolean remake) throws IOException {
		Path env = runnerRoot.resolve(name);
		boolean onlyCopySkript = Files.exists(env) && !remake;
		
		// Copy Skript to platform
		Path skript = env.resolve(skriptTarget);
		Files.createDirectories(skript.getParent());
		try {
			Files.copy(new File(getClass().getProtectionDomain().getCodeSource().getLocation()
				    .toURI()).toPath(), skript);
		} catch (URISyntaxException e) {
			throw new AssertionError(e);
		}
		
		if (onlyCopySkript) {
			return;
		}
		
		// Copy resources
		for (Resource resource : resources) {
			Path source = dataRoot.resolve(resource.getSource());
			Path target = env.resolve(resource.getTarget());
			Files.createDirectories(target.getParent());
			Files.copy(source, target);
		}
		
		// Download additional resources
		for (Resource resource : downloads) {
			assert resource != null;
			URL url = new URL(resource.getSource());
			Path target = env.resolve(resource.getTarget());
			Files.createDirectories(target.getParent());
			try (InputStream is = url.openStream()) {
				Files.copy(is, target);
			}
		}
	}
	
	public void runTests(Path root, boolean devMode, String... jvmArgs) throws IOException, InterruptedException {
		Path env = root.resolve(name);
		List<String> args = new ArrayList<>();
		args.add("-Dskript.testing.enabled=true");
		args.add("-Dskript.testing.dir=test_cases");
		args.add("-Dskript.testing.devMode=" + devMode);
		args.add("-Dskript.testing.results=test_results.json");
		args.addAll(Arrays.asList(commandLine));
		
		args.addAll(Arrays.asList(jvmArgs));
		Process process = new ProcessBuilder(args).directory(env.toFile()).start();
		process.waitFor();
	}
}
