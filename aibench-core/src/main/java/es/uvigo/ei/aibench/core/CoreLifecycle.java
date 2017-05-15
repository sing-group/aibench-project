/*
 * #%L
 * The AIBench Core Plugin
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
package es.uvigo.ei.aibench.core;

import java.util.List;

import org.platonos.pluginengine.Extension;
import org.platonos.pluginengine.PluginLifecycle;

/**
 * This class bootstraps the Core plugin
 * @author Ruben Dominguez Carbajales, Daniel Glez-Peña
 */
public class CoreLifecycle extends PluginLifecycle {
	
	public void start() {
		List<?> extensions = getExtensionPoint("aibench.core.gui").getExtensions();
		if (extensions.size() > 0) {
			// It would be possible to select the gui
			Extension extension = (Extension) extensions.get(0);
			IGenericGUI gui = (IGenericGUI) extension.getExtensionInstance();
			gui.init();
		}
		else{
			System.err.println("No available GUIS");
		}
		

	}
}
