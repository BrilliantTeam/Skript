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
	
	static class TimingObject extends HashMap<String,Timing> {}
	
	private static Map<Object,TimingObject> timings = new HashMap<Object,TimingObject>();
	
	public static Timing of(Object obj, String name) {
		TimingObject map;
		synchronized (timings) {
			if (timings.containsKey(obj)) {
				map = timings.get(obj);
			} else {
				map = new TimingObject();
				timings.put(obj, map);
			}
		}
		
		synchronized (map) {
			if (map.containsKey(name)) {
				return map.get(name);
			} else {
				Timing timing = new Timing();
				map.put(name, timing);
				return timing;
			}
		}
	}
}
