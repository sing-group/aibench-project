
package org.platonos.pluginengine.event;

import org.platonos.pluginengine.PluginEngine;

/**
 * Event fired by a PluginEngine.
 * @see PluginEngine#addPluginEngineListener(IPluginEngineListener)
 * @see PluginEngineEventType
 * @author Nathan Sweet (misc@n4te.com)
 */
public final class PluginEngineEvent {
	private final PluginEngineEventType eventType;
	private final PluginEngine pluginEngine;
	private Object payload;

	public PluginEngineEvent (PluginEngineEventType eventType, PluginEngine pluginEngine) {
		this.eventType = eventType;
		this.pluginEngine = pluginEngine;
	}

	public PluginEngineEventType getEventType () {
		return eventType;
	}

	public PluginEngine getPluginEngine () {
		return pluginEngine;
	}

	/**
	 * Returns any extra data associated to the event.
	 */
	public Object getPayload () {
		return payload;
	}

	/**
	 * Sets any extra data associated to the event.
	 */
	public void setPayload (Object payload) {
		this.payload = payload;
	}
}
