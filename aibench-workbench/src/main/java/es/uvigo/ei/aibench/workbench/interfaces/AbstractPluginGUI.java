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
 * AbstractPluginGUI.java
 * 
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.aibench.workbench.interfaces;

import java.util.Hashtable;

import javax.swing.ImageIcon;


public abstract class AbstractPluginGUI implements IGenericPluginGUI {

	protected Hashtable<String, ImageIcon> icons = new Hashtable<String, ImageIcon>();

	protected Hashtable<String, Boolean> toolbar = new Hashtable<String, Boolean>();


	public ImageIcon getOperationIcon(String operationID) {
		return this.icons.get(operationID);
	}

	public boolean isToolbarVisible(String operationID) {
		return this.toolbar.get(operationID);
	}

	public void setOperationIcon(String operationID, ImageIcon icon) {
		this.icons.put(operationID, icon);
	}

	public void setToolbarVisible(String operationID, boolean flag) {
		this.toolbar.put(operationID, flag);
	}
}
