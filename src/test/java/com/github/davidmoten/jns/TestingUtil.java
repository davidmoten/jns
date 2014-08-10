package com.github.davidmoten.jns;


class TestingUtil {

    static Mesh createMesh() {
        return Mesh.builder().cellSize(1).creator(new CellCreator(10, 10, 10)).build();
    }

    static Mesh createMesh2D() {
        return Mesh.builder().cellSize(1).creator(new CellCreator(10, 10, 1)).build();
    }

    static Mesh createMeshForWhirlpool2D() {
        return Util.createMeshForWhirlpool2D();
    }
}
