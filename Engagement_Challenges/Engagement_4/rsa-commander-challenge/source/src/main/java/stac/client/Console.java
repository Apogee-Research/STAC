package stac.client;

import stac.communications.Communications;
import stac.communications.PacketParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * This class understands how to read and write data to the terminal that
 * the user is sitting on. This class is used to display prompts, errors,
 * and read commands from the user of the client (ie to send a message).
 */
public class Console implements Runnable {
    private final Communications communications;
    private final Screen screen;

    private InputStream input;
    private PrintStream output;
    private PrintStream error;

    Console(Communications communications, Screen screen) {
        this.communications = communications;
        this.screen = screen;
        this.screen.registerRedrawHandler(this);
    }

    public InputStream getInput() {
        return input;
    }

    void setInput(InputStream input) {
        this.input = input;
    }

    public OutputStream getOutput() {
        return output;
    }

    void setOutput(PrintStream output) {
        this.output = output;
    }

    public OutputStream getError() {
        return error;
    }

    void setError(PrintStream error) {
        this.error = error;
    }

    @Override
    public void run() {
        try {
            boolean running = true;
            do {
                sendPrompt();
                String consoleLine = getUserInput();

                try {
                    running = handleUserInput(consoleLine);
                } catch (Exception e) {
                    if (e instanceof NullPointerException) throw new IOException(e);
                    output.println("That command failed. Type help to see options.");
                    e.printStackTrace(output);
                }
            } while (running);
        } catch (IOException e) {
            output.println("Client parse command failed");
            e.printStackTrace(output);
        }

        communications.stop();
    }

    private boolean handleUserInput(String consoleLine) throws IOException, PacketParserException {
        String[] split = consoleLine.split("\\s+");

        String command = split.length > 0 ? split[0] : "";
        String address = split.length > 1 ? split[1] : "";
        String port = split.length > 2 ? split[2] : "";

        switch (command) {
            case "help":
                displayHelp();
                break;
            case "send":
                String[] message = screen.getMessage();
                communications.sendMessage(
                        address,
                        port,
                        message[1],
                        message[0]
                );
                break;
            case "exit":
                return false;
            default:
                screen.postCommandError(command);
        }
        return true;
    }

    private String getUserInput() throws IOException {
        int c = 0;
        ByteArrayOutputStream build = new ByteArrayOutputStream();

        while (true) {
            c = input.read();
            if (c == System.lineSeparator().charAt(0) || c == -1) {
                break;
            }
            build.write(c);
        }

        return build.toString();
    }

    private void sendPrompt() {
        output.print("client> ");
    }

    private void displayHelp() {
        output.println("help : display help.");
        output.println("send <dest address> <port> : send a message to the server/client.");
        output.println("exit : quit the client.");
    }

    public void redraw() {
        sendPrompt();
    }
}
