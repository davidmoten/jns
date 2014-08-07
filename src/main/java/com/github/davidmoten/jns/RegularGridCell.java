package com.github.davidmoten.jns;

/**
 * A box of regularly spaced points with OBSTACLE at the floor, AIR above and
 * UNKNOWN on the sides.
 *
 */
public class RegularGridCell implements Cell {

	private CellType type;
	private final Vector position;
	private final RegularGrid box;
	private final int indexEast;
	private final int indexNorth;
	private final int indexUp;

	RegularGridCell(RegularGrid box, int indexEast, int indexNorth, int indexUp) {
		this.box = box;
		this.indexEast = indexEast;
		this.indexNorth = indexNorth;
		this.indexUp = indexUp;
		if (indexUp < 0)
			type = CellType.OBSTACLE;
		else if (indexUp > box.maxIndexUp())
			type = CellType.AIR;
		else if (indexEast < 0 || indexEast > box.maxIndexEast())
			type = CellType.UNKNOWN;
		else if (indexNorth < 0 || indexNorth > box.maxIndexNorth())
			type = CellType.UNKNOWN;
		else
			type = CellType.FLUID;
		position = Vector.create(indexEast * box.cellSizeEast(), indexNorth
				* box.cellSizeNorth(), indexUp * box.cellSizeUp());
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
		return box.pressure(indexEast, indexNorth, indexUp);
	}

	@Override
	public Vector velocity() {
		return box.velocity(indexEast, indexNorth, indexUp);
	}

	@Override
	public double temperature() {
		return box.temperature();
	}

	@Override
	public double density() {
		return box.density();
	}

	@Override
	public double viscosity() {
		return box.viscosity();
	}

	@Override
	public Cell neighbour(Direction direction, int count) {
		if (direction == Direction.EAST)
			return box.cell(indexEast + count, indexNorth, indexUp);
		else if (direction == Direction.NORTH)
			return box.cell(indexEast, indexNorth + count, indexUp);
		else if (direction == Direction.UP)
			return box.cell(indexEast, indexNorth, indexUp + count);
		else
			return Util.unexpected();
	}

	@Override
	public Cell modifyPressure(double pressure) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cell modifyVelocity(Vector velocity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cell modifyPosition(Vector position) {
		// TODO Auto-generated method stub
		return null;
	}
}
