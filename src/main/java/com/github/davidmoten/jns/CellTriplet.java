package com.github.davidmoten.jns;

public class CellTriplet {

	private final Cell c1;
	private final Cell c2;
	private final Cell c3;

	CellTriplet(Cell c1, Cell c2, Cell c3) {
		this.c1 = c1;
		this.c2 = c2;
		this.c3 = c3;
	}

	public Cell c1() {
		return c1;
	}

	public Cell c2() {
		return c2;
	}

	public Cell c3() {
		return c3;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CellTriplet [c1=");
		builder.append(c1);
		builder.append(", c2=");
		builder.append(c2);
		builder.append(", c3=");
		builder.append(c3);
		builder.append("]");
		return builder.toString();
	}

	public static CellTriplet create(Cell c1, Cell c2, Cell c3) {
		return new CellTriplet(c1, c2, c3);
	}

}