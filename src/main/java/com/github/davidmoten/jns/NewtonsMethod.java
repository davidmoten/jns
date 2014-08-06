package com.github.davidmoten.jns;

import java.util.Optional;
import java.util.function.Function;

public class NewtonsMethod {

	public static Optional<Double> solve(Function<Double, Double> f, double x,
			double h, double precision, int maxIterations) {
		checkParameters(f, h, precision, maxIterations);
		double fx = f.apply(x);
		int i = 1;
		while (Math.abs(fx) > precision && i <= maxIterations) {
			double gradient = (f.apply(x + h) - fx) / h;
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

	private static void checkParameters(Function<Double, Double> f, double h,
			double precision, int maxIterations) {
		if (f == null)
			throw new NullPointerException("f must not be null");
		if (h <= 0)
			throw new IllegalArgumentException("h must be >0");
		if (precision <= 0)
			throw new IllegalArgumentException("precision must be >0");
		if (maxIterations < 1)
			throw new IllegalArgumentException(
					"maxIterations must be 1 or more");
	}

}
