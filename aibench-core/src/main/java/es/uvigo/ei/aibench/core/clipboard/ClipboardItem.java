/*
 * #%L
 * The AIBench Core Plugin
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
package es.uvigo.ei.aibench.core.clipboard;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Daniel González Peña 12-sep-2006
 *
 */
public interface ClipboardItem {
	/**
	 * @return an unique ID for this item in the clipboard.
	 */
	public int getID();
	
	/**
	 * @return the user's object. If this element was removed it will return {@code null}.
	 */
	public Object getUserData();
	
	/**
	 * @return a symbolic name.
	 */
	public String getName();
	
	
	/**
	 * Changes the name of this clipboard item.
	 * 
	 * @param name the new name of the clipboard item.
	 */
	public void setName(String name);
	
	/**
	 * @return the Class with the user object was registered in the clipboard.
	 */
	public Class<?> getRegisteredUserClass();
	
	/**
	 * Checks if this clipboard item was discarded.
	 * 
	 * @return {@code true} if this clipboard item was discarded. {@code false} otherwise. 
	 */
	public boolean wasRemoved();
	
	public ReentrantReadWriteLock getLock();
}
