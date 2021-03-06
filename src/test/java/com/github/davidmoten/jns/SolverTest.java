package com.github.davidmoten.jns;

import static com.github.davidmoten.jns.TestingUtil.createMesh;
import static com.github.davidmoten.jns.TestingUtil.createMesh2D;
import static com.github.davidmoten.jns.TestingUtil.createMeshForWhirlpool2DTenByTen;
import static com.github.davidmoten.jns.Util.pressureAtDepth;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.davidmoten.jns.CellImpl.Builder;

@Ignore
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
        final Mesh mesh = createMesh();
        final Cell cell = mesh.cell(5, 5, 5);
        assertNotNull(cell);
        final Vector result = solver.getVelocityAfterTime(cell, 1);
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

    @Test
    public void test2DUpDerivative() {
        final Solver solver = new Solver();
        final Mesh mesh = createMesh2D();
        final Cell cell = mesh.cell(5, 9, 0);
        final double v = solver.getPressureGradient(cell, Direction.UP);
    }

    @Test
    public void testTransform2D() {
        final Mesh mesh = createMesh2D();
        final Cell c2 = mesh.cell(5, 9, 0);
        final Cell c1 = c2.down();
        final Cell c3 = c2.up();
        log.info("cells={}.{},{}", c1.position(), c2.position(), c3.position());
        final CellTriplet t = Solver.transform(CellTriplet.create(c1, c2, c3));
        log.info("cells={}.{},{}", t.c1().position(), t.c2().position(), t.c3().position());
        assertFalse((t.c1().position().equals(t.c2().position())));
    }

    @Test
    public void testObstacleToValue() {
        final Mesh mesh = createMesh2D();
        final Cell cell = mesh.cell(5, 9, 0);
        final Cell obstacle = mesh.cell(5, 9, -1);
        checkEquals(obstacle.position(), Solver.obstacleToValue(obstacle, cell).position(),
                VELOCITY_PRECISION);
    }

    @Test
    public void testMeshStep() {
        checkNoChangeAfterStep(5, 5, 5);
    }

    @Test
    public void testCellAtBoundaryOfWhirpoolMeshReturnsConstantVelocity() {
        final Mesh mesh = TestingUtil.createMeshForWhirlpool2DTenByTen();
        final Solver solver = new Solver();
        // cell is on open boundary so should not be predicted using Navier
        // Stokes, rather it is a boundary condition that is updated at some
        // interval (or perhaps interpolated)
        final Cell cell = mesh.cell(5, 9, 0);
        assertEquals(CellType.FLUID, mesh.cell(5, 9, 0).type());
        checkEquals(Vector.create(1, 0, 0), cell.velocity(), VELOCITY_PRECISION);
        assertEquals(CellType.UNKNOWN, mesh.cell(5, 9, 1).type());
        assertEquals(CellType.OBSTACLE, cell.down().type());
        checkEquals(Vector.create(1, 0, 0), solver.getVelocityAfterTime(cell, 1),
                VELOCITY_PRECISION);
    }

    @Test
    public void testWhirlpool() {
        Mesh m = createMeshForWhirlpool2DTenByTen();
        double initialPressure = m.cell(5, 7, 0).pressure();
        assertTrue(initialPressure > 0);
        Vector initialVelocity = m.cell(5, 7, 0).velocity();
        assertEquals(Vector.ZERO, initialVelocity);
        Vector initialVelocityNorthBorder = m.cell(5, 9, 0).velocity();
        checkEquals(Vector.create(1.0, 0.0, 0.0), initialVelocityNorthBorder, VELOCITY_PRECISION);
        final Mesh mesh = m.stepMultiple(1, 20);
        Cell cell = mesh.cell(5, 7, 0);
        Vector v = cell.velocity();
        log.info("vector={}", v);
        assertTrue(v.north() != 0);
        assertNotEquals(initialPressure, cell.pressure());
    }

    private Mesh checkNoChange(int eastIndex, int northIndex, int upIndex) {
        final Solver solver = new Solver();
        final Mesh mesh = createMesh();
        final Cell cell = mesh.cell(eastIndex, northIndex, upIndex);
        final double pressure = cell.pressure();
        assertNotNull(cell);
        final VelocityPressure result = solver.step(cell, 1);
        checkEquals(Vector.ZERO, result.getVelocity(), VELOCITY_PRECISION);
        assertEquals(pressure, result.getPressure(), PRESSURE_PRECISION);
        return mesh;
    }

    private Mesh checkNoChangeAfterStep(int eastIndex, int northIndex, int upIndex) {
        final Solver solver = new Solver();
        final Mesh mesh = createMesh().step(1);
        final Cell cell = mesh.cell(eastIndex, northIndex, upIndex);
        final double pressure = cell.pressure();
        assertNotNull(cell);
        final VelocityPressure result = solver.step(cell, 1);
        checkEquals(Vector.ZERO, result.getVelocity(), VELOCITY_PRECISION);
        assertEquals(pressure, result.getPressure(), PRESSURE_PRECISION);
        return mesh;
    }

    private void checkNoChange2D(int eastIndex, int northIndex, int upIndex) {
        final Solver solver = new Solver();
        final Mesh mesh = createMesh2D();
        final Cell cell = mesh.cell(eastIndex, northIndex, upIndex);
        final double pressure = cell.pressure();
        assertNotNull(cell);
        final VelocityPressure result = solver.step(cell, 1);
        checkEquals(Vector.ZERO, result.getVelocity(), VELOCITY_PRECISION);
        assertEquals(pressure, result.getPressure(), PRESSURE_PRECISION);
    }

    private static void checkEquals(Vector a, Vector b, double precision) {
        assertEquals(a.east(), b.east(), precision);
    }

    private static Cell createCell() {

        final Cell north = builder().position(0, 1, -1).build();
        final Cell south = builder().position(0, -1, -1).build();
        final Cell east = builder().position(-1, 0, -1).build();
        final Cell west = builder().position(1, 0, -1).build();
        final Cell up = builder().position(0, 0, 0).pressure(pressureAtDepth(0)).build();
        final Cell down = builder().position(0, 0, -2).pressure(pressureAtDepth(2)).build();
        final Cell centre = builder().position(0, 0, -1).north(north).south(south).east(east)
                .west(west).up(up).down(down).build();
        return centre;

    }

    private static Builder builder() {
        return CellImpl.builder() //
                .pressure(pressureAtDepth(1)) //
                .density(Util.SEAWATER_MEAN_DENSITY_KG_PER_M3) //
                .viscosity(30) //
                .type(CellType.FLUID) //
                .velocity(0, 0, 0);
    }

    
}
