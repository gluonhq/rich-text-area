package com.gluonhq.chat.views;

import com.gluonhq.charm.glisten.control.CharmListView;
import com.gluonhq.charm.glisten.control.TextField;
import com.gluonhq.chat.model.Channel;
import com.gluonhq.chat.model.ChatMessage;
import com.gluonhq.chat.service.Service;
import javafx.beans.InvalidationListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.ResourceBundle;

public class ChannelPresenter {

    @FXML private TextField search;
    @FXML private CharmListView<Channel, String> channelList;

    @Inject private Service service;
    @FXML private ResourceBundle resources;
    
    private FilteredList<Channel> channelFilteredList;

    public void initialize() {
        channelFilteredList = createChannelList();
        search.textProperty().addListener((o, ov, nv) -> channelFilteredList.setPredicate(channel -> {
            if (nv == null || nv.isEmpty()) {
                return true;
            }
            return channel.contains(nv);
        }));
        channelList.setItems(channelFilteredList);
        channelList.setCellFactory(param -> new ChannelCell());
        channelList.setHeadersFunction(param -> param.isDirect() ? "DIRECT" : "CHANNELS");
        service.initializeService();
    }

    public void updateChannels(boolean removeSelection) {
        if (removeSelection) {
            channelList.setSelectedItem(null);
        }
        channelList.refresh();
    }

    private FilteredList<Channel> createChannelList() {
        SortedList<Channel> sortedList = new SortedList<>(service.getChannels());
        sortedList.setComparator(Comparator.comparing(this::latestMessageTime).reversed());
        for (Channel channel : sortedList) {
            channel.getMessages().addListener((InvalidationListener) o -> {
                sortedList.setComparator(Comparator.comparing(ChannelPresenter.this::latestMessageTime).reversed());
            });
        }
        return new FilteredList<>(sortedList);
    }

    private LocalDateTime latestMessageTime(Channel channel) {
        return channel.getMessages().stream()
                .map(ChatMessage::getTime)
                .max(LocalDateTime::compareTo)
                .orElseGet(() -> LocalDateTime.of(1970, 1, 1, 0, 0));
    }
}
