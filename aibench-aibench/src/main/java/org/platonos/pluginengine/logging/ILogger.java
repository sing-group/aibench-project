
package org.platonos.pluginengine.logging;

/**
 * Allows an application using the PluginEngine to use its own logging mechanism.
 * @author Kevin Duffey (kevinmduffey@yahoo.com)
 * @author Evert
 */
public interface ILogger {
	/**
	 * Logs the appropriate message at the provided level, including the optional Throwable parameter when necessary.
	 */
	public void log (LoggerLevel level, String message, Throwable thr);
}
