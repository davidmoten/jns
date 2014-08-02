package com.github.davidmoten.jns;

import java.util.Optional;
import java.util.function.Function;

public class Solver {

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

    private Function<Double, Double> getPressureCorrectionFunction(Cell cell, Vector newVelocity,
            double timeStepSeconds) {
        return pressure -> getPressureCorrection(cell, cell.modifyPressure(pressure));
    }

    private double getPressureCorrection(Cell cell, Cell override) {
        // Vector pressureLaplacian = getPressureLaplacian(position,
        // overrideValue)
        // return pressureLaplacian +
        // directions.map(d => overrideValue.velocity.get(d) *
        // getGradient(position, d,
        // gradientDot(d),
        // FirstDerivative, Some(overrideValue))).sum
        //
        return 0;
    }

    private Vector getVelocityAfterTime(Cell cell, double timeStepSeconds) {
        // TODO Auto-generated method stub
        return null;
    }
}
