/*
 * #%L
 * The AIBench Plugin Manager Plugin
 * %%
 * Copyright (C) 2006 - 2017 Daniel Glez-Peña and Florentino Fdez-Riverola
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
 * $Id: PasswordStore.java,v 1.1 2009-04-13 22:17:52 mrjato Exp $
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

/**
 *  PasswordStore specifies a mechanism to store passwords used to authenticate
 *  using the <strong>LoginService</strong>. The actual mechanism used
 *  to store the passwords is left up to the implementation.
 *
 * @author Bino George
 */
public abstract class PasswordStore {
    /**
     *  Saves a password for future use. 
     *
     *  @param username username used to authenticate.
     *  @param server server used for authentication
     *  @param password password to save. Password can't be null. Use empty array for empty password.
     */
    public abstract boolean set(String username, String server, char[] password);
    
    /** 
     * Fetches the password for a given server and username.
     *  @param username username
     *  @param server server
     *  @return <code>null</code> if not found, a character array representing the password 
     *  otherwise. Returned array can be empty if the password is empty.
     */
    public abstract char[] get(String username, String server);
}
