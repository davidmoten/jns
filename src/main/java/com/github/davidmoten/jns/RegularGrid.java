package com.github.davidmoten.jns;

import java.util.concurrent.atomic.AtomicReference;

public class RegularGrid {

	private final Vector[][][] velocity;
	private final double[][][] pressure;
	private final AtomicReference<Cell>[][][] cells;

	private final int cellsEast;
	private final int cellsNorth;
	private final int cellsUp;
	private final double cellSizeEast;
	private final double cellSizeNorth;
	private final double cellSizeUp;
	private final double density;// 1025;
	private final double viscosity = 30;

	// private ConcurrentHashMap<Indices, Cell> cells;

	private RegularGrid(int cellsEast, int cellsNorth, int cellsUp,
			double cellSizeEast, double cellSizeNorth, double cellSizeUp,
			double density, double viscosity) {
		this.cellsEast = cellsEast;
		this.cellsNorth = cellsNorth;
		this.cellsUp = cellsUp;
		this.cellSizeEast = cellSizeEast;
		this.cellSizeNorth = cellSizeNorth;
		this.cellSizeUp = cellSizeUp;
		this.density = density;
		velocity = new Vector[cellsEast][cellsNorth][cellsUp];
		pressure = new double[cellsEast][cellsNorth][cellsUp];
		cells = new AtomicReference[cellsEast][cellsNorth][cellsUp];
		// initialize the grid in equilibrium
		for (int i = 0; i < cells.length; i++)
			for (int j = 0; j < cells[i].length; j++)
				for (int k = 0; k < cells[i][j].length; k++) {
					cells[i][j][k] = new AtomicReference<Cell>();
					velocity[i][j][k] = Vector.ZERO;
					pressure[i][j][k] = Util.pressureAtDepth(cellSizeUp
							* (cellsUp - k - 1));
				}

	}

	public int maxIndexEast() {
		return cellsEast - 1;
	}

	public int maxIndexNorth() {
		return cellsNorth - 1;
	}

	public int maxIndexUp() {
		return cellsUp - 1;
	}

	public Vector velocity(int indexEast, int indexNorth, int indexUp) {
		return velocity[indexEast][indexNorth][indexUp];
	}

	public double pressure(int indexEast, int indexNorth, int indexUp) {
		return pressure[indexEast][indexNorth][indexUp];
	}

	public double density() {
		return density;
	}

	public double viscosity() {
		return viscosity;
	}

	public double cellSizeEast() {
		return cellSizeEast;
	}

	public double cellSizeNorth() {
		return cellSizeNorth;
	}

	public double cellSizeUp() {
		return cellSizeUp;
	}

	public Cell cell(int indexEast, int indexNorth, int indexUp) {
		if (indexEast >= 0 && indexEast < cellsEast && indexNorth >= 0
				&& indexNorth < cellsNorth && indexUp >= 0 && indexUp < cellsUp) {
			final AtomicReference<Cell> ref = cells[indexEast][indexNorth][indexUp];
			if (ref.get() == null) {
				ref.compareAndSet(null, new RegularGridCell(this, indexEast,
						indexNorth, indexUp));
			}
			return ref.get();
		} else
			// TODO return edges and beyond from cache as well?
			return new RegularGridCell(this, indexEast, indexNorth, indexUp);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private int cellsEast;
		private int cellsNorth;
		private int cellsUp;
		private double cellSizeEast;
		private double cellSizeNorth;
		private double cellSizeUp;
		private double density = 1025;
		private double viscosity = 30;

		private Builder() {
		}

		public Builder cellsEast(int cellsEast) {
			this.cellsEast = cellsEast;
			return this;
		}

		public Builder cellsNorth(int cellsNorth) {
			this.cellsNorth = cellsNorth;
			return this;
		}

		public Builder cellsUp(int cellsUp) {
			this.cellsUp = cellsUp;
			return this;
		}

		public Builder cellSize(double cellSize) {
			this.cellSizeEast = cellSize;
			this.cellSizeNorth = cellSize;
			this.cellSizeUp = cellSize;
			return this;
		}

		public Builder cellSizeEast(double cellSizeEast) {
			this.cellSizeEast = cellSizeEast;
			return this;
		}

		public Builder cellSizeNorth(double cellSizeNorth) {
			this.cellSizeNorth = cellSizeNorth;
			return this;
		}

		public Builder cellSizeUp(double cellSizeUp) {
			this.cellSizeUp = cellSizeUp;
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

		public RegularGrid build() {
			return new RegularGrid(cellsEast, cellsNorth, cellsUp,
					cellSizeEast, cellSizeNorth, cellSizeUp, density, viscosity);
		}
	}
}
