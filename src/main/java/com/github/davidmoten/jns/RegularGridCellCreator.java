package com.github.davidmoten.jns;

import java.util.function.Function;

public class RegularGridCellCreator implements Function<Indices, CellData> {

	private final int eastSize;
	private final int northSize;
	private final int upSize;
	private final double density;
	private final double viscosity;

	public RegularGridCellCreator(int eastSize, int northSize, int upSize,
			double density, double viscosity) {
		this.eastSize = eastSize;
		this.northSize = northSize;
		this.upSize = upSize;
		this.density = density;
		this.viscosity = viscosity;
	}

	public RegularGridCellCreator(int eastSize, int northSize, int upSize) {
		this(eastSize, northSize, upSize, Util.SEAWATER_MEAN_DENSITY_KG_PER_M3,
				Util.SEAWATER_MEAN_VISCOSITY);
	}

	@Override
	public CellData apply(Indices i) {
		return new CellData() {

			@Override
			public CellType type() {
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

			@Override
			public Vector position() {
				// directly translate index to metres for east and north
				// use 0 at surface for up
				return Vector.create(i.east(), i.north(), upSize - i.up() - 1);
			}

			@Override
			public double pressure() {
				// in pressure equilibrium
				double depth = upSize - i.up() - 1;
				return Util.pressureAtDepth(depth);
			}

			@Override
			public Vector velocity() {
				// still water
				return Vector.ZERO;
			}

			@Override
			public double density() {
				return density;
			}

			@Override
			public double viscosity() {
				return viscosity;
			}
		};
	}

}
