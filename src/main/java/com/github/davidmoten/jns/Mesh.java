package com.github.davidmoten.jns;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mesh {

    private static Logger log = LoggerFactory.getLogger(Mesh.class);

    private final double cellSizeEast;
    private final double cellSizeNorth;
    private final double cellSizeUp;

    private final ConcurrentHashMap<Indices, Cell> cells = new ConcurrentHashMap<>();
    private final Function<Indices, CellData> creator;

    private Mesh(Function<Indices, CellData> creator, double cellSizeEast, double cellSizeNorth,
            double cellSizeUp) {
        this.creator = creator;
        this.cellSizeEast = cellSizeEast;
        this.cellSizeNorth = cellSizeNorth;
        this.cellSizeUp = cellSizeUp;
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

    public Cell cell(Indices ind) {
        return cell(ind.east(), ind.north(), ind.up());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Function<Indices, CellData> creator;
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

        public Mesh build() {
            return new Mesh(creator, cellSizeEast, cellSizeNorth, cellSizeUp);
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

    public Mesh stepMultiple(double timeStepSeconds, long numberOfSteps) {
        Mesh m = this;
        for (int i = 0; i < numberOfSteps; i++) {
            log.info("step " + i);
            m = m.step(timeStepSeconds);
        }
        return m;
    }

    public Mesh step(double timeStepSeconds) {
        final Mesh m = this;
        return new Mesh(i -> new CellData() {
            final Solver solver = new Solver();
            final AtomicReference<VelocityPressure> vp = new AtomicReference<VelocityPressure>();

            @Override
            public CellType type() {
                return m.cell(i).type();
            }

            @Override
            public Vector position() {
                return m.cell(i).position();
            }

            @Override
            public double pressure() {
                return velocityPressure().getPressure();
            }

            @Override
            public Vector velocity() {
                return velocityPressure().getVelocity();
            }

            @Override
            public double density() {
                return m.cell(i).density();
            }

            @Override
            public double viscosity() {
                return m.cell(i).viscosity();
            }

            private VelocityPressure velocityPressure() {
                if (vp.get() == null) {
                    synchronized (vp) {
                        if (vp.get() == null)
                            vp.set(solver.step(m.cell(i), timeStepSeconds));
                    }
                }
                return vp.get();
            }

            @Override
            public boolean isBoundary() {
                return m.cell(i).isBoundary();
            }

        }, cellSizeEast, cellSizeNorth, cellSizeUp);
    }
}
