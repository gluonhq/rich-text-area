package com.gluonhq.chat.impl.chatlistview;

import java.util.Collection;

import com.gluonhq.chat.chatlistview.ChatListView;
import com.gluonhq.chat.impl.chatlistview.util.Properties;
import javafx.animation.PauseTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.concurrent.Service;
import javafx.scene.control.ListCell;
import javafx.util.Duration;

public class ChatListViewSkin<T> extends ListViewSkin<T> {
        
    private static final double THRESHOLD = 0.2;
    
    private final ChatListView<T> control;
    private final VirtualFlow<ListCell<T>> chatVirtualFlow;
    
    private boolean lockedByBatchOperation = false;
    private boolean lockedByTrailMessage = false;

    public ChatListViewSkin(final ChatListView<T> control) {
        super(control);
        this.control = control;
        
        chatVirtualFlow = getVirtualFlow();

        chatVirtualFlow.getVbar().valueProperty().addListener((obs, ov, nv) -> {
            if (lockedByBatchOperation || lockedByTrailMessage) {
                return;
            }

            if (checkThreshold(ov.doubleValue(), nv.doubleValue())) {
                final Service<Collection<T>> service = control.getOnDataRequest();
                if (service != null) {
                    // lock
                    lockedByBatchOperation = true;
                    service.restart();
                    return;
                }
            }
            
            ListCell<T> lastCell = chatVirtualFlow.getLastVisibleCell();
            if (control.getUnreadIndex() > -1 && lastCell != null && control.getUnreadIndex() < lastCell.getIndex()) {
                // update index
                control.setUnreadIndex(lastCell.getIndex());
                final int remainder = control.getItems().size() - control.getUnreadIndex();
                control.setUnreadMessages(remainder);
            }
            
            if (nv.doubleValue() == 1.0) {
                control.setUnreadIndex(-1);
                control.setUnreadMessages(-1);
            }
        });
        
        control.stackFromBottomProperty().addListener((obs, ov, nv) -> {
            chatVirtualFlow.setStackFromBottom(nv);
        });
        chatVirtualFlow.setStackFromBottom(control.isStackFromBottom());
        chatVirtualFlow.getVbar().visibleProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                if (chatVirtualFlow.getVbar().isVisible()) {
                    chatVirtualFlow.getVbar().setValue(control.isStackFromBottom() ? 1 : 0);
                    chatVirtualFlow.getVbar().visibleProperty().removeListener(this);
                }
            }
        });
    }

    @Override
    protected void updateItemCount() {
        super.updateItemCount(); 
        if (control != null && ! lockedByBatchOperation && lockedByTrailMessage) {
            checkItemNotVisible();
            lockedByTrailMessage = false;
        }
    }   
    
    @Override
    protected void processInputData(int index, int size) {
        super.processInputData(index, size);

        // Check for possible batch of items added at the beginning of the list.
        lockedByTrailMessage = index > 0;

        if (lockedByTrailMessage || Boolean.FALSE.equals((Boolean) control.getProperties().getOrDefault(Properties.DATA_INSERTED, false))) {
            return;
        }
        control.getProperties().put(Properties.DATA_INSERTED, false);
                
        ListCell<T> firstCell = chatVirtualFlow.getFirstVisibleCellWithinViewPort();
        if (firstCell != null) {
            double offsetY = chatVirtualFlow.getCellPosition(firstCell);

            // Scroll to previous firstCell at its current index position
            lockScroll(size + firstCell.getIndex(), offsetY);

            // Verify and unlock
            PauseTransition pause = new PauseTransition(Duration.millis(50));
            pause.setOnFinished(f -> {
                if (control.getUnreadIndex() != -1) {
                    // update first unread index
                    control.setUnreadIndex(control.getUnreadIndex() + size);
                }

                // release lock
                lockedByBatchOperation = false;
            });
            pause.play();
        }
    }
    
    /**
     * When data is inserted at the beginning of the list, the viewport doesn't
     * change its position, only reflects a new scrollbar position.
     * 
     * @param index
     * @param delta 
     */
    private void lockScroll(int index, double delta) {
        // We try to maintain the list at the exact same position 
        // as it was before, while updating the scroll bar properly.
        // Force relayout first
        markItemCountDirty();
        control.requestLayout();
        
        updateItemCount();
        
        chatVirtualFlow.scrollToTop(index);
        chatVirtualFlow.scrollPixels(-delta);
        chatVirtualFlow.requestLayout();
            
        // New layout pass
        markItemCountDirty();
        control.requestLayout();
    }
    
    private boolean checkThreshold(double oldValue, double newValue) {
        
        if ((control.isStackFromBottom() && newValue > oldValue) || (! control.isStackFromBottom() && newValue < oldValue)) {
            return false;
        }
        
        final double range = chatVirtualFlow.getVbar().getMax() - chatVirtualFlow.getVbar().getMin();
        final double limit = chatVirtualFlow.getVbar().getMin() + THRESHOLD * range;
        return control.isStackFromBottom() ? newValue <= limit : newValue >= limit;
    }
    
    private void checkItemNotVisible() {
        if (control.getItems().isEmpty() || chatVirtualFlow.getLastVisibleCell() == null) {
            return;
        }
        
        int lastVisible = chatVirtualFlow.getLastVisibleCell().getIndex();
        int newIndex = control.getItems().size() - 1;
        
        if (newIndex > lastVisible) {
            if (control.getUnreadIndex() == -1) {
                // update first unread index
                control.setUnreadIndex(newIndex);
            }

            if (control.getUnreadIndex() > -1) {
                int remainder = control.getItems().size() - control.getUnreadIndex();
                // update number unread messages
                control.setUnreadMessages(remainder);
            }
        }
    }
}
