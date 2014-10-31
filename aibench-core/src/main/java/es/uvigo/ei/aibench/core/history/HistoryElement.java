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
 * HistoryElement.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 * Created on 22/09/2006
 */
package es.uvigo.ei.aibench.core.history;

import java.util.List;

import es.uvigo.ei.aibench.core.ParamSpec;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.operation.OperationDefinition;

/**
 * @author Daniel Glez-Peña 22-sep-2006
 *
 */
public interface HistoryElement {

	
	/**
	 * @return The operation executed
	 */
	public OperationDefinition<?> getOperation();
	
	/**
	 * @return Returns the parameters used to execute the opearation
	 */
	public ParamSpec[] getParams();
	
	/**
	 * @return Returns all the clipboard items that this operation added as output. That is, all the port ouputs and the nested elements of the complex data-types
	 */
	public List<ClipboardItem> getClipboardItems();
	
	
	/**
	 * @return Returns the plain port ouputs.
	 * 
	 */
	public List<Object> getOutputs();

}
