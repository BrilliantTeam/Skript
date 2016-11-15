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

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.LogHandler;
import ch.njol.skript.log.ParseLogHandler;

/**
 * Interface for logging during parsing.
 */
public interface ParseLogger {
	
	/**
	 * Submits a parse log handler. Errors will be displayed
	 * when enabling scripts, which allows them to be ordered.
	 * 
	 * It is not recommended to write anything to log after submitting it.
	 * @param log Log handler.
	 */
	void submitErrorLog(LogHandler log);
	
	void submitParseLog(ParseLogHandler log);
	
	void error(String msg, ErrorQuality quality);
	
	void error(String msg);
	
	void warning(String msg);
	
	void info(String msg);
	
	default void debug(String msg) {
		if (Skript.debug())
			info(msg);
	}
	
	void log(LogEntry entry);
	
	/**
	 * Sets node for this parser instance.
	 * @param node Node.
	 */
	void setNode(@Nullable Node node);
	
	/**
	 * Gets node from this parser instance.
	 * @return Node or null, if there is no node.
	 */
	@Nullable
	Node getNode();
	
	/**
	 * Enters current node set using {@link #setNode(Node)}. This allows using
	 * logging methods, e.g. {@link #error(String, ErrorQuality)}.
	 */
	void enterNode();
	
	/**
	 * Exits node that was entered using {@link #enterNode()}. This will commit
	 * the log to be printed when enabling scripts.
	 */
	void exitNode();
}
