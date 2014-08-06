package com.github.davidmoten.jns;

public class Matrix {

	private final Vector row1;
	private final Vector row2;
	private final Vector row3;

	public Matrix(Vector row1, Vector row2, Vector row3) {
		this.row1 = row1;
		this.row2 = row2;
		this.row3 = row3;
	}

	public Vector times(Vector v) {
		return Vector.create(row1.dotProduct(v), row2.dotProduct(v),
				row3.dotProduct(v));
	}

}
