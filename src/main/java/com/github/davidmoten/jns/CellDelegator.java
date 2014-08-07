package com.github.davidmoten.jns;

public class CellDelegator implements Cell {

    private final Cell cell;

    public CellDelegator(Cell cell) {
        this.cell = cell;
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
        return cell.pressure();
    }

    @Override
    public Vector velocity() {
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

    @Override
    public Cell modifyPressure(double pressure) {
        return cell.modifyPressure(pressure);
    }

    @Override
    public Cell modifyVelocity(Vector v1) {
        return cell.modifyVelocity(v1);
    }

    @Override
    public Cell modifyPosition(Vector position) {
        return cell.modifyPosition(position);
    }

}
