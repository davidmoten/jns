package com.github.davidmoten.jns;

public interface Cell extends CellData {

    Cell neighbour(Direction direction, int count);

    default Cell north() {
        return neighbour(Direction.NORTH, 1);
    }

    default Cell south() {
        return neighbour(Direction.NORTH, -1);
    }

    default Cell east() {
        return neighbour(Direction.EAST, 1);
    }

    default Cell west() {
        return neighbour(Direction.EAST, -1);
    }

    default Cell up() {
        return neighbour(Direction.UP, 1);
    }

    default Cell down() {
        return neighbour(Direction.UP, -1);
    }

}
