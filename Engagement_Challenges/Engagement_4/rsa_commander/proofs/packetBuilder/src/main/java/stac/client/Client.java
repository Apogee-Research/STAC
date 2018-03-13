package stac.client;

import stac.communications.Communications;
import stac.crypto.PrivateKey;
import stac.parser.CommandLine;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class Client {
    private final CommandLine.Options options;
    private Communications communications;
    private Screen screen;

    public Client(CommandLine.Options options) {
        this.options = options;
        this.screen = new Screen();
        this.screen.setError(System.err);
        this.screen.setOutput(System.out);
        this.screen.setInput(System.in);
        this.communications = new Communications(options, screen);
    }

    public int run() {
        PrivateKey clientKey;
        try {
            String client_key = options.findByLongOption("client-key").getValue();
            if (client_key == null) {
                System.err.println("Missing Client key: --help --client-key for more information.");
                return 1;
            } else {
                clientKey = new PrivateKey(new File(client_key));
            }
        } catch (IOException e) {
            System.err.println("**ERROR** Failed to read client-key from: " + options.findByLongOption("client-key"));
            return 1;
        }
        communications.setListenerKey(clientKey);

        ListenerRunner listenerRunner = new ListenerRunner(communications);

        Console console = new Console(communications, screen);
        console.setError(System.err);
        console.setInput(System.in);
        console.setOutput(System.out);

        Thread listenerThread = new Thread(listenerRunner);

        listenerThread.start();

        console.run();

        try {
            listenerThread.join();
        } catch (InterruptedException e) {
            return -1;
        }

        return listenerRunner.getValue();
    }

    private class ListenerRunner implements Runnable {
        private Communications communications;
        private int value = 0;

        private ListenerRunner(Communications completelySetUpAndReadyToListen) {
            communications = completelySetUpAndReadyToListen;
        }

        @Override
        public void run() {
            value = communications.listen();
        }

        public int getValue() {
            return value;
        }
    }
}
