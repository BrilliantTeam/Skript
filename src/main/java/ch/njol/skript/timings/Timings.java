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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Static utils for Skript timings.
 */
public class Timings {
	
	private static Map<String,Timing> timings = new HashMap<String,Timing>();
	
	public static Timing of(String name) {
		Timing timing;
		synchronized (timings) {
			if (timings.containsKey(name)) {
				timing = timings.get(name);
			} else {
				timing = new Timing();
				timings.put(name, timing);
			}
		}
		
		assert timing != null;
		return timing;
	}
}
