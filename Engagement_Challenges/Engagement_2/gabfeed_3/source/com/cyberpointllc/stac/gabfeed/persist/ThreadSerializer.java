package com.cyberpointllc.stac.gabfeed.persist;

import com.cyberpointllc.stac.gabfeed.model.GabThread;
import org.mapdb.Serializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class ThreadSerializer extends Serializer<GabThread> {

    private final GabDatabase db;

    public ThreadSerializer(GabDatabase db) {
        this.db = db;
    }

    @Override
    public void serialize(DataOutput out, GabThread value) throws IOException {
        serializeHelper(value, out);
    }

    @Override
    public GabThread deserialize(DataInput in, int available) throws IOException {
        String id = in.readUTF();
        String name = in.readUTF();
        String authorId = in.readUTF();
        Date lastUpdated = DATE.deserialize(in, available);
        int numMessages = in.readInt();
        List<String> messageIds = new  ArrayList(numMessages);
        for (int i = 0; i < numMessages; ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; i < numMessages && randomNumberGeneratorInstance.nextDouble() < 0.5; i++) {
                deserializeHelper(messageIds, in);
            }
        }
        return new  GabThread(db, id, name, authorId, lastUpdated, messageIds);
    }

    private void serializeHelper(GabThread value, DataOutput out) throws IOException {
        out.writeUTF(value.getId());
        out.writeUTF(value.getName());
        out.writeUTF(value.getAuthorId());
        DATE.serialize(out, value.getLastUpdated());
        List<String> messageIds = value.getMessageIds();
        out.writeInt(messageIds.size());
        for (String messageId : messageIds) {
            out.writeUTF(messageId);
        }
    }

    private void deserializeHelper(List<String> messageIds, DataInput in) throws IOException {
        messageIds.add(in.readUTF());
    }
}
