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

package ch.njol.skript.timings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.njol.skript.Skript;

import com.google.common.collect.Lists;

/**
 * Timing for certain action.
 */
public class Timing {
	
	public class Capture {
		
		final String thread;
		
		long start;
		long end;
		
		long paused; // Time while paused
		long pauseBegin;
		
		protected Capture(final Thread thread) {
			String name = thread.getName();
			assert name != null;
			this.thread = name;
		}
		
		public void start() {
			start = System.nanoTime();
		}
		
		public void stop() {
			end = System.nanoTime();
		}
		
		public void pause() {
			pauseBegin = System.nanoTime();
		}
		
		public void unpause() {
			long pauseTime = System.nanoTime() - pauseBegin;
			paused += pauseTime;
		}
		
		public long result() {
			if (end == 0L)
				end = System.nanoTime();
			
			return end - start - paused;
		}
		
		public boolean isOf(Thread t) {
			return (thread.equals(t.getName()));
		}
	}
	
	private List<Capture> captures = new ArrayList<Capture>();
	private Map<Thread,Capture> inProgress;
	
	/**
	 * Creates a new timing. Only used by {@link Timings}
	 */
	protected Timing() {
		captures = new ArrayList<Capture>();
		inProgress = new HashMap<Thread,Capture>();
	}
	
	/**
	 * Starts timing measurement for caller thread.
	 * @return Timing for chaining
	 */
	public Timing start() {
		Thread current = Thread.currentThread();
		assert current != null;
		if (inProgress.containsKey(current)) {
			inProgress.get(current).stop();
			inProgress.remove(current);
		}
		
		Capture c = new Capture(current);
		c.start();
		captures.add(c);
		inProgress.put(current, c);
		
		return this;
	}
	
	/**
	 * Stops timing measurements for caller thread if it was in progress.
	 */
	public void stop() {
		Thread current = Thread.currentThread();
		assert current != null;
		if (inProgress.containsKey(current)) {
			inProgress.get(current).stop();
			inProgress.remove(current);
		}
	}
	
	/**
	 * Pauses timing measurement for caller thread until {@link #unpause()} is called.
	 * @return Timing for chaining.
	 */
	public Timing pause() {
		Thread current = Thread.currentThread();
		assert current != null;
		if (inProgress.containsKey(current)) {
			inProgress.get(current).pause();
		}
		
		return this;
	}
	
	/**
	 * Unpauses timing measurement for caller thread if it was paused.
	 * @return Timing for chaining.
	 */
	public Timing unpause() {
		Thread current = Thread.currentThread();
		assert current != null;
		if (inProgress.containsKey(current)) {
			inProgress.get(current).unpause();
		}
		
		return this;
	}
	
	/**
	 * Gets list of captures for given thread. Very expensive to call.
	 * @param thread
	 * @return Captures for given thread
	 */
	public List<Capture> getCaptures(Thread thread) {
		List<Capture> ret = new ArrayList<Capture>();
		for (Capture c : captures)
			if (c.isOf(thread))
				ret.add(c);
		
		return ret;
	}
}
