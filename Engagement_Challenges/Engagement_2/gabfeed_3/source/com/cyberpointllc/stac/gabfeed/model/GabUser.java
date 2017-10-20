package com.cyberpointllc.stac.gabfeed.model;

import com.cyberpointllc.stac.gabfeed.persist.GabDatabase;
import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.template.Templated;
import org.apache.commons.lang3.StringEscapeUtils;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GabUser implements Templated {

    private final GabDatabase db;

    private final String id;

    private final String displayName;

    private final String password;

    private final List<String> messageIds;

    public GabUser(GabDatabase db, String id, String displayName, String password) {
        this(db, id, displayName, password, new  LinkedList<String>());
    }

    public GabUser(GabDatabase db, String id, String displayName, String password, List<String> messageIds) {
        this.db = db;
        this.id = id;
        this.displayName = displayName;
        this.password = password;
        this.messageIds = messageIds;
    }

    public void addMessage(String messageId) {
        addMessageHelper(messageId);
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getMessageIds() {
        return messageIds;
    }

    public List<GabMessage> getMessages() {
        LinkedList<GabMessage> messages = new  LinkedList();
        for (String messageId : getMessageIds()) {
            messages.add(db.getMessage(messageId));
        }
        return messages;
    }

    @Override
    public Map<String, String> getTemplateMap() {
        Map<String, String> templateMap = new  HashMap();
        templateMap.put("userId", id);
        templateMap.put("displayName", StringEscapeUtils.escapeHtml4(displayName));
        return templateMap;
    }

    private void addMessageHelper(String messageId) {
        messageIds.add(messageId);
        db.addUser(this);
    }
}
