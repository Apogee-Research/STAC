package stac.server;

import org.junit.Before;
import org.junit.Test;
import stac.crypto.PublicKey;
import stac.parser.CommandLine;
import stac.permissions.User;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 *
 */
public class UserStoreTest {
    private UserStore userStore;
    private CommandLine.Options options;

    @Before
    public void setUp() throws Exception {
        CommandLine commandLine = new CommandLine("");
        String userStore = ClassLoader.getSystemResource("userStore").getPath();
        String keyStore = ClassLoader.getSystemResource("test.pem").getPath();
        keyStore = keyStore.substring(0, keyStore.length() - "test.pem".length());
        commandLine.newOption().longOption("user-store").hasValue(true, userStore).done();
        commandLine.newOption().longOption("key-store").hasValue(true, keyStore).done();
        options = commandLine.parse(new String[]{});
        this.userStore = new UserStore(options);
    }

    @Test
    public void findMissingUser() throws Exception {
        PublicKey publicKey = new PublicKey();
        publicKey.setFingerPrint(new byte[]{6,-49,-18,45,-68,69,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,});
        User nonUser = userStore.findUser(publicKey);
        assertNull(nonUser);
    }

    @Test
    public void findUser() throws Exception {
        PublicKey publicKey = new PublicKey();
        publicKey.setFingerPrint(new byte[]{6,-49,-18,45,-68,69,95,29,-100,-5,67,-81,-37,-120,-111,-45,-28,-36,3,51,-80,45,20,-45,18,106,110,-120,1,-20,100,-67});
        User nonUser = userStore.findUser(publicKey);
        assertNotNull(nonUser);
    }

}