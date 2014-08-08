package com.github.davidmoten.jns;

import static com.github.davidmoten.jns.Util.pressureAtDepth;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.davidmoten.jns.CellImpl.Builder;

public class SolverTest {

	private static final Logger log = LoggerFactory.getLogger(SolverTest.class);
	private static final double VELOCITY_PRECISION = 0.0000001;
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
	public void testStepWithRegularGridStillWaterCellFromCentreOfGrid() {
		checkNoChange(5, 5, 5);
	}

	@Test
	public void testStepWithRegularGridStillWaterCellFromBottomOfGrid() {
		checkNoChange(5, 5, 0);
	}

	@Test
	public void testStepWithRegularGridStillWaterCellFromSurfaceOfGrid() {
		checkNoChange(5, 5, 9);
	}

	@Test
	public void testStepWithRegularGridStillWaterCellFromEastSideOfGrid() {
		checkNoChange(0, 5, 5);
	}

	@Test
	public void testStepWithRegularGridStillWaterCellFromNorthSideOfGrid() {
		checkNoChange(5, 0, 5);
	}

	@Test
	public void testStepWithRegularGridStillWaterCellFromBottomCornerofGrid() {
		checkNoChange(0, 0, 0);
	}

	@Test
	public void testStepWithRegularGridStillWaterCellFromSurfaceSouthEastCornerofGrid() {
		checkNoChange(9, 9, 9);
	}

	@Test
	public void testStepWithRegularGrid2DStillWaterCellFromCentreOfGrid() {
		checkNoChange2D(5, 5, 0);
	}

	private void checkNoChange(int eastIndex, int northIndex, int upIndex) {
		final Solver solver = new Solver();
		final RegularGrid grid = createGrid();
		final Cell cell = grid.cell(eastIndex, northIndex, upIndex);
		double pressure = cell.pressure();
		assertNotNull(cell);
		VelocityPressure result = solver.step(cell, 1);
		checkEquals(Vector.ZERO, result.getVelocity(), VELOCITY_PRECISION);
		assertEquals(pressure, result.getPressure(), PRESSURE_PRECISION);
	}

	private void checkNoChange2D(int eastIndex, int northIndex, int upIndex) {
		final Solver solver = new Solver();
		final RegularGrid grid = createGrid2D();
		final Cell cell = grid.cell(eastIndex, northIndex, upIndex);
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

	private RegularGrid createGrid2D() {
		return RegularGrid.builder().cellSize(1).cellsEast(10).cellsNorth(10)
				.cellsUp(1).density(1025).viscosity(30).build();
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
