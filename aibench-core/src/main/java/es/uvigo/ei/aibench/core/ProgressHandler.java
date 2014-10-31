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
 * ProgressHandler.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 8/11/2006
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
	 * Called when some parameter could'n be validated
	 * @param t
	 */
	public void validationError(Throwable t);
	
	/**
	 * Called when the operation starts
	 * @param progressBean A POJO with properties holding some operation's specific progress information. This bean may be updated periodically during the operation progress
	 * @param operationID A unique identifier of the operation's execution
	 */
	public void operationStart(Object progressBean, Object operationID);
	
	/**
	 * Called when the operation finishes (with error or whithout)
	 * @param results The port outputs
	 * @param clipboardItems All clipboard elements (nested elements in complex data-types are also included)
	 */
	public void operationFinished(List<Object> results, List<ClipboardItem> clipboardItems);
	
	/**
	 * Called when some error has occurred during the operation execution
	 * @param t The error
	 */
	public void operationError(Throwable t);
}
