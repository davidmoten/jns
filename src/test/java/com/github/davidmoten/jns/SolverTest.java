package com.github.davidmoten.jns;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.davidmoten.jns.CellImpl.Builder;

public class SolverTest {

	private static final double GRAVITY_M_PER_S2 = 9.80665;
	private static final int SEA_LEVEL_PRESSURE_PASCALS = 101325;
	private static final double DENSITY_KG_PER_M3 = 1025;

	@Test
	public void testSolverOn() {
		final Solver solver = new Solver();
		final Cell result = solver.step(createCell(), 1);
		System.out.println(result);
	}

	@Test
	public void testGetVelocityAfterTime() {
		final Solver solver = new Solver();
		final Vector result = solver.getVelocityAfterTime(createCell(), 1);
		System.out.println(result);
		assertEquals2(Vector.ZERO, result, 0.0000001);
	}

	private static void assertEquals2(Vector a, Vector b, double precision) {
		assertEquals(a.east(), b.east(), precision);
	}

	private static Cell createCell() {

		final Cell north = builder().position(0, 1, -1).build();
		final Cell south = builder().position(0, -1, -1).build();
		final Cell east = builder().position(-1, 0, -1).build();
		final Cell west = builder().position(1, 0, -1).build();
		final Cell up = builder().position(0, 0, 0)
				.pressure(pressureAtDepth(0)).build();
		final Cell down = builder().position(0, 0, -2)
				.pressure(pressureAtDepth(2)).build();
		final Cell centre = builder().position(0, 0, -1).north(north)
				.south(south).east(east).west(west).up(up).down(down).build();
		return centre;

	}

	private static Cell createCell2() {

		final CellImpl north = builder().position(0, 1, -1).build();
		final CellImpl south = builder().position(0, -1, -1).build();
		final CellImpl east = builder().position(-1, 0, -1).build();
		final CellImpl west = builder().position(1, 0, -1).build();
		final CellImpl up = builder().position(0, 0, 0)
				.pressure(pressureAtDepth(0)).build();
		final CellImpl down = builder().position(0, 0, -2)
				.pressure(pressureAtDepth(2)).build();
		final CellImpl centre = builder().position(0, 0, -1).north(north)
				.south(south).east(east).west(west).up(up).down(down).build();
		north.south(centre);
		south.north(centre);
		east.west(centre);
		west.east(centre);
		up.down(centre);
		down.up(centre);
		return centre;

	}

	private static double pressureAtDepth(double depthMetres) {
		return SEA_LEVEL_PRESSURE_PASCALS + DENSITY_KG_PER_M3 * depthMetres
				* GRAVITY_M_PER_S2;
	}

	private static Builder builder() {
		return CellImpl.builder().pressure(pressureAtDepth(1))
				.density(DENSITY_KG_PER_M3).temperature(293).viscosity(30)
				.type(CellType.FLUID).velocity(0, 0, 0);
	}

}
