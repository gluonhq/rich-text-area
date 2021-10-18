package com.gluonhq.chat.views;

import com.gluonhq.charm.glisten.control.Avatar;
import com.gluonhq.charm.glisten.control.CharmListCell;
import com.gluonhq.charm.glisten.control.ListTile;
import com.gluonhq.chat.model.Channel;
import com.gluonhq.chat.model.User;
import com.gluonhq.chat.service.Service;
import com.gluonhq.chat.util.ImageCache;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;

public class ChannelCell extends CharmListCell<Channel> {

    private static final PseudoClass PSEUDO_CLASS_UNREAD = PseudoClass.getPseudoClass("unread");

    private final ListTile tile;
    private Channel channel;

    public ChannelCell() {
        tile = new ListTile();
        setGraphic(tile);
        setText(null);
        getStyleClass().add("channel-cell");
        tile.setOnMouseReleased(event -> {
            if (channel != null) {
                channel.setUnread(false);
                AppViewManager.CHAT_VIEW.getPresenter()
                        .ifPresent(presenter -> ((ChatPresenter) presenter).updateMessages(channel));
                // TODO: We want a better way to switch views if the screen size is <= 600
                if (tile.getScene().getWidth() <= 600) {
                    AppViewManager.PORTRAIT_VIEW.switchView().ifPresent(p -> ((PortraitPresenter) p).loadChat());
                }
            }
        });
    }

    @Override
    public void updateItem(Channel channel, boolean empty) {
        super.updateItem(channel, empty);
        this.channel = channel;
        updateUI();
    }

    private void updateUI() {
        if (channel != null) {
            tile.setTextLine(0, channel.isUnread() ? "*" + channel.displayName() + "*" : channel.displayName());
            pseudoClassStateChanged(PSEUDO_CLASS_UNREAD, channel.isUnread());

            if (!channel.getMembers().isEmpty()) {
                User author = channel.getMembers().get(0);
                ImageCache.getImage(author.getAvatarPath())
                         .ifPresentOrElse(im -> {
                                     Avatar avatar = new Avatar(0, im);
                                     avatar.setMouseTransparent(true);
                                     tile.setPrimaryGraphic(avatar);
                                 }, () -> {
                                     String initials = Service.getInitials(author.displayName());
                                     Label icon = new Label(initials.substring(0, Math.min(initials.length(), 2)));
                                     icon.getStyleClass().add("channel-icon");
                                     tile.setPrimaryGraphic(icon);
                                 });
            } else {
                tile.setPrimaryGraphic(null);
            }
        } else {
            tile.setTextLine(0, null);
            tile.setPrimaryGraphic(null);
            pseudoClassStateChanged(PSEUDO_CLASS_UNREAD, false);
        }
    }
}
