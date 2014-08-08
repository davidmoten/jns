package com.github.davidmoten.jns;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Mesh {

    private final double cellSizeEast;
    private final double cellSizeNorth;
    private final double cellSizeUp;
    private final double density;// 1025;
    private final double viscosity;// 30

    private final ConcurrentHashMap<Indices, Cell> cells = new ConcurrentHashMap<>();
    private final Function<Indices, CellData> creator;

    private Mesh(Function<Indices, CellData> creator, double cellSizeEast, double cellSizeNorth,
            double cellSizeUp, double density, double viscosity) {
        this.creator = creator;
        this.cellSizeEast = cellSizeEast;
        this.cellSizeNorth = cellSizeNorth;
        this.cellSizeUp = cellSizeUp;
        this.density = density;
        this.viscosity = viscosity;
    }

    public double density() {
        return density;
    }

    public double viscosity() {
        return viscosity;
    }

    public Collection<Cell> cells() {
        return cells.values();
    }

    public Cell cell(int indexEast, int indexNorth, int indexUp) {
        final Indices indices = new Indices(indexEast, indexNorth, indexUp);
        if (cells.get(indices) == null) {
            final CellData cellData = creator.apply(indices);
            cells.putIfAbsent(indices, new MeshCell(this, indexEast, indexNorth, indexUp, cellData));
        }
        return cells.get(indices);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Function<Indices, CellData> creator;
        private double density = 1025;
        private double viscosity = 30;
        private double cellSizeEast;
        private double cellSizeNorth;
        private double cellSizeUp;

        private Builder() {
        }

        public Builder creator(Function<Indices, CellData> creator) {
            this.creator = creator;
            return this;
        }

        public Builder cellSize(double cellSize) {
            this.cellSizeEast = cellSize;
            this.cellSizeNorth = cellSize;
            this.cellSizeUp = cellSize;
            return this;
        }

        public Builder cellSizeEast(double cellSizeEast) {
            this.cellSizeEast = cellSizeEast;
            return this;
        }

        public Builder cellSizeNorth(double cellSizeNorth) {
            this.cellSizeNorth = cellSizeNorth;
            return this;
        }

        public Builder cellSizeUp(double cellSizeUp) {
            this.cellSizeUp = cellSizeUp;
            return this;
        }

        public Builder density(double density) {
            this.density = density;
            return this;
        }

        public Builder viscosity(double viscosity) {
            this.viscosity = viscosity;
            return this;
        }

        public Mesh build() {
            return new Mesh(creator, cellSizeEast, cellSizeNorth, cellSizeUp, density, viscosity);
        }
    }

    public double cellSizeEast() {
        return cellSizeEast;
    }

    public double cellSizeNorth() {
        return cellSizeNorth;
    }

    public double cellSizeUp() {
        return cellSizeUp;
    }

}
