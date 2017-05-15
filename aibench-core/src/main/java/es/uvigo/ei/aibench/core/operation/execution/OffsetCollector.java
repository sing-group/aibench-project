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

	}

}
