package org.platonos.pluginengine.event;

import org.platonos.pluginengine.PluginEngine;

/**
 * Interface for receiving PluginEngineEvents.
 * @see PluginEngine#addPluginEngineListener(IPluginEngineListener)
 * @author Nathan Sweet (misc@n4te.com)
 */
public interface IPluginEngineListener {

	public void handlePluginEngineEvent (PluginEngineEvent event);

}
