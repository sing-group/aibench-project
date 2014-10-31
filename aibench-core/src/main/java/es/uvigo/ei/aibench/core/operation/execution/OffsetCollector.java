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
 * OffsetCollector.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.aibench.core.operation.execution;

class OffsetCollector implements ResultsCollector {

	private final ResultsCollector decorated;
	private final int offset;

	public OffsetCollector(int offset, ResultsCollector compositedCollector) {
		if(compositedCollector==null) throw new NullPointerException("compositedCollector can't be null");
		if (offset < 0)
			throw new IllegalArgumentException("the ofset must be greater than zeroF");
		this.decorated=compositedCollector;
		this.offset=offset;
	}

	public void finish() {
		decorated.finish();
	}

	public void newResult(int index, Object result) {
		decorated.newResult(index+offset, result);
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.core.operation.execution.ResultsCollector#crash(java.lang.Exception)
	 */
	public void crash(Throwable cause) {
		// TODO Auto-generated method stub

	}

}
