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
 * AIBenchIterpreter.java
 * 
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.sing.aibench.shell;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.util.JConsole;

public class AIBenchInterpreter {
	private static volatile AIBenchInterpreter instance = null;

	public static AIBenchInterpreter getInstance() {
		if (AIBenchInterpreter.instance == null)
			AIBenchInterpreter.createInstance();
		
		return AIBenchInterpreter.instance;
	}
	
	private synchronized static void createInstance() {
		if (AIBenchInterpreter.instance == null)
			AIBenchInterpreter.instance = new AIBenchInterpreter();
	}

	private Interpreter interpreter = null;
	private JConsole jConsole = new JConsole();

	public JConsole getConsole() {
		return jConsole;
	}

	private int port = -1;

	public void server(int port) {
		this.port = port;
		try {
			synchronized (this) {
				if (this.interpreter == null) {
					this.wait();
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			this.interpreter.eval("server(" + port + ");");
		} catch (EvalError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private AIBenchInterpreter() {
		this.jConsole.setFont(new java.awt.Font("Monospaced",
				java.awt.Font.PLAIN, 11));

		Runnable robustInterpreter = new Runnable() {
			public void run() {
				while (true) {
					// Interpreter interpreter =
					// null;

					synchronized (AIBenchInterpreter.this) {
						interpreter = new Interpreter(jConsole);
						interpreter.setClassLoader(ShellFrame.class.getClassLoader());
						try {
							interpreter.eval("import es.uvigo.ei.aibench.core.*;");
							interpreter.eval("import es.uvigo.ei.aibench.workbench.*;");
							interpreter.eval("import es.uvigo.ei.sing.aibench.shell.*;");
							interpreter.eval(findOperationCommand);
							interpreter.eval(findEnumCommand);
							interpreter.eval("importCommands(\"commands\");");
						} catch (EvalError e) {
							e.printStackTrace();
						}
						// interpreter.setConsole(jConsole);

						if (port != -1) {
							try {
								interpreter.eval("server(" + port + ");");
							} catch (EvalError e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						AIBenchInterpreter.this.notifyAll();
					}

					Thread weak = new Thread(interpreter);
					weak.setName("BshInterpreter");
					weak.start();
					try {
						weak.join();
					} catch (InterruptedException e) {
						// TODO Auto-generated
						// catch block
						e.printStackTrace();
					}
				}
			}
		};
		Thread robust = new Thread(robustInterpreter);
		robust.setName("BshRobust");
		robust.start(); // start a thread to call the run()
		// method*/

		// wait until we have the first interpreter
		try {
			synchronized (this) {
				if (interpreter == null) {
					this.wait();
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Capture the application log
	}

	// some hard-coded commands, that's horrible!
	private String findOperationCommand = "findOperation(id){"
			+ "operations  = Core.getInstance().getOperations();"
			+ "for(int i =0; i<operations.size(); i++){"
			+ "if (operations.get(i).getID().equals(id)) return operations.get(i);"
			+ "}" + "return null;" + "}";

	private String findEnumCommand = "findEnum(type, name){" +

	"for (int i = 0; i<type.getEnumConstants().length; i++){"
			+ "	if(type.getEnumConstants()[i].name().equals(name)){"
			+ "		return type.getEnumConstants()[i];" + "	}" + "}" + "};";

	/**
	 * @return Returns the interpreter.
	 */
	public Interpreter getInterpreter() {
		return interpreter;
	}
}
