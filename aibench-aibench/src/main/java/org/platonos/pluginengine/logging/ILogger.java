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
 * Allows an application using the PluginEngine to use its own logging mechanism.
 * @author Kevin Duffey (kevinmduffey@yahoo.com)
 * @author Evert
 */
public interface ILogger {
	/**
	 * Logs the appropriate message at the provided level, including the optional Throwable parameter when necessary.
	 * 
	 * @param level log level.
	 * @param message message to log.
	 * @param thr exception thrown. Optional.
	 */
	public void log (LoggerLevel level, String message, Throwable thr);
}
