package com.github.davidmoten.jns;

public interface Cell {

    Position position();

    double pressure();

    Velocity velocity();

    double temperature();

    double density();

    double viscosity();

    Cell neighbour(Direction direction, int count);

}
