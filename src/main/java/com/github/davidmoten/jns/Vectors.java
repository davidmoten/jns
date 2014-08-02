package com.github.davidmoten.jns;

import java.util.List;

public class Vectors {

    public static final Vector create(double east, double north, double up) {
        return new VectorImpl(east, north, up);
    }

    public static final Vector create(List<Double> list) {
        return new VectorImpl(list.get(0), list.get(1), list.get(2));
    }

}
