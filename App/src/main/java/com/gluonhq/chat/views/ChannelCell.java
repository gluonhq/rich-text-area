package com.gluonhq.chat.views;

import com.gluonhq.charm.glisten.control.CharmListCell;
import com.gluonhq.charm.glisten.control.ListTile;
import com.gluonhq.chat.model.Channel;

public class ChannelCell extends CharmListCell<Channel> {

    private final ListTile tile;

    public ChannelCell() {
        tile = new ListTile();
        setGraphic(tile);
        setText(null);
        getStyleClass().add("focus-cell");
        getStyleClass().add("channel-cell");
    }

    @Override
    public void updateItem(Channel channel, boolean empty) {
        super.updateItem(channel, empty);
        if ((channel!= null) && (channel.isUnread())){
            getStyleClass().add("focus-cell");
        } else {
            getStyleClass().remove("focus-cell");
        }
        /* TODO: Add User Image
        final Avatar avatar = new Avatar;
        avatar.setMouseTransparent(true);
        tile.setPrimaryGraphic(avatar);
        */

        tile.setTextLine(0, channel.displayName());

        tile.setOnMouseReleased(event -> {
            AppViewManager.CHAT_VIEW.getPresenter().ifPresent(presenter -> ((ChatPresenter) presenter).updateMessages(channel));
            // TODO: We want a better way to switch views if the screen size is <= 600
            if (tile.getScene().getWidth() <= 600) {
                AppViewManager.PORTRAIT_VIEW.switchView().ifPresent(p -> ((PortraitPresenter) p).loadChat());
            }
        });

    }
}
