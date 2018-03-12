package com.techtip.chatbox;

import com.techtip.communications.DialogsDeviation;
import com.techtip.control.Command;
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
    private final DropBy withMi;

    public AddCustomerCommand(DropBy withMi) {
        super(COMMAND, "Adds a known user to the current group chat", USAGE, new AggregateCompleter(new ArgumentCompleter(
                new StringsCompleter(COMMAND), new CustomerCompleterBuilder().assignWithMi(withMi).formCustomerCompleter())));
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> argList = cmd.getArgList();
        if (argList.size() != 1) {
            executeGateKeeper(out);
        } else {
            executeSupervisor(out, argList);
        }
    }

    private void executeSupervisor(PrintStream out, List<String> argList) {
        new AddCustomerCommandGateKeeper(out, argList).invoke();
    }

    private void executeGateKeeper(PrintStream out) {
        new AddCustomerCommandAid(out).invoke();
    }

    private class AddCustomerCommandAid {
        private PrintStream out;

        public AddCustomerCommandAid(PrintStream out) {
            this.out = out;
        }

        public void invoke() {
            out.println(USAGE);
        }
    }

    private class AddCustomerCommandGateKeeper {
        private PrintStream out;
        private List<String> argList;

        public AddCustomerCommandGateKeeper(PrintStream out, List<String> argList) {
            this.out = out;
            this.argList = argList;
        }

        public void invoke() {
            try {
                Forum currentForum = withMi.fetchCurrentForum();

                if (currentForum.canAddMoreCustomers()) {
                    // get a known user
                    String customerName = argList.get(0);
                    WithMiUser customer = withMi.getCustomer(customerName);

                    if (customer == null) {
                        withMi.printCustomerMsg("No user is known with the name " + customerName);
                        return;
                    }

                    if (!withMi.grabCurrentCustomers().contains(customer)) {
                        withMi.printCustomerMsg("Not connected to user " + customerName);
                        return;
                    }

                    // add the user to the chat
                    withMi.addCustomerToForum(currentForum, customer);

                    // create a state change builder
                    Chat.WithMiMsg.Builder withMiMsgBuilder = MessageUtils.formForumStateMsgBuilder(withMi.getMyIdentity(),
                            currentForum);

                    // send it to everyone in the chat, including the new user
                    withMi.transmitMessage(withMiMsgBuilder, currentForum);

                    out.println("Added user to group");

                } else {
                    out.println("You cannot add another user to " + currentForum.obtainName());
                }
            } catch (DialogsDeviation e) {
                out.println("Failed to connect");
            }
        }
    }
}
