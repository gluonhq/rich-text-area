package com.gluonhq.richtextarea;

import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;

import java.util.Objects;

class BackgroundColorPath extends Path {

    public BackgroundColorPath(PathElement[] elements) {
        super(elements);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BackgroundColorPath that = (BackgroundColorPath) o;
        return Objects.equals(getLayoutBounds(), that.getLayoutBounds()) && Objects.equals(getFill(), that.getFill());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLayoutBounds(), getFill());
    }
}
