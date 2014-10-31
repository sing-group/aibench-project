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
 * ClipboardItem.java
 * 
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 12/09/2006
 */
package es.uvigo.ei.aibench.core.clipboard;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Daniel González Peña 12-sep-2006
 *
 */
public interface ClipboardItem {
	/**
	 * @return an unique ID for this item in the clipboard
	 */
	public int getID();
	/**
	 * @return the user's object. If this element was removed it will return null
	 */
	public Object getUserData();
	/**
	 * @return a symbolic name
	 */
	public String getName();
	
	
	/**
	 * Changes the name of this clipboard item
	 * @param name
	 * 
	 */
	public void setName(String name);
	
	/**
	 * @return The Class with the user object was registered in the clipboard
	 */
	public Class<?> getRegisteredUserClass();
	
	/**
	 * Checks if this clipboard item was discarded
	 * @return
	 */
	public boolean wasRemoved();
	
	
	public ReentrantReadWriteLock getLock();
	
}
