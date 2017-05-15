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
package es.uvigo.ei.pipespecification.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import es.uvigo.ei.aibench.core.operation.execution.CollectorAdapterFactory;
import es.uvigo.ei.aibench.core.operation.execution.Executable;
import es.uvigo.ei.aibench.core.operation.execution.IncompatibleConstraintsException;
import es.uvigo.ei.aibench.core.operation.execution.SerialExecutable;

class CompositedPipeDefinition extends PipeDefinition {

	private final List<PipeDefinition> sons;

	private final Map<String, Class<?>> argumentsSpecification;

	static Map<String, Class<?>> aggregateArgumentsSpecification(
			PipeDefinition pipe1, PipeDefinition pipe2) {
		Map<String, Class<?>> map1 = pipe1.getArgumentsSpecification();
		return aggregateArgumentsSpecification(map1, pipe2);
	}

	static Map<String, Class<?>> aggregateArgumentsSpecification(
			Map<String, Class<?>> map1, PipeDefinition pipe2) {
		Map<String, Class<?>> result = new HashMap<String, Class<?>>(map1);
		Map<String, Class<?>> map2 = pipe2.getArgumentsSpecification();
		for (String key : map2.keySet()) {
			if (result.containsKey(key)) {
				if (!result.get(key).equals(map2.get(key)))
					throw new RuntimeException("There is a name clashing for "
							+ key + ". There are two different classes "
							+ result.get(key) + ", " + map2.get(key));
			}
			result.put(key, map2.get(key));
		}
		return result;
	}

	CompositedPipeDefinition(PipeDefinition pipe1, PipeDefinition pipe2)
			throws IncompatibleConstraintsException {
		this(aggregateArgumentsSpecification(pipe1, pipe2), join(pipe1, pipe2));
	}

	private static List<PipeDefinition> join(PipeDefinition pipe1,
			PipeDefinition pipe2) throws IncompatibleConstraintsException {
		CollectorAdapterFactory.join(pipe1, pipe2);
		return Arrays.asList(pipe1, pipe2);
	}

	private CompositedPipeDefinition(Map<String, Class<?>> argumentsSpecification, List<PipeDefinition> sons) {
		super(sons.get(0).getIncomeTypes(), sons.get(sons.size() - 1).getOutcomeTypes());

		if (argumentsSpecification == null)
			throw new IllegalArgumentException("the arguments specificacion can't be null");
		if (sons.size() < 2)
			throw new IllegalArgumentException("at least two pipeDefinitions");
		this.argumentsSpecification = new HashMap<String, Class<?>>(
				argumentsSpecification);
		this.sons = new ArrayList<PipeDefinition>(sons);
		invariant();
	}

	private void invariant() {
		assert atLeastTwoSons() : "there must be at least two sons";
	}

	private boolean atLeastTwoSons() {
		return sons.size() > 1;
	}

	@Override
	protected Executable instantiate_(Map<String, Object> args) {
		// TODO create checked exception
		for (Entry<String, Object> entry : args.entrySet()) {
			if (!argumentsSpecification.containsKey(entry.getKey()))
				throw new IllegalArgumentException("the argument "
						+ entry.getKey() + " doesn't exist");
			Class<?> klass = argumentsSpecification.get(entry.getKey());
			if (!klass.isAssignableFrom(entry.getValue().getClass()))
				throw new IllegalArgumentException(
						"the object for the argument " + entry.getKey()
								+ " of class " + entry.getClass()
								+ " cannot be assigned to " + klass);
		}
		List<Executable> pipes = new ArrayList<Executable>();
		for (PipeDefinition pipeDefinition : sons) {
			pipes.add(pipeDefinition.instantiate(args));
		}
		try {
			return new SerialExecutable(pipes);
		} catch (Exception e) {
			throw new RuntimeException(
					"The compatibility must had been checked");
		} finally {
			invariant();
		}
	}

	@Override
	public PipeDefinition join(PipeDefinition rightPart)
			throws IncompatibleConstraintsException {
		CollectorAdapterFactory.join(this, rightPart);
		List<PipeDefinition> newSons = new ArrayList<PipeDefinition>(sons);
		newSons.add(rightPart);
		return new CompositedPipeDefinition(aggregateArgumentsSpecification(
				this, rightPart), newSons);

	}

	@Override
	public Map<String, Class<?>> getArgumentsSpecification() {
		return argumentsSpecification;
	}

	@Override
	public int length() {
		return sons.size();
	}

	private int totalLength = -1;

	@Override
	public int totalLength() {
		if (totalLength == -1) {
			totalLength = 0;
			for (PipeDefinition def : sons) {
				totalLength += def.totalLength();
			}
		}
		return totalLength;
	}
}
