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
 * SerialExecutable.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.aibench.core.operation.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SerialExecutable extends AbstractExecutable {

	private List<Executable> executables;

	private final List<CollectorAdapterFactory> adapters;

	public SerialExecutable(Executable... executables) {
		this(Arrays.asList(executables));

	}

	public SerialExecutable(List<Executable> executables)
			throws IllegalArgumentException {
		if (executables.size() == 0)
			throw new IllegalArgumentException(
					"There must be at least one executable");
		checkElementsNotNull(executables);
		List<Executable> list = new ArrayList<Executable>();
		CollectorAdapterFactory[] adapters = new CollectorAdapterFactory[executables
				.size() - 1];

		Iterator<Executable> iter = executables.iterator();
		Executable present = iter.next();
		list.add(present);
		int i = 0;
		while (iter.hasNext()) {
			Executable next = iter.next();
			adapters[i++] = CollectorAdapterFactory.adapt(present, next);
			present = next;
			list.add(present);
		}
		assert i == adapters.length;
		this.executables = Collections.unmodifiableList(list);
		this.adapters = Arrays.asList(adapters);
	}

	public List<Class<?>> getIncomeArgumentTypes() {
		return executables.get(0).getIncomeArgumentTypes();
	}
	@Override
	public ExecutionSession openExecutionSession(ResultsCollector collector) {
		return executables.get(0).openExecutionSession(adaptExecutionSession(0,collector));
	}

	private ResultsCollector adaptExecutionSession(int pos, ResultsCollector originalCollector){
		if(pos==adapters.size()) return originalCollector;
		return adapters.get(pos).getAdapter(adaptExecutionSession(pos+1,originalCollector));
	}

	public List<Class<?>> getOutcomeArgumentTypes() {
		return executables.get(executables.size() - 1)
				.getOutcomeArgumentTypes();
	}

}
