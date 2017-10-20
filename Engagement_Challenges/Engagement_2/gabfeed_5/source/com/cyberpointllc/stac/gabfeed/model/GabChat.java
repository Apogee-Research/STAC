package com.cyberpointllc.stac.gabfeed.model;

import com.cyberpointllc.stac.gabfeed.persist.GabDatabase;
import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.template.Templated;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GabChat implements Templated {

    public static final GabChatAscendingComparator ASCENDING_COMPARATOR = new  GabChatAscendingComparator();

    public static final GabChatDescendingComparator DESCENDING_COMPARATOR = new  GabChatDescendingComparator();

    private final GabDatabase db;

    private final String id;

    private final Set<String> userIds;

    private final List<String> messageIds;

    private Date lastUpdated;

    public GabChat(GabDatabase db, Set<String> usersIds) {
        this(db, db.getChatId(), usersIds, new  Date(), new  LinkedList<String>());
    }

    public GabChat(GabDatabase db, String id, Set<String> userIds, Date lastUpdated, List<String> messageIds) {
        this.db = db;
        this.id = id;
        this.userIds = userIds;
        this.lastUpdated = lastUpdated;
        this.messageIds = messageIds;
    }

    public GabMessage addMessage(String contents, String authorId) {
        String messageId = getId() + "_" + messageIds.size();
        Date postDate = new  Date();
        GabMessage message = new  GabMessage(db, messageId, contents, authorId, postDate, false);
        lastUpdated = postDate;
        messageIds.add(messageId);
        db.addMessage(message);
        db.addChat(this);
        return message;
    }

    public String getId() {
        return id;
    }

    public Set<String> getUserIds() {
        return userIds;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public List<String> getMessageIds() {
        return messageIds;
    }

    public List<GabMessage> getMessages() {
        LinkedList<GabMessage> messages = new  LinkedList();
        for (String messageId : getMessageIds()) {
            getMessagesHelper(messageId, messages);
        }
        return messages;
    }

    public String getOthers(String userId) {
        StringBuilder sb = new  StringBuilder();
        boolean firstTime = true;
        for (String user : userIds) {
            if (!user.equals(userId)) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    sb.append(" and ");
                }
                GabUser gabUser = db.getUser(user);
                sb.append((gabUser != null) ? gabUser.getDisplayName() : user);
            }
        }
        return sb.toString();
    }

    @Override
    public Map<String, String> getTemplateMap() {
        Map<String, String> templateMap = new  HashMap();
        templateMap.put("chatId", id);
        templateMap.put("chatLastUpdated", lastUpdated.toString());
        return templateMap;
    }

    public static class GabChatAscendingComparator implements Comparator<GabChat> {

        @Override
        public int compare(GabChat gabChat1, GabChat gabChat2) {
            Date lastUpdated1 = gabChat1.getLastUpdated();
            Date lastUpdated2 = gabChat2.getLastUpdated();
            return lastUpdated1.compareTo(lastUpdated2);
        }
    }

    public static class GabChatDescendingComparator implements Comparator<GabChat> {

        @Override
        public int compare(GabChat gabChat1, GabChat gabChat2) {
            Date lastUpdated1 = gabChat1.getLastUpdated();
            Date lastUpdated2 = gabChat2.getLastUpdated();
            return lastUpdated2.compareTo(lastUpdated1);
        }
    }

    private void getMessagesHelper(String messageId, LinkedList<GabMessage> messages) {
        messages.add(db.getMessage(messageId));
    }
}
