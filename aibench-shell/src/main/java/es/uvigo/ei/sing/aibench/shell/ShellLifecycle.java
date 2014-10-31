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
 * ShellLifecycle.java
 * 
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.sing.aibench.shell;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.platonos.pluginengine.PluginLifecycle;

import bsh.EvalError;

public class ShellLifecycle extends PluginLifecycle {
	@Override
	public void initialize() {

	}
	
	public void start() {
		final AIBenchInterpreter interpreter = AIBenchInterpreter.getInstance();
//		if (System.getProperty("aibench.nogui") == null) {
//			new ShellFrame(interpreter);
//
//		}
		if (System.getProperty("sing.aibench.shell.port") != null) {
			int port = -1;
			try {
				port = Integer.parseInt(System.getProperty("sing.aibench.shell.port"));
				
				if (port > 0 && port < 65000) {
					interpreter.server(port);
				}
			} catch (NumberFormatException e) {
				System.err.println("Shell: Port must be an integer");
			}
		}
		if (System.getProperty("sing.aibench.shell.script") != null) {
			new Thread() {
				public void run() {
					try {
						interpreter.getInterpreter().source(
							new File(System.getProperty("sing.aibench.shell.script")).getAbsolutePath()
						);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (EvalError e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}.start();
		}

	}

}
