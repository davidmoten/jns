package com.github.davidmoten.jns;

import java.util.function.Function;

class TestingUtil {

    static Mesh createMesh() {
        return Mesh.builder().cellSize(1).creator(new CellCreator(10, 10, 10)).build();
    }

    static Mesh createGrid2D() {
        return Mesh.builder().cellSize(1).creator(new CellCreator(10, 10, 1)).build();
    }

    static Mesh createMeshForWhirlpool2D() {
        final int cellsUp = 1;
        final int cellsEast = 10;
        final int cellsNorth = 10;
        final Function<Indices, CellType> typeFunction = i -> {
            // Floored bottom, obstacle sides, open north side
            if (i.up() < 0)
                return CellType.OBSTACLE;
            else if (i.up() > cellsUp - 1)
                return CellType.UNKNOWN;// air
            else if (i.east() < 0 || i.east() > cellsEast - 1)
                if (i.north() == cellsNorth - 1)
                    return CellType.UNKNOWN;
                else
                    return CellType.OBSTACLE;
            else if (i.north() < 0)
                return CellType.OBSTACLE;
            else if (i.north() > cellsNorth - 1)
                return CellType.UNKNOWN;
            else
                // inside the box so is fluid
                return CellType.FLUID;
        };
        final Function<Indices, Vector> velocityFunction = i -> {
            if (i.north() == cellsNorth - 1)
                return Vector.create(1, 0, 0);
            else
                return Vector.ZERO;
        };
        return Mesh
                .builder()
                .cellSize(1)
                .creator(
                        CellCreator.builder().cellsEast(cellsEast).cellsNorth(cellsNorth)
                                .cellsUp(cellsUp).typeFunction(typeFunction)
                                .velocityFunction(velocityFunction).build()).build();
    }
}
