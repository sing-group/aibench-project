/*
 * #%L
 * The AIBench Core Plugin
 * %%
 * Copyright (C) 2006 - 2016 Daniel Glez-Peña and Florentino Fdez-Riverola
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
import java.util.Map.Entry;

import es.uvigo.ei.pipespecification.storage.PipeDefinition;

public class CollectorAdapterFactory {

	private final List<Entry<Class<?>, Integer>> groupsSpecification;

	private final Executable adapted;

	public static CollectorAdapterFactory adapt(Executable one, Executable next) {
		List<Class<?>> oneOutcome = one.getOutcomeArgumentTypes();
		List<Class<?>> nextIncome = next.getIncomeArgumentTypes();
		List<Entry<Class<?>, Integer>> groups = unify(oneOutcome, nextIncome);
		return new CollectorAdapterFactory(next, groups);
	}

	public static List<Entry<Class<?>, Integer>> join(PipeDefinition present,
			PipeDefinition next) throws IncompatibleConstraintsException {
		List<Entry<Class<?>, Integer>> unify = null;
		try {
			unify = unify(present.getOutcomeTypes(), next.getIncomeTypes());
		} catch (IllegalArgumentException e) {
			throw new IncompatibleConstraintsException(e.getMessage());
		}
		return unify;
	}

	private static String format(List<Class<?>> oneOutcome, List<Class<?>> nextIncome) {
		return oneOutcome.toString() + " , " + nextIncome.toString();
	}

	private static List<Entry<Class<?>, Integer>> unify(List<Class<?>> oneOutcome,
			List<Class<?>> nextIncome) {
		if (oneOutcome.size() < nextIncome.size())
			throw new IllegalArgumentException(
					"the executables cannot be unified since the next can't "
							+ "have less incomingEndPoints than the present outcomingEndPoints. "
							+ format(oneOutcome, nextIncome));
		List<Entry<Class<?>, Integer>> result = new ArrayList<Entry<Class<?>, Integer>>();
		if (!join(new ArrayList<Class<?>>(oneOutcome), new ArrayList<Class<?>>(
				nextIncome), result))
			throw new IllegalArgumentException(
					"the executables cannot be unified. "
							+ format(oneOutcome, nextIncome));
		return result;
	}

	private static boolean join(List<Class<?>> left, List<Class<?>> right,
			List<Entry<Class<?>, Integer>> result) {
		if (left.size() == 0) {
			return right.size() == 0;
		}
		final int resultSize = result.size();
		int extra = left.size() - right.size();
		Class<?> first = left.remove(0);
		assert extra >= 0;
		boolean sucess = false;
		do {
			List<Class<?>> newRight = new ArrayList<Class<?>>(right);
			sucess = joinClass(first, newRight, extra, result)
					&& join(left, newRight, result);
			if (!sucess) {
				int difference = result.size() - resultSize;
				assert difference >= 0;
				for (int i = result.size() - difference; i < result.size(); i++) {
					result.remove(i);
				}
				assert result.size() == resultSize;
			}
			extra--;
		} while (!sucess && extra >= 0);
		return sucess;
	}

	private static class GroupSpecification implements Entry<Class<?>, Integer> {
		private final Class<?> klass;

		private final Integer integer;

		public GroupSpecification(Class<?> klass, Integer integer) {
			this.klass = klass;
			this.integer = integer;
		}

		public Class<?> getKey() {
			return klass;
		}

		public Integer getValue() {
			return integer;
		}

		public Integer setValue(Integer value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return klass + ":" + integer;
		}

	}

	private static boolean joinClass(Class<?> first, List<Class<?>> right, int extra,
			List<Entry<Class<?>, Integer>> result) {
		int total = extra + 1;
		for (int i = 0; i < total; i++) {
			if (!compatible(first, right.get(i))) {
				return false;
			}
		}
		for (int i = 0; i < total; i++) {
			right.remove(0);
		}
		result.add(new GroupSpecification(first, total));
		return true;

	}

	private static boolean compatible(Class<?> left, Class<?> right) {
		return (left.isAssignableFrom(right) || right.equals(Void.TYPE));

	}

	private CollectorAdapterFactory(Executable adapted,
			List<Entry<Class<?>, Integer>> groups) {
		if (adapted == null)
			throw new NullPointerException("adapted can't be null");
		this.adapted = adapted;
		this.groupsSpecification = groups;

	}

	ResultsCollector getAdapter(ResultsCollector collector) {
		return new CollectorAdapter(adapted.openExecutionSession(collector),
				groupsSpecification);
	}
}

class CollectorAdapter implements ResultsCollector {

	private final ExecutionSession adapted;

	private List<IncomingEndPoint> incomingEndPoints;

	CollectorAdapter(ExecutionSession adapted,
			List<Entry<Class<?>, Integer>> groupsSpecification) {
		this.adapted = adapted;
		int actual = 0;
		List<? extends IncomingEndPoint> forwardedTo = adapted
				.getIncomingEndpoints();
		List<IncomingEndPoint> incoming = new ArrayList<IncomingEndPoint>();
		for (Entry<Class<?>, Integer> entry : groupsSpecification) {
			int n = entry.getValue();
			assert n >= 1;
			incoming.add(new IncomingEndPointForwarder(forwardedTo.subList(
					actual, actual + n).toArray(new IncomingEndPoint[0])));
			actual += n;
		}
		assert actual == forwardedTo.size();
		incomingEndPoints = Collections.unmodifiableList(incoming);
	}

	public void finish() {
		adapted.finish();
	}

	public void newResult(int index, Object result) {
		incomingEndPoints.get(index).call(result);
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.core.operation.execution.ResultsCollector#crash(java.lang.Exception)
	 */
	public void crash(Throwable cause) {
		// TODO Auto-generated method stub

	}

}
