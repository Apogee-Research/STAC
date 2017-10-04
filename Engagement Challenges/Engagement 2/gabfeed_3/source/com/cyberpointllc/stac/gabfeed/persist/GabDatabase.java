package com.cyberpointllc.stac.gabfeed.persist;

import com.cyberpointllc.stac.common.DESHelper;
import com.cyberpointllc.stac.gabfeed.model.GabChat;
import com.cyberpointllc.stac.gabfeed.model.GabIndexEntry;
import com.cyberpointllc.stac.gabfeed.model.GabMessage;
import com.cyberpointllc.stac.gabfeed.model.GabRoom;
import com.cyberpointllc.stac.gabfeed.model.GabThread;
import com.cyberpointllc.stac.gabfeed.model.GabUser;
import com.cyberpointllc.stac.hashmap.HashMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Random;

public class GabDatabase {

    private static final String ROOMS_MAP = "ROOMS";

    private static final String THREADS_MAP = "THREADS";

    private static final String MESSAGES_MAP = "MESSAGES";

    private static final String USERS_MAP = "USERS";

    private static final String CHAT_MAP = "CHATS";

    private final DB db;

    private final Map<String, GabRoom> rooms;

    private final Map<String, GabThread> threads;

    private final Map<String, GabMessage> messages;

    private final Map<String, GabUser> users;

    private final Map<String, GabIndexEntry> indices;

    private final Map<String, GabChat> chats;

    public GabDatabase(File databaseFile) {
        db = DBMaker.fileDB(databaseFile).fileMmapEnableIfSupported().transactionDisable().asyncWriteEnable().make();
        rooms = db.hashMap(ROOMS_MAP, Serializer.STRING, new  RoomSerializer(this));
        threads = db.hashMap(THREADS_MAP, Serializer.STRING, new  ThreadSerializer(this));
        messages = db.hashMap(MESSAGES_MAP, Serializer.STRING, new  MessageSerializer(this));
        users = db.hashMap(USERS_MAP, Serializer.STRING, new  UserSerializer(this));
        indices = new  HashMap();
        chats = db.hashMap(CHAT_MAP, Serializer.STRING, new  ChatSerializer(this));
        updateIndex();
    }

    private void updateIndex() {
        System.out.print("Indexing... ");
        for (GabMessage message : messages.values()) {
            indexMessage(message);
        }
        System.out.println("done.");
    }

    /**
     * Users file format: username,display-name,password
     */
    private Map<String, GabUser> initializeUsers(String dataDir, String passwordKey) {
        Map<String, GabUser> users = new  HashMap();
        File usersFile = new  File(dataDir, "gabfeed_users.txt");
        try (BufferedReader br = new  BufferedReader(new  FileReader(usersFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                initializeUsersHelper(users, line, passwordKey);
            }
        } catch (IOException e) {
            System.err.println("Error initializing users in database.");
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Rooms file format: room-id, Room-name, Room-description
     */
    private Map<String, GabRoom> initializeRooms(String dataDir) {
        Map<String, GabRoom> rooms = new  HashMap();
        File roomsFile = new  File(dataDir, "gabfeed_rooms.txt");
        try (BufferedReader br = new  BufferedReader(new  FileReader(roomsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                initializeRoomsHelper(line, rooms);
            }
        } catch (IOException e) {
            System.err.println("Error initializing rooms in database.");
            e.printStackTrace();
        }
        return rooms;
    }

    /**
     * Thread file format:
     * thread lines format:
     * , room_id, thread-name, starting-user
     * message lines format:
     * user, message
     *
     * @param dataDir directory containing DB initialization files
     * @param users   Map mapping usernames to GabUsers
     * @param rooms   Map mapping room ids to GabRooms
     */
    private void initializeThreads(String dataDir, Map<String, GabUser> users, Map<String, GabRoom> rooms) {
        File threadsFile = new  File(dataDir, "gabfeed_threads.txt");
        try (BufferedReader br = new  BufferedReader(new  FileReader(threadsFile))) {
            String line;
            GabThread thread = null;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(",")) {
                    //this is a thread
                    String[] parts = line.split(",", 4);
                    GabRoom room = rooms.get(parts[1]);
                    thread = room.addThread(parts[2], parts[3]);
                    addRoom(room);
                } else if (thread != null) {
                    initializeThreadsHelper(users, thread, line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error initializing threads in database.");
            e.printStackTrace();
        }
    }

    /**
     * Chat file format:
     * c,userId,...
     * m,userId,message
     * where:
     * c means create a new chat with the list of users involved
     * m means a message in the chat from the specified user
     *
     * @param dataDir directory containing DB initialization files
     * @param users   Map mapping usernames to GabUsers
     */
    private void initializeChats(String dataDir, Map<String, GabUser> users) {
        File file = new  File(dataDir, "gabfeed_chats.txt");
        try (BufferedReader br = new  BufferedReader(new  FileReader(file))) {
            String line;
            GabChat chat = null;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].trim().equalsIgnoreCase("c")) {
                    // A new chat
                    Set<String> userIds = new  HashSet();
                    for (int i = 1; i < parts.length; ) {
                        Random randomNumberGeneratorInstance = new  Random();
                        for (; i < parts.length && randomNumberGeneratorInstance.nextDouble() < 0.5; i++) {
                            // Skip first value ('c')
                            if (users.containsKey(parts[i])) {
                                userIds.add(parts[i]);
                            }
                        }
                    }
                    chat = new  GabChat(this, userIds);
                    addChat(chat);
                } else if (parts[0].trim().equalsIgnoreCase("m") && (chat != null)) {
                    // Add a message to the chat
                    if (users.containsKey(parts[1])) {
                        String message = parts[2];
                        // Manage case where commas are included in the message
                        for (int i = 3; i < parts.length; i++) {
                            message += "," + parts[i];
                        }
                        chat.addMessage(message, parts[1]);
                        try {
                            // Wait just a bit so msgs don't have the same timestamp
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            System.err.println("Unexptected trouble sleeping");
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error initializing threads in database.");
            e.printStackTrace();
        }
    }

    public void initialize(String dataDir, String passwordKey) {
        System.out.println("Initializing DB");
        Map<String, GabUser> users = initializeUsers(dataDir, passwordKey);
        Map<String, GabRoom> rooms = initializeRooms(dataDir);
        initializeThreads(dataDir, users, rooms);
        initializeChats(dataDir, users);
        db.commit();
    }

    public void close() {
        db.commit();
        db.close();
    }

    public String getChatId() {
        return Integer.toString(chats.size());
    }

    public GabIndexEntry getIndexEntry(String word) {
        word = word.replaceAll("[^a-zA-Z]", "");
        return indices.get(word);
    }

    public List<GabRoom> getRooms() {
        return new  LinkedList(rooms.values());
    }

    public List<GabUser> getUsers() {
        return new  LinkedList(users.values());
    }

    public GabRoom getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public GabThread getThread(String threadId) {
        return threads.get(threadId);
    }

    public GabMessage getMessage(String messageId) {
        return messages.get(messageId);
    }

    public GabUser getUser(String userId) {
        return users.get(userId);
    }

    public GabChat getChat(String chatId) {
        return chats.get(chatId);
    }

    public Collection<GabChat> getChats(String... userIds) {
        Collection<GabChat> gabChats = new  ArrayList();
        Collection<String> setOfUserIds = Arrays.asList(userIds);
        for (GabChat gabChat : chats.values()) {
            getChatsHelper(gabChat, gabChats, setOfUserIds);
        }
        return gabChats;
    }

    public void addUser(GabUser user) {
        users.put(user.getId(), user);
    }

    public void addThread(GabThread thread) {
        threads.put(thread.getId(), thread);
    }

    public void addMessage(GabMessage message) {
        messages.put(message.getId(), message);
        indexMessage(message);
    }

    public void addRoom(GabRoom room) {
        addRoomHelper(room);
    }

    public void addChat(GabChat chat) {
        chats.put(chat.getId(), chat);
    }

    private void indexMessage(GabMessage gabMessage) {
        if (gabMessage.isPublicMessage()) {
            indexMessageHelper(gabMessage);
        }
    }

    private void initializeUsersHelper(Map<String, GabUser> users, String line, String passwordKey) throws IOException {
        String[] parts = line.split(",", 3);
        String id = parts[0];
        String displayName = parts[1];
        String password = parts[2];
        String encryptedPw = DESHelper.getEncryptedString(password, passwordKey);
        GabUser user = new  GabUser(this, id, displayName, encryptedPw);
        addUser(user);
        users.put(parts[0], user);
    }

    private void initializeRoomsHelper(String line, Map<String, GabRoom> rooms) throws IOException {
        String[] parts = line.split(",", 3);
        GabRoom room = new  GabRoom(this, parts[0], parts[1], parts[2]);
        addRoom(room);
        rooms.put(parts[0], room);
    }

    private void initializeThreadsHelper(Map<String, GabUser> users, GabThread thread, String line) throws IOException {
        // this is a message for the most recently added thread
        String[] parts = line.split(",", 2);
        String username = parts[0];
        GabMessage msg = thread.addMessage(parts[1], username);
        GabUser user = users.get(username);
        user.addMessage(msg.getId());
    }

    private void getChatsHelper(GabChat gabChat, Collection<GabChat> gabChats, Collection<String> setOfUserIds) {
        if (gabChat.getUserIds().containsAll(setOfUserIds)) {
            gabChats.add(gabChat);
        }
    }

    private void addRoomHelper(GabRoom room) {
        rooms.put(room.getId(), room);
    }

    private void indexMessageHelper(GabMessage gabMessage) {
        String message = gabMessage.getContents();
        Date date = gabMessage.getPostDate();
        String[] words = message.split(" ");
        Map<String, Integer> wordCount = new  HashMap();
        for (String word : words) {
            word = word.replaceAll("[^a-zA-Z]", "");
            if (wordCount.containsKey(word)) {
                wordCount.put(word, wordCount.get(word) + 1);
            } else {
                wordCount.put(word, 1);
            }
        }
        for (String word : wordCount.keySet()) {
            String messageId = gabMessage.getId();
            int count = wordCount.get(word);
            if (indices.containsKey(word)) {
                GabIndexEntry entry = indices.get(word);
                entry.addItem(word, messageId, count, date);
                indices.put(word, entry);
            } else {
                GabIndexEntry indexEntry = new  GabIndexEntry(this, word);
                indexEntry.addItem(word, messageId, count, date);
                indices.put(word, indexEntry);
            }
        }
    }
}
