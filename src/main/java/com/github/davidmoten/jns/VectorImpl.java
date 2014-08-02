package com.github.davidmoten.jns;

public class VectorImpl implements Vector {

    private double east;
    private double north;
    private double up;

    public VectorImpl(double east, double north, double up) {
        this.east = east;
        this.north = north;
        this.up = up;
    }

    @Override
    public double value(Direction direction) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Vector add(Vector vector) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Vector minus(Vector vector) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Vector times(double value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Vector divideBy(double value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double sum() {
        return east + north + up;
    }

}
