package com.github.davidmoten.jns;

public interface CellData {
    CellType type();

    Vector position();

    double pressure();

    Vector velocity();

    double density();

    double viscosity();

    boolean isBoundary();

}
