package ch.njol.skript.tests;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Tracks failed and succeeded tests.
 */
public class TestTracker {
	
	/**
	 * Started tests.
	 */
	private static final Set<String> startedTests = new HashSet<>();
		
	/**
	 * Failed tests to failure assert messages.
	 */
	private static final Map<String, String> failedTests = new HashMap<>();
	
	@Nullable
	private static String currentTest;
	
	public static void testStarted(String name) {
		startedTests.add(name);
		currentTest = name;
	}
	
	public static void testFailed(String msg) {
		failedTests.put(currentTest, msg);
	}
	
	public static Map<String, String> getFailedTests() {
		return failedTests;
	}
	
	public static Set<String> getSucceededTests() {
		Set<String> tests = new HashSet<>(startedTests);
		tests.removeAll(failedTests.keySet());
		return tests;
	}
	
	public TestResults collectResults() {
		TestResults results = new TestResults(getSucceededTests(), getFailedTests());
		startedTests.clear();
		failedTests.clear();
		return results;
	}
	
}
