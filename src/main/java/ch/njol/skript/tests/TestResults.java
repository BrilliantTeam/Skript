package ch.njol.skript.tests;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Test results.
 */
public class TestResults {
	
	/**
	 * Succeeded tests.
	 */
	private final Set<String> succeeded;
	
	/**
	 * Failed tests.
	 */
	private final Map<String, String> failed;
	
	public TestResults(Set<String> succeeded, Map<String, String> failed) {
		this.succeeded = succeeded;
		this.failed = failed;
	}
	
	public Set<String> getSucceeded() {
		return succeeded;
	}
	
	public Map<String, String> getFailed() {
		return failed;
	}
	
}
