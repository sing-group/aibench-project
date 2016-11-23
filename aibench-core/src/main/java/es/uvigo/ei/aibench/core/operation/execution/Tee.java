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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Tee extends AbstractExecutable {

	private final List<Executable> branches;

	private final List<Class<?>> incomeArgumentTypes;

	private final List<Class<?>> outcomeArgumentsTypes;

	public Tee(Executable... branches) {
		if (branches.length == 0)
			throw new IllegalArgumentException(
					"the branches must be at least one");
		checkElementsNotNull((Object[]) branches);
		this.branches = Arrays.asList(branches);
		List<Class<?>> outcomeArguments = new ArrayList<Class<?>>();
		for (Executable executable : branches) {
			outcomeArguments.addAll(executable.getOutcomeArgumentTypes());
		}
		List<Class<?>> incomeArgumentTypes = Unifier.unifier(Arrays
				.asList(branches));
		if (incomeArgumentTypes == null)
			throw new IllegalArgumentException(
					"The branches of the Tee can't be unified. Have incompatible types");
		this.incomeArgumentTypes = Collections
				.unmodifiableList(incomeArgumentTypes);
		this.outcomeArgumentsTypes = Collections
				.unmodifiableList(outcomeArguments);

	}

	public List<Class<?>> getIncomeArgumentTypes() {
		return incomeArgumentTypes;
	}

	public List<Class<?>> getOutcomeArgumentTypes() {
		return outcomeArgumentsTypes;
	}

	@Override
	public ExecutionSession openExecutionSession(ResultsCollector collector) {
		return new TeeExecutionSession(extractSessions(branches,
				new CompositedCollector(collector, branches.size())));
	}

	private static List<ExecutionSession> extractSessions(
			List<Executable> branches2, ResultsCollector collector) {
		List<ExecutionSession> result = new ArrayList<ExecutionSession>();
		int offset = 0;
		for (Executable executable : branches2) {
			result.add(executable.openExecutionSession(new OffsetCollector(
					offset, collector)));
			offset += executable.getIncomeArgumentTypes().size();
		}
		return result;
	}

}
