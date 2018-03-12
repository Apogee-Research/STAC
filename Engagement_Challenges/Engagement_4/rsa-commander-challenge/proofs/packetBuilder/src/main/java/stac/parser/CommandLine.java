package stac.parser;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class CommandLine {
    private static final String NL = String.format("%n");
    private final LinkedList<Option> options = new LinkedList<>();
    private final ExceptionBuilder exceptionBuilder;

    public CommandLine(String motd) {
        exceptionBuilder = new ExceptionBuilder(options, motd);
    }

    public OptionBuilder newOption() {
        return new OptionBuilder(options, this);
    }

    public Options parse(String[] args) {
        Options options = new Options(this.options, exceptionBuilder.motd);

        helpBrownout(args, options);

        fulfillOptions(args, options);

        missingOptionsBailout();

        return options;
    }

    private void missingOptionsBailout() {
        for (Option option : this.options) {
            if (option.required && !option.present) {
                String arg = (option.shopt != null)
                        ? "-" + option.shopt
                        : "--" + option.lopt;
                throw new ParseException("Missing required argument. See --help " + arg + " for details.");
            }
        }
    }

    private void fulfillOptions(String[] args, Options options) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            Option option;
            try {
                if (arg.startsWith("--")) {
                    option = options.findByLongOption(arg);
                    if (option.arg) {
                        int beginIndex = arg.indexOf('=') + 1;
                        if (beginIndex > 2 && beginIndex < arg.length()) {
                            option.value = arg.substring(beginIndex);
                            option.present = true;
                        } else {
                            throw new ParseHelpfulException("Parsing option " + arg + " failed (missing =?). For help see --help " + option.lopt);
                        }
                    } else {
                        option.set = true;
                        option.present = true;
                    }
                } else if (arg.startsWith("-")) {
                    option = options.findByShortOption(arg);
                    if (option.arg) {
                        if (i < args.length - 1) {
                            option.value = args[++i];
                            option.present = true;
                        } else {
                            throw new ParseHelpfulException("Parsing option " + arg + " failed. Missing option value");
                        }
                    } else {
                        option.set = true;
                        option.present = true;
                    }
                } else {
                    throw new InvalidOptionException(arg);
                }
            } catch (InvalidOptionException e) {
                throw new ParseHelpfulException("Parsing option " + arg + " failed. Invalid argument.", e);
            }
        }
    }

    private void helpBrownout(String[] args, Options options) {
        if (args.length == 0) return;
        if (args[0].equals("--help") || args[0].equals("-h")) {
            Option state = null;
            try {
                if (args.length == 2) {
                    if (args[1].startsWith("--")) {
                        state = options.findByLongOption(args[1]);
                    } else if (args[1].startsWith("-")) {
                        state = options.findByShortOption(args[1]);
                    }
                }
            } catch (InvalidOptionException e) {
                throw new ParseException("Cannot provide help on option " + args[1] + ". Invalid argument.");
            }
            throwHelp(state);
        }
    }

    public void throwHelp(Option state) {
        throw exceptionBuilder.build(state);
    }

    public static class Options {
        private final List<Option> options;
        private String motd;

        public Options(List<Option> options, String motd) {
            this.options = options;
            this.motd = motd;
        }

        public Option findByLongOption(String longOption) throws InvalidOptionException {
            int endIndex = longOption.indexOf('=');
            longOption = longOption.substring(longOption.startsWith("--") ? 2 : 0, endIndex < 0 ? longOption.length() : endIndex);
            for (Option option : options) {
                if (option.lopt != null && option.lopt.equals(longOption)) {
                    return option;
                }
            }
            throw new InvalidOptionException(longOption);
        }

        public Option findByShortOption(String shortOption) throws InvalidOptionException {
            shortOption = shortOption.substring(shortOption.startsWith("-") ? 1 : 0);
            for (Option option : options) {
                if (option.shopt != null && option.shopt.equals(shortOption)) {
                    return option;
                }
            }
            throw new InvalidOptionException(shortOption);
        }

        public List<Option> getOptions() {
            return options;
        }

        public String getMotd() {
            return motd;
        }
    }

    public static class Option {
        private String shopt = null;
        private String lopt = null;
        private String shdesc = null;
        private String ldesc = null;
        private boolean required = false;
        private boolean arg = false;
        private String value = null;
        private boolean present = false;
        private boolean set;

        public String getValue() {
            return value;
        }

        public boolean isSet() {
            return set;
        }
    }

    public static class OptionBuilder {
        private List<Option> options;
        private Option opt;
        private CommandLine cmdline;

        private OptionBuilder(LinkedList<Option> options, CommandLine commandLine) {
            this.options = options;
            this.cmdline = commandLine;
            this.opt = new Option();
        }

        public CommandLine done() {
            if (opt != null) {
                options.add(opt);
            } else {
                throw new RuntimeException("done() re-called on finalized commandline option.");
            }

            if (opt.lopt == null && opt.shopt == null)
                throw new RuntimeException("done() called on un-finished commandline option.");
            CommandLine c = cmdline;
            this.cmdline = null;
            this.opt = null;
            this.options = null;
            return c;
        }

        public OptionBuilder longDescription(String desc) {
            opt.ldesc = desc;
            return this;
        }

        public OptionBuilder shortDescription(String desc) {
            opt.shdesc = desc;
            return this;
        }

        public OptionBuilder shortOption(String shopt) {
            opt.shopt = shopt;
            return this;
        }

        public OptionBuilder longOption(String lopt) {
            opt.lopt = lopt;
            return this;
        }

        public OptionBuilder required(boolean required) {
            opt.required = required;
            opt.value = null; // force value back to null. Defaults are not valid in terms of required args.
            opt.set = false; // force set back to false. Defaults are not valid in terms of required args.
            return this;
        }

        public OptionBuilder hasValue(boolean arg) {
            return hasValue(arg, null);
        }

        public OptionBuilder hasValue(boolean arg, String defaultValue) {
            if (opt.required && defaultValue != null) {
                throw new IllegalOptionStateException("Required options cannot have default values");
            }
            opt.arg = arg;
            opt.value = defaultValue;
            opt.set = false;
            return this;
        }

        public OptionBuilder hasValue(boolean arg, boolean defaultValue) {
            if (opt.required) {
                throw new IllegalOptionStateException("Required options cannot have default values");
            }
            if (arg && defaultValue) {
                throw new IllegalOptionStateException("Argument options cannot be set by default");
            }
            opt.arg = arg;
            opt.set = false;
            return this;
        }
    }

    private class ExceptionBuilder {
        private final List<Option> options;
        private final String motd;

        public ExceptionBuilder(List<Option> options, String motd) {
            this.options = options;
            this.motd = motd;
        }

        public ParseHelpfulException build(Option state) {
            return new ParseHelpfulException(options, motd, state);
        }
    }

    protected static class IllegalOptionStateException extends RuntimeException {
        private static final long serialVersionUID = -8403076555609965422L;

        /**
         * Constructs a new runtime exception with the specified detail message.
         * The cause is not initialized, and may subsequently be initialized by a
         * call to {@link #initCause}.
         *
         * @param message the detail message. The detail message is saved for
         *                later retrieval by the {@link #getMessage()} method.
         */
        public IllegalOptionStateException(String message) {
            super(message);
        }
    }

    public static class InvalidOptionException extends RuntimeException {
        private static final long serialVersionUID = 8689565177678826574L;

        private final String longOption;

        public InvalidOptionException(String longOption) {
            this.longOption = longOption;
        }

        /**
         * Returns the detail message string of this throwable.
         *
         * @return the detail message string of this {@code Throwable} instance
         * (which may be {@code null}).
         */
        @Override
        public String getMessage() {
            return longOption + ": " + super.getMessage();
        }
    }

    protected class ParseException extends RuntimeException {
        private static final long serialVersionUID = -4612359786474361320L;

        /**
         * Constructs a new runtime exception with the specified detail message.
         * The cause is not initialized, and may subsequently be initialized by a
         * call to {@link #initCause}.
         *
         * @param message the detail message. The detail message is saved for
         *                later retrieval by the {@link #getMessage()} method.
         */
        public ParseException(String message) {
            super(message);
        }

        /**
         * Constructs a new runtime exception with the specified detail message and
         * cause.  <p>Note that the detail message associated with
         * {@code cause} is <i>not</i> automatically incorporated in
         * this runtime exception's detail message.
         *
         * @param message the detail message (which is saved for later retrieval
         *                by the {@link #getMessage()} method).
         * @param cause   the cause (which is saved for later retrieval by the
         *                {@link #getCause()} method).  (A <tt>null</tt> value is
         *                permitted, and indicates that the cause is nonexistent or
         *                unknown.)
         * @since 1.4
         */
        public ParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ParseHelpfulException extends RuntimeException {
        private static final long serialVersionUID = -5017929931944916471L;
        private final String message;

        public ParseHelpfulException(final List<Option> options, final String motd, final Option state) {
            StringBuilder sb = new StringBuilder(motd).append(NL).append(NL).append("Usage:").append(NL);

            if (state == null) {
                for (Option option : options) {
                    String desc = (option.shdesc != null) ?
                            option.shdesc :
                            ((option.ldesc != null) ?
                                    option.ldesc :
                                    "Missing description");
                    Scanner scanner = new Scanner(desc);
                    scanner.useDelimiter("%n");
                    sb.append("-").append(option.shopt != null ? option.shopt : "<undefined>")
                            .append(" --").append(option.lopt != null ? option.lopt : "<undefined>")
                            .append(": ").append(NL);
                    while (scanner.hasNext()) {
                        sb.append('\t').append(scanner.nextLine()).append(NL);
                    }
                    sb.append(NL);
                }
            } else {
                String desc = (state.ldesc != null) ?
                        state.ldesc :
                        ((state.shdesc != null) ?
                                state.shdesc :
                                "Missing description");
                Scanner scanner = new Scanner(desc);
                scanner.useDelimiter("%n");
                sb.append(state.shopt != null ? "-" + state.shopt : "-<undefined>")
                        .append(state.lopt != null ? " --" + state.lopt : " --<undefined>")
                        .append(": ").append(NL);
                while (scanner.hasNext()) {
                    sb.append('\t').append(scanner.nextLine()).append(NL);
                }
                sb.append(NL);
            }
            message = sb.toString();
        }

        public ParseHelpfulException(String s) {
            message = s;
        }

        public ParseHelpfulException(String s, Throwable e) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
            e.printStackTrace(printWriter);

            message = s + System.lineSeparator() + outputStream.toString();
        }

        /**
         * Returns the detail message string of this throwable.
         *
         * @return the detail message string of this {@code Throwable} instance
         * (which may be {@code null}).
         */
        @Override
        public String getMessage() {
            return message;
        }
    }


}
