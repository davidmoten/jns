package com.github.davidmoten.jns;

public class Vector {

	private final double east;
	private final double north;
	private final double up;

	Vector(double east, double north, double up) {
		this.east = east;
		this.north = north;
		this.up = up;
	}

	public double value(Direction direction) {
		if (direction.equals(Direction.EAST))
			return east;
		else if (direction.equals(Direction.NORTH))
			return north;
		else if (direction.equals(Direction.UP))
			return up;
		else
			throw new RuntimeException("direction " + direction
					+ " not expected");
	}

	public Vector add(Vector v) {
		return Vectors.create(east + v.east, north + v.north, up + v.up);
	}

	public Vector minus(Vector v) {
		return Vectors.create(east - v.east, north - v.north, up - v.up);
	}

	public Vector times(double value) {
		return Vectors.create(east * value, north * value, up * value);
	}

	public Vector divideBy(double value) {
		return Vectors.create(east / value, north / value, up / value);
	}

	public double sum() {
		return east + north + up;
	}

	public double dotProduct(Vector v) {
		return east * v.east + north * v.north + up * v.up;
	}
}
