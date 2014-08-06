package com.github.davidmoten.jns;

import static org.junit.Assert.assertEquals;

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
}
