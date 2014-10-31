
package org.platonos.pluginengine;

/**
 * Exception that indicates a Platonos PluginEngine specific problem.
 * @author Kevin Duffey (kevinmduffey@yahoo.com)
 * @author Evert
 * @author Nathan Sweet (misc@n4te.com)
 */
public class PluginEngineException extends Exception {
	/**
	 * Serial version.
	 */
	private static final long serialVersionUID = 1L;

	public PluginEngineException () {
		super();
	}

	public PluginEngineException (String message) {
		super(message);
	}

	public PluginEngineException (String message, Throwable cause) {
		super(message, cause);
	}

	public PluginEngineException (Throwable cause) {
		super(cause);
	}
}