package com.cyberpointllc.stac.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import jline.console.ConsoleReader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

public class Console {

    private final PrintStream out;

    private final ConsoleReader reader;

    private List<String> history = new  ArrayList<String>();

    private final Map<String, Command> commands = new  TreeMap(String.CASE_INSENSITIVE_ORDER);

    private final CommandLineParser commandLineParser = new  DefaultParser();

    private boolean shouldExit = false;

    public Console(String name) throws IOException {
        this(name, System.in, System.out);
    }

    public Console(String name, InputStream inputStream, PrintStream out) throws IOException {
        if (StringUtils.isBlank(name)) {
            throw new  IllegalArgumentException("Console name may not be null");
        }
        if (inputStream == null) {
            throw new  IllegalArgumentException("InputStream may not be null");
        }
        if (out == null) {
            throw new  IllegalArgumentException("PrintStream may not be null");
        }
        this.out = out;
        reader = new  ConsoleReader(inputStream, out);
        reader.setPrompt(name + "> ");
        reader.addCompleter(new  CommandCompleter(this));
        addCommand(new  ExitCommand(this));
        addCommand(new  HelpCommand(this));
        addCommand(new  RepeatCommand(this));
        addCommand(new  HistoryCommand(this));
        addCommand(new  ScriptCommand(this));
    }

    public void addCommand(Command command) {
        if (command == null) {
            throw new  IllegalArgumentException("Command may not be null");
        }
        commands.put(command.getName(), command);
        if (command.getCompleter() != null) {
            reader.addCompleter(command.getCompleter());
        }
    }

    /**
     * @return a copy of the commands
     */
    public List<Command> getCommands() {
        return new  ArrayList(commands.values());
    }

    public boolean hasCommand(String name) {
        return commands.containsKey(name);
    }

    public Command getCommand(String name) {
        return commands.get(name);
    }

    /**
     * Convenience routine that runs by executing each subsequent command until
     * directed to exit.
     */
    public void execute() throws IOException {
        executeHelper();
    }

    /**
     * Executes the next command entered by the user
     * 
     * @throws IOException
     */
    public void executeNextCommand() throws IOException {
        executeNextCommandHelper();
    }

    /**
     * Executes the command passed in
     *
     * @param line the command and parameters to execute
     * @throws IOException
     */
    public void executeCommand(String line) throws IOException {
        executeCommandHelper(line);
    }

    /**
    * Executes the command passed in
    *
    * @param line the command and parameters to execute
    * @param addToHistory indicates if the line should be added to history
    * @throws IOException
    */
    public void executeCommand(String line, boolean addToHistory) throws IOException {
        if (line == null) {
            // end of file? must have been a console redirect
            // we need to exit no matter what.
            setShouldExit(true);
            return;
        }
        // split the line by spaces, the first item is the command
        String[] items = line.split(" ");
        if (items.length == 0) {
            // no op
            return;
        }
        String name = items[0];
        if (StringUtils.isEmpty(name)) {
            // no op
            return;
        }
        if (hasCommand(name)) {
            executeCommandHelper1(addToHistory, items, name, line);
        } else {
            executeCommandHelper2(name);
        }
    }

    public void setShouldExit(boolean shouldExit) {
        setShouldExitHelper(shouldExit);
    }

    public boolean shouldExit() {
        return shouldExit;
    }

    /**
     * Takes in a file who has a command on each line and executes those
     * commands
     * 
     * @param file
     */
    public void runScript(File file) throws Exception {
        runScriptHelper(file);
    }

    /**
     * 
     * @return a copy of the command history
     */
    public List<String> history() {
        return new  ArrayList<String>(history);
    }

    private void executeHelper() throws IOException {
        while (!shouldExit()) {
            executeNextCommand();
        }
    }

    private void executeNextCommandHelper() throws IOException {
        executeCommand(reader.readLine());
    }

    private void executeCommandHelper(String line) throws IOException {
        executeCommand(line, true);
    }

    private void executeCommandHelper1(boolean addToHistory, String[] items, String name, String line) throws IOException {
        Command command = getCommand(name);
        try {
            CommandLine cmdLine = commandLineParser.parse(command.getOptions(), Arrays.copyOfRange(items, 1, items.length));
            // add command to command history
            if (addToHistory) {
                history.add(line);
            }
            command.execute(out, cmdLine);
        } catch (ParseException e) {
            out.print("Error: " + e.getMessage());
        }
    }

    private void executeCommandHelper2(String name) throws IOException {
        out.println("Invalid command: '" + name + "'");
    }

    private void setShouldExitHelper(boolean shouldExit) {
        this.shouldExit = shouldExit;
    }

    private void runScriptHelper(File file) throws Exception {
        BufferedReader reader = new  BufferedReader(new  FileReader(file));
        String read = reader.readLine();
        while (read != null) {
            read = read.trim();
            // don't execute the line if the line is empty or is a comment
            if (!read.isEmpty() && read.charAt(0) != '#') {
                String[] commandArgs = read.split(" ");
                // don't execute script commands from scripts
                if (!commandArgs[0].equalsIgnoreCase(ScriptCommand.NAME)) {
                    // print the command so the user knows what is being executed
                    System.out.println(read);
                    this.executeCommand(read);
                }
            }
            read = reader.readLine();
        }
    }
}
