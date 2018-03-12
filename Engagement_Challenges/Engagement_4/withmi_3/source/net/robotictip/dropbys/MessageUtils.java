package net.robotictip.dropbys;

/**
 * This class helps create messages
 */
public class MessageUtils {

    /**
     * Creates a basic chat message
     * @param line text message
     * @return message builder
     */
    public static Chat.WithMiMsg.Builder generateDiscussionMsgBuilder(String line) {
        Chat.ChatMsg discussionMsg = Chat.ChatMsg.newBuilder()
                .setTextMsg(line)
                .build();
        Chat.WithMiMsg.Builder msgBuilder = Chat.WithMiMsg.newBuilder()
                .setType(Chat.WithMiMsg.Type.CHAT)
                .setTextMsg(discussionMsg);
        return msgBuilder;
    }

    public static Chat.WithMiMsg.Builder generateReceipt(boolean handleSuccess, float messageId) {
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