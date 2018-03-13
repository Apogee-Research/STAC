package stac.server;

import stac.communications.Communications;
import stac.crypto.PrivateKey;
import stac.parser.CommandLine;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class Server {
    private final CommandLine.Options options;
    private final Communications communications;
    private final UserStore userStore;
    private final MessageStore messageStore;

    public Server(CommandLine.Options options, UserStore userStore, Communications communications) throws IOException {
        if (options == null) {
            throw new RuntimeException("Options was never passed to the server.");
        }
        this.options = options;
        if (userStore == null) {
            String user_store = options.findByLongOption("user-store").getValue();
            if (user_store == null) {
                throw new CommandLine.ParseHelpfulException("Missing User store: --help --user-store for more information.");
            }
            this.userStore = new UserStore(options);
        } else {
            this.userStore = userStore;
        }

        this.messageStore = new MessageStore(this.userStore);

        if (communications == null) {
            this.communications = new Communications(options, userStore, messageStore);
        } else {
            this.communications = communications;
        }
    }

    public Server(CommandLine.Options options) throws IOException {
        this(options, null, null);
    }

    public int run() {
        PrivateKey serverKey;
        try {
            String server_key = options.findByLongOption("server-key").getValue();
            if (server_key == null) {
                System.err.println("Missing Server key: --help --server-key for more information.");
                return 1;
            } else {
                serverKey = new PrivateKey(new File(server_key));
            }
        } catch (IOException e) {
            System.err.println("**ERROR** Failed to read server-key from: " + options.findByLongOption("server-key"));
            return 1;
        }
        communications.setListenerKey(serverKey);
        return communications.listen();
    }

}
