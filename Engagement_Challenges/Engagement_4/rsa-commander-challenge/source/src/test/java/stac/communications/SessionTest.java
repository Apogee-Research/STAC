package stac.communications;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.annotation.MockNice;
import org.powermock.modules.junit4.PowerMockRunner;
import stac.client.Screen;
import stac.communications.handlers.HandshakeHandler;
import stac.communications.handlers.RequestHandler;
import stac.parser.CommandLine;

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
    Screen screen;

    @Test
    public void testConstructionAndDestructionDoNotProduceExceptions() throws Exception {
        Session session = new Session(communications, options, new HandshakeHandler(), new RequestHandler(screen));
        session.destroy();
    }
}