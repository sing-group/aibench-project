/*
 * #%L
 * The AIBench Core Plugin
 * %%
 * Copyright (C) 2006 - 2017 Daniel Glez-Peña and Florentino Fdez-Riverola
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class StandardExecutable<T> extends AbstractExecutable {

	private final List<Class<?>> incomeArguments;

	private final List<Class<?>> outcomeArguments;

	private final List<EndpointsFactory<T>> factories;

	private final T executableInstance;

	/*
	 * dani comment...
	 * private final Object[] values;
	*/
	private final ExecutorService executor;

	public StandardExecutable(List<EndpointsFactory<T>> factories,
			List<Class<?>> incomeArgumentTypes, List<Class<?>> outcomeArgumentTypes,
			ExecutorService executor, T executableInstance,
			Object[] values) {
		if (values == null)
			throw new IllegalArgumentException("target cannot be null");
		if (executableInstance == null)
			throw new NullPointerException("executable instance can't be null");
		//
		/* TODO: DANI COMMENTED THIS */ //checkParams(executableInstance.getParameterTypes(), values);
		this.executableInstance = executableInstance;
		this.factories = Collections
				.unmodifiableList(new ArrayList<EndpointsFactory<T>>(factories));
		this.incomeArguments = Collections
				.unmodifiableList(new ArrayList<Class<?>>(incomeArgumentTypes));
		this.outcomeArguments = Collections
				.unmodifiableList(new ArrayList<Class<?>>(outcomeArgumentTypes));
		/*
		 * dani comment
		 * this.values = values.clone();
		 */
		this.executor = executor;
	}

	/*
	 * TODO: DANI COMMENTED THIS
	 * private static void checkParams(Class<?>[] parameterTypes, Object[] values) {
		for (int i = 0; i < values.length; i++) {
			if (!parameterTypes[i].isInstance(values[i]))
				throw new IllegalArgumentException(
						"the values are not compatible with the constructor signature");
		}
	}*/

	@Override
	public ExecutionSession openExecutionSession(ResultsCollector collector) {
		try {
			return new StandardExecutionSession<T>(factories, executableInstance, new TypesChecker(
					getOutcomeArgumentTypes().toArray(new Class[] {}),
					collector), executor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<Class<?>> getIncomeArgumentTypes() {
		return incomeArguments;
	}

	public List<Class<?>> getOutcomeArgumentTypes() {
		return outcomeArguments;
	}

}
