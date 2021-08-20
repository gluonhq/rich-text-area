package com.gluonhq.chat.views;

import com.gluonhq.charm.glisten.control.CharmListCell;
import com.gluonhq.charm.glisten.control.ListTile;
import com.gluonhq.chat.model.User;

public class UserCell extends CharmListCell<User> {

    private final ListTile tile;

    public UserCell() {
        tile = new ListTile();
        setGraphic(tile);
        setText(null);
        getStyleClass().add("speaker-cell");
    }

    @Override
    public void updateItem(User user, boolean empty) {
        super.updateItem(user, empty);

        /* TODO: Add User Image
        final Avatar avatar = new Avatar;
        avatar.setMouseTransparent(true);
        tile.setPrimaryGraphic(avatar);
        */

        tile.setTextLine(0, user.toString());

        tile.setOnMouseReleased(event -> {
            AppViewManager.CHAT_VIEW.getPresenter().ifPresent(presenter -> ((ChatPresenter) presenter).updateMessages(user));
            // TODO: We want a better way to switch views if the screen size is <= 600
            if (tile.getScene().getWidth() <= 600) {
                AppViewManager.PORTRAIT_VIEW.switchView().ifPresent(p -> ((PortraitPresenter) p).loadChat());
            }
        });

    }
}
