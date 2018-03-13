package stac.client;

import stac.communications.parsers.RequestPacket;
import stac.crypto.PrivateKey;
import stac.crypto.PublicKey;
import stac.util.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

/**
 *
 */
public class Screen {
    private final String TERMINATOR = System.lineSeparator() + "EOM" + System.lineSeparator();
    private PrintStream output;
    private PrintStream error;
    private InputStream input;

    void setOutput(PrintStream output) {
        this.output = output;
    }

    void setError(PrintStream error) {
        this.error = error;
    }

    void setInput(InputStream input) {
        this.input = input;
    }

    public String postMessage(RequestPacket packet) throws IOException {
        printMessage(packet);
        return null;
    }

    public String[] getMessage() throws IOException {
        output.print("Enter the name of the receiver: ");

        ByteArrayOutputStream nameStream = new ByteArrayOutputStream();
        while (true) {
            int read = input.read();
            if (read == System.lineSeparator().charAt(0) || read == -1) {
                break;
            }
            nameStream.write(read);
        }

        output.println("Enter a message (max 4096 bytes) followed by EOM on a blank line:");
        ByteArrayOutputStream messageStream = new ByteArrayOutputStream();
        while (true) {
            int read = input.read();
            if (read == -1) break;
            messageStream.write(read);
            if ((messageStream.toString().length() >= 5 && TERMINATOR.equals(messageStream.toString().substring(messageStream.toString().length() - 5)))) {
                break;
            }
        }

        return new String[] {nameStream.toString(), messageStream.toString().substring(0, messageStream.toString().length() - 4)};
    }

    private void printMessage(RequestPacket packet) {
        StringBuilder b = new StringBuilder();
        messageFromHeader(packet, b);
        messageToHeader(packet, b);
        messageBody(packet, b);

        output.println(b.toString());
    }

    private void messageBody(RequestPacket packet, StringBuilder b) {
        b.append("===== Begin Message =====").append(System.lineSeparator());
        b.append(packet.getMessage()).append(System.lineSeparator());
        b.append("=====  End Message  =====").append(System.lineSeparator());
        b.append("===== Begin Reply Nonce =====").append(System.lineSeparator());
        b.append(Hex.bytesToHex(packet.getNonce().getBytes())).append(System.lineSeparator());
        b.append("=====  End Reply Nonce  =====").append(System.lineSeparator());
    }

    private void messageToHeader(RequestPacket packet, StringBuilder b) {
        PublicKey receiverKey = packet.getReceiverKey();
        byte[] fingerPrint = receiverKey != null ? receiverKey.getFingerPrint() : " < null > ".getBytes();
        b.append("Message is for [");
        b.append(packet.getReceiverName());
        b.append(":");
        b.append(Hex.bytesToHex(fingerPrint, 0, 8));
        b.append("]");
        b.append(System.lineSeparator());
    }

    private void messageFromHeader(RequestPacket packet, StringBuilder b) {
        PrivateKey senderKey = packet.getSenderKey();
        byte[] fingerPrint = senderKey != null ? senderKey.getFingerPrint() : " < null > ".getBytes();
        b.append("Message From [");
        b.append(packet.getSenderName());
        b.append(":");
        b.append(Hex.bytesToHex(fingerPrint, 0, 8));
        b.append("]");
        b.append(System.lineSeparator());
    }

    void postCommandError(String command) {
        if (command != null && command.length() > 0) {
            output.println(command + ": no such command. For help try 'help'");
        }
    }
}
