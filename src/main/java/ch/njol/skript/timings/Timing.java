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

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

public class Timing {
	
	public class Capture {
		
		Timing parent;
		
		protected Capture(Timing parent) {
			this.parent = parent;
		}
		
		public void start() {
			
		}
	}
	
	private List<Capture> captures = new ArrayList<Capture>();
	
	/**
	 * Creates a new timing. Only used for {@link Timings}
	 */
	protected Timing() {
		captures = new ArrayList<Capture>();
	}
	
	/**
	 * Creates a capture for timing.
	 * @return
	 */
	public Capture capture() {
		return new Capture(this);
	}
}
