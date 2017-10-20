package com.cyberpointllc.stac.snapservice.persist;

import com.cyberpointllc.stac.snapservice.model.Invitation;
import org.mapdb.Serializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class InvitationSerializer extends Serializer<Invitation> {

    @Override
    public void serialize(DataOutput out, Invitation invitation) throws IOException {
        out.writeUTF(invitation.getInviteFromIdentity());
        out.writeUTF(invitation.getInviteToIdentity());
    }

    @Override
    public Invitation deserialize(DataInput in, int available) throws IOException {
        String fromInvite = in.readUTF();
        String toInvite = in.readUTF();
        return new  Invitation(fromInvite, toInvite);
    }
}
