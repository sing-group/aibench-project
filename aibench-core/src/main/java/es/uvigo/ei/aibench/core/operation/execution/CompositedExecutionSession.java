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
 * CompositedExecutionSession.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.aibench.core.operation.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class CompositedExecutionSession implements ExecutionSession {

	private final List<ExecutionSession> sessions;

	private final List<IncomingEndPoint> incomingEndPoints;

	private boolean finished = false;

	CompositedExecutionSession(ResultsCollector collector,
			ExecutionSession... sessions) {
		this(Arrays.asList(sessions));
	}

	CompositedExecutionSession(List<ExecutionSession> sessions) {
		this.sessions = Collections
				.unmodifiableList(new ArrayList<ExecutionSession>(sessions));
		List<IncomingEndPoint> list = new ArrayList<IncomingEndPoint>();
		for (ExecutionSession session : sessions) {
			list.addAll(session.getIncomingEndpoints());
		}
		this.incomingEndPoints = Collections
				.unmodifiableList(list);
	}

	public List<? extends IncomingEndPoint> getIncomingEndpoints() {
		return incomingEndPoints;
	}

	public void finish() {
		if (finished)
			throw new IllegalStateException("already finished");
		finished = true;
		for (ExecutionSession session : sessions) {
			session.finish();
		}
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.core.operation.execution.ExecutionSession#cancel()
	 */
	public void cancel() {
		// TODO Auto-generated method stub

	}

}
