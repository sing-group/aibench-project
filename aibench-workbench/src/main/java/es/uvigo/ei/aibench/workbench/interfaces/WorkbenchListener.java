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
 * WorkbenchListener.java
 * 
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.aibench.workbench.interfaces;

import javax.swing.JComponent;

import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;

/**
 * @author Daniel Glez-Peña
 *
 */
public interface WorkbenchListener {
	
	/**
	 * Called when some data is showed (or bringed to front) in the document area
	 * @param data
	 */
	public void dataShowed(ClipboardItem data);
	
	/**
	 * Called when some data is hidden
	 * @param data
	 */
	public void dataHidded(ClipboardItem data);
	
	/**
	 * Called when some data is closed
	 * @param data
	 */
	public void dataClosed(ClipboardItem data);
	
	/**
	 * Called when some component is added
	 * @param slotID
	 * @param componentName
	 * @param componentID
	 * @param component
	 */
	public void componentAdded(String slotID, String componentName,  String componentID, JComponent component);

	/**
	 * Called when some component is removed
	 * @param slotID
	 * @param component
	 */
	public void componentRemoved(String slotID, JComponent component);
}
