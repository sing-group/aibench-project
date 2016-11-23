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
