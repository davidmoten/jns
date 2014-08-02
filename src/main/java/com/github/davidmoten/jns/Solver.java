package com.github.davidmoten.jns;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class Solver {

    private final static Vector GRAVITY = new VectorImpl(0, 0, -9.8);

    private final static Set<Direction> DIRECTIONS = Collections
            .unmodifiableSet(new HashSet<Direction>(Arrays.asList(Direction.EAST, Direction.NORTH,
                    Direction.UP)));

    public Cell step(Cell cell, double timeStepSeconds) {
        Vector v1 = getVelocityAfterTime(cell, timeStepSeconds);
        Function<Double, Double> f = getPressureCorrectionFunction(cell, v1, timeStepSeconds);
        double newPressure = solveForPressure(cell, f);
        return cell.modifyPressure(newPressure).modifyVelocity(v1);
    }

    private double solveForPressure(Cell cell, Function<Double, Double> pressureCorrectionFunction) {
        // TODO what values for h,precision?
        double h = 1;
        double precision = 0.000001;
        int maxIterations = 15;
        Optional<Double> p = NewtonsMethod.solve(pressureCorrectionFunction, cell.pressure(), h,
                precision, maxIterations);
        double newPressure;
        if (p.isPresent())
            if (p.get() < 0)
                newPressure = cell.pressure();
            else
                newPressure = p.get();
        else
            newPressure = cell.pressure();
        return newPressure;
    }

    private Vector getVelocityAfterTime(Cell cell, double timeStepSeconds) {
        return cell.velocity().add(dvdt(cell).times(timeStepSeconds));
    }

    private Vector dvdt(Cell cell) {
        Vector velocityLaplacian = getVelocityLaplacian(cell);
        Vector pressureGradient = getPressureGradient(cell);
        Matrix velocityJacobian = getVelocityJacobian(cell);
        Vector divergenceOfStress = velocityLaplacian.times(cell.viscosity()).minus(
                pressureGradient);
        Vector result = divergenceOfStress.divideBy(cell.density()).add(GRAVITY)
                .minus(velocityJacobian.times(cell.velocity()));
        return result;
    }

    private Vector getVelocityLaplacian(Cell cell) {
        return Vectors.create(getVelocityLaplacian(cell, Direction.EAST),
                getVelocityLaplacian(cell, Direction.NORTH),
                getVelocityLaplacian(cell, Direction.UP));
    }

    private double getVelocityLaplacian(Cell cell, Direction direction) {
        return getVelocityGradient2nd(cell, direction).sum();
    }

    private Vector getVelocityGradient2nd(Cell cell, Direction direction) {
        final Function<Direction, Function<Cell, Double>> velocity = d -> {
            return c -> c.velocity().value(d);
        };
        final Function<Direction, Double> gradient =
        // gradient in given direction
        d -> getGradient(cell, direction, velocity.apply(d), DerivativeType.SECOND, Optional.empty());

        return Vectors.create(gradient);
    }

    private Vector getPressureGradient(Cell cell) {
        // TODO Auto-generated method stub
        return null;
    }

    private Matrix getVelocityJacobian(Cell cell) {
        // TODO Auto-generated method stub
        return null;
    }

    private Function<Double, Double> getPressureCorrectionFunction(Cell cell, Vector newVelocity,
            double timeStepSeconds) {
        return pressure -> getPressureCorrection(cell, cell.modifyPressure(pressure));
    }

    private double getPressureCorrection(Cell cell, Cell override) {
        double pressureLaplacian = getPressureLaplacian(cell, override);
        return pressureLaplacian
                + DIRECTIONS
                        .stream()
                        // for each direction
                        .map(d -> override.velocity().value(d)
                                * getGradient(cell, d, gradientDot(d), DerivativeType.FIRST,
                                        Optional.of(override)))
                        // sum
                        .mapToDouble(x -> x).sum();
    }

    private double getGradient(Cell cell, Direction d, Function<Cell, Double> gradientDot,
            DerivativeType derivativeType, Optional<Cell> override) {
        // TODO Auto-generated method stub
        return 0;
    }

    private Function<Cell, Double> gradientDot(Direction d) {
        // TODO Auto-generated method stub
        return null;
    }

    private double getPressureLaplacian(Cell cell, Cell override) {
        // TODO Auto-generated method stub
        return 0;
    }
}
