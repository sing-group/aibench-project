/*
 * #%L
 * The AIBench Core Plugin
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
package es.uvigo.ei.aibench.core;

import java.util.List;

import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;

/**
 * @author Daniel González Peña 08-nov-2006
 *
 */
public interface ProgressHandler {

	/**
	 * Called when some parameter could not be validated.
	 * 
	 * @param t an exception thrown on parameter validation.
	 */
	public void validationError(Throwable t);
	
	/**
	 * Called when the operation starts-
	 * @param progressBean a POJO with properties holding some operation's specific progress information. This bean may be updated periodically during the operation progress.
	 * @param operationID an unique identifier of the operation's execution.
	 */
	public void operationStart(Object progressBean, Object operationID);
	
	/**
	 * Called when the operation finishes (with error or whithout).
	 * @param results the port outputs.
	 * @param clipboardItems all clipboard elements (nested elements in complex data-types are also included).
	 */
	public void operationFinished(List<Object> results, List<ClipboardItem> clipboardItems);
	
	/**
	 * Called when some error has occurred during the operation execution.
	 * 
	 * @param t the error.
	 */
	public void operationError(Throwable t);
}
