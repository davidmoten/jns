package com.github.davidmoten.jns;

public interface Cell extends CellData {

    Cell neighbour(Direction direction, int count);

}
