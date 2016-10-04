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

import ch.njol.skript.config.Config;

/**
 * Manages loading of ALL scripts.
 */
public class ScriptManager extends Thread {
	
	private AtomicInteger waitLoading = new AtomicInteger();
	private Map<String,Config> loadMap = new ConcurrentHashMap<>();
	
	private AtomicInteger waitParsing = new AtomicInteger();
	private Map<String,Config> parseMap = new ConcurrentHashMap<>();
	
	/**
	 * Cached thread pool to execute the tasks.
	 */
	private ExecutorService pool = Executors.newCachedThreadPool();
	
	public void load(File[] files) {
		int numScripts = 0;
		
		File[] scripts = new File[files.length]; // All to-be-loaded scripts
		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			if (!name.startsWith("-") && name.endsWith(".sk")) {
				scripts[i] = files[i];
				numScripts++;
			}
		}
		
		if (numScripts == 0) // Load nothing
			return;
		
		loadMap = new ConcurrentHashMap<>();
		waitLoading.set(numScripts);
		
		parseMap = new ConcurrentHashMap<>();
		waitParsing.set(numScripts);
		
		for (File f : scripts) {
			if (f == null) // Non-scripts and disabled scripts
				continue;
			pool.execute(new ScriptLoader(f, this));
		}
		
		while (waitLoading.get() > 0) // Only park this thread if work is not done
			LockSupport.park(); // Then use while for in case spurious unpark happens
		
		for (Entry<String,Config> entry : loadMap.entrySet()) {
			pool.execute(new ParserInstance(entry.getKey(), entry.getValue(), this));
		}
	}
	
	public void loadReady(String file, Config config) {
		int counter = waitLoading.decrementAndGet();
		loadMap.put(file, config);
		if (counter < 1)
			LockSupport.unpark(this);
	}
}
