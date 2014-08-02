package com.github.davidmoten.jns;

public interface Vector {
    double value(Direction direction);

    Vector add(Vector vector);

    Vector minus(Vector vector);

    Vector times(double value);

    Vector divideBy(double value);
}
