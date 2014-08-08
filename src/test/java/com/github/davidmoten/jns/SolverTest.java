package com.github.davidmoten.jns;

import static com.github.davidmoten.jns.Util.pressureAtDepth;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.davidmoten.jns.CellImpl.Builder;

public class SolverTest {

	private static final double VELOCITY_PRECISION = 0.0000001;
	private static final Logger log = LoggerFactory.getLogger(SolverTest.class);
	private static final double PRESSURE_PRECISION = 0.01;

	@Test
	public void testGetVelocityAfterTime() {
		final Solver solver = new Solver();
		final Vector result = solver.getVelocityAfterTime(createCell(), 1);
		log.info("velocityAfterTime={}", result);
		checkEquals(Vector.ZERO, result, VELOCITY_PRECISION);
	}

	@Test
	public void testGetVelocityAfterTimeWithRegularGridStillWater() {
		final Solver solver = new Solver();
		final RegularGrid grid = createGrid();
		final Cell cell = grid.cell(5, 5, 5);
		assertNotNull(cell);
		Vector result = solver.getVelocityAfterTime(cell, 1);
		checkEquals(Vector.ZERO, result, VELOCITY_PRECISION);
	}

	@Test
	public void testStepWithRegularGridStillWater() {
		final Solver solver = new Solver();
		final RegularGrid grid = createGrid();
		final Cell cell = grid.cell(5, 5, 5);
		double pressure = cell.pressure();
		assertNotNull(cell);
		VelocityPressure result = solver.step(cell, 1);
		checkEquals(Vector.ZERO, result.getVelocity(), VELOCITY_PRECISION);
		assertEquals(pressure, result.getPressure(), PRESSURE_PRECISION);
	}

	private RegularGrid createGrid() {
		return RegularGrid.builder().cellSize(1).cellsEast(10).cellsNorth(10)
				.cellsUp(10).density(1025).viscosity(30).build();
	}

	private static void checkEquals(Vector a, Vector b, double precision) {
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
