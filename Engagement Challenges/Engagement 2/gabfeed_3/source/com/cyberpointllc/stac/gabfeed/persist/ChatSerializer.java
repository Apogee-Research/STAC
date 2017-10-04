package com.cyberpointllc.stac.gabfeed.persist;

import com.cyberpointllc.stac.gabfeed.model.GabChat;
import org.mapdb.Serializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ChatSerializer extends Serializer<GabChat> {

    private final GabDatabase db;

    public ChatSerializer(GabDatabase db) {
        this.db = db;
    }

    @Override
    public void serialize(DataOutput out, GabChat value) throws IOException {
        out.writeUTF(value.getId());
        DATE.serialize(out, value.getLastUpdated());
        Set<String> userIds = value.getUserIds();
        out.writeInt(userIds.size());
        for (String userId : userIds) {
            out.writeUTF(userId);
        }
        List<String> messageIds = value.getMessageIds();
        out.writeInt(messageIds.size());
        for (String messageId : messageIds) {
            out.writeUTF(messageId);
        }
    }

    @Override
    public GabChat deserialize(DataInput in, int available) throws IOException {
        String id = in.readUTF();
        Date lastUpdated = DATE.deserialize(in, available);
        int numberOfUsers = in.readInt();
        Set<String> userIds = new  LinkedHashSet(numberOfUsers);
        for (int i = 0; i < numberOfUsers; i++) {
            userIds.add(in.readUTF());
        }
        int numberOfMessages = in.readInt();
        List<String> messageIds = new  ArrayList(numberOfMessages);
        for (int i = 0; i < numberOfMessages; i++) {
            deserializeHelper(messageIds, in);
        }
        return new  GabChat(db, id, userIds, lastUpdated, messageIds);
    }

    private void deserializeHelper(List<String> messageIds, DataInput in) throws IOException {
        messageIds.add(in.readUTF());
    }
}
