/*
 * #%L
 * The AIBench Workbench Plugin
 * %%
 * Copyright (C) 2006 - 2016 Daniel Glez-Peña and Florentino Fdez-Riverola
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
