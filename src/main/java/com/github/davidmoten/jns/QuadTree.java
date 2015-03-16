package com.github.davidmoten.jns;

import java.util.EnumMap;
import java.util.Optional;

public final class QuadTree<T> {

    private final Optional<T> value;
    private final Optional<EnumMap<Position, QuadTree<T>>> children;

    public static enum Position {
        NW, NE, SW, SE
    }

    private QuadTree(Optional<T> value, Optional<EnumMap<Position, QuadTree<T>>> children) {
        this.value = value;
        this.children = children;
    }

    public QuadTree(T value) {
        this(Optional.of(value), Optional.empty());
    }

    public QuadTree(EnumMap<Position, QuadTree<T>> children) {
        this(Optional.empty(), Optional.of(children));
    }

    public boolean hasChildren() {
        return children.isPresent();
    }

    public QuadTree<T> child(Position position) {
        return children.get().get(position);
    }

    public T value() {
        return value.get();
    }

}
