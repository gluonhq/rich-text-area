package com.gluonhq.emoji.impl.skin;

import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;

import java.util.Optional;

public class Traversal {

    static Optional<Node> findNext(Node node) {
        if (node == null || node.getParent() == null) return Optional.ofNullable(node);
        final ObservableList<Node> children = node.getParent().getChildrenUnmodifiable();
        final int index = children.indexOf(node);
        if (index < children.size() - 1) {
            return Optional.of(children.get(index + 1));
        } else {
            return Optional.empty();
        }
    }

    static Optional<Node> findPrevious(Node node) {
        if (node == null || node.getParent() == null) return Optional.ofNullable(node);
        final ObservableList<Node> children = node.getParent().getChildrenUnmodifiable();
        final int index = children.indexOf(node);
        if (index > 0) {
            return Optional.of(children.get(index - 1));
        } else {
            return Optional.empty();
        }
    }

    static Optional<Node> findTop(Node node, double gap, double topPadding) {
        if (node == null || node.getParent() == null) return Optional.ofNullable(node);
        final Bounds childBounds = node.getBoundsInParent();
        if (childBounds.getMinY() - topPadding <= topPadding) {
            return Optional.empty();
        } else {
            Bounds expectedBounds = new BoundingBox(childBounds.getMinX(),
                    childBounds.getMinY() - childBounds.getHeight() - gap,
                    childBounds.getWidth(),
                    childBounds.getHeight());
            return getTargetNode(node, expectedBounds);
        }
    }


    static Optional<Node> findBottom(Node node, double gap) {
        if (node == null || node.getParent() == null) return Optional.ofNullable(node);
        final Bounds parentBounds = node.getParent().getBoundsInLocal();
        final Bounds childBounds = node.getBoundsInParent();
        if (parentBounds.getHeight() > childBounds.getMaxY() + childBounds.getHeight()) {
            Bounds expectedBounds = new BoundingBox(childBounds.getMinX(),
                    childBounds.getMaxY() + gap,
                    childBounds.getWidth(),
                    childBounds.getHeight());
            
            return getTargetNode(node, expectedBounds);
        }
        return Optional.empty();
    }

    /**
     * Finds the sibling whose maximum area lies in the expectedBounds region 
     * @param node The nodes whose siblings are to the tested
     * @param expectedBounds The bounds against which the sibling nodes are to be tested.
     * @return Sibling with maximum percentage of area lying in the expected bounds
     */
    private static Optional<Node> getTargetNode(Node node, Bounds expectedBounds) {
        return node.getParent().getChildrenUnmodifiable().stream()
                .filter(child -> !child.equals(node))
                .filter(child -> child.getBoundsInParent().intersects(expectedBounds))
                .max((o1, o2) -> {
                    final Bounds bound1 = o1.getBoundsInParent();
                    final Bounds bound2 = o2.getBoundsInParent();
                    double per1 = getPercentageIntersect(expectedBounds, bound1);
                    double per2 = getPercentageIntersect(expectedBounds, bound2);
                    return Double.compare(per1, per2);
                });
    }

    private static double getPercentageIntersect(Bounds source, Bounds destination) {
        double minX, maxX;
        if (source.getMinX() > destination.getMinX()) {
            minX = source.getMinX();
        } else {
            minX = destination.getMinX();
        }
        if (source.getMaxX() > destination.getMaxX()) {
            maxX = destination.getMaxX();
        } else {
            maxX = source.getMaxX();
        }
        final double width = maxX - minX;
        return width / source.getWidth();
    }
}
