package com.github.davidmoten.jns;

/**
 * A cell in a possible irregular mesh with the rule that every cell has a
 * single neighbour to north, south, west, east, up, down for the purposes of
 * differential calculations.
 */
public class MeshCell implements Cell {

	private final CellType type;
	private final Vector position;
	private final Mesh mesh;
	private final int indexEast;
	private final int indexNorth;
	private final int indexUp;
	private final Vector velocity;
	private final double pressure;
	private final double density;
	private final double viscosity;

	MeshCell(Mesh mesh, int indexEast, int indexNorth, int indexUp,
			CellData cellData) {
		this.mesh = mesh;
		this.indexEast = indexEast;
		this.indexNorth = indexNorth;
		this.indexUp = indexUp;
		this.type = cellData.type();
		this.pressure = cellData.pressure();
		this.position = cellData.position();
		this.velocity = cellData.velocity();
		this.density = cellData.density();
		this.viscosity = cellData.viscosity();
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
	public double density() {
		return density;
	}

	@Override
	public double viscosity() {
		return viscosity;
	}

	@Override
	public Cell neighbour(Direction direction, int count) {
		if (direction == Direction.EAST)
			return mesh.cell(indexEast + count, indexNorth, indexUp);
		else if (direction == Direction.NORTH)
			return mesh.cell(indexEast, indexNorth + count, indexUp);
		else if (direction == Direction.UP)
			return mesh.cell(indexEast, indexNorth, indexUp + count);
		else
			return Util.unexpected();
	}

	@Override
	public Cell modifyPressure(double pressure) {
		return new CellDelegator(this) {
			@Override
			public double pressure() {
				return pressure;
			}
		};
	}

	@Override
	public Cell modifyVelocity(Vector velocity) {
		return new CellDelegator(this) {
			@Override
			public Vector velocity() {
				return velocity;
			}
		};
	}

	@Override
	public Cell modifyPosition(final Vector position) {
		return new CellDelegator(this) {
			@Override
			public Vector position() {
				return position;
			}
		};

	}
}
