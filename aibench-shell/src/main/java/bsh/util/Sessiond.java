/*
 * #%L
 * The AIBench Shell Plugin
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
/*****************************************************************************
 *                                                                           *
 *  This file is part of the BeanShell Java Scripting distribution.          *
 *  Documentation and updates may be found at http://www.beanshell.org/      *
 *                                                                           *
 *  Sun Public License Notice:                                               *
 *                                                                           *
 *  The contents of this file are subject to the Sun Public License Version  *
 *  1.0 (the "License"); you may not use this file except in compliance with *
 *  the License. A copy of the License is available at http://www.sun.com    * 
 *                                                                           *
 *  The Original Code is BeanShell. The Initial Developer of the Original    *
 *  Code is Pat Niemeyer. Portions created by Pat Niemeyer are Copyright     *
 *  (C) 2000.  All Rights Reserved.                                          *
 *                                                                           *
 *  GNU Public License Notice:                                               *
 *                                                                           *
 *  Alternatively, the contents of this file may be used under the terms of  *
 *  the GNU Lesser General Public License (the "LGPL"), in which case the    *
 *  provisions of LGPL are applicable instead of those above. If you wish to *
 *  allow use of your version of this file only under the  terms of the LGPL *
 *  and not to allow others to use your version of this file under the SPL,  *
 *  indicate your decision by deleting the provisions above and replace      *
 *  them with the notice and other provisions required by the LGPL.  If you  *
 *  do not delete the provisions above, a recipient may use your version of  *
 *  this file under either the SPL or the LGPL.                              *
 *                                                                           *
 *  Patrick Niemeyer (pat@pat.net)                                           *
 *  Author of Learning Java, O'Reilly & Associates                           *
 *  http://www.pat.net/~pat/                                                 *
 *                                                                           *
 *****************************************************************************/

package bsh.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import bsh.Interpreter;
import bsh.NameSpace;

/**
 * BeanShell remote session server. Starts instances of bsh for client
 * connections. Note: the sessiond effectively maps all connections to the same
 * interpreter (shared namespace).
 */
public class Sessiond extends Thread {
        private ServerSocket ss;

        NameSpace globalNameSpace;

        /*
         * public static void main(String argv[]) throws IOException { new
         * Sessiond( Integer.parseInt(argv[0])).start(); }
         */

        public Sessiond(NameSpace globalNameSpace, int port) throws IOException {
                ss = new ServerSocket(port);
                this.globalNameSpace = globalNameSpace;
        }

        public void run() {
                try {
                        while (true)
                                new SessiondConnection(globalNameSpace, ss.accept()).start();
                } catch (IOException e) {
                        System.out.println(e);
                }
        }
}

class SessiondConnection extends Thread {
        NameSpace globalNameSpace;

        Socket client;

        SessiondConnection(NameSpace globalNameSpace, Socket client) {
                this.client = client;
                this.globalNameSpace = globalNameSpace;
        }

        public void run() {
                try {
                        InputStream in = client.getInputStream();
                        PrintStream out = new PrintStream(client.getOutputStream());
                        Interpreter i = new Interpreter(new InputStreamReader(in), out, out, true, globalNameSpace);
                        i.setExitOnEOF(false); // don't exit interp
                        i.run();
                } catch (IOException e) {
                        System.out.println(e);
                }
        }
}
