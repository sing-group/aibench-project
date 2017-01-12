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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import es.uvigo.ei.aibench.core.operation.execution.Executable;
import es.uvigo.ei.aibench.core.operation.execution.IncompatibleConstraintsException;
import es.uvigo.ei.aibench.core.operation.execution.Tee;
import es.uvigo.ei.aibench.core.operation.execution.Unifier;

class TeeDefinition extends PipeDefinition {
	private final PipeDefinition[] branches;

	TeeDefinition(PipeDefinition... branches) {
		super(branches[0].getIncomeTypes(), branches[0].getOutcomeTypes());
		for (PipeDefinition definition : branches) {
			if (definition == null)
				throw new IllegalArgumentException(
						"All definitions must be not null");
		}
		if (!Unifier.unifies(Arrays.asList(branches)))
			throw new IllegalArgumentException("the branches"
					+ format(branches) + "don't unify");
		this.branches = branches;
	}

	private static String format(PipeDefinition[] branches) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < branches.length; i++) {
			stringBuilder.append(branches[i].getIncomeTypes());
			stringBuilder.append(",");
		}
		stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		return stringBuilder.toString();
	}

	@Override
	protected Executable instantiate_(Map<String, Object> args) {
		Executable[] pipes = new Executable[branches.length];
		for (int i = 0; i < pipes.length; i++) {
			pipes[i] = branches[i].instantiate(args);
		}
		return new Tee(pipes);
	}

	@Override
	public PipeDefinition join(PipeDefinition rightPart)
			throws IncompatibleConstraintsException {
		return new CompositedPipeDefinition(this, rightPart);
	}

	private int length = -1;

	@Override
	public int length() {
		if (length == -1) {
			length = 0;
			for (int i = 0; i < branches.length; i++) {
				length += branches[i].length();
			}
		}
		return length;
	}

	private Map<String, Class<?>> argumentsSpecification = null;

	@Override
	public Map<String, Class<?>> getArgumentsSpecification() {
		if (argumentsSpecification == null) {
			argumentsSpecification = new HashMap<String, Class<?>>();
			for (int i = 0; i < branches.length; i++) {
				Map<String, Class<?>> argsSpec = branches[i]
						.getArgumentsSpecification();
				addAll(argumentsSpecification, argsSpec);
			}
		}
		return argumentsSpecification;
	}

	private void addAll(Map<String, Class<?>> accumulator,
			Map<String, Class<?>> argsSpec) {
		for (String key : argsSpec.keySet()) {
			if (accumulator.containsKey(key)) {
				if (!accumulator.get(key).equals(argsSpec.get(key))) {
					throw new RuntimeException(
							"Incompatible classes for the argument of the same name "
									+ key);
				}
			}
			accumulator.put(key, argsSpec.get(key));
		}
	}

	private int totalLength = -1;

	@Override
	public int totalLength() {
		if (totalLength == -1) {
			totalLength = 0;
			for (int i = 0; i < branches.length; i++) {
				totalLength += branches[i].totalLength();
			}
		}
		return totalLength;

	}

}
