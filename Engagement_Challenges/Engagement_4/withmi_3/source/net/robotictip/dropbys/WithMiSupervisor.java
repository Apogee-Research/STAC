package net.robotictip.dropbys;

import net.robotictip.protocols.SenderReceiversTrouble;
import net.robotictip.protocols.SenderReceiversPublicIdentity;

import java.util.List;

public class WithMiSupervisor {
    private final HangIn withMi;

    public WithMiSupervisor(HangIn withMi) {
        this.withMi = withMi;
    }

    public void transferMessage(Chat.WithMiMsg.Builder msgBuilder, Conversation discussion) throws SenderReceiversTrouble {
        msgBuilder.setMessageId(withMi.getNextMessageId());
        msgBuilder.setUser(withMi.fetchMyUsername());
        msgBuilder.setChatId(discussion.takeUniqueId());

        byte[] data = msgBuilder.build().toByteArray();

        List<Chatee> users = discussion.obtainUsers();
        for (int p = 0; p < users.size(); p++) {
            new WithMiSupervisorManager(data, users, p).invoke();
        }
    }

    /**
     * Adds the given user to the given chat
     *
     * @param discussion
     * @param user
     */
    public void addUserToDiscussion(Conversation discussion, Chatee user) {
        withMi.getDiscussionManager().addUserToDiscussion(discussion, user);
    }

    public boolean isConnectedTo(SenderReceiversPublicIdentity identity) {
        return withMi.grabIdentityConnection(identity) != null;
    }

    /**
     * Takes the given identity and creates a WithMiUser from the identity or finds
     * the WithMiUser already associated with the identity. Stores the user.
     *
     * @param identity of new connection
     * @return user
     * @throws SenderReceiversTrouble
     */
    public Chatee generateAndStoreUser(SenderReceiversPublicIdentity identity) throws SenderReceiversTrouble {
        Chatee user = withMi.obtainUserManager().fetchOrGenerateUser(identity);
        withMi.obtainUserManager().storeUser(user);
        return user;
    }

    public SenderReceiversPublicIdentity obtainMyIdentity() {
        return withMi.grabIdentity().grabPublicIdentity();
    }

    private class WithMiSupervisorManager {
        private byte[] data;
        private List<Chatee> users;
        private int a;

        public WithMiSupervisorManager(byte[] data, List<Chatee> users, int a) {
            this.data = data;
            this.users = users;
            this.a = a;
        }

        public void invoke() throws SenderReceiversTrouble {
            Chatee user = users.get(a);
            if (user.hasConnection()) {
                withMi.transferMessage(data, user.takeConnection());
            } else {
                withMi.printUserMsg("Couldn't find connection for " + user.obtainName());
            }
        }
    }
}