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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    Map<Channel, ObservableList<ChatMessage>> channelMap = new HashMap<>();
    boolean channelsClean = false;
    private ObservableList<Contact> contacts;
    private BootstrapClient bootstrapClient;
    private ObservableList<User> users;
    private ObservableList<Channel> channels;

    public WaveService() {
        wave = WaveManager.getInstance();
        wave.setLogLevel(Level.DEBUG);
        wave.getWaveLogger().log(Level.DEBUG, "Creating waveService: " + System.identityHashCode(wave));
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
        if (this.users == null) {
            this.users = retrieveUsers();
        }
        return users;
    }
    
    private ObservableList<User> retrieveUsers() {
        ObservableList<User> answer = FXCollections.observableArrayList();
        contacts = wave.getContacts();

        answer.addAll(contacts.stream()
                .map(a -> createUserFromContact(a))
                .collect(Collectors.toList()));
        contacts.addListener((ListChangeListener.Change<? extends Contact> change) -> {
            while (change.next()) {
                List<User> addedUsers = change.getAddedSubList().stream()
                        .map(contact -> createUserFromContact(contact))
                        .collect(Collectors.toList());
                Platform.runLater(()-> answer.addAll(addedUsers));
                List<User> removedUsers = change.getRemoved().stream()
                        .map(contact -> findUserByContact(contact, answer))
                        .filter(opt -> opt.isPresent())
                        .map(opt -> opt.get()).collect(Collectors.toList());
                Platform.runLater(() -> answer.removeAll(removedUsers));
            }
        });
        return answer;
    }

    @Override
    public ObservableList<Channel> getChannels() {
        if (this.channels == null) {
            this.channels = retrieveChannels();
        }
        return this.channels;
    }
    
    private ObservableList<Channel> retrieveChannels() {
        ObservableList<Channel> answer = FXCollections.observableArrayList();
        ObservableList<User> users = getUsers();
        answer.addAll(users.stream().map(user -> createChannelFromUser(user))
                .collect(Collectors.toList()));
        answer.forEach(c -> readMessageForChannel(c));
        users.addListener((ListChangeListener.Change<? extends User> change) -> {
            while (change.next()) {
                List<Channel> addedChannels = change.getAddedSubList().stream()
                        .map(user -> createChannelFromUser(user))
                        .collect(Collectors.toList());
                answer.addAll(addedChannels);
            }
        });
        return answer;
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
                String content = URLDecoder.decode(lines.get(i + 1),StandardCharsets.UTF_8);
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
        gotMessage(senderUuid, content, timestamp, senderUuid);
    }

    @Override
    public void gotMessage(String senderUuid, String content, long timestamp, String receiverUuid) {
        wave.getWaveLogger().log(Level.DEBUG,
                "GOT MESSAGE from " + senderUuid + " for "+ receiverUuid+" with content " + content);
        Channel dest = this.channels.stream()
                .filter(c -> c.getMembers().size() > 0)
                .filter(c -> c.getMembers().get(0).getId().equals(receiverUuid))
                .findFirst().get();
        User sender = findUserByUuid(senderUuid, users).get();
        ChatMessage chatMessage = new ChatMessage(content, sender, LocalDateTime.now());
        Platform.runLater(() -> dest.getMessages().add(chatMessage));
        storeMessage(receiverUuid, senderUuid, content, timestamp);
    }

    private void storeMessage(String userUuid, String senderUuid, String content, long timestamp) {
        try {
            File contactsDir = wave.SIGNAL_FX_CONTACTS_DIR;
            Path contactPath = contactsDir.toPath().resolve(userUuid);
            Path messagelog = contactPath.resolve("chatlog");
            Files.createDirectories(contactPath);
            content = URLEncoder.encode(content, StandardCharsets.UTF_8);
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
            wave.getWaveLogger().log(Level.DEBUG, "[WAVESERVICE] rnd = " + rnd + ", create account");
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

    private Optional<User> findUserByUuid(String uuid, List<User> users) {
        Optional<User> target = users.stream().filter(u -> u.getId().equals(uuid)).findFirst();
        return target;
    }

    private Optional<User> findUserByContact(Contact contact, List<User> users) {
        String cuuid = contact.getUuid();
        Optional<User> target = users.stream().filter(u -> u.getId().equals(cuuid)).findFirst();
        return target;
    }

    // this will never give a match since we use a random id when creating a channel.
    // There is no 1-1 relation between User and Channel in case a channel has many users.
    private Optional<Channel> findChannelByUser(User user, List<Channel> channels) {
        String cuuid = user.getId();
        Optional<Channel> target = channels.stream().filter(u -> u.getId().equals(cuuid)).findFirst();
        return target;
    }

}
