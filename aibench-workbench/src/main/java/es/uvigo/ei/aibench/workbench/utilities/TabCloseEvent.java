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
 * CloseEvent.java
 *
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on May 12, 2010
 */
package es.uvigo.ei.aibench.workbench.utilities;

import java.awt.AWTEvent;

/**
 * @author Miguel Reboiro-Jato
 *
 */
public class TabCloseEvent extends AWTEvent {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	public static final int TAB_CLOSING = AWTEvent.RESERVED_ID_MAX + 1;
	public static final int TAB_CLOSED = AWTEvent.RESERVED_ID_MAX + 2;
	
	private final int tabIndex;
	private boolean cancelled;
	
	public TabCloseEvent(Object source, int id, int tabIndex) {
		super(source, id);
		this.tabIndex = tabIndex;
		this.cancelled = false;
	}
	
	/**
	 * @return the index
	 */
	public int getTabIndex() {
		return this.tabIndex;
	}
	
	/**
	 * @return the cancelled
	 */
	public boolean isCancelled() {
		return this.cancelled;
	}
	
	public void cancel() {
		this.cancelled = true;
	}
}
