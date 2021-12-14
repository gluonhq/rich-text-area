package com.gluonhq.chat.service;

import com.gluonhq.chat.model.Channel;
import com.gluonhq.chat.model.ChatImage;
import com.gluonhq.chat.model.ChatMessage;
import com.gluonhq.chat.model.GithubRelease;
import com.gluonhq.chat.model.User;
import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.equation.WaveManager;
import com.gluonhq.equation.message.MessagingClient;
import com.gluonhq.equation.model.Contact;
import com.gluonhq.equation.provision.ProvisioningClient;
import com.gluonhq.equation.util.QRGenerator;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

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
import java.util.*;
import java.util.stream.Collectors;

import static com.gluonhq.chat.service.UpdateService.*;
import com.gluonhq.equation.model.Group;
import com.gluonhq.equation.model.Message;
import java.util.stream.Stream;
import javafx.beans.InvalidationListener;

public class WaveService implements Service, ProvisioningClient, MessagingClient {

    private User loggedUser;
    private final WaveManager wave;
    private final Map<Channel, ObservableList<ChatMessage>> channelMap = new HashMap<>();
    boolean channelsClean = false;
    private ObservableList<Contact> contacts;
    private BootstrapClient bootstrapClient;
    private ObservableList<User> users;
    private ObservableList<Group> groups;
    private ObservableList<Channel> channels;

    public WaveService() {
        // set this property to edit the time we allow to sync contacts at bootstrap
       // System.setProperty("com.gluonhq.wave.provisioningTimeout","30000"); // 30 seconds
        wave = WaveManager.getInstance();
        wave.setLogLevel(Level.DEBUG);
        wave.getWaveLogger().log(Level.DEBUG, "Creating waveService: " + System.identityHashCode(wave));
        this.wave.setMessageListener(this);
        if (wave.isProvisioned()) {
            login(wave.getMyUuid());
        }
    }

    @Override
    public void initializeService() {
        System.err.println("WAVESERVICE INIT");
        this.wave.initialize();
        System.err.println("WAVESERVICE INIT DONE");

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
        System.err.println("WAVEservice, getUsers asked");
        if (this.users == null) {
            System.err.println("WAVEservice, retrieveUsers asked");
            this.users = retrieveUsers();
        }
        return users;
    }
    
    public ObservableList<Group> getGroups() {
        if (this.groups == null) {
            this.groups = retrieveGroups();
        }
        return groups;
    }
    
    private ObservableList<Group> retrieveGroups() {
        ObservableList<Group> answer = FXCollections.observableArrayList();
        groups = wave.getGroups();
        answer.addAll(groups);
        groups.addListener((ListChangeListener.Change<? extends Group> change) -> {
            while (change.next()) {
                Stream<? extends Group> addedGroups = change.getAddedSubList().stream();
                answer.addAll(addedGroups.collect(Collectors.toList()));
                System.err.println("WAVESERVICE, groups now = "+answer);
            }
        });
        System.err.println("WAVESERVICE initially, groups = "+answer);
        return answer;
    }
    
    private ObservableList<User> retrieveUsers() {
        ObservableList<User> answer = FXCollections.observableArrayList();
        contacts = wave.getContacts();
        for (Contact contact : contacts) {
            if (contact.isActive()) {
                User u = createUserFromContact(contact);
                answer.add(u);
            }
        }
//        answer.addAll(contacts.stream()
//                .filter(a -> a.isActive())
//                .map(a -> createUserFromContact(a))
//                .collect(Collectors.toList()));
        contacts.addListener((ListChangeListener.Change<? extends Contact> change) -> {
            while (change.next()) {
                List<User> addedUsers = change.getAddedSubList().stream()
                        .filter(a -> a.isActive())
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
        System.err.println("WAVE service getchannels asked");
        if (this.channels == null) {
            System.err.println("WAVE service, needs to retrieve channels");
            this.channels = retrieveChannels();
        }
        System.err.println("WAVE service, got channels: "+this.channels);
        return this.channels;
    }
    
    private ObservableList<Channel> retrieveChannels() {
        ObservableList<Channel> answer = FXCollections.observableArrayList();
        ObservableList<User> users = getUsers();
        answer.addAll(users.stream().map(user -> createChannelFromUser(user))
                .collect(Collectors.toList()));
        answer.addAll(getGroups().stream().map(group -> createChannelFromGroup(group))
                .collect(Collectors.toList()));
        answer.forEach(c -> readMessageForChannel(c));
        answer.addListener((ListChangeListener.Change<? extends Channel> change) -> {
            while (change.next()) {
                change.getAddedSubList().forEach(channel -> readMessageForChannel(channel));
            }
        });

        users.addListener((ListChangeListener.Change<? extends User> change) -> {
            while (change.next()) {
                List<Channel> addedChannels = change.getAddedSubList().stream()
                        .map(user -> createChannelFromUser(user))
                        .collect(Collectors.toList());
                Platform.runLater(() -> answer.addAll(addedChannels));
                List<Channel> removedChannels = change.getRemoved().stream()
                        .map(user -> findChannelByUser(user, answer))
                        .filter(opt -> opt.isPresent())
                        .map(opt -> opt.get()).collect(Collectors.toList());
                Platform.runLater(() -> answer.removeAll(removedChannels));
            }
        });
        groups.addListener((ListChangeListener.Change<? extends Group> change) -> {
            while (change.next()) {
                List<Channel> addedChannels = change.getAddedSubList().stream()
                        .map(group -> createChannelFromGroup(group))
                        .collect(Collectors.toList());
                Platform.runLater(() -> answer.addAll(addedChannels));
                List<Channel> removedChannels = change.getRemoved().stream()
                        .map(group -> findChannelByGroup(group, answer))
                        .filter(opt -> opt.isPresent())
                        .map(opt -> opt.get()).collect(Collectors.toList());
                Platform.runLater(() -> answer.removeAll(removedChannels));
            }
        });
        return answer;
    }

    private void readMessageForChannel(Channel c) {
        try {
            if (!c.isDirect()) {
                System.err.println("GROUPCHANNELS not yet supported 100%");
                return;
            } 
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
                ChatMessage cm = new ChatMessage(content, author, timestamp);
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
    public ObservableList<ChatMessage> getMessages(User loggedUser) {
        return channelMap.get(loggedUser);
    }

    // MESSAGECLIENT
    
    @Override
    public void gotMessage(String senderUuid, String content, long timestamp) {
        gotMessage(senderUuid, content, timestamp, senderUuid);
    }

    @Override
    public void gotMessage(String senderUuid, String content, long timestamp, String receiverUuid) {
        Message msg = new Message();
        msg.senderUuid(senderUuid).content(content).receiverUuid(receiverUuid).timestamp(timestamp);
        gotMessage(msg);
    }

    @Override
    public void gotMessage(Message msg) {
        String senderUuid = msg.getSenderUuid();
        String receiverUuid = msg.getReceiverUuid();
        if (receiverUuid == null) {
            receiverUuid = msg.getSenderUuid();
        }
        String content = msg.getContent();
        long timestamp = msg.getTimestamp();
        wave.getWaveLogger().log(Level.DEBUG,
                "GOT MESSAGE from " + senderUuid + " for "+ receiverUuid+" with content " + content);
        Channel dest;
        if (msg.getGroup()!= null) {
            Group g = msg.getGroup();
            dest = getChannelByGroup(g);
        } else {
            dest = getChannelByUuid(senderUuid);
        }
        if (dest == null) {
            wave.getWaveLogger().log(Level.WARNING, "unknown sender for incoming message: "
                    +senderUuid+"\nIgnoring message.");
            return;
        }
        User sender = findUserByUuid(senderUuid, users).get();
        ChatMessage chatMessage = new ChatMessage(content, sender, timestamp);
        chatMessage.setAttachment(msg.getAttachment());
        InvalidationListener il = o -> new InvalidationListener() {
            @Override
            public void invalidated(javafx.beans.Observable obs) {
                ChatMessage.ReceiptType rtype = chatMessage.receiptProperty().get();
                wave.getWaveLogger().log(Level.DEBUG, "new receipt for msg with timestamp "+timestamp+" and senderuid = "+senderUuid);
                if (rtype.equals(ChatMessage.ReceiptType.READ)) {
                    wave.sendReadReceipt(timestamp, senderUuid);
                    chatMessage.receiptProperty().removeListener(this);
                }
            }
        };
        chatMessage.receiptProperty().addListener(il);
        Platform.runLater(() -> dest.getMessages().add(chatMessage));
        storeMessage(receiverUuid, senderUuid, content, timestamp);
    }
    
    @Override
    public void gotTypingAction(String senderUuid, boolean startTyping, boolean stopTyping) {
        Channel dest = getChannelByUuid(senderUuid);
        Platform.runLater(() -> {
            if (startTyping) {
                dest.setTyping(true);
            }
            if (stopTyping) {
                dest.setTyping(false);
            }
        });
    }

    @Override
    public void gotReceiptMessage(String senderUuid, int type, List<Long> timestamps) {
        Channel dest = getChannelByUuid(senderUuid);
        if (dest == null) {
            System.err.println("[WARNING] This should never happen. We got a receipt message"
                    + "for someone we don't know: "+senderUuid);
            System.err.println("Channels = "+channels);
            return;
        }
        Platform.runLater(() -> {
            ChatMessage.ReceiptType rtype = ChatMessage.ReceiptType.valueOf(type);
            dest.getMessages()
                    .filtered(m -> timestamps.contains(m.getTimestamp()))
                    .forEach(msg -> msg.setReceiptType(rtype));
        });
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
                    addedmsg.stream().filter((ChatMessage m) -> m.getLocalOriginated())
                            .forEach((ChatMessage m) -> {
                                String uuid = u.getId();
                                try {
                                    long time = wave.sendMessage(uuid, m.getMessage(), m.getAttachment());
                                    m.setTimestamp(time);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                storeMessage(uuid, loggedUser.getId(), m.getMessage(), System.currentTimeMillis());
                                if (!uuid.equals(loggedUser.getId())) {
                                    answer.setUnread(true);
                                }
                            });
                    if (readUnreadList().contains(answer.getId())) {
                        answer.setUnread(true);
                    }
                }
            }
        });
        return answer;
    }

    private Channel createChannelFromGroup(Group g) {
        ObservableList<ChatMessage> messages = FXCollections.observableArrayList();
        Channel answer = new Channel(g, messages);
        channelMap.put(answer, messages);
        messages.addListener(new ListChangeListener<ChatMessage>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends ChatMessage> change) {
                while (change.next()) {
                    List<? extends ChatMessage> addedmsg = change.getAddedSubList();
                    System.err.println("Added groupsmsg: "+addedmsg);
                    addedmsg.stream().filter((ChatMessage m) -> m.getLocalOriginated())
                            .forEach((ChatMessage m) -> {
                                String uuid = g.getName();
                                try {
                                    long time = wave.sendGroupMessage(uuid, m.getMessage(), m.getAttachment());
                                    m.setTimestamp(time);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                storeMessage(uuid, loggedUser.getId(), m.getMessage(), System.currentTimeMillis());
                                if (!uuid.equals(loggedUser.getId())) {
                                    answer.setUnread(true);
                                }
                            });
//                    if (readUnreadList().contains(answer.getId())) {
//                        answer.setUnread(true);
//                    }
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
            wave.createAccount(number, "Gluon" + rnd);
            login(wave.getMyUuid());

            wave.setMessageListener(this);
            wave.reset();
            Platform.runLater(() -> bootstrapClient.bootstrapSucceeded());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Override public void gotProvisioningError(String msg) {
        System.err.println("WAVESERVICE received error: "+ msg);
        if ("CONTACT_SYNC_ERROR".equals(msg)) {
            String text = "We couldn't get your contacts in the past 30 seconds. "+
                    "This happens sometimes at bootstrapping. Restarting the application "
                    + "typically fixes this. You do not need to pair your device anymore, "
                    + "that's already done.";
            Platform.runLater(() -> bootstrapClient.bootstrapFailed(text));
        }
    }

    public List<String> readUnreadList() {
        try {
            File contactsDir = wave.SIGNAL_FX_CONTACTS_DIR;
            Path contactPath = contactsDir.toPath().resolve("unreadcontacts.csv");
            if (!Files.exists(contactPath)) {
                return List.of();
            }
            final String csvString = Files.readString(contactPath);
            return List.of(csvString.split(","));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }
    
    public void updateUnreadList(String channelId, boolean add) {
        try {
            final List<String> unreadList = new ArrayList<>(readUnreadList());
            if (add) {
                if (!unreadList.contains(channelId)) unreadList.add(channelId);
            } else {
                unreadList.remove(channelId);
            }
            File contactsDir = wave.SIGNAL_FX_CONTACTS_DIR;
            Path contactPath = contactsDir.toPath().resolve("unreadcontacts.csv");
            final String unreadCSVString = String.join(",", unreadList);
            Files.writeString(contactPath, unreadCSVString);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public BooleanProperty newVersionAvailable() {
        BooleanProperty versionAvailable = new SimpleBooleanProperty();
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("win") || OS.contains("mac")) {
            GluonObservableList<GithubRelease> githubReleases = UpdateService.queryReleases();
            githubReleases.setOnSucceeded(e -> {
                Optional<GithubRelease> latestVersion = githubReleases.stream()
                        .max((o1, o2) -> compareVersions(o1.getTag_version(), o2.getTag_version()));
                final String appVersion = currentAppVersion();
                if (latestVersion.isPresent() && !appVersion.contains("SNAPSHOT")) {
                    if (compareVersions(latestVersion.get().getTag_version(), appVersion) > 0) {
                        downloadNewVersion(latestVersion.get(), versionAvailable::set);
                    } else {
                        deleteExistingFiles();
                    }
                }
            });
        }
        return versionAvailable;
    }

    @Override
    public void installNewVersion() {
        UpdateService.installNewVersion();
    }

    /**
     * Return the channel that contains the given senderUuid as its first member.
     * In case of a direct channel, there is only 1 member, so this returns the
     * direct channel belonging to the specified senderUuid.
     * @param senderUuid
     * @return the Channel corresponding to this user, or null if no such channel  exists.
     */
    private Channel getChannelByUuid(String senderUuid) {
        Channel dest = this.channels.stream()
                .filter(c -> c.getMembers().size() > 0)
                .filter(c -> c.getMembers().get(0).getId().equals(senderUuid))
                .findFirst().orElse(null);
        return dest;
    }
    
    private Channel getChannelByGroup(Group group) {
        Channel answer = this.channels.stream()
                .filter(c -> c.getId().equals("g"+group.getName()))
                .findFirst().orElse(null);
        System.err.println("CBG for "+group+" returns "+answer);
        return answer;
    }
    
    private static User createUserFromContact(Contact c) {
        System.err.println("CREATEUSERFROMCONTACT "+c);
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

    // A direct channel has an id "c" + userId. This is brittle though, and should be
    // made more robust once we introduce group channels
    private Optional<Channel> findChannelByUser(User user, List<Channel> channels) {
        String cuuid = "c" + user.getId();
        Optional<Channel> target = channels.stream().filter(c -> c.getId().equals(cuuid)).findFirst();
        return target;
    }
    
    // we introduce group channels, so need to fix the above soon now.
    private Optional<Channel> findChannelByGroup(Group group, List<Channel> channels) {
        String guuid = "g" + group.getName();
        Optional<Channel> target = channels.stream().filter(c -> c.getId().equals(guuid)).findFirst();
        return target;
    }


}
