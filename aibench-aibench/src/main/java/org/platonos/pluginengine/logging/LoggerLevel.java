/*
 * #%L
 * The AIBench basic runtime and plugin engine
 * %%
 * Copyright (C) 2006 - 2016 Daniel Glez-Peña and Florentino Fdez-Riverola
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
 * Immutable typesafe enum constants for PluginEngine logging.
 * @see ILogger#log
 * @author Nathan Sweet (misc@n4te.com)
 */
public final class LoggerLevel {
	private final String name;

	private LoggerLevel (String name) {
		this.name = name;
	}

	public String toString () {
		return name;
	}

	/**
	 * Very granual messages. Most of the PluginEngine logging is done at this level. This is only really useful during debugging
	 * or developement and can be ignored for production.
	 */
	static public final LoggerLevel FINE = new LoggerLevel("Fine");

	/**
	 * Informative and useful messages. Very little is logged at this level.
	 */
	static public final LoggerLevel INFO = new LoggerLevel("Info");

	/**
	 * Important messages that may indicate a failure.
	 */
	static public final LoggerLevel WARNING = new LoggerLevel("Warning");

	/**
	 * Important messages that indicate a failure.
	 */
	static public final LoggerLevel SEVERE = new LoggerLevel("Severe");
}
