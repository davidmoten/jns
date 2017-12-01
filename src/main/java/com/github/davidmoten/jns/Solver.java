package com.github.davidmoten.jns;

import static com.github.davidmoten.jns.CellType.FLUID;
import static com.github.davidmoten.jns.CellType.OBSTACLE;
import static com.github.davidmoten.jns.CellType.UNKNOWN;
import static com.github.davidmoten.jns.NewtonsMethod.solve;
import static com.github.davidmoten.jns.Util.pressureGradientDueToGravity;
import static com.github.davidmoten.jns.Util.unexpected;
import static com.github.davidmoten.jns.Util.validate;

import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Navier-Stokes equation solver for incompressible fluid.
 */
public class Solver {

    private static Logger log = LoggerFactory.getLogger(Solver.class);

    public VelocityPressure step(Cell cell, double timeStepSeconds) {
        if (cell.isBoundary())
            return new VelocityPressure(cell.velocity(), cell.pressure());
        log.debug("step {}", str(cell));
        // explicit time advance scheme as per Ferziger and Peric 7.3.2
        final Vector v = getVelocityAfterTime(cell, timeStepSeconds);
        log.debug("velocity={}", v);
        final Function<Double, Double> f = getContinuityFunction(cell, v, timeStepSeconds);
        final double p = solveForPressure(cell, f);
        return new VelocityPressure(v, p);
    }

    private double solveForPressure(Cell cell, Function<Double, Double> continuityFunction) {
        // 10 Pa is probably reasonable given that pressures are normally
        // >100000Pa.
        final double delta = 100;// Pa
        // TODO what value for precision?
        final double precision = 10;
        final int maxIterations = 15;
        final Optional<Double> p = solve(continuityFunction, cell.pressure(), delta, precision,
                maxIterations)
                        // don't accept negative values
                        .filter(d -> d >= 0);
        if (!p.isPresent())
            unexpected("could not find pressure at " + str(cell));
        return p.orElse(cell.pressure());
    }

    // Visible for testing
    Vector getVelocityAfterTime(Cell cell, double timeSeconds) {
        return cell.velocity().add(dvdt(cell).times(timeSeconds));
    }

    private Vector dvdt(Cell cell) {
        log.debug("dvdt at {}", str(cell));
        final Vector velocityLaplacian = getVelocityLaplacian(cell);
        final Vector pressureGradient = getPressureGradient(cell);
        final Matrix velocityJacobian = getVelocityJacobian(cell);
        final Vector divergenceOfStress = velocityLaplacian.times(cell.viscosity())
                .minus(pressureGradient).add(pressureGradientDueToGravity(cell));
        final Vector result = divergenceOfStress.divideBy(cell.density())
                .minus(velocityJacobian.times(cell.velocity()));
        return result;
    }

    private Vector getVelocityLaplacian(Cell cell) {
        return Vector.create(getVelocityLaplacian(cell, Direction.EAST),
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
                d -> getGradient(cell, direction, velocity.apply(d), DerivativeType.SECOND);

        return Vector.create(gradient);
    }

    private Vector getPressureGradient(Cell cell) {
        return Vector.create(d -> getPressureGradient(cell, d));
    }

    // Visible for testing
    double getPressureGradient(Cell cell, Direction direction) {
        return getGradient(cell, direction, c -> c.pressure(), DerivativeType.FIRST);
    }

    private Matrix getVelocityJacobian(Cell cell) {
        return Matrixes.create(d -> getVelocityGradient(cell, d));
    }

    private Function<Double, Double> getContinuityFunction(Cell cell, Vector newVelocity,
            double timeStepSeconds) {
        return pressure -> getContinuityFunction(
                Util.override(cell, cell.type(), newVelocity, pressure));
    }

    private double getContinuityFunction(Cell cell) {
        final double pressureLaplacian = getPressureLaplacian(cell);
        final Function<Direction, Double> f = d -> cell.velocity().value(d)
                * getGradient(cell, d, gradientDot(d), DerivativeType.FIRST);
        return pressureLaplacian + Vector.create(f).sum();
    }

    private double getContinuityFunction2(Cell cell) {
        final double pressureLaplacian = getPressureLaplacian(cell);
        log.info("pressureLaplacian={}", pressureLaplacian);
        final Function<Direction, Double> f = d -> getGradient(cell, d, gradientDot(d),
                DerivativeType.FIRST);
        return pressureLaplacian + Vector.create(f).sum();
    }

    private Function<Cell, Double> gradientDot(Direction d) {
        return cell -> getVelocityGradient(cell, d).dotProduct(cell.velocity());
    }

    private Vector getVelocityGradient(Cell cell, Direction direction) {
        final Function<Direction, Double> gradientFn = d -> getGradient(cell, direction,
                c -> c.velocity().value(d), DerivativeType.FIRST);
        return Vector.create(gradientFn);
    }

    private double getPressureLaplacian(Cell cell) {
        return getPressureGradient2nd(cell).sum();
    }

    private Vector getPressureGradient2nd(Cell cell) {
        final Function<Direction, Double> f = d -> getGradient(cell, d, c -> c.pressure(),
                DerivativeType.SECOND);
        return Vector.create(f);
    }

    private double getGradient(
            // cell
            Cell cell,
            // direction
            Direction d,
            // function
            Function<Cell, Double> f,
            // first or second derivative
            DerivativeType derivativeType) {
        return getGradient(f, cell.neighbour(d, -1), cell, cell.neighbour(d, 1), d, derivativeType);
    }

    private double getGradient(Function<Cell, Double> f, Cell c1, Cell c2, Cell c3, Direction d,
            DerivativeType derivativeType) {
        if (c2.type() == CellType.OBSTACLE) {
            return unexpected("why ask for gradient at obstacle?");
        } else {
            try {
                final CellTriplet t = transform(c1, c2, c3);
                if (is(FLUID, FLUID, FLUID, t)) {
                    return getGradientFromFluid(f, t.c1(), t.c2(), t.c3(), d, derivativeType);
                } else if (is(FLUID, FLUID, UNKNOWN, t))
                    return getGradientFromFluid(f, t.c1(), t.c2(), d, derivativeType);
                else
                    return unexpected();
            } catch (final RuntimeException e) {
                log.error("{}:{},{},{}", d, c1.position(), c2.position(), c3.position());
                throw e;
            }
        }
    }

    private static CellTriplet transform(Cell c1, Cell c2, Cell c3) {
        return transform(CellTriplet.create(c1, c2, c3));
    }

    // visible for testing
    static CellTriplet transform(CellTriplet t) {
        if (is(FLUID, FLUID, FLUID, t))
            return t;
        else if (is(FLUID, FLUID, UNKNOWN, t))
            return t;
        else if (is(ANY, OBSTACLE, ANY, t))
            return t;
        else if (is(UNKNOWN, FLUID, FLUID, t))
            return CellTriplet.create(t.c2(), t.c3(), t.c1());
        else if (is(ANY, FLUID, OBSTACLE, t))
            return transform(t.c1(), t.c2(), obstacleToValue(t.c3(), t.c2()));
        else if (is(OBSTACLE, FLUID, ANY, t))
            return transform(obstacleToValue(t.c1(), t.c2()), t.c2(), t.c3());
        else
            return unexpected("not handled " + str(t.c1()) + "," + str(t.c2()) + "." + str(t.c3()));
    }

    private static String str(Cell c) {
        final StringBuilder s = new StringBuilder();
        s.append("Cell[");
        s.append(c.type());
        s.append(",");
        s.append(c.position());
        s.append("]");
        return s.toString();
    }

    private double getGradientFromFluid(Function<Cell, Double> f, Cell c1, Cell c2, Direction d,
            DerivativeType derivativeType) {
        if (derivativeType == DerivativeType.FIRST) {
            return firstDerivative(f, c1, c2, d);
        } else if (derivativeType == DerivativeType.SECOND)
            // only have two points so must assume 2nd derivative is zero
            return 0;
        else
            return unexpected();
    }

    private static double getGradientFromFluid(Function<Cell, Double> f, Cell c1, Cell c2, Cell c3,
            Direction d, DerivativeType derivativeType) {
        if (derivativeType == DerivativeType.FIRST) {
            return firstDerivativeSecondOrder(f, c1, c2, c3, d);
        } else if (derivativeType == DerivativeType.SECOND)
            return secondDerivative(f, c1, c2, c3, d);
        else
            return unexpected();
    }

//    private static double firstDerivativeFirstOrder(Function<Cell, Double> f, Cell c1, Cell c2,
//            Cell c3, Direction d) {
//        return validate(
//                (f.apply(c3) - f.apply(c1)) / (c3.position().value(d) - c1.position().value(d)));
//    }

    private static double firstDerivativeSecondOrder(Function<Cell, Double> f, Cell c1, Cell c2,
            Cell c3, Direction d) {
        double a = c1.position().value(d);
        double b = c2.position().value(d);
        double c = c3.position().value(d);
        double h1 = b - a;
        double h2 = c - b;
        double sqrH1 = h1 * h1;
        double sqrH2 = h2 * h2;
        double fa = f.apply(c1);
        double fb = f.apply(c2);
        double fc = f.apply(c3);
        double result = ((sqrH2 - sqrH1) * fb + sqrH1 * fc - sqrH2 * fa)
                / (sqrH1 * h2 + h1 * sqrH2);
        return validate(result);
    }

    private static double firstDerivative(Function<Cell, Double> f, Cell c1, Cell c3, Direction d) {
        return validate(
                (f.apply(c3) - f.apply(c1)) / (c3.position().value(d) - c1.position().value(d)));
    }

    private static double secondDerivative(Function<Cell, Double> f, Cell c1, Cell c2, Cell c3,
            Direction d) {
        return validate((f.apply(c3) + f.apply(c1) - 2 * f.apply(c2))
                / sqr(c3.position().value(d) - c1.position().value(d)));
    }

    private static double sqr(double d) {
        return d * d;
    }

    private static CellType ANY = null;

    private static boolean is(CellType ct1, CellType ct2, CellType ct3, CellTriplet t) {

        return (t.c1().type() == ct1 || ct1 == ANY) && (t.c2().type() == ct2 || ct2 == ANY)
                && (t.c3().type() == ct3 || ct3 == ANY);
    }

    /**
     * Returns a {@link Cell} representation of an obstacle that exists with respect
     * to the the cell <code>wrt</code>.
     *
     * @param obstacle
     * @param wrt
     * @return
     */
    // @VisibleForTesting
    static Cell obstacleToValue(Cell obstacle, Cell wrt) {
        final double p = getEquilibriumPressureRelativeTo(obstacle, wrt);
        return Util.override(obstacle, CellType.FLUID, Vector.ZERO, p);
    }

    private static Cell unknownToValue(Cell unknown, Cell wrt) {
        final double p = getEquilibriumPressureRelativeTo(unknown, wrt);
        return Util.override(unknown, CellType.FLUID, wrt.velocity(), p);
    }

    private static double getEquilibriumPressureRelativeTo(Cell obstacle, Cell wrt) {
        final double p = wrt.pressure() + obstacle.position().minus(wrt.position())
                .dotProduct(Util.pressureGradientDueToGravity(wrt));
        return p;
    }
}
