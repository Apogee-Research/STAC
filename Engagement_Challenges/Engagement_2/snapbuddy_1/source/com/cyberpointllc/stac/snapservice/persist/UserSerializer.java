package com.cyberpointllc.stac.snapservice.persist;

import com.cyberpointllc.stac.webserver.User;
import org.mapdb.Serializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class UserSerializer extends Serializer<User> {

    @Override
    public void serialize(DataOutput out, User user) throws IOException {
        serializeHelper(user, out);
    }

    @Override
    public User deserialize(DataInput in, int available) throws IOException {
        String identity = in.readUTF();
        String username = in.readUTF();
        String password = in.readUTF();
        return new  User(identity, username, password);
    }

    private void serializeHelper(User user, DataOutput out) throws IOException {
        out.writeUTF(user.getIdentity());
        out.writeUTF(user.getUsername());
        out.writeUTF(user.getPassword());
    }
}
