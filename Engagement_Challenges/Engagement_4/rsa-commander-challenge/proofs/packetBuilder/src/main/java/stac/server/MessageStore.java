package stac.server;

import stac.communications.parsers.RequestPacket;
import stac.permissions.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 */
public class MessageStore {
    final private HashMap<User, List<RequestPacket>> messages = new HashMap<>();
    final private UserStore userStore;

    public MessageStore(UserStore userStore) {
        this.userStore = userStore;
    }

    public boolean handleMessage(RequestPacket rpkt) {
        User user = userStore.findUser(rpkt.getReceiverName());
        if (user != null) {
            List<RequestPacket> packets = messages.get(user);
            if (packets == null) {
                packets = new ArrayList<>();
                messages.put(user, packets);
            }
            packets.add(rpkt);
            return true;
        }
        return false;
    }

    public List<RequestPacket> popMessagesFor(User user) {
        return messages.remove(user);
    }
}
