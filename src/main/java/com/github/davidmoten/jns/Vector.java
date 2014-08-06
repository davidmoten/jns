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

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Vector [east=");
        builder.append(east);
        builder.append(", north=");
        builder.append(north);
        builder.append(", up=");
        builder.append(up);
        builder.append("]");
        return builder.toString();
    }

    public double up() {
        return up;
    }

    public double east() {
        return east;
    }

    public double north() {
        return north;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(east);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(north);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(up);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Vector other = (Vector) obj;
        if (Double.doubleToLongBits(east) != Double.doubleToLongBits(other.east))
            return false;
        if (Double.doubleToLongBits(north) != Double.doubleToLongBits(other.north))
            return false;
        if (Double.doubleToLongBits(up) != Double.doubleToLongBits(other.up))
            return false;
        return true;
    }

}
