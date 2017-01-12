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

import javax.swing.JComponent;

import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;

/**
 * @author Daniel Glez-Peña
 *
 */
public interface WorkbenchListener {
	
	/**
	 * Called when some data is showed (or bringed to front) in the document area.
	 * 
	 * @param data the clipboard item showed.
	 */
	public void dataShowed(ClipboardItem data);
	
	/**
	 * Called when some data is hidden.
	 * 
	 * @param data the clipboard item hidden.
	 */
	public void dataHidded(ClipboardItem data);
	
	/**
	 * Called when some data is closed.
	 * 
	 * @param data the clipboard item closed.
	 */
	public void dataClosed(ClipboardItem data);
	
	/**
	 * Called when some component is added.
	 * 
	 * @param slotID the slot where the component was added.
	 * @param componentName the name of the component.
	 * @param componentID the identifier of the component.
	 * @param component the component added.
	 */
	public void componentAdded(String slotID, String componentName,  String componentID, JComponent component);

	/**
	 * Called when some component is removed.
	 * 
	 * @param slotID the slot from where the component was removed.
	 * @param component the component removed.
	 */
	public void componentRemoved(String slotID, JComponent component);
}
