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
 *  BeanShell is distributed under the terms of the LGPL:                    *
 *  GNU Library Public License http://www.gnu.org/copyleft/lgpl.html         *
 *                                                                           *
 *  Patrick Niemeyer (pat@pat.net)                                           *
 *  Author of Exploring Java, O'Reilly & Associates                          *
 *  http://www.pat.net/~pat/                                                 *
 *                                                                           *
 *****************************************************************************/

package bsh.util;

import java.awt.BorderLayout;

import javax.swing.JApplet;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.TargetError;

/**
 * Run bsh as an applet for demo purposes.
 */
public class JDemoApplet extends JApplet {
        public void init() {
                String debug = getParameter("debug");
                if (debug != null && debug.equals("true"))
                        Interpreter.DEBUG = true;

                String type = getParameter("type");
                if (type != null && type.equals("desktop"))
                        // start the desktop
                        try {
                                new Interpreter().eval("desktop()");
                        } catch (TargetError te) {
                                te.printStackTrace();
                                System.out.println(te.getTarget());
                                te.getTarget().printStackTrace();
                        } catch (EvalError evalError) {
                                System.out.println(evalError);
                                evalError.printStackTrace();
                        }
                else {
                        getContentPane().setLayout(new BorderLayout());
                        JConsole console = new JConsole();
                        getContentPane().add("Center", console);
                        Interpreter interpreter = new Interpreter(console);
                        new Thread(interpreter).start();
                }
        }
}
