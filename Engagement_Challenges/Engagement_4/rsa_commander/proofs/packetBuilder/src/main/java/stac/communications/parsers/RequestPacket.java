package stac.communications.parsers;

import stac.communications.Communications;
import stac.communications.PACKETS;
import stac.communications.Packet;
import stac.communications.PacketParser;
import stac.communications.Session;
import stac.communications.packets.RequestPacketParser;
import stac.crypto.PrivateKey;
import stac.crypto.PublicKey;
import stac.parser.OpenSSLRSAPEM;

/**
 *
 */
public class RequestPacket extends Packet {
    private RequestType type;
    private String message;
    private String senderName;
    private String receiverName;
    private PrivateKey senderKey;
    private PublicKey receiverKey;
    private OpenSSLRSAPEM.INTEGER nonce;
    private Communications communications;

    RequestPacket() {
    }

    public static RequestPacket newRawRequestPacket() {
        return new RequestPacket();
    }

    public static RequestPacket newRawRequestPacket(Session session) {
        RequestPacket requestPacket = new RequestPacket();
        requestPacket.setCommunications(session.getCommunications());
        return requestPacket;
    }

    @Override
    public PacketParser getParser() {
        return new RequestPacketParser(this);
    }

    public RequestPacket setType(RequestType type) {
        this.type = type;
        return this;
    }

    public RequestType getType() {
        if (type == null) throw new RuntimeException("RequestPacket has invalid name.");
        return type;
    }

    public String getMessage() {
        return message;
    }

    public RequestPacket setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getSenderName() {
        return senderName;
    }

    public RequestPacket setSenderName(String senderName) {
        this.senderName = senderName;
        return this;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public RequestPacket setReceiverName(String receiverName) {
        this.receiverName = receiverName;
        return this;
    }

    public static RequestPacket newMessage(PrivateKey senderKey, PublicKey recieverKey, String name, String receiverName) {
        RequestPacket requestPacket = new RequestPacket().setType(RequestType.Message).setSenderKey(senderKey).setReceiverKey(recieverKey);
        requestPacket.setNonce();
        requestPacket.setSenderName(name);
        requestPacket.setReceiverName(receiverName);
        return requestPacket;
    }

    private RequestPacket setNonce() {
        this.nonce = OpenSSLRSAPEM.INTEGER.randomLong().abs();
        return this;
    }

    public RequestPacket setNonce(OpenSSLRSAPEM.INTEGER oldNonce) {
        this.nonce = oldNonce.duplicate();
        this.nonce.add(1);
        if (this.nonce.compareTo(Long.MAX_VALUE) == 0 || this.nonce.compareTo(0) < 0) {
            this.nonce = OpenSSLRSAPEM.INTEGER.randomLong().abs();
        }
        return this;
    }

    public static RequestPacket newReply(PrivateKey senderKey, OpenSSLRSAPEM.INTEGER nonce) {
        RequestPacket requestPacket = new RequestPacket().setType(RequestType.Message).setSenderKey(senderKey);
        requestPacket.setNonce(nonce);
        return requestPacket;
    }

    public static RequestPacket newRelay(PrivateKey senderKey, PublicKey publicKey) {
        RequestPacket requestPacket = new RequestPacket().setType(RequestType.MessageRelay).setSenderKey(senderKey).setReceiverKey(publicKey);
        requestPacket.setNonce();
        return requestPacket;
    }

    public RequestPacket setSenderKey(PrivateKey senderKey) {
        this.senderKey = senderKey;
        return this;
    }

    public PrivateKey getSenderKey() {
        return senderKey;
    }

    public RequestPacket setReceiverKey(PublicKey receiverKey) {
        this.receiverKey = receiverKey;
        return this;
    }

    public PublicKey getReceiverKey() {
        return receiverKey;
    }

    public OpenSSLRSAPEM.INTEGER getNonce() {
        return nonce;
    }

    public Communications getCommunications() {
        return communications;
    }

    public void setCommunications(Communications communications) {
        this.communications = communications;
    }

    public static RequestPacket newReply(PrivateKey listenerKey, RequestPacket packet, String replyMessage) {
        RequestPacket requestPacket = new RequestPacket().setType(RequestType.Message);
        requestPacket.senderKey = listenerKey;
        requestPacket.nonce = packet.getNonce(); // This is why this section is performed this way. setNonce modifies the nonce on the way through
        requestPacket.message = replyMessage;
        requestPacket.senderName = packet.getReceiverName();
        requestPacket.receiverName = packet.getSenderName();
        requestPacket.communications = packet.getCommunications();
        requestPacket.receiverKey = packet.getSenderKey().toPublicKey();
        return requestPacket;
    }

    public static RequestPacket newTermination(PrivateKey listenerKey, PublicKey key, String name, String receiverName) {
        RequestPacket requestPacket = newMessage(listenerKey, key, name, receiverName);
        requestPacket.setType(RequestType.Terminate);
        requestPacket.setMessage("");
        return requestPacket;
    }

    public enum RequestType {
        Message, MessageRelay, RetrieveMessages, Terminate;

        public PACKETS toPacketType() {
            switch (this) {
                case Message:
                    return PACKETS.REQUEST_MESSAGE;
                case MessageRelay:
                    return PACKETS.REQUEST_MESSAGE_RELAY;
                case RetrieveMessages:
                    return PACKETS.RETRIEVE_MESSAGES;
                case Terminate:
                    return PACKETS.REQUEST_TERMINATE;
                default:
                    throw new RuntimeException("Failed to convert message type.");
            }
        }
    }
}
