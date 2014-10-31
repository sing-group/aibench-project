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
 * IncomingEndPointForwarder.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.aibench.core.operation.execution;

class IncomingEndPointForwarder extends IncomingEndPoint {
	private final IncomingEndPoint[] incoming;

	public IncomingEndPointForwarder(IncomingEndPoint... incoming) {
		this.incoming = incoming.clone();
		if (incoming.length == 0)
			throw new IllegalArgumentException(
					"At least one incomingEndPoint to forward");
	}

	@Override
	protected void invoke(Object... args) {
		int argsSize = args.length;
		assert args.length <= 1;
		SerializerCopyer<Object[]> copyer = new SerializerCopyer<Object[]>(args);
		for (IncomingEndPoint in : incoming) {
			if (argsSize == 0)
				in.call();
			else
				in.call(copyer.makeCopy()[0]);

		}
	}

	@Override
	public void finish() {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.core.operation.execution.IncomingEndPoint#getArgumentTypes()
	 */
	@Override
	public Class<?>[] getArgumentTypes() {
		// TODO DANNY no se que poner aqui
		return null;
	}

}
