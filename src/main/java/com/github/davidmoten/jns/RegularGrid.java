package com.github.davidmoten.jns;

import java.util.concurrent.atomic.AtomicReference;

public class RegularGrid {

	private final Vector[][][] velocity;
	private final double[][][] pressure;
	private final AtomicReference<Cell>[][][] cells;
	private final double density = 1025;
	private final double viscosity = 30;
	private final double temperature = 293; // kelvin
	private final int cellsEast;
	private final int cellsNorth;
	private final int cellsUp;
	private final double cellSizeEast;
	private final double cellSizeNorth;
	private final double cellSizeUp;

	public RegularGrid(int cellsEast, int cellsNorth, int cellsUp,
			double cellSizeEast, double cellSizeNorth, double cellSizeUp) {
		this.cellsEast = cellsEast;
		this.cellsNorth = cellsNorth;
		this.cellsUp = cellsUp;
		this.cellSizeEast = cellSizeEast;
		this.cellSizeNorth = cellSizeNorth;
		this.cellSizeUp = cellSizeUp;
		velocity = new Vector[cellsEast][cellsNorth][cellsUp];
		pressure = new double[cellsEast][cellsNorth][cellsUp];
		cells = new AtomicReference[cellsEast][cellsNorth][cellsUp];
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

	public double temperature() {
		return temperature;
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

		}

	}
}
