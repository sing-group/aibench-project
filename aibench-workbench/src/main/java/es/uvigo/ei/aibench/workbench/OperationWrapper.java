/*
 * #%L
 * The AIBench Workbench Plugin
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
package es.uvigo.ei.aibench.workbench;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.log4j.Logger;

import es.uvigo.ei.aibench.core.operation.OperationDefinition;

/**
 * An Operation Wrapper is a Swing Action that requests the Workbench to execute an operation when its activated
 * @author Ruben Dominguez Carbajales, Daniel Glez-Peña
 * @see es.uvigo.ei.aibench.core.operation.OperationDefinition
 */
public class OperationWrapper extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(OperationWrapper.class);

	public static final String SUSPENSION_POINTS = "mainwindow.menubar.names_append_suspension_points";
	public static final boolean DEFAULT_SUSPENSION_POINTS = true;
	private OperationDefinition<?>	operation;

	/**
	 * Creates the operation wrapper
	 * @param op The real operation
	 */
	public OperationWrapper(OperationDefinition<?> op) {
		this.operation = op;
		this.putValue(Action.NAME, getOperationName());
		this.setEnabled(op.isEnabled());
	}

	private String getOperationName() {
		return operation.getMenuName() + getSuspensionPoints();
	}

	private String getSuspensionPoints() {
		return isAddSuspensionPoints() ? "..." : "";
	}

	private boolean isAddSuspensionPoints() {
		String suspensionPoints = Workbench.CONFIG.getProperty(SUSPENSION_POINTS);
		if (suspensionPoints != null) {
			if (suspensionPoints.toLowerCase().equals("false")) {
				return false;
			} else if (suspensionPoints.toLowerCase().equals("true")) {
				return true;
			} else {
				LOGGER.warn("Warning: invalid value for " + SUSPENSION_POINTS + ". Using default value instead.");
			}
		}
		return DEFAULT_SUSPENSION_POINTS;
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
