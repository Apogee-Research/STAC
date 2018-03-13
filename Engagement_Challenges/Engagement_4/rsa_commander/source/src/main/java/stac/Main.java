package stac;

import stac.client.Client;
import stac.parser.CommandLine;

/**
 *
 */
public class Main {
    private static final String MOTD = "SSHC(d) - A secure shell and chat tool for developers.";

    public static void main(String[] args) {
        CommandLine commandLine = configureCommands();
        try {
            CommandLine.Options options = commandLine.parse(args);
            if (options.findByLongOption("client").isSet()){
                clientMain(options);
            } else {
                commandLine.throwHelp(null);
            }
        } catch ( CommandLine.ParseHelpfulException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void clientMain(CommandLine.Options options) {
        Client client = new Client(options);
        System.exit(client.run());
    }

    private static CommandLine configureCommands() {
        CommandLine commandLine = new CommandLine(MOTD);

        commandLine
                .newOption()
                    .longOption("client").longDescription("Start the RSA client.")
                    .shortOption("c").shortDescription("Start the SSHC client.").hasValue(false, true).done()
                .newOption()
                    .longOption("client-key").longDescription("Client Key. Must be an unencrypted OpenSSL Private Key File.")
                    .shortOption("ckey").shortDescription("Client Key file.").hasValue(true, false).done()
                .newOption()
                    .longOption("user-store").longDescription("Remote Store. Point to the user store file to read users from.")
                    .shortOption("us").shortDescription("Remote Store file.").hasValue(true, false).done()
                .newOption()
                    .longOption("key-store").longDescription("Key Store. Point to the key store file to read keys from.")
                    .shortOption("ks").shortDescription("Key Store file.").hasValue(true, false).done()
                .newOption()
                    .longOption("bind-address").longDescription("The server address to bind to. This defaults to 'localhost'.")
                    .shortOption("b").shortDescription("The server address to bind to.").hasValue(true, "localhost").done()
                .newOption()
                    .longOption("bind-port").longDescription("The server port to bind to.")
                    .shortOption("p").shortDescription("The port the server listen on.").hasValue(true, "8080").done();

        return commandLine;
    }


}