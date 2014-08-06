package com.github.davidmoten.jns;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.davidmoten.jns.CellImpl.Builder;

public class SolverTest {

    private static Cell createCell() {

        final Cell north = builder().position(0, 1, -1).build();
        final Cell south = builder().position(0, -1, -1).build();
        final Cell east = builder().position(-1, 0, -1).build();
        final Cell west = builder().position(1, 0, -1).build();
        final Cell up = builder().position(0, 0, 0).pressure(101325).build();
        final Cell down = builder().position(0, 0, -2).pressure(121429).build();
        final Cell centre = builder().position(0, 0, -1).north(north).south(south).east(east)
                .west(west).up(up).down(down).build();
        return centre;

    }

    private static Builder builder() {
        return CellImpl.builder().pressure(111377).density(1025).temperature(293).viscosity(30)
                .type(CellType.FLUID).velocity(0, 0, 0);
    }

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
        assertEquals(Vector.ZERO, result);
    }

}
