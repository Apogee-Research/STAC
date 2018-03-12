package stac.server;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.api.easymock.annotation.MockNice;
import org.powermock.modules.junit4.PowerMockRunner;
import stac.communications.Communications;
import stac.crypto.PrivateKey;
import stac.parser.CommandLine;
import stac.parser.OpenSSLRSAPEM;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

/**
 *
 */
@RunWith(PowerMockRunner.class)
public class ServerTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @MockNice
    CommandLine.Options opts;

    @MockNice
    UserStore userStore;

    @Mock
    Communications communications;

    @Test
    public void testConstructionWithNulls() throws Exception {
        expectedException.expect(RuntimeException.class);
        Server server = new Server(null);
    }

    @Test
    public void testConstruction() throws Exception {
        expectedException.expect(RuntimeException.class);
        Server server = new Server(opts);
    }

    @Test
    public void testRun() throws Exception {
        Server server = new Server(opts, userStore, communications);
        CommandLine.Option option = createNiceMock(CommandLine.Option.class);
        expect(opts.findByLongOption("server-key")).andReturn(option);

        option.getValue();
        expectLastCall().andReturn(ClassLoader.getSystemResource("test.pem").getFile());

        communications.setListenerKey(anyObject(PrivateKey.class));
        expectLastCall().once();

        communications.listen();
        expectLastCall().andReturn(0).once();

        replay(opts, userStore, communications, option);
        server.run();

        verify(communications);
    }

    @Test
    public void testRunWithBadKey() throws Exception {
        expectedException.expect(OpenSSLRSAPEM.DER.MalformedDERException.class);
        Server server = new Server(opts, userStore, communications);
        CommandLine.Option option = createNiceMock(CommandLine.Option.class);
        expect(opts.findByLongOption("server-key")).andReturn(option);

        option.getValue();
        expectLastCall().andReturn(ClassLoader.getSystemResource("test.der").getFile());

        replay(opts, userStore, communications, option);
        server.run();
    }

    @Test
    public void testRunWithMissingKey() throws Exception {
        Server server = new Server(opts, userStore, communications);
        CommandLine.Option option = createNiceMock(CommandLine.Option.class);
        expect(opts.findByLongOption("server-key")).andReturn(option);

        option.getValue();
        expectLastCall().andReturn("nothere.pem");

        replay(opts, userStore, communications, option);
        assertEquals(1, server.run());
    }

}