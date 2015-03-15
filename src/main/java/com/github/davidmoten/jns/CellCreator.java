package com.github.davidmoten.jns;

import java.util.Optional;
import java.util.function.Function;

public class CellCreator implements Function<Indices, CellData> {

	private final double density;
	private final double viscosity;
	private final Function<Indices, Vector> positionFunction;
	private final Function<Indices, Vector> velocityFunction;
	private final Function<Indices, CellType> typeFunction;
	private final Function<Indices, Double> pressureFunction;
	private final Function<Indices, Boolean> isBoundaryFunction;
	private final Function<Indices, Vector> positionFunctionDefault;
	private final Function<Indices, CellType> defaultTypeFunction;
	private final Function<Indices, Double> pressureFunctionDefault;

	public CellCreator(int eastSize, int northSize, int upSize, double density,
			double viscosity,
			Optional<Function<Indices, Vector>> positionFunction,
			Optional<Function<Indices, Vector>> velocityFunction,
			Optional<Function<Indices, CellType>> typeFunction,
			Optional<Function<Indices, Double>> pressureFunction,
			Optional<Function<Indices, Boolean>> isBoundaryFunction) {
		this.density = density;
		this.viscosity = viscosity;
		this.positionFunctionDefault = i -> {
			return Vector.create(i.east(), i.north(), i.up() - upSize + 1);
		};
		this.positionFunction = positionFunction
				.orElse(positionFunctionDefault);
		this.velocityFunction = velocityFunction
				.orElse(velocityFunctionDefault);
		this.pressureFunctionDefault = i -> {
			final double depth = upSize - i.up() - 1;
			return Util.pressureAtDepth(depth);
		};
		this.pressureFunction = pressureFunction
				.orElse(pressureFunctionDefault);
		this.isBoundaryFunction = isBoundaryFunction
				.orElse(isBoundaryFunctionDefault);
		this.defaultTypeFunction = i -> {
			// Floored bottom, unknown other boundaries
			if (i.up() < 0)
				return CellType.OBSTACLE;
			else if (i.up() > upSize - 1)
				return CellType.UNKNOWN;// air
			else if (i.east() < 0 || i.east() > eastSize - 1)
				return CellType.UNKNOWN;
			else if (i.north() < 0 || i.north() > northSize - 1)
				return CellType.UNKNOWN;
			else
				// inside the box so is fluid
				return CellType.FLUID;
		};
		this.typeFunction = typeFunction.orElse(defaultTypeFunction);
	}

	public CellCreator(int eastSize, int northSize, int upSize) {
		this(eastSize, northSize, upSize, Util.SEAWATER_MEAN_DENSITY_KG_PER_M3,
				Util.SEAWATER_MEAN_VISCOSITY, Optional.empty(), Optional
						.empty(), Optional.empty(), Optional.empty(), Optional
						.empty());
	}

	private final Function<Indices, Boolean> isBoundaryFunctionDefault = i -> false;

	private static final Function<Indices, Vector> velocityFunctionDefault = i -> Vector.ZERO;

	@Override
	public CellData apply(Indices i) {
		return new CellData() {

			@Override
			public CellType type() {
				return typeFunction.apply(i);
			};

			@Override
			public Vector position() {
				return positionFunction.apply(i);
			}

			@Override
			public double pressure() {
				return pressureFunction.apply(i);
			}

			@Override
			public Vector velocity() {
				return velocityFunction.apply(i);
			}

			@Override
			public double density() {
				return density;
			}

			@Override
			public double viscosity() {
				return viscosity;
			}

			@Override
			public boolean isBoundary() {
				return isBoundaryFunction.apply(i);
			}
		};
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private int eastSize = 0;
		private int northSize = 0;
		private int upSize = 0;
		private double density = Util.SEAWATER_MEAN_DENSITY_KG_PER_M3;
		private double viscosity = Util.SEAWATER_MEAN_VISCOSITY;
		private Optional<Function<Indices, Vector>> positionFunction = Optional
				.empty();
		private Optional<Function<Indices, Vector>> velocityFunction = Optional
				.empty();
		private Optional<Function<Indices, CellType>> typeFunction = Optional
				.empty();
		private Optional<Function<Indices, Double>> pressureFunction = Optional
				.empty();
		private Optional<Function<Indices, Boolean>> isBoundaryFunction = Optional
				.empty();

		private Builder() {
		}

		public Builder cellsEast(int eastSize) {
			this.eastSize = eastSize;
			return this;
		}

		public Builder cellsNorth(int northSize) {
			this.northSize = northSize;
			return this;
		}

		public Builder cellsUp(int upSize) {
			this.upSize = upSize;
			return this;
		}

		public Builder density(double density) {
			this.density = density;
			return this;
		}

		public Builder viscosity(double viscosity) {
			this.viscosity = viscosity;
			return this;
		}

		public Builder positionFunction(
				Function<Indices, Vector> positionFunction) {
			this.positionFunction = Optional.of(positionFunction);
			return this;
		}

		public Builder velocityFunction(
				Function<Indices, Vector> velocityFunction) {
			this.velocityFunction = Optional.of(velocityFunction);
			return this;
		}

		public Builder typeFunction(Function<Indices, CellType> typeFunction) {
			this.typeFunction = Optional.of(typeFunction);
			return this;
		}

		public Builder pressureFunction(
				Function<Indices, Double> pressureFunction) {
			this.pressureFunction = Optional.of(pressureFunction);
			return this;
		}

		public Builder isBoundaryFunction(
				Function<Indices, Boolean> isBoundaryFunction) {
			this.isBoundaryFunction = Optional.of(isBoundaryFunction);
			return this;
		}

		public CellCreator build() {
			return new CellCreator(eastSize, northSize, upSize, density,
					viscosity, positionFunction, velocityFunction,
					typeFunction, pressureFunction, isBoundaryFunction);
		}
	}

}
