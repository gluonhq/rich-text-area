package com.gluonhq.chat.impl.chatlistview;

import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.skin.VirtualFlow;

public class ChatVirtualFlow<T> extends VirtualFlow<ListCell<T>> {

    protected ScrollBar getVerticalBar() {
        return getVbar();
    }

    @Override
    protected void rebuildCells() {
        super.rebuildCells();
    }

    @Override
    protected void recreateCells() {
        super.recreateCells();
    }

    @Override
    public ListCell<T> getLastVisibleCell() {
        return super.getLastVisibleCell();
    }

    @Override
    protected ListCell<T> getFirstVisibleCellWithinViewport() {
        return super.getFirstVisibleCellWithinViewport();
    }

    @Override
    public void resizeRelocate(double x, double y, double w, double h) {
        double offsetY = 0;
        if (stackFromBottom) {
            double ph = computePrefHeight(getWidth());
            double parentH = getParent().getBoundsInParent().getHeight();
            offsetY = Math.max(0, parentH - ph);
        }
        super.resizeRelocate(x, y + offsetY, w, h - offsetY);
    }

    public double getCellPosition(ListCell<T> cell) {
        if (cell == null) return 0;

        return isVertical() ?
                cell.getLayoutY()
                : cell.getLayoutX();
    }

    private boolean stackFromBottom;
    void setStackFromBottom(boolean value) {
        stackFromBottom = value;
    }
    boolean getStackFromBottom() {
        return stackFromBottom;
    }
}
