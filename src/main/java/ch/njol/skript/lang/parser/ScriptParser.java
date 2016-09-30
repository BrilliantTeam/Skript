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
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import ch.njol.skript.config.Config;

/**
 *
 */
public class ScriptParser implements Runnable {
	
	private Config config;
	private Queue<Config> queue;
	private AtomicInteger counter;
	
	public ScriptParser(Config config, Queue<Config> queue, AtomicInteger counter) {
		this.config = config;
		this.queue = queue;
		this.counter = counter;
	}
	
	@Override
	public void run() {
		
	}
	
}
