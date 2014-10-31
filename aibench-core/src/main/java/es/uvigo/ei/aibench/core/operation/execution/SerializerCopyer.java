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
 * SerializerCopyer.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.aibench.core.operation.execution;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializerCopyer<O extends Serializable> {

	private final O originalObject;

	private final byte[] objectBytes;

	public SerializerCopyer(O objectToCopy) {
		try {
			this.originalObject = objectToCopy;
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream output = new ObjectOutputStream(
					byteArrayOutputStream);
			output.writeObject(this.originalObject);
			output.close();
			this.objectBytes = byteArrayOutputStream.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@SuppressWarnings("unchecked")
	public O makeCopy() {
		try {
			ObjectInputStream input = new ObjectInputStream(
					new ByteArrayInputStream(objectBytes));
			O copy = (O) input.readObject();
			input.close();
			return  copy;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}
