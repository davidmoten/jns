package com.github.davidmoten.jns;

import org.junit.Test;

import com.github.davidmoten.jns.CellImpl.Builder;

public class SolverTest {

	private static Cell createCell() {

		Cell north = builder().position(0, 1, -1).build();
		Cell south = builder().position(0, -1, -1).build();
		Cell east = builder().position(-1, 0, -1).build();
		Cell west = builder().position(1, 0, -1).build();
		Cell up = builder().position(0, 0, 0).pressure(101325).build();
		Cell down = builder().position(0, 0, -2).pressure(121429).build();
		Cell centre = builder().position(0, 0, -1).north(north).south(south)
				.east(east).west(west).up(up).down(down).build();
		return centre;

	}

	private static Builder builder() {
		return CellImpl.builder().pressure(111377).density(1025)
				.temperature(293).viscosity(30).type(CellType.FLUID)
				.velocity(0, 0, 0);
	}

	@Test
	public void test() {
		Solver solver = new Solver();
		Cell result = solver.step(createCell(), 1);
		System.out.println(result);
	}

}
