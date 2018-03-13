package stac.client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import stac.communications.Communications;
import stac.communications.NameGenerator;
import stac.communications.packets.RequestPacket;
import stac.crypto.Key;
import stac.crypto.PrivateKey;
import stac.parser.OpenSSLRSAPEM;

import javax.util.ModifiableByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.regex.Pattern;

import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.expectLastCall;

/**
 *
 */
@RunWith(PowerMockRunner.class)
public class ConsoleTest {

    @Mock
    private Communications communications;

    private Screen screen;

    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;

    private PrintStream output;
    private PrintStream error;
    private ModifiableByteArrayInputStream input;

    private Console console;

    private String name;

    @Before
    public void setUp() throws Exception {
        screen = new Screen();

        outputStream = new ByteArrayOutputStream();
        errorStream = new ByteArrayOutputStream();

        output = new PrintStream(outputStream);
        error = new PrintStream(errorStream);
        input = new ModifiableByteArrayInputStream();

        screen.setOutput(output);
        screen.setInput(input);
        screen.setError(error);

        console = new Console(communications, this.screen);

        communications.stop();
        expectLastCall().anyTimes();

        name = NameGenerator.randomName();


        console.setOutput(output);
        console.setError(error);
        console.setInput(input);
    }

    @Test
    public void testExitCommand() throws Exception {
        input.substitute("exit\n");
        console.run();

        assertEquals("client> ", outputStream.toString());
    }

    @Test
    public void testHelpCommand() throws Exception {
        input.substitute("help\nexit\n");
        console.run();

        assertEquals("client> help : display help.\n" +
                "send <dest address> <port> : send a message to the server/client.\n" +
                "exit : quit the client.\n" +
                "client> ", outputStream.toString());
    }

    @Test
    public void testSend() throws Exception {
        input.substitute("send 127.0.0.1 8080\ntoWhom\nThis is a message\nEOM\nexit\n");
        //---------------------------------------------------------------------^ For the no-reply prompt

        RequestPacket requestPacket = RequestPacket.newMessage((PrivateKey) randomKey(), ((PrivateKey)randomKey()).toPublicKey(), "toWhom", name);

        requestPacket.setCommunications(communications);
        requestPacket.setMessage("This is the response");

        communications.sendMessage("127.0.0.1", "8080", "This is a message\n", "toWhom");

        replay(communications);

        console.run();

        doMatch(outputStream, "client> Enter the name of the receiver \\(max 62 bytes\\): Enter a message \\(max 4096 bytes\\) followed by EOM on a blank line:\n" +
                "client> ");

        verify(communications);
    }

    private void doMatch(ByteArrayOutputStream outputStream, String regex) {
        Pattern matches = Pattern.compile(regex, Pattern.DOTALL);

        System.out.println(outputStream.toString());

        assertTrue(matches.matcher(outputStream.toString()).matches());
    }

    public Key randomKey() throws Exception {
        return new PrivateKey(new OpenSSLRSAPEM(OpenSSLRSAPEM.INTEGER.randomINTEGER().abs(), OpenSSLRSAPEM.INTEGER.randomINTEGER().abs(), OpenSSLRSAPEM.INTEGER.randomINTEGER().abs()));
    }
}