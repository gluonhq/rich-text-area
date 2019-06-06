package com.gluonhq.chat.chatlistview;

import java.util.Collection;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import com.gluonhq.chat.impl.chatlistview.ChatListViewSkin;
import com.gluonhq.chat.impl.chatlistview.util.Properties;

/**
 * ChatListView is a ListView control that display contents stacked vertically.
 * The contents can be displayed on top or from the bottom up.
 * @param <T> 
 */
public class ChatListView<T> extends ListView<T> {
        
    private static final String DEFAULT_STYLE_CLASS = "chat-list-view";
    private String stylesheets;
    
    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a default ChatListView which will display contents stacked vertically.
     * The contents can be displayed on top or from the bottom up.
     * 
     * <p>As no {@link ObservableList} is provided in this constructor, an empty
     * ObservableList is created.
     */
    public ChatListView() {
        this(FXCollections.<T>observableArrayList());
    }
    
    /**
     * Creates a default ChatListView which will stack the contents retrieved from the
     * provided {@link ObservableList} vertically.
     * The contents can be displayed on top or from the bottom up.
     * 
     * <p>Attempts to add a listener to the {@link ObservableList}, such that all
     * subsequent changes inside the list will be shown to the user.
     * 
     * @param items the list of items
     */
    public ChatListView(ObservableList<T> items) {
        super(items);
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);
    }
    
    /**************************************************************************
     *
     * Properties
     *
     **************************************************************************/

    // --- stackFromBottom
    /**
     * This property indicates if the content is shown at the top, or
     * from the bottom up (default).
     * 
     * @return the stackFromBottom property for this ChatListView
     */
    private final BooleanProperty stackFromBottom = new SimpleBooleanProperty(this, "stackFromBottom", true) {
        @Override protected void invalidated() { refresh(); }
    };
    public final void setStackFromBottom(boolean value) { stackFromBottom.set(value); }
    public final boolean isStackFromBottom() { return stackFromBottom.get(); }
    public final BooleanProperty stackFromBottomProperty() { return stackFromBottom; }

    /**
     * Allows calling a {@link Service} when the scroll value is lower than a threshold 
     * (stacked from bottom) or greater than a threashold (stacked from top). 
     * 
     * The user is responsible for providing a proper implementation of the service
     * such as it retrievies a batch of items, typically from a backend connection,
     * in a background thread.
     * 
     * When the service succeedes, the resulting batch of items will be added to the
     * beginning of the list of items.
     */
    private final ObjectProperty<Service<Collection<T>>> onDataRequest = new SimpleObjectProperty<>(this, "onDataRequest") {
        @Override
        protected void invalidated() {
            Service<Collection<T>> service = get();
            if (service != null) {
                service.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> {
                    getProperties().put(Properties.DATA_INSERTED, Boolean.TRUE);
                    getItems().addAll(0, service.getValue());
                    
                });
            }
        }
    };
    public final ObjectProperty<Service<Collection<T>>> onDataRequestProperty() { return onDataRequest; }
    public final void setOnDataRequest(Service<Collection<T>> eventHandler) { onDataRequest.set(eventHandler); }
    public final Service<Collection<T>> getOnDataRequest() { return onDataRequest.get(); }

    /**
     * Keeps the number of unread messages, if any. 
     */
    private final IntegerProperty unreadMessages = new SimpleIntegerProperty(this, "unreadMessages", -1);
    public final IntegerProperty unreadMessagesProperty() { return unreadMessages; }
    public final void setUnreadMessages(int value) { unreadMessages.set(value); }
    public final int getUnreadMessages() { return unreadMessages.get(); }

    /**
     * Keeps the first index of unread messages, if any. 
     */
    private final IntegerProperty unreadIndex = new SimpleIntegerProperty(this, "unreadIndex", -1);
    public final IntegerProperty unreadIndexProperty() { return unreadIndex; }
    public final void setUnreadIndex(int value) { unreadIndex.set(value); }
    public final int getUnreadIndex() { return unreadIndex.get(); }

    /** {@inheritDoc} */
    @Override protected Skin<?> createDefaultSkin() {
        return new ChatListViewSkin<>(ChatListView.this);
    }

    @Override
    public String getUserAgentStylesheet() {
        if (stylesheets == null) {
            stylesheets = ChatListView.class.getResource("chatlistview.css").toExternalForm();
        }
        return stylesheets;
    }
    
}
