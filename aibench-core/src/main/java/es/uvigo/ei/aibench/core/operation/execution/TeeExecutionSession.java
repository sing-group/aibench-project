/*
 * #%L
 * The AIBench Core Plugin
 * %%
 * Copyright (C) 2006 - 2017 Daniel Glez-Peña and Florentino Fdez-Riverola
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
import java.util.Collections;
import java.util.List;

class TeeExecutionSession implements ExecutionSession {

	private final List<IncomingEndPoint> incomingEndPoints;

	private final List<ExecutionSession> branches;

	public TeeExecutionSession(List<ExecutionSession> branches) {
		if (branches.size() == 0)
			throw new IllegalArgumentException(
					"the number of branches must be at least one");
		this.branches = new ArrayList<ExecutionSession>(branches);
		List<List<? extends IncomingEndPoint>> allIncoming = new ArrayList<List<? extends IncomingEndPoint>>();
		for (ExecutionSession branch : branches) {
			allIncoming.add(branch.getIncomingEndpoints());
		}
		final int size = allIncoming.get(0).size();
		List<IncomingEndPoint> incoming = new ArrayList<IncomingEndPoint>();
		for (int i = 0; i < size; i++) {
			IncomingEndPoint[] endPoints = extract(allIncoming, i);
			incoming.add(new IncomingEndPointForwarder(endPoints));
		}
		assert incoming.size() == size;
		this.incomingEndPoints = Collections.unmodifiableList(incoming);
	}

	private static IncomingEndPoint[] extract(
			List<List<? extends IncomingEndPoint>> branchesIncoming,
			final int index) {
		IncomingEndPoint[] result = new IncomingEndPoint[branchesIncoming
				.size()];
		int i = 0;
		for (List<? extends IncomingEndPoint> list : branchesIncoming) {
			result[i++] = list.get(index);
		}
		assert i == branchesIncoming.size();
		return result;
	}

	public List<? extends IncomingEndPoint> getIncomingEndpoints() {
		return incomingEndPoints;
	}

	public void finish() {
		for (ExecutionSession branch : branches) {
			branch.finish();
		}

	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.core.operation.execution.ExecutionSession#cancel()
	 */
	public void cancel() {

	}

}
