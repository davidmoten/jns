package com.github.davidmoten.jns;

import java.util.function.Function;

public class Matrixes {

    public static Matrix createWithRows(Vector row1, Vector row2, Vector row3) {
        return new Matrix(row1, row2, row3);
    }

    public static Matrix create(Function<Direction, Vector> f) {
        return createWithRows(f.apply(Direction.EAST), f.apply(Direction.NORTH),
                f.apply(Direction.UP));
    }
}
