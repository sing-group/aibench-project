/*
 * #%L
 * The AIBench Plugin Manager Plugin
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
/*
 * $Id: UserNameStore.java,v 1.1 2009-04-13 22:17:53 mrjato Exp $
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jdesktop.swingx.auth;

import org.jdesktop.beans.AbstractBean;

/**
 * <b>UsernameStore</b> is a class that implements persistence of usernames
 *
 * @author Bino George
 * @author rbair
 */
public abstract class UserNameStore extends AbstractBean {
    /**
     * Gets the current list of users.
     */
    public abstract String[] getUserNames();
    /**
     */
    public abstract void setUserNames(String[] names);
    /**
     * lifecycle method for loading names from persistent storage
     */
    public abstract void loadUserNames();
    /**
     * lifecycle method for saving name to persistent storage
     */
    public abstract void saveUserNames();
    /**
     */
    public abstract boolean containsUserName(String name);
    
    /**
     * Add a username to the store.
     * @param userName
     */
    public abstract void addUserName(String userName);
    
    /**
     * Removes a username from the list.
     * @param userName
     */
    public abstract void removeUserName(String userName);
}
