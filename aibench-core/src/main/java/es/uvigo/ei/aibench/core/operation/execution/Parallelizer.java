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
 * Parallelizer.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.aibench.core.operation.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Parallelizer extends AbstractExecutable {

	private final List<Executable> branches;

	private final List<Class<?>> incomeArgumentTypes;

	private final List<Class<?>> outcomeArgumentTypes;

	public Parallelizer(Executable... branches) throws IllegalArgumentException {
		if (branches == null)
			throw new IllegalArgumentException("branches can't be null");
		this.branches = Collections.unmodifiableList(Arrays.asList(branches
				.clone()));
		checkElementsNotNull(this.branches);
		List<Class<?>> incomeArgsTypes = new ArrayList<Class<?>>();
		List<Class<?>> outcomeArgsTypes = new ArrayList<Class<?>>();
		for (Executable branch : branches) {
			incomeArgsTypes.addAll(branch.getIncomeArgumentTypes());
			outcomeArgsTypes.addAll(branch.getOutcomeArgumentTypes());
		}
		this.incomeArgumentTypes = Collections
				.unmodifiableList(incomeArgsTypes);
		this.outcomeArgumentTypes = Collections
				.unmodifiableList(outcomeArgsTypes);
	}

	@Override
	public ExecutionSession openExecutionSession(ResultsCollector collector) {
		ExecutionSession[] sessions = new ExecutionSession[branches.size()];
		ResultsCollector compositedCollector = new CompositedCollector(
				collector, branches.size());
		int offset =0;		
		for (int i = 0; i < sessions.length; i++) {
			Executable branch = branches.get(i);
			sessions[i] = branch.openExecutionSession(
					new OffsetCollector(offset,compositedCollector));
			offset+=branch.getIncomeArgumentTypes().size();
		}
		return new CompositedExecutionSession(collector, sessions);
	}

	public List<Class<?>> getIncomeArgumentTypes() {
		return incomeArgumentTypes;
	}

	public List<Class<?>> getOutcomeArgumentTypes() {
		return outcomeArgumentTypes;
	}

}
