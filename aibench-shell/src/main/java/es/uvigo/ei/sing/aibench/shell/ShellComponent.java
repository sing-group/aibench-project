/*
 * #%L
 * The AIBench Shell Plugin
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
package es.uvigo.ei.sing.aibench.shell;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

public class ShellComponent extends JPanel{
	
	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;

	public ShellComponent(){
		AIBenchInterpreter interpreter = AIBenchInterpreter.getInstance();
		
		this.setLayout(new BorderLayout());
		this.add(new ShellFrame(interpreter).getContentPane(), BorderLayout.CENTER);
		this.setPreferredSize(new Dimension(200, 200));
		
	}
}
