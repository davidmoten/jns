package com.github.davidmoten.jns.v3;

@FunctionalInterface
public interface Forcing {

    double get(int i, int j, int k, double time);

}
