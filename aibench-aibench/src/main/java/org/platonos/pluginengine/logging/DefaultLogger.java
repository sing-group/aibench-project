/*
 * #%L
 * The AIBench basic runtime and plugin engine
 * %%
 * Copyright (C) 2006 - 2017 Daniel Glez-Peña and Florentino Fdez-Riverola
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

package org.platonos.pluginengine.logging;

/**
 * Simple ILogger implementation that provides rudimentary logging if the application using the PluginEngine does not provide it's
 * own implementation.
 * @author Kevin Duffey (kevinmduffey@yahoo.com)
 * @author Evert
 * @author Nathan Sweet (misc@n4te.com)
 */
public final class DefaultLogger implements ILogger {
	/**
	 * Logs a message to the console.
	 */
	public void log (LoggerLevel level, String message, Throwable thr) {
		if (level == LoggerLevel.SEVERE || level == LoggerLevel.WARNING) {
			System.err.println("[" + level + "] " + message);
			if (thr != null) thr.printStackTrace();
		} else
			System.out.println("[" + level + "] " + message);
	}
}
