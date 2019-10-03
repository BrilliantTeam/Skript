package ch.njol.skript.tests.runner;

import java.nio.file.Path;
import java.nio.file.Paths;

import ch.njol.skript.tests.TestResults;

/**
 * Static utilities for Skript's 'test mode'.
 */
public class TestMode {
	
	private static final String ROOT = "skript.testing.";
	
	/**
	 * Determines if test mode is enabled. In test mode, Skript will not load
	 * normal scripts, working with {@link #TEST_DIR} instead.
	 */
	public static final boolean ENABLED = "true".equals(System.getProperty(ROOT + "enabled"));
	
	/**
	 * Root path for scripts containing tests. If {@link #DEV_MODE} is enabled,
	 * a command will be available to run them individually or collectively.
	 * Otherwise, all tests are run, results are written in JSON format to
	 * {@link #RESULTS_FILE} as in {@link TestResults}.
	 */
	@SuppressWarnings("null")
	public static final Path TEST_DIR = Paths.get(System.getProperty(ROOT + "dir"));
	
	/**
	 * Enable test development mode. Skript will allow individual test scripts
	 * to be loaded and ran, and prints results to chat or console.
	 */
	public static final boolean DEV_MODE = ENABLED && "true".equals(System.getProperty(ROOT + "devMode"));
	
	/**
	 * Path to file where to save results in JSON format.
	 */
	@SuppressWarnings("null")
	public static final Path RESULTS_FILE = Paths.get(System.getProperty(ROOT + "results"));
	
}
