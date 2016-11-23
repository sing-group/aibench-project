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
