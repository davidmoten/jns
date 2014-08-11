package com.github.davidmoten.jns;

import java.util.Optional;

public class CellDelegator implements Cell {

    private final Cell cell;
    private final Optional<Vector> velocity;
    private final Optional<Double> pressure;

    public CellDelegator(Cell cell, Optional<Vector> velocity, Optional<Double> pressure) {
        this.cell = cell;
        this.velocity = velocity;
        this.pressure = pressure;
    }

    @Override
    public CellType type() {
        return cell.type();
    }

    @Override
    public Vector position() {
        return cell.position();
    }

    @Override
    public double pressure() {
        if (pressure.isPresent())
            return pressure.get();
        else
            return cell.pressure();
    }

    @Override
    public Vector velocity() {
        if (velocity.isPresent())
            return velocity.get();
        else
            return cell.velocity();
    }

    @Override
    public double density() {
        return cell.density();
    }

    @Override
    public double viscosity() {
        return cell.viscosity();
    }

    @Override
    public Cell neighbour(Direction direction, int count) {
        return cell.neighbour(direction, count);
    }

}
