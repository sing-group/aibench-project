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
 * InputGUI.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.aibench.workbench;

import es.uvigo.ei.aibench.core.operation.OperationDefinition;

/**
 * @author Daniel Glez-Peña
 *
 */
public interface InputGUI {
	/**
	 * Inits the gui. For example setting it visible
	 * @param receiver Use this object to send the params back
	 * @param operation The operation whose params are needed
	 */
	public void init(ParamsReceiver receiver, OperationDefinition<?> operation);
	
	/**
	 * When some param is invalid, this method will be called
	 * @param t The problem itself
	 */
	public void onValidationError(Throwable t);
	
	/**
	 * Called when the params where sended back and the operation could start.
	 */
	public void finish();
}
