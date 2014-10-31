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
 * Executable.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.aibench.core.operation.execution;

import java.util.List;
/**
 * Interface for executing a workflow. 
 * @author Oscar
 *
 */
public interface Executable {
	/**
	 * Creates a new execution session
	 * @param collector the observer which will be notified with the results as soon as they arrive
	 * @return a session 
	 * @see ExecutionSession
	 */
	ExecutionSession openExecutionSession(ResultsCollector collector);
	
	/**
	 * 
	 * @return the types of the incoming arguments.  
	 */
	List<Class<?>> getIncomeArgumentTypes();
	
	/**
	 * 
	 * @return the types of outcome arguments
	 */
	
	List<Class<?>> getOutcomeArgumentTypes();

}
