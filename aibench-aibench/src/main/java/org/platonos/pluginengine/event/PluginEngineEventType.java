
package org.platonos.pluginengine.event;

/**
 * Immutable typesafe enum constants for PluginEngineEvents.
 * @see PluginEngineEvent
 * @author Nathan Sweet (misc@n4te.com)
 */
public final class PluginEngineEventType {
	private final String name;

	private PluginEngineEventType (String name) {
		this.name = name;
	}

	public String toString () {
		return name;
	}

	/**
	 * Event sent when the PluginEngine and all startup Plugins have been started. <br>
	 * <br>
	 * The payload is null.
	 */
	static public final PluginEngineEventType STARTUP = new PluginEngineEventType("PluginEngine Started");
	
	
	/**
	 * Event sent immediately before the PluginEngine is to be shutdown and all Plugins stopped. <br>
	 * <br>
	 * The payload is null.
	 */
	static public final PluginEngineEventType SHUTDOWN = new PluginEngineEventType("PluginEngine Shutdown");

	/**
	 * @author Miguel Reboiro Jato
	 */
	static public final PluginEngineEventType PLUGIN_ENABLED = new PluginEngineEventType("Plugin Enabled");
	
	/**
	 * @author Miguel Reboiro Jato
	 */
	static public final PluginEngineEventType PLUGIN_DISABLED = new PluginEngineEventType("Plugin Disabled");
	
	/**
	 * Event sent when a Plugin is loaded. <br>
	 * <br>
	 * The payload is the Plugin being loaded.
	 */
	static public final PluginEngineEventType PLUGIN_LOADED = new PluginEngineEventType("Plugin Loaded");

	/**
	 * Event sent when a Plugin is unloaded. <br>
	 * <br>
	 * The payload is the Plugin being unloaded.
	 */
	static public final PluginEngineEventType PLUGIN_UNLOADED = new PluginEngineEventType("Plugin Unloaded");

	/**
	 * Event sent when a Plugin is resolved. <br>
	 * <br>
	 * The payload is the Plugin that has resolved.
	 */
	static public final PluginEngineEventType PLUGIN_RESOLVED = new PluginEngineEventType("Plugin Resolved");

	/**
	 * Event sent when a Plugin is about to be unresolved. <br>
	 * <br>
	 * The payload is the Plugin about to be unresolved.
	 */
	static public final PluginEngineEventType PLUGIN_UNRESOLVED = new PluginEngineEventType("Plugin Unresolved");

	/**
	 * Event sent when a Plugin is started. <br>
	 * <br>
	 * The payload is the Plugin that has started.
	 */
	static public final PluginEngineEventType PLUGIN_STARTED = new PluginEngineEventType("Plugin Started");
	
	/**
	 * Event sent when a Plugin is about to start. <br>
	 * <br>
	 * The payload is the Plugin that is about to start.
	 */
	static public final PluginEngineEventType PLUGIN_STARTING = new PluginEngineEventType("Plugin Starting");

	/**
	 * Event sent when a Plugin is about to be stopped. <br>
	 * <br>
	 * The payload is the Plugin about to be stopped.
	 */
	static public final PluginEngineEventType PLUGIN_STOPPED = new PluginEngineEventType("Plugin Stopped");

}
