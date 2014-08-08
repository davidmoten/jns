package com.github.davidmoten.jns;

public interface Cell extends CellData {

	Cell neighbour(Direction direction, int count);

	Cell modifyPressure(double pressure);

	Cell modifyVelocity(Vector velocity);

	Cell modifyPosition(Vector position);

}
