/*
Copyright 2007 Daniel Gonzalez Peña, Florentino Fernandez Riverola


This file is part the AIBench Project. 

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
 * Lifecycle.java
 *
 * This class is part of the AIBench Project.
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 09/04/2007
 */
package es.uvigo.ei.aibench.workbench;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.platonos.pluginengine.PluginLifecycle;

/**
 * @author Daniel Glez-Peña
 *
 */
public class Lifecycle extends PluginLifecycle {

	public void start(){
		if(System.getProperty("aibench.nogui")==null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					/* (non-Javadoc)
					 * @see java.lang.Runnable#run()
					 */
					public void run() {
						while (Workbench.getInstance().getMainFrame() == null) Thread.yield();
						Workbench.getInstance().getMainFrame().setVisible(true);
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			SwingUtilities.invokeLater(new Runnable() {
				/* (non-Javadoc)
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					if (Workbench.getInstance().getMainFrame() instanceof MainWindow) {
						((MainWindow) Workbench.getInstance().getMainFrame()).packSplitters();
					}
				}
			});
		}
	}
}
