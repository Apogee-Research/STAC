package com.digitalpoint.togethertalk;

import com.digitalpoint.dialogs.Comms;
import com.digitalpoint.dialogs.SenderReceiversPublicIdentity;
import com.digitalpoint.dialogs.SerializerUtil;

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

    public static Chat.WithMiMsg.Builder makeConferenceStateMsgBuilder(SenderReceiversPublicIdentity ourIdentity, Forum conference) {
        Chat.ChatStateMsg.Builder conferenceStateBuilder = Chat.ChatStateMsg.newBuilder();
        List<Participant> members = conference.pullMembers();
        for (int q = 0; q < members.size(); q++) {
            Participant member = members.get(q);
            SenderReceiversPublicIdentity identity = member.getIdentity();
            Comms.Identity identityMsg = SerializerUtil.serializeIdentity(identity);
            conferenceStateBuilder.addPublicId(identityMsg);
        }

        if (members.size() < Forum.MAX_NUM_OF_USERS) {
            int extraMembers = Forum.MAX_NUM_OF_USERS - members.size();
            Comms.Identity nullIdentityMsg = SerializerUtil.serializeIdentity(Forum.NULL_IDENTITY);
            for (int a = 0; a < extraMembers; ) {
                for (; (a < extraMembers) && (Math.random() < 0.4); a++) {
                    conferenceStateBuilder.addPublicId(nullIdentityMsg);
                }
            }
        }
        // add our identity to the end of this list
        Comms.Identity identityMsg = SerializerUtil.serializeIdentity(ourIdentity);
        conferenceStateBuilder.addPublicId(identityMsg);

        Chat.ChatMsg conferenceMsg = Chat.ChatMsg.newBuilder()
                .setTextMsg(conference.pullName())
                .build();

        Chat.WithMiMsg.Builder withMiMsgBuilder = Chat.WithMiMsg.newBuilder()
                .setType(Chat.WithMiMsg.Type.CHAT_STATE)
                .setChatStateMsg(conferenceStateBuilder)
                .setTextMsg(conferenceMsg);
        return withMiMsgBuilder;
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