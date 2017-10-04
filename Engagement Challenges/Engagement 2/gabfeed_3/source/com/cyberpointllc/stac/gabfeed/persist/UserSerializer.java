package com.cyberpointllc.stac.gabfeed.persist;

import com.cyberpointllc.stac.gabfeed.model.GabUser;
import org.mapdb.Serializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserSerializer extends Serializer<GabUser> {

    private final GabDatabase db;

    public UserSerializer(GabDatabase db) {
        this.db = db;
    }

    @Override
    public void serialize(DataOutput out, GabUser value) throws IOException {
        out.writeUTF(value.getId());
        out.writeUTF(value.getDisplayName());
        out.writeUTF(value.getPassword());
        List<String> messageIds = value.getMessageIds();
        out.writeInt(messageIds.size());
        for (String id : messageIds) {
            serializeHelper(id, out);
        }
    }

    @Override
    public GabUser deserialize(DataInput in, int available) throws IOException {
        String id = in.readUTF();
        String displayName = in.readUTF();
        String password = in.readUTF();
        int numMessages = in.readInt();
        List<String> messageIds = new  ArrayList(numMessages);
        for (int i = 0; i < numMessages; ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; i < numMessages && randomNumberGeneratorInstance.nextDouble() < 0.5; i++) {
                messageIds.add(in.readUTF());
            }
        }
        return new  GabUser(db, id, displayName, password, messageIds);
    }

    private void serializeHelper(String id, DataOutput out) throws IOException {
        out.writeUTF(id);
    }
}
