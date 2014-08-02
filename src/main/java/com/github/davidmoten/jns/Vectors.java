package com.github.davidmoten.jns;

import java.util.function.Function;

public class Vectors {

    public static final Vector create(double east, double north, double up) {
        return new VectorImpl(east, north, up);
    }

    public static final Vector create(Function<Direction, Double> f) {
        return new VectorImpl(f.apply(Direction.EAST), f.apply(Direction.NORTH),
                f.apply(Direction.UP));
    }

}
