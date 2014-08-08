package com.github.davidmoten.jns;

public class Indices {

	private final int east;
	private final int north;
	private final int up;

	public Indices(int east, int north, int up) {
		super();
		this.east = east;
		this.north = north;
		this.up = up;
	}

	public int east() {
		return east;
	}

	public int north() {
		return north;
	}

	public int up() {
		return up;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + east;
		result = prime * result + north;
		result = prime * result + up;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Indices other = (Indices) obj;
		if (east != other.east)
			return false;
		if (north != other.north)
			return false;
		if (up != other.up)
			return false;
		return true;
	}

}
