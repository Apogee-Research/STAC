package org.digitaltip.chatroom;

import org.digitaltip.dialogs.TalkersDeviation;
import org.digitaltip.ui.Command;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.StringsCompleter;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;

/**
 * Adds the specified user to the current chat
 */
public class AddCustomerCommand extends Command {
    private static final String COMMAND = "adduser";
    private static final String USAGE = "Usage: adduser <user name>";
    private final HangIn withMi;

    public AddCustomerCommand(HangIn withMi) {
        super(COMMAND, "Adds a known user to the current group chat", USAGE, new AggregateCompleter(new ArgumentCompleter(
                new StringsCompleter(COMMAND), new CustomerCompleter(withMi))));
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> argList = cmd.getArgList();
        if (argList.size() != 1) {
            executeAdviser(out);
        } else {
            try {
                Conversation currentConference = withMi.pullCurrentConference();

                if (currentConference.canAddMoreCustomers()) {
                    // get a known user
                    String customerName = argList.get(0);
                    User customer = withMi.pullCustomer(customerName);

                    if (customer == null) {
                        executeWorker(customerName);
                        return;
                    }

                    if (!withMi.fetchCurrentCustomers().contains(customer)) {
                        withMi.printCustomerMsg("Not connected to user " + customerName);
                        return;
                    }

                    // add the user to the chat
                    withMi.addCustomerToConference(currentConference, customer);

                    // create a state change builder
                    Chat.WithMiMsg.Builder withMiMsgBuilder = MessageUtils.makeConferenceStateMsgBuilder(withMi.obtainMyIdentity(),
                            currentConference);

                    // send it to everyone in the chat, including the new user
                    withMi.transmitMessage(withMiMsgBuilder, currentConference);

                    out.println("Added user to group");

                } else {
                    out.println("You cannot add another user to " + currentConference.getName());
                }
            } catch (TalkersDeviation e) {
                out.println("Failed to connect");
            }
        }
    }

    private void executeWorker(String customerName) {
        withMi.printCustomerMsg("No user is known with the name " + customerName);
        return;
    }

    private void executeAdviser(PrintStream out) {
        out.println(USAGE);
    }
}
