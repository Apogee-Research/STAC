package stac.communications.packets;

import stac.communications.Packet;
import stac.communications.PacketParser;
import stac.crypto.Key;
import stac.crypto.PublicKey;

/**
 * This packet is used for all handshake information.
 */
public class HandshakePacket extends Packet {

    private Key key = new PublicKey();
    private Flags flags = new Flags();

    @Override
    public PacketParser getParser() {
        return new HandshakePacketParser(this);
    }

    public Key getKey() {
        return key;
    }

    public Key setKey(Key key) {
        this.key = key;
        return this.key;
    }

    public Flags getFlags() {
        return flags;
    }

    public void setFlags(Flags flags) {
        this.flags = flags;
    }

    /**
     *  Flags
     *   1) Registered Public Key. This is used to determine if the fingerprint can be used to look up the key or not.
     *     Enables:
     *       pSize (public exponent size)
     *       Public Exponent
     *       mSize (modulus size / key size)
     *       Public Key Modulus
     *   2) Server Should Store. This is used to determine if an update to a key is being performed.
     *       In this case the Fingerprint still refers to the old key in order to update it by fingerprint.
     *       ** The server will challenge the update by requesting a challenge to be signed and returned **
     *   3) Requests Return Service. This is used to ask for a public key from the other party.
     *       ** The client will challenge this new public key to ensure that the other user can sign things with it **
     *   4) Handshake Request. This is set to request an ongoing communication.
     *   5) Handshake Accepted. This is set to confirm an open ongoing communication.
     *   6) Handshake Stalled. This is set to allow more key passing and verification without a restart.
     *   7) Handshake Failed. This is set to tell the alternate party of a failure state.
     */
    public static class Flags {

        private boolean registered;
        private boolean shouldStore;
        private boolean requestsReturnService;
        private boolean handshakeRequest;
        private boolean handshakeAccepted;
        private boolean handshakeStalled;
        private boolean handshakeFailed;

        public boolean isRegistered() {
            return registered;
        }

        public Flags setRegistered(boolean registered) {
            this.registered = registered;
            return this;
        }

        public boolean isShouldStore() {
            return shouldStore;
        }

        public Flags setShouldStore(boolean shouldStore) {
            this.shouldStore = shouldStore;
            return this;
        }

        public boolean isRequestsReturnService() {
            return requestsReturnService;
        }

        public Flags setRequestsReturnService(boolean requestsReturnService) {
            this.requestsReturnService = requestsReturnService;
            return this;
        }

        public boolean isHandshakeRequest() {
            return handshakeRequest;
        }

        public Flags setHandshakeRequest(boolean handshakeRequest) {
            this.handshakeRequest = handshakeRequest;
            return this;
        }

        public boolean isHandshakeAccepted() {
            return handshakeAccepted;
        }

        public Flags setHandshakeAccepted(boolean handshakeAccepted) {
            this.handshakeAccepted = handshakeAccepted;
            return this;
        }

        public boolean isHandshakeStalled() {
            return handshakeStalled;
        }

        public Flags setHandshakeStalled(boolean handshakeStalled) {
            this.handshakeStalled = handshakeStalled;
            return this;
        }

        public boolean isHandshakeFailed() {
            return handshakeFailed;
        }

        public Flags setHandshakeFailed(boolean handshakeFailed) {
            this.handshakeFailed = handshakeFailed;
            return this;
        }

        public byte toByte() {
            return (byte) ((isRegistered() ? 1 : 0)
                    | ((isShouldStore() ? 1 : 0) << 1)
                    | ((isRequestsReturnService() ? 1 : 0) << 2)
                    | ((isHandshakeRequest() ? 1 : 0) << 3)
                    | ((isHandshakeAccepted() ? 1 : 0) << 4)
                    | ((isHandshakeStalled() ? 1 : 0) << 5)
                    | ((isHandshakeFailed() ? 1 : 0) << 6)
            );
        }

        public void fromByte(byte b) {
            setRegistered((b & 1) != 0);
            setShouldStore((b & 1 << 1) != 0);
            setRequestsReturnService((b & 1 << 2) != 0);
            setHandshakeRequest((b & 1 << 3) != 0);
            setHandshakeAccepted((b & 1 << 4) != 0);
            setHandshakeStalled((b & 1 << 5) != 0);
            setHandshakeFailed((b & 1 << 6) != 0);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Flags flags = (Flags) o;

            if (isRegistered() != flags.isRegistered()) return false;
            if (isShouldStore() != flags.isShouldStore()) return false;
            if (isRequestsReturnService() != flags.isRequestsReturnService()) return false;
            if (isHandshakeRequest() != flags.isHandshakeRequest()) return false;
            if (isHandshakeAccepted() != flags.isHandshakeAccepted()) return false;
            if (isHandshakeStalled() != flags.isHandshakeStalled()) return false;
            return isHandshakeFailed() == flags.isHandshakeFailed();

        }

        @Override
        public int hashCode() {
            int result = (isRegistered() ? 1 : 0);
            result = 31 * result + (isShouldStore() ? 1 : 0);
            result = 31 * result + (isRequestsReturnService() ? 1 : 0);
            result = 31 * result + (isHandshakeRequest() ? 1 : 0);
            result = 31 * result + (isHandshakeAccepted() ? 1 : 0);
            result = 31 * result + (isHandshakeStalled() ? 1 : 0);
            result = 31 * result + (isHandshakeFailed() ? 1 : 0);
            return result;
        }
    }
}
