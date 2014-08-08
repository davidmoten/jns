package com.github.davidmoten.jns;

class TestingUtil {

	static RegularGrid createGrid() {
		return RegularGrid.builder().cellSize(1).density(1025).viscosity(30)
				.creator(new RegularGridCreator(10, 10, 10)).build();
	}

	static RegularGrid createGrid2D() {
		return RegularGrid.builder().cellSize(1).density(1025).viscosity(30)
				.creator(new RegularGridCreator(10, 10, 1)).build();
	}
}
