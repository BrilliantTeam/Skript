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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.njol.skript.localization.Language;
import ch.njol.skript.timings.Timing.Capture;

/**
 * Creates timing reports.
 */
public class TimingReporter {
	
	public static String generateReport() {
		Map<Object,Timing> timings = Timings.timings;
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format(Language.get("timings.start"), ((Timings.disableTime - Timings.enableTime)) / 1000000000));
		Map<String,Long> results = parseTimings();
		Iterator<Entry<String, Long>> it = results.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Long> entry = it.next();
			long time = entry.getValue();
			sb.append(entry.getKey() + ": " + time + " (" + (time / 1000000000) + "ms)");
		}
		
		return sb.toString();
	}
	
	@SuppressWarnings("null")
	public static Map<String,Long> parseTimings() {
		Map<String,Long> ret = new HashMap<String,Long>();
		
		Iterator<Entry<Object, Timing>> it = Timings.timings.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Object, Timing> entry = it.next();
			List<Capture> captures = entry.getValue().getCaptures(Thread.currentThread());
			long time = 0;
			for (Capture c : captures)
				time += c.result();
			ret.put(entry.toString(), time);
		}
		
		return ret;
	}
}
