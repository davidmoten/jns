package com.github.davidmoten.jns;

class TestingUtil {

	static Mesh createGrid() {
		return Mesh.builder().cellSize(1).density(1025).viscosity(30)
				.creator(new RegularGridCellCreator(10, 10, 10)).build();
	}

	static Mesh createGrid2D() {
		return Mesh.builder().cellSize(1).density(1025).viscosity(30)
				.creator(new RegularGridCellCreator(10, 10, 1)).build();
	}
}
