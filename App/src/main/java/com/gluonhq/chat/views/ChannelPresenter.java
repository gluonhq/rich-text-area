package com.gluonhq.chat.views;

import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.control.CharmListView;
import com.gluonhq.charm.glisten.control.TextField;
import com.gluonhq.chat.GluonChat;
import com.gluonhq.chat.model.Channel;
import com.gluonhq.chat.service.Service;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;

import javax.inject.Inject;
import java.util.ResourceBundle;

public class ChannelPresenter extends GluonPresenter<GluonChat> {

    @FXML private TextField search;
    @FXML private CharmListView<Channel, String> channelList;

    @Inject private Service service;
    @FXML private ResourceBundle resources;
    
    private FilteredList<Channel> channelFilteredList;

    public void initialize() {
        search.textProperty().addListener((o, ov, nv) -> channelFilteredList.setPredicate(channel -> {
            if (nv == null || nv.isEmpty()) {
                return true;
            }
            return channel.contains(nv);
        }));
        channelFilteredList = new FilteredList<>(service.getChannels());
        channelList.setItems(channelFilteredList);
        channelList.setCellFactory(param -> new ChannelCell());
        channelList.setHeadersFunction(param -> param.isDirect() ? "DIRECT" : "CHANNELS");
    }
 }
