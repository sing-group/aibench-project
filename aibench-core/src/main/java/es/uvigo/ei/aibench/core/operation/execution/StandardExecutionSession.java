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
 * StandardExecutionSession.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.aibench.core.operation.execution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.ResultTreatment;

class StandardExecutionSession<T> implements ExecutionSession {

	private final List<IncomingEndPoint> incoming;

	private final List<SimpleIncomingEndPoint> all = new ArrayList<SimpleIncomingEndPoint>();

	private final Map<Integer, OutcomeTransformer> output = new HashMap<Integer, OutcomeTransformer>();

	private boolean finished = false;

	private final ResultsCollector collector;

	private final ExecutorService executor;

	private Thread dispatcher;

	StandardExecutionSession(List<EndpointsFactory<T>> factories,
			T target, ResultsCollector collector, ExecutorService executor) {
		if (executor == null)
			throw new NullPointerException("excecutor can't be null");
		this.executor = executor;
		List<IncomingEndPoint> incomingList = new ArrayList<IncomingEndPoint>();
		int pos = 0;

		for (int i = 0; i < factories.size(); i++) {
			EndpointsFactory<T> factory = factories.get(i);
			SimpleIncomingEndPoint endPoint = factory.instantiate(target);
			all.add(endPoint);
			if (factory.getDirection() != Direction.OUTPUT)
				incomingList.add(endPoint);
			if (factory.getDirection() != Direction.INPUT)
				output.put(i, new OutcomeTransformer(
						factory.getTreatment() == ResultTreatment.DATASOURCE,
						collector, pos++));
		}
		this.incoming = Collections
				.unmodifiableList(new ArrayList<IncomingEndPoint>(incomingList));
		if (collector == null)
			throw new NullPointerException("collector can't be null");
		this.collector = collector;
		this.dispatcher = new Thread(new Dispatcher());
		this.dispatcher.start();
	}

	private class Dispatcher implements Runnable {
		public void run() {
			Collection<Callable<Object>> jobs = new ArrayList<Callable<Object>>();
			for (int i = 0; i < all.size(); i++) {
				
				SimpleIncomingEndPoint endPoint = all.get(i);
				while ((endPoint.getWorkToDo(jobs)) > 0) { //deadlock!!!!
					
					Iterator<Callable<Object>> iter = jobs.iterator();
					while (iter.hasNext()) {

						Callable<Object> job = iter.next();
						Future<Object> future = executor.submit(job);
						try {

							Object result = future.get();

							if (output.containsKey(i)) {

								output.get(i).resultMade(result);

							}
							iter.remove();

						} catch (Exception e) {
							e.printStackTrace();
							
							
							Throwable cause =e;
							if (cause.getCause()!=null){
								cause = e.getCause();
							}
							if (cause.getCause()!=null){
								cause = cause.getCause();
							}
							
							if (cause.getCause()!=null){
								cause = cause.getCause();
							}
							//obtenemos hasta 3 causas
							System.err.println(output);
							System.err.println(output.get(i));
							for (Integer key : output.keySet()){
								OutcomeTransformer transformer = output.get(key);
								transformer.crash(cause);
							}
						
							
							// throw new RuntimeException(e);
						}

					}

					assert jobs.isEmpty();
				}

			}

			synchronized (StandardExecutionSession.this) {
				
				collector.finish();
			}
		}

	}

	public List<IncomingEndPoint> getIncomingEndpoints() {
		if (finished)
			throw new IllegalStateException("The result have been collected");
		return incoming;
	}

	public void finish() {
		synchronized (this) {
			for (IncomingEndPoint endPoint : all) {
				if (!endPoint.wasCalled())
					throw new IllegalStateException(
							"all endpoints must have been called");
				endPoint.finish();
			}
			finished = true;
		}
	}

	public void cancel() {
		this.executor.shutdownNow();
	}

}
