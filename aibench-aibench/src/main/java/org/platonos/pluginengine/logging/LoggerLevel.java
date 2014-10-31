
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
