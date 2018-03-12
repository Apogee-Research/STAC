package stac.communications.packets;

import stac.communications.Communications;
import stac.communications.PACKETS;
import stac.communications.Packet;
import stac.communications.PacketParser;
import stac.communications.Session;
import stac.crypto.Key;
import stac.crypto.PrivateKey;
import stac.crypto.PublicKey;
import stac.parser.OpenSSLRSAPEM;

import java.text.MessageFormat;

/**
 * Requests include messages and termination packets.
 */
public class RequestPacket extends Packet {
    private RequestType type;
    private String message;
    private String senderName;
    private String receiverName;
    private Key senderKey;
    private Key receiverKey;
    private OpenSSLRSAPEM.INTEGER nonce;
    private Communications communications;

    RequestPacket() {
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

    RequestPacket setType(RequestType type) {
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

    RequestPacket setNonce(OpenSSLRSAPEM.INTEGER oldNonce) {
        this.nonce = oldNonce.duplicate();
        this.nonce.add(1);
        if (this.nonce.compareTo(Long.MAX_VALUE) == 0 || this.nonce.compareTo(0) < 0) {
            this.nonce = OpenSSLRSAPEM.INTEGER.valueOf(0).abs();
        }
        return this;
    }

    public static RequestPacket newRelay(PrivateKey senderKey, PublicKey publicKey) {
        RequestPacket requestPacket = new RequestPacket().setType(RequestType.MessageRelay).setSenderKey(senderKey).setReceiverKey(publicKey);
        requestPacket.setNonce();
        return requestPacket;
    }

    public RequestPacket setSenderKey(Key senderKey) {
        this.senderKey = senderKey;
        return this;
    }

    public Key getSenderKey() {
        return senderKey;
    }

    public RequestPacket setReceiverKey(Key receiverKey) {
        this.receiverKey = receiverKey;
        return this;
    }

    public Key getReceiverKey() {
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

    public static RequestPacket newTermination(PrivateKey listenerKey, RequestPacket packet, PublicKey otherEndKey) {
        RequestPacket requestPacket = new RequestPacket().setType(RequestType.Terminate);
        requestPacket.senderKey = listenerKey;
        requestPacket.nonce = packet.getNonce(); // setNonce would increment the nonce on the way through. That is why this section is performed this way.
        requestPacket.message = "";
        requestPacket.senderName = packet.getReceiverName();
        requestPacket.receiverName = packet.getSenderName();
        requestPacket.communications = packet.getCommunications();
        requestPacket.receiverKey = otherEndKey;
        return requestPacket;
    }

    public static RequestPacket newTermination(PrivateKey listenerKey, RequestPacket packet, Session session) {
        return newTermination(listenerKey, packet, session.getRemote().getKey());
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

    @Override
    public String toString() {
        return MessageFormat.format("RequestPacket(" +
                "type: {0}, " +
                "message: {1}, " +
                "senderName: {2}, " +
                "receiverName: {3}, " +
                "senderKey: {4}, " +
                "receiverKey: {5}, " +
                "nonce: {6}, " +
                "communications{7}" +
                ")", type, message, senderName, receiverName, senderKey, receiverKey, nonce, communications);
    }
}
