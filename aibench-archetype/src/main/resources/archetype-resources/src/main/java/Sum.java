/*
 * #%L
 * The AIBench Maven Archetype for plugins/apps
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
package ${package};

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port; 

@Operation(description = "This is a sample operation that adds two numbers.")
public class Sum {
	private int x, y;

	@Port(direction = Direction.INPUT, name = "x param", order = 1)
	public void setX(int x) {
		this.x = x;
	}

	@Port(direction = Direction.INPUT, name = "y param", order = 2)
	public void setY(int y) {
		this.y = y;
	}

	@Port(direction = Direction.OUTPUT, order = 3)
	public int sum() {
		return this.x + this.y;
	}
}
