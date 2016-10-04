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
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Signature;

/**
 * Loads script from disk to memory and registers function signatures.
 */
public class ScriptLoader implements Runnable {
	
	private File f;
	private ScriptManager manager;
	
	public ScriptLoader(File f, ScriptManager manager) {
		this.f = f;
		this.manager = manager;
	}
	
	@Override
	public void run() {
		try {
			Config config = new Config(f, true, false, ":");
			for (final Node cnode : config.getMainNode()) {
				if (!(cnode instanceof SectionNode)) {
					Skript.error("invalid line - all code has to be put into triggers");
					continue;
				}
				final SectionNode node = ((SectionNode) cnode);
				String key = node.getKey();
				if (key == null)
					continue;
				
				if (key.toLowerCase().startsWith("function ")) { // Just go with dummy parser instance for now
					final Signature<?> func = Functions.loadSignature(config.getFileName(), node, ParserInstance.DUMMY);
				}
			}
			
			manager.loadReady(config);
		} catch (IOException e) {
			// TODO report error
		}
	}
	
}
