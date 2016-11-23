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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import es.uvigo.ei.aibench.core.operation.execution.Executable;
import es.uvigo.ei.aibench.core.operation.execution.IncompatibleContraintsException;
import es.uvigo.ei.aibench.core.operation.execution.Parallelizer;

public class ParallelizerPipeDefinition extends PipeDefinition {

	private final List<PipeDefinition> sons;

	private final Map<String, Class<?>> argumentsSpecification;

	private final int length;

	private final int totalLength;

	ParallelizerPipeDefinition(PipeDefinition... sons) {
		super(aggregateIncoming(sons), aggregateOutcoming(sons));
		this.sons = new ArrayList<PipeDefinition>(Arrays.asList(sons));
		this.argumentsSpecification = aggregateArgumentsSpecification(sons);
		this.length = getLength(sons);
		this.totalLength = getTotalLength(sons);
	}

	private static int getLength(PipeDefinition[] sons) {
		int acc = 0;
		for (int i = 0; i < sons.length; i++) {
			acc += sons[i].length();
		}
		return acc;
	}

	private static int getTotalLength(PipeDefinition[] sons) {
		int acc = 0;
		for (int i = 0; i < sons.length; i++) {
			acc += sons[i].totalLength();
		}
		return acc;
	}

	private static List<Class<?>> aggregateIncoming(
			PipeDefinition[] pipeDefinitions) {
		List<Class<?>> result = new ArrayList<Class<?>>();
		for (int i = 0; i < pipeDefinitions.length; i++) {
			result.addAll(pipeDefinitions[i].getIncomeTypes());
		}
		return result;

	}

	private static List<Class<?>> aggregateOutcoming(
			PipeDefinition[] pipeDefinitions) {
		List<Class<?>> result = new ArrayList<Class<?>>();
		for (int i = 0; i < pipeDefinitions.length; i++) {
			result.addAll(pipeDefinitions[i].getOutcomeTypes());
		}
		return result;
	}

	private static Map<String, Class<?>> aggregateArgumentsSpecification(
			PipeDefinition[] definitions) {
		List<PipeDefinition> temp = new ArrayList<PipeDefinition>(Arrays
				.asList(definitions));
		Map<String, Class<?>> args = temp.remove(0).getArgumentsSpecification();
		while (temp.size() > 0) {
			args = CompositedPipeDefinition.aggregateArgumentsSpecification(
					args, temp.remove(0));
		}
		return args;
	}

	@Override
	public PipeDefinition join(PipeDefinition rightPart)
			throws IncompatibleContraintsException {
		return new CompositedPipeDefinition(this,rightPart);
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public int totalLength() {
		return totalLength;
	}

	@Override
	protected Executable instantiate_(Map<String, Object> args) {
		Executable[] executables = new Executable[sons.size()];
		return new Parallelizer(executables);
	}

	@Override
	public Map<String, Class<?>> getArgumentsSpecification() {
		return argumentsSpecification;
	}

}
