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
 * SynchronousResultCollector.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.aibench.core.operation.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class SynchronousResultCollector implements ResultsCollector {

	private boolean finished = false;

	private Throwable exception = null;

	private final List<List<Object>> results = new ArrayList<List<Object>>();

	private final CountDownLatch latch = new CountDownLatch(1);

	public synchronized void finish() {
		if (finished)
			throw new IllegalStateException("already finished");
		finished = true;
		latch.countDown();
	}

	public synchronized void newResult(int index, Object result) {
		if (finished)
			throw new IllegalStateException(
					"Once finished can't receive more results");
		if (index < 0)
			throw new IllegalArgumentException(
					"index must be greater than zero");
		while (index > results.size() - 1) {
			results.add(new ArrayList<Object>());
		}
		results.get(index).add(result);
	}

	public List<List<Object>> getResults() throws Throwable{
		try {
			latch.await();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		if (exception !=null) throw exception;
		return unmodify(results);
	}

	private static List<List<Object>> unmodify(List<List<Object>> results2) {
		List<List<Object>> unmodifiable = new ArrayList<List<Object>>();
		for (List<Object> list : results2) {
			unmodifiable.add(Collections.unmodifiableList(list));
		}
		return Collections.unmodifiableList(unmodifiable);
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.core.operation.execution.ResultsCollector#crash(java.lang.Exception)
	 */
	public void crash(Throwable cause) {
		if (finished)
			throw new IllegalStateException("already finished");
		finished = true;
		exception = cause;
		latch.countDown();

	}

}
