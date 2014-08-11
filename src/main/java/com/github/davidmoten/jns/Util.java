package com.github.davidmoten.jns;

import java.util.Optional;
import java.util.function.Function;

public class Util {

    public static final double GRAVITY_M_PER_S2 = 9.80665;
    public static final int SEA_LEVEL_PRESSURE_PASCALS = 101325;
    public static final double SEAWATER_MEAN_DENSITY_KG_PER_M3 = 1025;
    public final static Vector GRAVITY = Vector.create(0, 0, -9.80665);
    public static final double SEAWATER_MEAN_VISCOSITY = 30;

    public static <T> T unexpected() {
        return unexpected("unexpected");
    }

    public static <T> T unexpected(String msg) {
        throw new RuntimeException(msg);
    }

    public static double pressureAtDepth(double depthMetres) {
        return SEA_LEVEL_PRESSURE_PASCALS + SEAWATER_MEAN_DENSITY_KG_PER_M3 * depthMetres
                * GRAVITY_M_PER_S2;
    }

    public static Vector pressureGradientDueToGravity(Cell cell) {
        return GRAVITY.times(cell.density());
    }

    public static Cell override(Cell cell, CellType type, Vector velocity, double pressure) {
        return new CellDelegator(cell, Optional.of(type), Optional.of(velocity),
                Optional.of(pressure));
    }

    static Mesh createMeshForWhirlpool2D() {
        final int cellsUp = 1;
        final int cellsEast = 10;
        final int cellsNorth = 10;
        final Function<Indices, CellType> typeFunction = i -> {
            // Floored bottom, obstacle sides, open north side
            if (i.up() < 0)
                return CellType.OBSTACLE;
            else if (i.up() > cellsUp - 1)
                return CellType.UNKNOWN;// air
            else if (i.east() <= 0 || i.east() >= cellsEast - 1)
                if (i.north() == cellsNorth - 1)
                    return CellType.UNKNOWN;
                else
                    return CellType.OBSTACLE;
            else if (i.north() <= 0)
                return CellType.OBSTACLE;
            else if (i.north() > cellsNorth - 1)
                return CellType.UNKNOWN;
            else
                // inside the box so is fluid
                return CellType.FLUID;
        };
        final Function<Indices, Vector> velocityFunction = i -> {
            if (i.north() == cellsNorth - 1)
                return Vector.create(1, 0, 0);
            else
                return Vector.ZERO;
        };
        return Mesh
                .builder()
                .cellSize(1)
                .creator(
                        CellCreator.builder().cellsEast(cellsEast).cellsNorth(cellsNorth)
                                .cellsUp(cellsUp).typeFunction(typeFunction)
                                .velocityFunction(velocityFunction).build()).build();
    }

    public static boolean isValid(Double d) {
        return d != Double.NaN && d != Double.NEGATIVE_INFINITY && d != Double.POSITIVE_INFINITY;
    }

    public static double validate(Double d) {
        if (isValid(d))
            return d;
        else
            throw new RuntimeException("invalid double value: " + d);
    }

}
