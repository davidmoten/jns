package com.github.davidmoten.jns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Optional;

import org.junit.Test;

public class NewtonsMethodTest {

	@Test
	public void testCanSolveForSquareRootOf2() {
		double precision = 0.00001;
		Optional<Double> r = NewtonsMethod.solve(x -> x * x - 2, 1, 0.1,
				precision, 100);
		assertEquals(Math.sqrt(2.0), r.get(), precision);
	}

	@Test
	public void testCanSolveForSquareRootOf2UsingBuilder() {
		double precision = 0.00001;
		Optional<Double> r = NewtonsMethod.solver().function(x -> x * x - 2)
				.initialValue(1.0).delta(0.1).precision(precision)
				.maxIterations(100).solve();
		assertEquals(Math.sqrt(2.0), r.get(), precision);
	}

	@Test
	public void testCanSolveForSquareRootOf2UsingBuilderAndDefaults() {
		double precision = 0.00001;
		Optional<Double> r = NewtonsMethod.solver().function(x -> x * x - 2)
				.solve();
		assertEquals(Math.sqrt(2.0), r.get(), precision);
	}

	@Test
	public void testCannotMeetPrecision() {
		double precision = 0.00001;
		Optional<Double> r = NewtonsMethod.solve(x -> x * x - 2, 1, 0.1,
				precision, 1);
		assertFalse(r.isPresent());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testStepIsZeroThrowsException() {
		NewtonsMethod.solve(x -> x * x - 2, 1, 0, 0.01, 100);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPrecisionIsZeroThrowsException() {
		NewtonsMethod.solve(x -> x * x - 2, 1, 0.1, 0, 100);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMaxIterationsIsZeroThrowsException() {
		NewtonsMethod.solve(x -> x * x - 2, 1, 0.1, 0.0001, 0);
	}

	@Test(expected = NullPointerException.class)
	public void testFunctionIsNullThrowsException() {
		NewtonsMethod.solve(null, 1, 0.1, 0.0001, 100);
	}
}
