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
