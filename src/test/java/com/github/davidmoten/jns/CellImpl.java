package com.github.davidmoten.jns;

public class CellImpl implements Cell {

	private final CellType type;
	private final Vector position;
	private final double pressure;
	private final Vector velocity;
	private final double temperature;
	private final double density;
	private final double viscosity;
	private Cell west;
	private Cell east;
	private Cell north;
	private Cell south;
	private Cell up;
	private Cell down;

	private CellImpl(CellType type, Vector position, double pressure,
			Vector velocity, double temperature, double density,
			double viscosity, Cell west, Cell east, Cell south, Cell north,
			Cell down, Cell up) {
		this.type = type;
		this.position = position;
		this.pressure = pressure;
		this.velocity = velocity;
		this.temperature = temperature;
		this.density = density;
		this.viscosity = viscosity;
		this.west = west;
		this.east = east;
		this.north = north;
		this.south = south;
		this.up = up;
		this.down = down;
	}

	@Override
	public Cell neighbour(Direction d, int count) {
		if (d == Direction.EAST && count == -1)
			return west;
		else if (d == Direction.EAST && count == 1)
			return east;
		else if (d == Direction.NORTH && count == -1)
			return south;
		else if (d == Direction.NORTH && count == 1)
			return north;
		else if (d == Direction.UP && count == -1)
			return down;
		else if (d == Direction.UP && count == 1)
			return up;
		else
			return Util.unexpected("neighbour not supported: " + d + count);
	}

	public void setCell(Direction d, int count, Cell cell) {
		if (d == Direction.EAST && count == -1)
			west = cell;
		else if (d == Direction.EAST && count == 1)
			east = cell;
		else if (d == Direction.NORTH && count == -1)
			south = cell;
		else if (d == Direction.NORTH && count == 1)
			north = cell;
		else if (d == Direction.UP && count == -1)
			down = cell;
		else if (d == Direction.UP && count == 1)
			up = cell;
		else
			Util.unexpected("neighbour not supported: " + d + count);
	}

	@Override
	public Cell modifyPressure(double pressure) {
		return new CellImpl(type, position, pressure, velocity, temperature,
				density, viscosity, west, east, south, north, down, up);
	}

	@Override
	public Cell modifyVelocity(Vector velocity) {
		return new CellImpl(type, position, pressure, velocity, temperature,
				density, viscosity, west, east, south, north, down, up);
	}

	@Override
	public Cell modifyPosition(Vector position) {
		return new CellImpl(type, position, pressure, velocity, temperature,
				density, viscosity, west, east, south, north, down, up);
	}

	@Override
	public CellType type() {
		return type;
	}

	@Override
	public Vector position() {
		return position;
	}

	@Override
	public double pressure() {
		return pressure;
	}

	@Override
	public Vector velocity() {
		return velocity;
	}

	@Override
	public double temperature() {
		return temperature;
	}

	@Override
	public double density() {
		return density;
	}

	@Override
	public double viscosity() {
		return viscosity;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private CellType type;
		private Vector position;
		private double pressure;
		private Vector velocity;
		private double temperature;
		private double density;
		private double viscosity;
		private Cell east;
		private Cell west;
		private Cell south;
		private Cell north;
		private Cell down;
		private Cell up;

		private Builder() {
		}

		public Builder type(CellType type) {
			this.type = type;
			return this;
		}

		public Builder position(Vector position) {
			this.position = position;
			return this;
		}

		public Builder pressure(double pressure) {
			this.pressure = pressure;
			return this;
		}

		public Builder velocity(Vector velocity) {
			this.velocity = velocity;
			return this;
		}

		public Builder temperature(double temperature) {
			this.temperature = temperature;
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

		public Builder west(Cell west) {
			this.west = west;
			return this;
		}

		public Builder east(Cell east) {
			this.east = east;
			return this;
		}

		public Builder north(Cell north) {
			this.north = north;
			return this;
		}

		public Builder south(Cell south) {
			this.south = south;
			return this;
		}

		public Builder up(Cell up) {
			this.up = up;
			return this;
		}

		public Builder down(Cell down) {
			this.down = down;
			return this;
		}

		public CellImpl build() {
			return new CellImpl(type, position, pressure, velocity,
					temperature, density, viscosity, west, east, south, north,
					down, up);
		}

	}
}
