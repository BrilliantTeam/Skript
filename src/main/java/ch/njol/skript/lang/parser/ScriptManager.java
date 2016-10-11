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

package ch.njol.skript.lang.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.scheduler.BukkitRunnable;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;

/**
 * Manages loading of ALL scripts.
 * 
 * There should never be more than one instace of this in use at same time.
 */
public class ScriptManager {
	
	private AtomicInteger waitLoading = new AtomicInteger();
	private Map<String,Config> loadMap = new ConcurrentHashMap<>();
	
	private AtomicInteger waitParsing = new AtomicInteger();
	@SuppressWarnings("null")
	private List<ParserInstance> parsed = Collections.synchronizedList(new ArrayList<>());	
	/**
	 * Cached thread pool to execute the tasks.
	 */
	@SuppressWarnings("null")
	private ExecutorService pool = Executors.newCachedThreadPool();
	
	@Nullable
	private Thread lockedThread;
	
	private static final ReentrantLock lock = new ReentrantLock(true);
	
	/**
	 * Loads and then enables given scripts. This method is ran asynchronously,
	 * then synchronously to enable scripts in server's main thread.
	 * 
	 * This method will wait (on another thread) if script loading is
	 * in progress. When it has finished, this will load whatever was originally
	 * asked.
	 * @param files
	 */
	public void loadAndEnable(File[] files) {
		pool.execute(new Runnable() {

			@Override
			public void run() {
				Skript.debug("Trying to load scripts asynchronously...");
				List<ParserInstance> parsed = load(files); // Parsed scripts; this is blocking operation
				ScriptLoader.unloadScripts(files); // Unload what was reloaded
				ScriptLoader.enableScripts(parsed); // Re-enable what was unloaded
			}
			
		});
	}
	
	/**
	 * Loads scripts from given files. Note that this is run in caller thread, which is
	 * then parked - do use separate thread to call this.
	 * @param files List of scripts.
	 * @return Parser instances that were completed.
	 */
	@SuppressWarnings("null")
	public List<ParserInstance> load(File[] files) {
		lock.lock();
		Skript.debug("Lock acquired for loading scripts");
		lockedThread = Thread.currentThread();
		
		int numScripts = files.length;
		
		if (numScripts == 0) // Load nothing
			return Collections.emptyList();
		
		loadMap = new ConcurrentHashMap<>();
		waitLoading.set(numScripts);
		
		parsed = Collections.synchronizedList(new ArrayList<>());
		waitParsing.set(numScripts);
		
		for (File f : files) {
			if (f == null) // Non-scripts and disabled scripts
				continue;
			pool.execute(new LoaderInstance(f.getName(), f, this, pool));
		}
		
		while (waitLoading.get() > 0) // Only park this thread if work is not done
			LockSupport.park(); // Then use while in case spurious unpark happens
		
		for (Entry<String,Config> entry : loadMap.entrySet()) {
			pool.execute(new ParserInstance(entry.getKey(), entry.getValue(), this));
		}
		
		while (waitParsing.get() > 0)
			LockSupport.park();
		
		
		lock.unlock();
		parsed.sort(null); // Sort alphabetically to not break everything
		return new ArrayList<>(parsed); // Return non-synchronized copy for speed
	}
	
	public void loadReady(String file, Config config) {
		int counter = waitLoading.decrementAndGet();
		loadMap.put(file, config);
		if (counter < 1)
			LockSupport.unpark(lockedThread);
	}
	
	public void parseReady(ParserInstance pi) {
		int counter = waitParsing.decrementAndGet();
		parsed.add(pi);
		if (counter < 1)
			LockSupport.unpark(lockedThread);
	}
}
