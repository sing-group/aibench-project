
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
