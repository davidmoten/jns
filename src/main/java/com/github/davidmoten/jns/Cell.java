package com.github.davidmoten.jns;

public interface Cell {

	Vector position();

	double pressure();

	Vector velocity();

	double temperature();

	double density();

	double viscosity();

	Cell neighbour(Direction direction, int count);

	Cell modifyPressure(double pressure);

	Cell modifyVelocity(Vector v1);

}
