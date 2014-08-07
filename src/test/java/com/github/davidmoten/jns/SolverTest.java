package com.github.davidmoten.jns;

import static com.github.davidmoten.jns.Util.pressureAtDepth;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.davidmoten.jns.CellImpl.Builder;

public class SolverTest {

	private static final Logger log = LoggerFactory.getLogger(SolverTest.class);

	@Test
	public void testGetVelocityAfterTime() {
		final Solver solver = new Solver();
		final Vector result = solver.getVelocityAfterTime(createCell(), 1);
		log.info("velocityAfterTime={}", result);
		assertEquals2(Vector.ZERO, result, 0.0000001);
	}

	@Test
	public void testSolverWithRegularGridStillWater() {
		final Solver solver = new Solver();
		final RegularGrid grid = RegularGrid.builder().cellSize(1)
				.cellsEast(10).cellsNorth(10).cellsUp(10).density(1025)
				.viscosity(30).build();
		final Cell cell = grid.cell(5, 5, 5);
		VelocityPressure result = solver.step(cell, 1);
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

	private static Builder builder() {
		return CellImpl.builder().pressure(pressureAtDepth(1))
				.density(Util.DENSITY_KG_PER_M3).viscosity(30)
				.type(CellType.FLUID).velocity(0, 0, 0);
	}

}
