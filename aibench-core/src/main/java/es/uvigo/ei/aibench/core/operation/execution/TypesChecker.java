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
 * TypesChecker.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.aibench.core.operation.execution;

class TypesChecker implements ResultsCollector {
	private final Class<?>[] classes;

	private final ResultsCollector decorated;

	private final boolean[] called;

	public TypesChecker(Class<?>[] classes, ResultsCollector collector) {
		if (collector == null)
			throw new NullPointerException("collector can't be null");
		this.classes = classes.clone();
		this.decorated = collector;
		this.called = new boolean[classes.length];
	}

	public void finish() {
		for (int i = 0; i < called.length; i++) {
			if (!called[i])
				throw new IllegalStateException("the endpoint " + i
						+ " has not been called");
		}
		decorated.finish();
	}

	public void newResult(int index, Object result)
			throws IndexOutOfBoundsException, ClassCastException {
		if (result != Void.TYPE)
			classes[index].cast(result);
		else if (!classes[index].equals(result))
			throw new ClassCastException("not expected Void.TYPE");
		called[index] = true;
		decorated.newResult(index, result);
	}



	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.core.operation.execution.ResultsCollector#crash(java.lang.Exception)
	 */
	public void crash(Throwable cause) {
		decorated.crash(cause);

	}
}
