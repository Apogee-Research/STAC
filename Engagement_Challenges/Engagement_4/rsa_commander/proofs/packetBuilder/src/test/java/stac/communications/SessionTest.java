package stac.communications;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.annotation.MockNice;
import org.powermock.modules.junit4.PowerMockRunner;
import stac.communications.handlers.HandshakeHandler;
import stac.communications.handlers.RequestHandler;
import stac.parser.CommandLine;
import stac.server.MessageStore;
import stac.server.UserStore;

/**
 *
 */
@RunWith(PowerMockRunner.class)
public class SessionTest {

    @MockNice
    CommandLine.Options options;

    @MockNice
    Communications communications;

    @MockNice
    UserStore userStore;

    @MockNice
    MessageStore messageStore;

    @Test
    public void testConstructionAndDestructionDoNotProduceExceptions() throws Exception {
        Session session = new Session(communications, options, userStore, new HandshakeHandler(), new RequestHandler(messageStore));
        session.destroy();
    }
}