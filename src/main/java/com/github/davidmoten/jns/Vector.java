package com.github.davidmoten.jns;

import java.util.function.Function;

public class Vector {

    public static final Vector ZERO = create(0, 0, 0);
    private final double east;
    private final double north;
    private final double up;

    private Vector(double east, double north, double up) {
        this.east = east;
        this.north = north;
        this.up = up;
    }

    public double value(Direction direction) {
        if (direction.equals(Direction.EAST))
            return east;
        else if (direction.equals(Direction.NORTH))
            return north;
        else if (direction.equals(Direction.UP))
            return up;
        else
            throw new RuntimeException("direction " + direction + " not expected");
    }

    public Vector add(Vector v) {
        return create(east + v.east, north + v.north, up + v.up);
    }

    public Vector minus(Vector v) {
        return create(east - v.east, north - v.north, up - v.up);
    }

    public Vector times(double value) {
        return create(east * value, north * value, up * value);
    }

    public Vector divideBy(double value) {
        return create(east / value, north / value, up / value);
    }

    public double sum() {
        return east + north + up;
    }

    public double dotProduct(Vector v) {
        return east * v.east + north * v.north + up * v.up;
    }

    public static final Vector create(double east, double north, double up) {
        return new Vector(east, north, up);
    }

    public static final Vector create(Function<Direction, Double> f) {
        return new Vector(f.apply(Direction.EAST), f.apply(Direction.NORTH), f.apply(Direction.UP));
    }
}
