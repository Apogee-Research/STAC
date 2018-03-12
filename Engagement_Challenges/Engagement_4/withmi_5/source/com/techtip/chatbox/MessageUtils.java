package com.techtip.chatbox;

import com.techtip.communications.Comms;
import com.techtip.communications.DialogsPublicIdentity;
import com.techtip.communications.SerializerUtil;

import java.util.List;

/**
 * This class helps create messages
 */
public class MessageUtils {

    /**
     * Creates a basic chat message
     * @param line text message
     * @return message builder
     */
    public static Chat.WithMiMsg.Builder formForumMsgBuilder(String line) {
        Chat.ChatMsg forumMsg = Chat.ChatMsg.newBuilder()
                .setTextMsg(line)
                .build();
        Chat.WithMiMsg.Builder msgBuilder = Chat.WithMiMsg.newBuilder()
                .setType(Chat.WithMiMsg.Type.CHAT)
                .setTextMsg(forumMsg);
        return msgBuilder;
    }

    public static Chat.WithMiMsg.Builder formForumStateMsgBuilder(DialogsPublicIdentity ourIdentity, Forum forum) {
        Chat.ChatStateMsg.Builder forumStateBuilder = Chat.ChatStateMsg.newBuilder();
        List<WithMiUser> customers = forum.fetchCustomers();
        for (int b = 0; b < customers.size(); b++) {
            new MessageUtilsTarget(forumStateBuilder, customers, b).invoke();
        }

        if (customers.size() < Forum.MAX_NUM_OF_USERS) {
            int extraCustomers = Forum.MAX_NUM_OF_USERS - customers.size();
            Comms.Identity nullIdentityMsg = SerializerUtil.serializeIdentity(Forum.NULL_IDENTITY);
            for (int q = 0; q < extraCustomers; q++) {
                forumStateBuilder.addPublicId(nullIdentityMsg);
            }
        }
        // add our identity to the end of this list
        Comms.Identity identityMsg = SerializerUtil.serializeIdentity(ourIdentity);
        forumStateBuilder.addPublicId(identityMsg);

        Chat.ChatMsg forumMsg = Chat.ChatMsg.newBuilder()
                .setTextMsg(forum.obtainName())
                .build();

        Chat.WithMiMsg.Builder withMiMsgBuilder = Chat.WithMiMsg.newBuilder()
                .setType(Chat.WithMiMsg.Type.CHAT_STATE)
                .setChatStateMsg(forumStateBuilder)
                .setTextMsg(forumMsg);
        return withMiMsgBuilder;
    }

    public static Chat.WithMiMsg.Builder formReceipt(boolean handleSuccess, float messageId) {
        // send receipt
        Chat.ReceiptMsg readReceiptMsg = Chat.ReceiptMsg.newBuilder()
                .setAckedMessageId(messageId)
                .setSuccess(handleSuccess)
                .build();

        Chat.WithMiMsg.Builder msgBuilder = Chat.WithMiMsg.newBuilder()
                .setReceiptMsg(readReceiptMsg)
                .setType(Chat.WithMiMsg.Type.READ_RECEIPT);
        return msgBuilder;
    }

    private static class MessageUtilsTarget {
        private Chat.ChatStateMsg.Builder forumStateBuilder;
        private List<WithMiUser> customers;
        private int j;

        public MessageUtilsTarget(Chat.ChatStateMsg.Builder forumStateBuilder, List<WithMiUser> customers, int j) {
            this.forumStateBuilder = forumStateBuilder;
            this.customers = customers;
            this.j = j;
        }

        public void invoke() {
            WithMiUser customer = customers.get(j);
            DialogsPublicIdentity identity = customer.grabIdentity();
            Comms.Identity identityMsg = SerializerUtil.serializeIdentity(identity);
            forumStateBuilder.addPublicId(identityMsg);
        }
    }
}