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
	 * 
	 * @return any extra data associated to the event.
	 */
	public Object getPayload () {
		return payload;
	}

	/**
	 * Sets any extra data associated to the event.
	 * 
	 * @param payload extra data associated to the event.
	 */
	public void setPayload (Object payload) {
		this.payload = payload;
	}
}
