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
 * CompositedCollector.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.aibench.core.operation.execution;

class CompositedCollector implements ResultsCollector {

	private final ResultsCollector collector;

	private int times;

	CompositedCollector(ResultsCollector collector, int i) {
		if (collector == null)
			throw new NullPointerException("collector can't be null");
		if (i < 1)
			throw new IllegalArgumentException("i must be greater than zero");
		this.collector = collector;
		times = i;
	}

	public void finish() {
		if (times == 0)
			throw new IllegalStateException("finish not expected");
		if (--times == 0) {
			collector.finish();
		}
	}

	public void newResult(int index, Object result) {
		collector.newResult(index, result);
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.core.operation.execution.ResultsCollector#crash(java.lang.Exception)
	 */
	public void crash(Throwable cause) {
		collector.crash(cause);

	}

}
