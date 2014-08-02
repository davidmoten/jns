package com.github.davidmoten.jns;

public class Vectors {

    public static final Vector create(double east, double north, double up) {
        return new VectorImpl(east, north, up);
    }

}
