package com.github.davidmoten.jns;

import java.util.Optional;
import java.util.function.Function;

public class NewtonsMethod {

    public static Optional<Double> solve(Function<Double, Double> f, double initialValue,
            double delta, double precision, int maxIterations) {
        double x = initialValue;
        checkParameters(f, delta, precision, maxIterations);
        double fx = f.apply(x);
        int i = 1;
        while (Math.abs(fx) > precision && i <= maxIterations) {
            final double gradient = (f.apply(x + delta) - fx) / delta;
            if (gradient == 0)
                return Optional.empty();
            else
                x = x - fx / gradient;
            fx = f.apply(x);
            i++;
        }
        if (Math.abs(fx) <= precision)
            return Optional.of(x);
        else
            return Optional.empty();
    }

    private static void checkParameters(Function<Double, Double> f, double h, double precision,
            int maxIterations) {
        if (f == null)
            throw new NullPointerException("f must not be null");
        if (h <= 0)
            throw new IllegalArgumentException("h must be >0");
        if (precision <= 0)
            throw new IllegalArgumentException("precision must be >0");
        if (maxIterations < 1)
            throw new IllegalArgumentException("maxIterations must be 1 or more");
    }

    public static Builder solver() {
        return new Builder();
    }

    public static final class Builder {

        private Function<Double, Double> f;
        private double initialValue = 1;
        private double delta = 0.1;
        private double precision = 0.00001;
        private int maxIterations = 100;

        private Builder() {

        }

        public Builder function(Function<Double, Double> f) {
            this.f = f;
            return this;
        }

        public Builder initialValue(Double d) {
            this.initialValue = d;
            return this;
        }

        public Builder delta(Double d) {
            this.delta = d;
            return this;
        }

        public Builder precision(Double d) {
            this.precision = d;
            return this;
        }

        public Builder maxIterations(int n) {
            this.maxIterations = n;
            return this;
        }

        public Optional<Double> solve() {
            return NewtonsMethod.solve(f, initialValue, delta, precision, maxIterations);
        }

    }

}
