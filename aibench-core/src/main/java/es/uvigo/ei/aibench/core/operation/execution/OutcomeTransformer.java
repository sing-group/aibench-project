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
 * OutcomeTransformer.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.aibench.core.operation.execution;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

class OutcomeTransformer {

	private final boolean dataSource;

	private final int pos;

	private final ResultsCollector collector;

	OutcomeTransformer(boolean dataSource, ResultsCollector collector, int pos) {
		if (collector == null)
			throw new NullPointerException("collector can't be null");
		if (pos < 0)
			throw new IllegalArgumentException(
					"pos must be equal or greater than zero");
		this.dataSource = dataSource;
		this.pos = pos;
		this.collector = collector;
	}

	void resultMade(Object result) {
		if (dataSource) {
			Collection<Object> results = extract(result);
			for (Object obj : results) {
				collector.newResult(pos, obj);
			}
		} else
			collector.newResult(pos, result);
	}

	void crash(Throwable e){
		collector.crash(e);
	}
	private Collection<Object> extract(Object result) {
		if (result instanceof Iterable<?>) {
			Iterable<?> returnValue = (Iterable<?>) result;
			return extract(returnValue.iterator());
		} else if (result.getClass().isArray()) {
			List<Object> list = new ArrayList<Object>();
			final int length = Array.getLength(result);
			for (int i = 0; i < length; i++) {
				list.add((Array.get(result, i)));
			}
			return list;
		} else
			return extract((Iterator<?>) result);
	}

	private Collection<Object> extract(Iterator<?> iter) {
		List<Object> result = new ArrayList<Object>();
		while (iter.hasNext()) {
			result.add(iter.next());
		}
		return result;
	}



}
