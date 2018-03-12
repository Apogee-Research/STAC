package stac.parser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 */
public class CommandLineTest {
    private static final String MOTD = "MOTD";
    private static final String BASIC_USAGE_HELP = String.format(MOTD + "%n%nUsage:%n-<undefined> --long-option: %n\tldesc%n%n-so --<undefined>: %n\tsdesc%n%n-las --long-and-short: %n\tlasshortdesc%n%n");
    private static final String SPECIFIC_HELP = MOTD + "%n%nUsage:%n-%s --%s: %n\t%s%n%n";
    private CommandLine commandLine;

    @Before
    public void setUp() throws Exception {
        commandLine = new CommandLine(MOTD);
    }

    /* Don't throw section */
    @Test
    public void testAddingNone() throws Exception {
        CommandLine commandLine = new CommandLine(MOTD);
        CommandLine.Options parse = commandLine.parse(new String[]{});
    }

    @Test
    public void testAddingAllOptions() throws Exception {
        commandLine.newOption().longOption("client").longDescription("ldesc").required(true).hasValue(false).done()
                    .newOption().shortOption("s").shortDescription("shdesc").hasValue(true, "sval").done()
                    .newOption().longOption("server").longDescription("ldesc").shortOption("se").shortDescription("sdesc").required(true).hasValue(true).done();

        CommandLine.Options parse = commandLine.parse(new String[]{"--client", "--server=s"});
    }

    /* Throw section */

    @Rule
    public ExpectedException expect = ExpectedException.none();

    @Test
    public void testHelpBrownout() throws Exception {
        expect.expect(CommandLine.ParseHelpfulException.class);
        commandLine.parse(new String[]{"--help"});
    }

    @Test
    public void testHelpBrownoutMessage() throws Exception {
        commandLine.newOption().longOption("long-option").longDescription("ldesc").done()
                .newOption().shortOption("so").shortDescription("sdesc").done()
                .newOption().longOption("long-and-short").shortOption("las").longDescription("laslongdesc").shortDescription("lasshortdesc").done();
        try {
            commandLine.parse(new String[]{"--help"});
        } catch (CommandLine.ParseHelpfulException e) {
            Assert.assertEquals(BASIC_USAGE_HELP, e.getMessage());
        }

        try {
            commandLine.parse(new String[]{"-h"});
        } catch (CommandLine.ParseHelpfulException e) {
            Assert.assertEquals(BASIC_USAGE_HELP, e.getMessage());
            return;
        }

        Assert.fail();
    }

    @Test
    public void testHelpBrownoutMessageLocalized() throws Exception {
        commandLine.newOption().longOption("long-option").longDescription("ldesc").done()
                .newOption().shortOption("so").shortDescription("sdesc").done()
                .newOption().longOption("long-and-short").shortOption("las").longDescription("laslongdesc").shortDescription("lasshortdesc").done();

        try {
            commandLine.parse(new String[]{"--help", "--long-option"});
        } catch (CommandLine.ParseHelpfulException e) {
            Assert.assertEquals(String.format(SPECIFIC_HELP, "<undefined>", "long-option", "ldesc"), e.getMessage());
        }

        try {
            commandLine.parse(new String[]{"-h", "--long-option"});
        } catch (CommandLine.ParseHelpfulException e) {
            Assert.assertEquals(String.format(SPECIFIC_HELP, "<undefined>", "long-option", "ldesc"), e.getMessage());
        }

        try {
            commandLine.parse(new String[]{"--help", "-so"});
        } catch (CommandLine.ParseHelpfulException e) {
            Assert.assertEquals(String.format(SPECIFIC_HELP, "so", "<undefined>", "sdesc"), e.getMessage());
        }

        try {
            commandLine.parse(new String[]{"-h", "-so"});
        } catch (CommandLine.ParseHelpfulException e) {
            Assert.assertEquals(String.format(SPECIFIC_HELP, "so", "<undefined>", "sdesc"), e.getMessage());
        }

        try {
            commandLine.parse(new String[]{"--help", "-las"});
        } catch (CommandLine.ParseHelpfulException e) {
            Assert.assertEquals(String.format(SPECIFIC_HELP, "las", "long-and-short", "laslongdesc"), e.getMessage());
        }

        try {
            commandLine.parse(new String[]{"-h", "-las"});
        } catch (CommandLine.ParseHelpfulException e) {
            Assert.assertEquals(String.format(SPECIFIC_HELP, "las", "long-and-short", "laslongdesc"), e.getMessage());
        }

        try {
            commandLine.parse(new String[]{"--help", "--long-and-short"});
        } catch (CommandLine.ParseHelpfulException e) {
            Assert.assertEquals(String.format(SPECIFIC_HELP, "las", "long-and-short", "laslongdesc"), e.getMessage());
        }

        try {
            commandLine.parse(new String[]{"-h", "--long-and-short"});
        } catch (CommandLine.ParseHelpfulException e) {
            Assert.assertEquals(String.format(SPECIFIC_HELP, "las", "long-and-short", "laslongdesc"), e.getMessage());
            return;
        }

        Assert.fail();
    }

    @Test
    public void testMissingRequiredVal() throws Exception {
        expect.expect(CommandLine.ParseException.class);
        commandLine.newOption().shortOption("a").hasValue(true).required(true).done()
                .newOption().shortOption("b").hasValue(false).required(true).done();
        commandLine.parse(new String[]{"-a", "-b"});
    }

    @Test
    public void testDontAllowEmptyOptions() throws Exception {
        expect.expectMessage("un-finished");
        commandLine.newOption().done();
    }

    @Test
    public void testDontAllowDuplicateOptions() throws Exception {
        expect.expectMessage("re-called");
        CommandLine.OptionBuilder s = commandLine.newOption().shortOption("s");
        s.done();
        s.done();
    }

    @Test
    public void testBarfOnInvalidOptions() throws Exception {
        expect.expectMessage("Invalid argument.");
        commandLine.newOption().shortOption("s").shortDescription("Try entering something other than s.").done();
        commandLine.parse(new String[]{"-n"});
    }

    @Test
    public void testDontAllowRequiredOptionsWithDefaults() throws Exception {
        expect.expectMessage("Required options cannot have default values");
        commandLine.newOption().shortOption("a").required(true).hasValue(true, "Hi");
    }

    @Test
    public void testDontAllowRequiredOptionsWithDefaults2() throws Exception {
        expect.expectMessage("Required options cannot have default values");
        commandLine.newOption().shortOption("a").required(true).hasValue(false, true);
    }

    @Test
    public void testDontAllowSetOnlyWithArgs() throws Exception {
        expect.expectMessage("Argument options cannot be set by default");
        commandLine.newOption().shortOption("a").hasValue(true, true);
    }

    @Test
    public void testAllowDefaultedArgumentlessOptions() throws Exception {
        commandLine.newOption().shortOption("a").hasValue(false, true);
    }

    @Test
    public void testDontAllowHelpOnBadArgs() throws Exception {
        expect.expectMessage("Cannot provide help on option");
        commandLine.newOption().shortOption("a").required(true);
        commandLine.parse(new String[]{"--help", "-f"});
    }

    @Test
    public void testCannotFindLongOption() throws Exception {
        expect.expect(CommandLine.InvalidOptionException.class);
        commandLine.newOption().shortOption("s").done();
        CommandLine.Options parse = commandLine.parse(new String[]{});

        parse.findByLongOption("--long");
    }

    @Test
    public void testCannotFindShortOption() throws Exception {
        expect.expect(CommandLine.InvalidOptionException.class);
        commandLine.newOption().shortOption("s").done();
        CommandLine.Options parse = commandLine.parse(new String[]{});

        parse.findByShortOption("-g");
    }

    @Test
    public void testArgumentExtractionFailsOnMissingArgLongNoEqual() throws Exception {
        expect.expectMessage("Parsing option");
        commandLine.newOption().longOption("fly").hasValue(true).done();
        commandLine.parse(new String[]{"--fly"});
    }

    @Test
    public void testArgumentExtractionFailsOnMissingArgShort() throws Exception {
        expect.expectMessage("Parsing option");
        commandLine.newOption().shortOption("fly").hasValue(true).done();
        commandLine.parse(new String[]{"-fly"});
    }

    @Test
    public void testArgumentExtractionFailsOnMissingArgLongEqual() throws Exception {
        expect.expectMessage("Parsing option");
        commandLine.newOption().longOption("fly").hasValue(true).done();
        commandLine.parse(new String[]{"--fly="});
    }

    @Test
    public void testArgumentExtraction() throws Exception {
        commandLine.newOption().longOption("fly").shortOption("f").hasValue(true).done()
        .newOption().shortOption("s").longOption("solong").hasValue(true).done()
        .newOption().shortOption("v").longOption("verbose").hasValue(false).done();
        CommandLine.Options parse = commandLine.parse(new String[]{"--fly=fly", "-s", "true", "-v"});

        Assert.assertEquals("fly", parse.findByLongOption("fly").getValue());
        Assert.assertEquals("fly", parse.findByLongOption("--fly").getValue());
        Assert.assertEquals("fly", parse.findByShortOption("f").getValue());
        Assert.assertEquals("fly", parse.findByShortOption("-f").getValue());

        Assert.assertEquals("true", parse.findByShortOption("-s").getValue());
        Assert.assertEquals("true", parse.findByShortOption("s").getValue());
        Assert.assertEquals("true", parse.findByLongOption("--solong").getValue());
        Assert.assertEquals("true", parse.findByLongOption("solong").getValue());

        Assert.assertEquals(true, parse.findByShortOption("-v").isSet());
        Assert.assertEquals(true, parse.findByShortOption("v").isSet());
        Assert.assertEquals(true, parse.findByLongOption("--verbose").isSet());
        Assert.assertEquals(true, parse.findByLongOption("verbose").isSet());

        Assert.assertEquals(null, parse.findByShortOption("-v").getValue());
        Assert.assertEquals(null, parse.findByShortOption("v").getValue());
        Assert.assertEquals(null, parse.findByLongOption("--verbose").getValue());
        Assert.assertEquals(null, parse.findByLongOption("verbose").getValue());
    }

    @Test
    public void testNotAnArgument() throws Exception {
        expect.expectMessage("Parsing option");
        commandLine.parse(new String[]{"thisis-no-option"});
    }
}