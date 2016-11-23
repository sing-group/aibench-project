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
package es.uvigo.ei.pipespecification.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import es.uvigo.ei.aibench.core.operation.execution.Executable;
import es.uvigo.ei.aibench.core.operation.execution.IncompatibleContraintsException;

public abstract class PipeDefinition {

	private final List<Class<?>> outcomeTypes;

	private final List<Class<?>> incomeTypes;

	protected PipeDefinition(List<Class<?>> incomeTypes, List<Class<?>> outcomeTypes) {
		this.outcomeTypes = Collections.unmodifiableList(new ArrayList<Class<?>>(
				outcomeTypes));
		this.incomeTypes = Collections.unmodifiableList(new ArrayList<Class<?>>(
				incomeTypes));
	}

	public abstract PipeDefinition join(PipeDefinition rightPart)
			throws IncompatibleContraintsException;

	public abstract int length();

	public abstract int totalLength();

	protected abstract Executable instantiate_(Map<String, Object> args);

	public final Executable instantiate(Map<String, Object> args) {
		Map<String, Class<?>> argsSpecification = new HashMap<String, Class<?>>();
		for (Entry<String, Object> argument : args.entrySet()) {
			argsSpecification.put(argument.getKey(), argument.getValue()
					.getClass());
		}
		checkArguments(argsSpecification);
		return instantiate_(args);
	}
	public final Executable instantiate(){
		return instantiate(new HashMap<String,Object>());
	}


	public final void checkArguments(Map<String, Class<?>> args)
			throws IllegalArgumentException {
		Map<String, Class<?>> specification = getArgumentsSpecification();
		for (Entry<String, Class<?>> entry : specification.entrySet()) {
			if (!args.containsKey(entry.getKey())) {
				throw new IllegalArgumentException(
						"it's necessary to receive the argument with name "
								+ entry.getKey());
			}
			Class<?> requiredType = entry.getValue();
			Class<?> receivedType = args.get(entry.getKey());
			if (!requiredType.isAssignableFrom(receivedType))
				throw new IllegalArgumentException("The type of the argument "
						+ entry.getKey() + ", " + receivedType
						+ ", is incompatible with the required type "
						+ requiredType);

		}
	}

	public abstract Map<String, Class<?>> getArgumentsSpecification();

	public final List<Class<?>> getIncomeTypes() {
		return incomeTypes;
	}

	public final List<Class<?>> getOutcomeTypes() {
		return outcomeTypes;
	}

	// public final List<PipeConstraints> getPipeConstraints() {
	// return pipeConstraints;
	// }

}
