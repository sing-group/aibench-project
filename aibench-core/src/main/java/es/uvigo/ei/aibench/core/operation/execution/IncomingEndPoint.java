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

import java.util.Iterator;

public abstract class IncomingEndPoint {

	private boolean wasCalled = false;

	public IncomingEndPoint() {

	}

	public void call() {
		call(Void.TYPE);
	}

	abstract void finish();

	public void call(final Object arg) {
		wasCalled = true;
		if (arg != Void.TYPE)
			invoke(arg);
		else
			invoke();
	}

	protected abstract void invoke(Object... args);

	public void call(Iterator<?> args) {
		wasCalled = true;
		if (!args.hasNext()) {
			call();
		} else {
			while (args.hasNext()) {
				call(args.next());
			}
		}
	}

	public void call(Iterable<?> args) {
		wasCalled = true;
		call(args.iterator());

	}

	public boolean wasCalled() {
		return wasCalled;
	}

	public abstract Class<?>[] getArgumentTypes();

}
