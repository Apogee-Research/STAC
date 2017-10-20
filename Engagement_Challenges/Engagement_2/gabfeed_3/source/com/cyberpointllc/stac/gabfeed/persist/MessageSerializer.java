package com.cyberpointllc.stac.gabfeed.persist;

import com.cyberpointllc.stac.gabfeed.model.GabMessage;
import org.mapdb.Serializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;

public class MessageSerializer extends Serializer<GabMessage> {

    private final GabDatabase db;

    public MessageSerializer(GabDatabase db) {
        this.db = db;
    }

    @Override
    public void serialize(DataOutput out, GabMessage value) throws IOException {
        out.writeUTF(value.getId());
        out.writeUTF(value.getAuthorId());
        out.writeUTF(value.getContents());
        DATE.serialize(out, value.getPostDate());
        out.writeBoolean(value.isPublicMessage());
    }

    @Override
    public GabMessage deserialize(DataInput in, int available) throws IOException {
        String id = in.readUTF();
        String authorId = in.readUTF();
        String contents = in.readUTF();
        Date postDate = DATE.deserialize(in, available);
        boolean messageIsPublic = in.readBoolean();
        return new  GabMessage(db, id, contents, authorId, postDate, messageIsPublic);
    }
}
