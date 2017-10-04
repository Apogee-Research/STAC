package com.cyberpointllc.stac.gabfeed.persist;

import com.cyberpointllc.stac.gabfeed.model.GabRoom;
import org.mapdb.Serializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RoomSerializer extends Serializer<GabRoom> {

    private final GabDatabase db;

    public RoomSerializer(GabDatabase db) {
        this.db = db;
    }

    @Override
    public void serialize(DataOutput out, GabRoom value) throws IOException {
        out.writeUTF(value.getId());
        out.writeUTF(value.getName());
        out.writeUTF(value.getDescription());
        List<String> threadIds = value.getThreadIds();
        out.writeInt(threadIds.size());
        for (String threadId : threadIds) {
            serializeHelper(threadId, out);
        }
    }

    @Override
    public GabRoom deserialize(DataInput in, int available) throws IOException {
        String id = in.readUTF();
        String name = in.readUTF();
        String description = in.readUTF();
        int numThreads = in.readInt();
        List<String> threadIds = new  ArrayList(numThreads);
        for (int i = 0; i < numThreads; i++) {
            threadIds.add(in.readUTF());
        }
        return new  GabRoom(db, id, name, description, threadIds);
    }

    private void serializeHelper(String threadId, DataOutput out) throws IOException {
        out.writeUTF(threadId);
    }
}
