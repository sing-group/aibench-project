/*
Copyright 2007 Daniel Gonzalez Peña, Florentino Fernandez Riverola


This file is part of the AIBench Project. 

AIBench Project is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

AIBench Project is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser Public License for more details.

You should have received a copy of the GNU Lesser Public License
along with AIBench Project.  If not, see <http://www.gnu.org/licenses/>.
*/


/*
 * CoreLifecycle.java
 * This file is part of the AIBench Project.
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 24/09/2005
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
