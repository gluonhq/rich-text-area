package com.gluonhq.chat.service;

import com.gluonhq.chat.model.Channel;
import com.gluonhq.chat.model.ChatImage;
import com.gluonhq.chat.model.ChatMessage;
import com.gluonhq.chat.model.User;
import com.gluonhq.wave.WaveManager;
import com.gluonhq.wave.message.MessagingClient;
import com.gluonhq.wave.model.Contact;
import com.gluonhq.wave.provision.ProvisioningClient;
import com.gluonhq.wave.util.QRGenerator;
import java.io.File;
import java.io.IOException;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

public class WaveService implements Service, ProvisioningClient, MessagingClient {

    private User loggedUser;
    private final WaveManager wave;
    ObservableList<Channel> channels = FXCollections.observableArrayList();
    Map<Channel, ObservableList<ChatMessage>> channelMap = new HashMap<>();
    boolean channelsClean = false;
    private ObservableList<Contact> contacts;
    private BootstrapClient bootstrapClient;

    public WaveService() {
        wave = WaveManager.getInstance();
        wave.setLogLevel(Level.INFO);
        System.err.println("Creating waveService: " + System.identityHashCode(wave));
        if (wave.isProvisioned()) {
            login(wave.getMyUuid());
            this.wave.setMessageListener(this);
            try {
                this.wave.ensureConnected();
            } catch (IOException ex) {
                ex.printStackTrace();
                System.err.println("We're offline. Not much we can do now!");
            }
        }
    }

    @Override
    public void initializeService() {
        this.wave.startListening();
    }

    @Override
    public ObservableList<ChatImage> getImages() {
        return FXCollections.emptyObservableList();
    }

    @Override
    public boolean login(String uuid) {
        this.loggedUser = new User("you", "First Name", "Last Name", uuid);
        return true;
    }

    @Override
    public ObservableList<User> getUsers() {
        ObservableList<User> answer = FXCollections.observableArrayList();
        contacts = wave.getContacts();

        answer.addAll(contacts.stream()
                .map(a -> createUserFromContact(a))
                .collect(Collectors.toList()));

        contacts.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                answer.clear();
                answer.addAll(contacts.stream()
                        .map(a -> createUserFromContact(a))
                        .collect(Collectors.toList()));
            }
        });
        return answer;
    }

    @Override
    public ObservableList<Channel> getChannels() {
        if (!channelsClean) {
            channels = FXCollections.observableArrayList();
            ObservableList<User> users = getUsers();
            channels.addAll(users.stream().map(user -> createChannelFromUser(user))
                    .collect(Collectors.toList()));
            channels.forEach(c -> readMessageForChannel(c));
            users.addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable o) {
                    Platform.runLater(() -> {
                        channels.clear();
                        channelMap.clear();
                        channels.addAll(users.stream().map(user -> createChannelFromUser(user))
                                .collect(Collectors.toList()));
                    });
                }
            });

            channelsClean = true;
        }
        return channels;
    }

    private void readMessageForChannel(Channel c) {
        try {
            User user = c.getMembers().get(0);
            String id = user.getId();
            File contactsDir = wave.SIGNAL_FX_CONTACTS_DIR;
            Path contactPath = contactsDir.toPath().resolve(id);
            Path messagelog = contactPath.resolve("chatlog");
            if (!Files.exists(messagelog)) {
                return;
            }
            List<String> lines = Files.readAllLines(messagelog);
            int cnt = lines.size();
            for (int i = 0; i < cnt; i = i + 3) {
                String senderuuid = lines.get(i);
                String content = lines.get(i + 1);
                long timestamp = Long.parseLong(lines.get(i + 2));
                Instant instant = Instant.ofEpochMilli(timestamp);
                LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                User author = this.loggedUser.getId().equals(senderuuid) ? this.loggedUser : user;
                ChatMessage cm = new ChatMessage(content, author, ldt);
                c.getMessages().add(cm);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public User loggedUser() {
        return loggedUser;
    }

    @Override
    public void gotMessage(String senderUuid, String content, long timestamp) {
        wave.getWaveLogger().log(Level.DEBUG, 
                "GOT MESSAGE from " + senderUuid + " with content " + content);
        Channel dest = this.channels.stream()
                .filter(c -> c.getMembers().size() > 0)
                .filter(c -> c.getMembers().get(0).getId().equals(senderUuid))
                .findFirst().get();
        ChatMessage chatMessage = new ChatMessage(content, dest.getMembers().get(0), LocalDateTime.now());
        Platform.runLater(() -> dest.getMessages().add(chatMessage));
        storeMessage(senderUuid, senderUuid, content, timestamp);
    }

    private void storeMessage(String userUuid, String senderUuid, String content, long timestamp) {
        try {
            File contactsDir = wave.SIGNAL_FX_CONTACTS_DIR;
            Path contactPath = contactsDir.toPath().resolve(userUuid);
            Path messagelog = contactPath.resolve("chatlog");
            Files.createDirectories(contactPath);
            List<String> ct = new LinkedList();
            ct.add(senderUuid);
            ct.add(content);
            ct.add(Long.toString(timestamp));
            Files.write(messagelog, ct, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void bootstrap(BootstrapClient loginPresenter) {
        this.bootstrapClient = loginPresenter;
        Runnable r = () -> wave.startProvisioning(this);
        Thread t = new Thread(r);
        t.start();
    }

    private Channel createChannelFromUser(User u) {
        ObservableList<ChatMessage> messages = FXCollections.observableArrayList();
        Channel answer = new Channel(u, messages);
        channelMap.put(answer, messages);
        messages.addListener(new ListChangeListener<ChatMessage>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends ChatMessage> change) {
                while (change.next()) {
                    List<? extends ChatMessage> addedmsg = change.getAddedSubList();
                    addedmsg.stream().filter(m -> m.getLocalOriginated())
                            .forEach(m -> {
                                String uuid = u.getId();
                                try {
                                    wave.sendMessage(uuid, m.getMessage());
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                storeMessage(uuid, loggedUser.getId(), m.getMessage(), System.currentTimeMillis());
                            });
                    answer.setUnread(true);
                }
            }

        });
        return answer;
    }


    @Override
    public void gotProvisioningUrl(String url) {
        Image image = QRGenerator.getImage(url);
        javafx.application.Platform.runLater(() -> bootstrapClient.gotImage(image));
    }

    @Override
    public void gotProvisionMessage(String number) {
        int rnd = new Random().nextInt(1000);
        try {
            wave.getWaveLogger().log(Level.DEBUG, "[WAVESERVICE] rnd = "+rnd+", create account");
            wave.createAccount(number, "Gluon-" + rnd);
            login(wave.getMyUuid());

            wave.setMessageListener(this);
            wave.getWaveLogger().log(Level.DEBUG, "[WAVESERVICE] synccontacts");
            wave.syncContacts();
            Platform.runLater(() -> bootstrapClient.bootstrapSucceeded());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static User createUserFromContact(Contact c) {
        String firstName = c.getName();
        if ((c.getName() == null) || (c.getName().isEmpty())) {
            firstName = c.getNr();
            if ((firstName == null) || (firstName.isEmpty())) {
                firstName = c.getUuid();
            }
        }
        User answer = new User(firstName, firstName, "", c.getUuid());
        answer.setAvatarPath(c.getAvatarPath());
        return answer;
    }

}
