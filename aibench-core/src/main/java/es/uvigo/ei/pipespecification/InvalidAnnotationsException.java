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
 * InvalidAnnotationsException.java
 * Created inside the SING research group (http://sing.ei.uvigo.es)
 * University of Vigo
 *
 */
package es.uvigo.ei.pipespecification;

public class InvalidAnnotationsException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int pipeNumber = -1;

	public InvalidAnnotationsException(String message) {
		super(message);
	}

	public InvalidAnnotationsException(int pipeNumber) {
		super();
		this.pipeNumber = pipeNumber;
	}

	public InvalidAnnotationsException(String message, Throwable cause,
			int pipeNumber) {
		super(message, cause);
		this.pipeNumber = pipeNumber;
	}

	public InvalidAnnotationsException(String message, int pipeNumber) {
		super(message);
		this.pipeNumber = pipeNumber;
	}

	public InvalidAnnotationsException(Throwable cause, int pipeNumber) {
		super(cause);
		this.pipeNumber = pipeNumber;
	}

	public int getPipeNumber() {
		return pipeNumber;
	}

}
