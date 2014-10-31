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
 * OperationWrapper.java
 *
 * This file is part of the AIBench Project.
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 16/10/2005
 */
package es.uvigo.ei.aibench.workbench;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import es.uvigo.ei.aibench.core.operation.OperationDefinition;

/**
 * An Operation Wrapper is a Swing Action that requests the Workbench to execute an operation when its activated
 * @author Ruben Dominguez Carbajales, Daniel Glez-Peña
 * @see es.uvigo.ei.aibench.core.operation.OperationDefinition
 */
public class OperationWrapper extends AbstractAction {
	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The real operation
	 */
	private OperationDefinition<?>	operation;

	/**
	 * Creates the operation wrapper
	 * @param op The real operation
	 */
	public OperationWrapper(OperationDefinition<?> op) {
		this.operation = op;
		this.putValue(Action.NAME, operation.getName()+"...");
		this.setEnabled(op.isEnabled());

	}

	public void actionPerformed(ActionEvent arg0) {
		Workbench.getInstance().executeOperation(this.operation);
	}

	public OperationDefinition<?> getOperationDefinition() {
		return operation;
	}

	public void updateState() {

	}

	public String toString(){
		return this.operation.getName()+" @ "+this.operation.getPath();
	}
}
