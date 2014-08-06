package com.github.davidmoten.jns;

import static com.github.davidmoten.jns.CellType.FLUID;
import static com.github.davidmoten.jns.CellType.OBSTACLE;
import static com.github.davidmoten.jns.CellType.UNKNOWN;

import java.util.Optional;
import java.util.function.Function;

public class Solver {

	private final static Vector GRAVITY = Vector.create(0, 0, -9.8);

	public Cell step(Cell cell, double timeStepSeconds) {
		final Vector v = getVelocityAfterTime(cell, timeStepSeconds);
		final Function<Double, Double> f = getContinuityFunction(cell, v,
				timeStepSeconds);
		final double newPressure = solveForPressure(cell, f);
		return cell.modifyPressure(newPressure).modifyVelocity(v);
	}

	private double solveForPressure(Cell cell,
			Function<Double, Double> continuityFunction) {
		// TODO what values for h,precision?
		final double h = 1;
		final double precision = 0.000001;
		final int maxIterations = 15;
		final Optional<Double> p = NewtonsMethod.solve(continuityFunction,
				cell.pressure(), h, precision, maxIterations);
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

	private Vector getVelocityAfterTime(Cell cell, double timeSeconds) {
		return cell.velocity().add(dvdt(cell).times(timeSeconds));
	}

	private Vector dvdt(Cell cell) {
		final Vector velocityLaplacian = getVelocityLaplacian(cell);
		final Vector pressureGradient = getPressureGradient(cell);
		final Matrix velocityJacobian = getVelocityJacobian(cell);
		final Vector divergenceOfStress = velocityLaplacian
				.times(cell.viscosity()).minus(pressureGradient).add(GRAVITY);
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
		d -> getGradient(cell, direction, velocity.apply(d),
				DerivativeType.SECOND, Optional.empty());

		return Vector.create(gradient);
	}

	private Vector getPressureGradient(Cell cell) {
		return Vector.create(d -> getPressureGradient(cell, d));
	}

	private double getPressureGradient(Cell cell, Direction direction) {
		return getGradient(cell, direction, c -> c.pressure(),
				DerivativeType.FIRST, Optional.empty());
	}

	private Matrix getVelocityJacobian(Cell cell) {
		return Matrixes.create(d -> getVelocityGradient(cell, d));
	}

	private Vector getVelocityGradient(Cell cell, Direction direction) {
		final Function<Direction, Double> gradient = d -> getGradient(cell,
				direction, c -> c.velocity().value(d), DerivativeType.FIRST,
				Optional.empty());
		return Vector.create(gradient);
	}

	private Function<Double, Double> getContinuityFunction(Cell cell,
			Vector newVelocity, double timeStepSeconds) {
		return pressure -> getContinuityFunction(cell,
				cell.modifyVelocity(newVelocity).modifyPressure(pressure));
	}

	private double getContinuityFunction(Cell cell, Cell override) {
		final double pressureLaplacian = getPressureLaplacian(cell, override);
		final Function<Direction, Double> f = d -> override.velocity().value(d)
				* getGradient(cell, d, gradientDot(d), DerivativeType.FIRST,
						Optional.of(override));
		return pressureLaplacian + Vector.create(f).sum();
	}

	private Function<Cell, Double> gradientDot(Direction d) {
		return cell -> getVelocityGradient(cell, d).dotProduct(cell.velocity());
	}

	private double getPressureLaplacian(Cell cell, Cell override) {
		return getPressureGradient2nd(cell, override).sum();
	}

	private Vector getPressureGradient2nd(Cell cell, Cell override) {
		final Function<Direction, Double> f = d -> getGradient(cell, d,
				c -> c.pressure(), DerivativeType.SECOND, Optional.of(override));
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
			DerivativeType derivativeType,
			// override cell values if present
			Optional<Cell> override) {
		final Cell c = override.orElse(cell);
		return getGradient(f, c.neighbour(d, -1), c, c.neighbour(d, 1), d,
				derivativeType);
	}

	private static <T> T unexpected(String msg) {
		throw new RuntimeException(msg);
	}

	private static <T> T unexpected() {
		return unexpected("unexpected");
	}

	private double getGradient(Function<Cell, Double> f, Cell c1, Cell c2,
			Cell c3, Direction d, DerivativeType derivativeType) {
		if (c2.type() == CellType.OBSTACLE) {
			return unexpected("why ask for gradient at obstacle?");
		} else {
			final CellTriplet t = transform(c1, c2, c3);
			if (is(FLUID, FLUID, FLUID, t)) {
				return getGradientFromFluid(f, t.c1(), t.c2(), t.c3(), d,
						derivativeType);
			} else if (is(FLUID, FLUID, UNKNOWN, t))
				return getGradientFromFluid(f, t.c1(), t.c2(), d,
						derivativeType);
			else
				return unexpected();
		}
	}

	private double getGradientFromFluid(Function<Cell, Double> f, Cell c1,
			Cell c2, Direction d, DerivativeType derivativeType) {
		if (derivativeType == DerivativeType.FIRST) {
			return (f.apply(c2) - f.apply(c1))
					/ (c2.position().value(d) - c1.position().value(d));
		} else if (derivativeType == DerivativeType.SECOND)
			return 0;
		else
			return unexpected();
	}

	private double getGradientFromFluid(Function<Cell, Double> f, Cell c1,
			Cell c2, Cell c3, Direction d, DerivativeType derivativeType) {
		if (derivativeType == DerivativeType.FIRST) {
			return (f.apply(c3) - f.apply(c1))
					/ (c3.position().value(d) - c1.position().value(d));
		} else if (derivativeType == DerivativeType.SECOND)
			return (f.apply(c3) + f.apply(c1) - 2 * f.apply(c2))
					/ sqr(c3.position().value(d) - c1.position().value(d));
		else
			return unexpected();
	}

	private static double sqr(double d) {
		return d * d;
	}

	private static boolean is(CellType ct1, CellType ct2, CellType ct3,
			CellTriplet t) {

		return t.c1().type() == ct1 && t.c2().type() == ct2
				&& t.c3().type() == ct3;
	}

	private static CellTriplet transform(CellTriplet t) {
		if (is(FLUID, FLUID, FLUID, t))
			return t;
		else if (is(FLUID, FLUID, UNKNOWN, t))
			return t;
		else if (t.c2().type() == OBSTACLE)
			return t;
		else if (is(UNKNOWN, FLUID, FLUID, t))
			return CellTriplet.create(t.c2(), t.c3(), t.c1());
		else if (t.c2().type() == FLUID && t.c3().type() == OBSTACLE)
			return transform(t.c1(), t.c2(), obstacleToValue(t.c3(), t.c2()));
		else if (t.c2().type() == FLUID && t.c1().type() == OBSTACLE)
			return transform(obstacleToValue(t.c1(), t.c2()), t.c2(), t.c3());
		else
			return unexpected("not handled " + t);

	}

	private static Cell obstacleToValue(Cell obstacle, Cell point) {
		return point.modifyVelocity(Vector.ZERO).modifyPosition(
				obstacle.position());
	}

	private static CellTriplet transform(Cell c1, Cell c2, Cell c3) {
		return transform(CellTriplet.create(c1, c2, c3));
	}
}
