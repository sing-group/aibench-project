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
 * SimpleIncomingEndPoint.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.aibench.core.operation.execution;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class SimpleIncomingEndPoint extends IncomingEndPoint {

	private final Method method;

	private final Object target;

	private final BlockingQueue<Callable<Object>> queue = new LinkedBlockingQueue<Callable<Object>>();

	private final Lock lock = new ReentrantLock();

	private final Condition notEmptyOrFinish = lock.newCondition();

	private boolean finished = false;

	public SimpleIncomingEndPoint(Method method, Object target) {
		if (method == null)
			throw new IllegalArgumentException("method can't be null");
		if (target == null)
			throw new IllegalArgumentException("target can't be null");
		if (!method.getDeclaringClass().isAssignableFrom(target.getClass()))
			throw new IllegalArgumentException(
					"the method must be applied to the object");
		this.method = method;
		this.target = target;

	}

	@Override
	final public void finish() {
		try {
			lock.lock();
			finished = true;
			notEmptyOrFinish.signalAll();
		} finally {
			lock.unlock();
		}
	}

	final int getWorkToDo(Collection<Callable<Object>> collection) {
		try {
			lock.lock();
			while (!finished && (queue.peek() == null))
				notEmptyOrFinish.awaitUninterruptibly();
			return queue.drainTo(collection);
		} finally {
			lock.unlock();
		}

	}

	@Override
	final protected void invoke(Object... args) {
		try {
			lock.lock();
			if (finished)
				throw new IllegalStateException("The session is already closed");
			assert args.length < 2;
			// ignore arguments if the method have no parameters
			if (method.getParameterTypes().length == 0)
				args = new Object[0];
			final Object[] finalArgs = args;
			queue.add(new Callable<Object>() {

				public Object call() throws Exception {
					Object result;
					try {
						result = method.invoke(target, finalArgs);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					if (method.getReturnType().equals(Void.TYPE))
						return Void.TYPE;
					else
						return result;
				}
			});
		} finally {
			lock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.core.operation.execution.IncomingEndPoint#getArgumentTypes()
	 */
	@Override
	public Class<?>[] getArgumentTypes() {

		return this.method.getParameterTypes();
	}
}
