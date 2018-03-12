package net.computerpoint.chatroom;

import net.computerpoint.dialogs.Comms;
import net.computerpoint.dialogs.ProtocolsPublicIdentity;

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
    public static Chat.WithMiMsg.Builder formDiscussionMsgBuilder(String line) {
        Chat.ChatMsg discussionMsg = Chat.ChatMsg.newBuilder()
                .setTextMsg(line)
                .build();
        Chat.WithMiMsg.Builder msgBuilder = Chat.WithMiMsg.newBuilder()
                .setType(Chat.WithMiMsg.Type.CHAT)
                .setTextMsg(discussionMsg);
        return msgBuilder;
    }

    public static Chat.WithMiMsg.Builder formDiscussionStateMsgBuilder(ProtocolsPublicIdentity ourIdentity, WithMiChat discussion) {
        Chat.ChatStateMsg.Builder discussionStateBuilder = Chat.ChatStateMsg.newBuilder();
        List<Participant> persons = discussion.fetchPersons();
        for (int i = 0; i < persons.size(); i++) {
            formDiscussionStateMsgBuilderFunction(discussionStateBuilder, persons, i);
        }

        // add our identity to the end of this list
        Comms.Identity identityMsg = ourIdentity.serializeIdentity();
        discussionStateBuilder.addPublicId(identityMsg);

        Chat.ChatMsg discussionMsg = Chat.ChatMsg.newBuilder()
                .setTextMsg(discussion.grabName())
                .build();

        Chat.WithMiMsg.Builder withMiMsgBuilder = Chat.WithMiMsg.newBuilder()
                .setType(Chat.WithMiMsg.Type.CHAT_STATE)
                .setChatStateMsg(discussionStateBuilder)
                .setTextMsg(discussionMsg);
        return withMiMsgBuilder;
    }

    private static void formDiscussionStateMsgBuilderFunction(Chat.ChatStateMsg.Builder discussionStateBuilder, List<Participant> persons, int p) {
        Participant person = persons.get(p);
        ProtocolsPublicIdentity identity = person.fetchIdentity();
        Comms.Identity identityMsg = identity.serializeIdentity();
        discussionStateBuilder.addPublicId(identityMsg);
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
}