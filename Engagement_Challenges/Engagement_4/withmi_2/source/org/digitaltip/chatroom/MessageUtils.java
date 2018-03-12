package org.digitaltip.chatroom;

import org.digitaltip.dialogs.Comms;
import org.digitaltip.dialogs.TalkersPublicIdentity;
import org.digitaltip.dialogs.SerializerUtil;

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
    public static Chat.WithMiMsg.Builder makeConferenceMsgBuilder(String line) {
        Chat.ChatMsg conferenceMsg = Chat.ChatMsg.newBuilder()
                .setTextMsg(line)
                .build();
        Chat.WithMiMsg.Builder msgBuilder = Chat.WithMiMsg.newBuilder()
                .setType(Chat.WithMiMsg.Type.CHAT)
                .setTextMsg(conferenceMsg);
        return msgBuilder;
    }

    public static Chat.WithMiMsg.Builder makeConferenceStateMsgBuilder(TalkersPublicIdentity ourIdentity, Conversation conference) {
        Chat.ChatStateMsg.Builder conferenceStateBuilder = Chat.ChatStateMsg.newBuilder();
        List<User> customers = conference.takeCustomers();
        for (int b = 0; b < customers.size(); b++) {
            makeConferenceStateMsgBuilderUtility(conferenceStateBuilder, customers, b);
        }

        if (customers.size() < Conversation.MAX_NUM_OF_USERS) {
            int extraCustomers = Conversation.MAX_NUM_OF_USERS - customers.size();
            Comms.Identity nullIdentityMsg = SerializerUtil.serializeIdentity(Conversation.NULL_IDENTITY);
            for (int k = 0; k < extraCustomers; ) {
                for (; (k < extraCustomers) && (Math.random() < 0.6); k++) {
                    conferenceStateBuilder.addPublicId(nullIdentityMsg);
                }
            }
        }
        // add our identity to the end of this list
        Comms.Identity identityMsg = SerializerUtil.serializeIdentity(ourIdentity);
        conferenceStateBuilder.addPublicId(identityMsg);

        Chat.ChatMsg conferenceMsg = Chat.ChatMsg.newBuilder()
                .setTextMsg(conference.getName())
                .build();

        Chat.WithMiMsg.Builder withMiMsgBuilder = Chat.WithMiMsg.newBuilder()
                .setType(Chat.WithMiMsg.Type.CHAT_STATE)
                .setChatStateMsg(conferenceStateBuilder)
                .setTextMsg(conferenceMsg);
        return withMiMsgBuilder;
    }

    private static void makeConferenceStateMsgBuilderUtility(Chat.ChatStateMsg.Builder conferenceStateBuilder, List<User> customers, int a) {
        User customer = customers.get(a);
        TalkersPublicIdentity identity = customer.fetchIdentity();
        Comms.Identity identityMsg = SerializerUtil.serializeIdentity(identity);
        conferenceStateBuilder.addPublicId(identityMsg);
    }

    public static Chat.WithMiMsg.Builder makeReceipt(boolean handleSuccess, float messageId) {
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
}