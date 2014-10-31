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
 * IncomingEndPoint.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
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
