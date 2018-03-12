package edu.cyberapex.server;

import edu.cyberapex.authenticate.KeyExchangeServer;
import edu.cyberapex.server.guide.AbstractHttpGuide;
import edu.cyberapex.server.guide.AuthenticationGuide;
import edu.cyberapex.server.guide.LoginFilter;
import edu.cyberapex.server.guide.LoginGuide;
import edu.cyberapex.server.guide.LoginGuideBuilder;
import edu.cyberapex.server.guide.LogoutGuide;
import edu.cyberapex.server.guide.LogoutGuideBuilder;
import edu.cyberapex.server.guide.NoLoginFilter;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsServer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

public class WebServer {
    private static final long DEFAULT_SESSION_TIMEOUT_IN_MINUTES = 1440L;

    private final HttpsServer httpsServer;
    private final WebSessionService webSessionService;
    private final KeyExchangeServer keyExchangeServer;
    private final String passwordKey;
    private static final int SECONDS_TO_WAIT_TO_CLOSE = 0;

    private Filter loginFilter;

    /**
     * Creates an HTTPS web server listening on the specified port.
     * This constructor omits authorization from the login workflow.
     * The specified appBaseName is used to construct a session cookie.
     * Because the server is HTTPS, the specified resource stream and
     * password are needed to create a valid SSL context.  The stream
     * should point to valid Java KeyStore content and the password is
     * used to load the content (the KeyStore password).
     * The optional passwordKeyFile points to a file that contains
     * the password used to encrypt user's passwords.
     * If the passwordKeyFile is <code>null</code>,
     * then passwords are not encrypted before comparison.
     *
     * @param appBaseName      name used to create a session cookie
     * @param port             used for server connections
     * @param resourceStream   InputStream to server KeyStore contents
     * @param resourcePassword password for KeyStore contents
     * @param passwordKeyFile  that contains key for encrypting passwords;
     *                         may be <code>null</code>
     * @throws IOException              if there is trouble creating the server
     * @throws GeneralSecurityException if there is trouble with the resource stream
     */
    public WebServer(String appBaseName,
                     int port,
                     InputStream resourceStream,
                     String resourcePassword,
                     File passwordKeyFile)
            throws IOException, GeneralSecurityException {
        this(appBaseName, port, resourceStream, resourcePassword, passwordKeyFile, null);
    }

    /**
     * Creates an HTTPS web server listening on the specified port.
     * The specified appBaseName is used to construct a session cookie.
     * Because the server is HTTPS, the specified resource stream and
     * password are needed to create a valid SSL context.  The stream
     * should point to valid Java KeyStore content and the password is
     * used to load the content (the KeyStore password).
     * The optional passwordKeyFile points to a file that contains
     * the password used to encrypt user's passwords.
     * If the passwordKeyFile is <code>null</code>,
     * then passwords are not encrypted before comparison.
     * The optional authKeyFile can be specified and points to a file
     * that contains the servers private key.
     * If the authKeyFile is not provided, then the authorization
     * step of logging in is omitted from the workflow.
     *
     * @param appBaseName      name used to create a session cookie
     * @param port             used for server connections
     * @param resourceStream   InputStream to server KeyStore contents
     * @param resourcePassword password for KeyStore contents
     * @param passwordKeyFile  that contains key for encrypting passwords;
     *                         may be <code>null</code>
     * @param authenticateKeyFile      server private key used for authorization;
     *                         may be <code>null</code>
     * @throws IOException              if there is trouble creating the server
     * @throws GeneralSecurityException if there is trouble with the resource stream
     */
    public WebServer(String appBaseName,
                     int port,
                     InputStream resourceStream,
                     String resourcePassword,
                     File passwordKeyFile,
                     File authenticateKeyFile)
            throws IOException, GeneralSecurityException {
        httpsServer = WebServerFactory.createServer(port, resourceStream, resourcePassword);
        // session times out after 10 minutes
        webSessionService = new WebSessionServiceBuilder().fixApplicationBaseName(appBaseName).assignSessionExpirationInMinutes(DEFAULT_SESSION_TIMEOUT_IN_MINUTES).generateWebSessionService();

        // Read in private key used for password storage...
        passwordKey = (passwordKeyFile == null) ? null : FileUtils.readFileToString(passwordKeyFile);

        // Read in private key string for auth
        if (authenticateKeyFile != null) {
            String authenticateKey = FileUtils.readFileToString(authenticateKeyFile);
            keyExchangeServer = new KeyExchangeServer(authenticateKey);
        } else {
            keyExchangeServer = null; // Authorization disabled
        }
    }

    public HttpsServer pullServer() {
        return httpsServer;
    }

    /**
     * Creates the default authorization handlers for the case
     * when there is only one user.
     * In this case, the specified user id is used to assign
     * all uses of this server as belonging to the associated user.
     *
     * @param memberOverseer used to locate the specified user
     * @param memberId      used to identify the active user
     */
    public void addDefaultAuthenticateGuides(MemberOverseer memberOverseer, String memberId) {
        loginFilter = new NoLoginFilter(memberOverseer, webSessionService, memberId);
    }

    /**
     * Adds the necessary handlers used to process the login workflow.
     * If this server has specified the authorization key file,
     * then an authentication page will be the first step visited.
     * Next, the login handler is added to handle username and password
     * processing. Finally, a logout handler is added to manage
     * explicit logout requests.
     * Finally a login filter is added that forces all handlers that
     * require authentication to be in a logged in state.
     * If not, the handler is redirected to the first step.
     * On login success, the user is redirected to the specified
     * login destination path.
     *
     * @param memberOverseer          used to locate valid users
     * @param loginDestinationPath default destination on login success
     */
    public void addAuthenticateGuides(MemberOverseer memberOverseer, String loginDestinationPath) {
        LoginGuide loginGuide = new LoginGuideBuilder().setMemberOverseer(memberOverseer).fixWebSessionService(webSessionService).assignKeyExchangeServer(keyExchangeServer).fixDestinationPath(loginDestinationPath).definePasswordKey(passwordKey).generateLoginGuide();
        generateContext(loginGuide, false);

        LogoutGuide logoutGuide = new LogoutGuideBuilder().fixWebSessionService(webSessionService).generateLogoutGuide();
        generateContext(logoutGuide, false);

        String loginFilterPath = loginGuide.getPath();

        if (keyExchangeServer != null) {
            AuthenticationGuide authenticationGuide = new AuthenticationGuide(loginGuide.getPath());
            generateContext(authenticationGuide, false);
            loginFilterPath = authenticationGuide.getPath();
        }

        loginFilter = new LoginFilter(memberOverseer, webSessionService, loginFilterPath);
    }

    public WebSessionService getWebSessionService() {
        return webSessionService;
    }

    public void stop() {
        httpsServer.stop(SECONDS_TO_WAIT_TO_CLOSE);
    }

    public void stop(int secondsToWaitToClose) {
        httpsServer.stop(secondsToWaitToClose);
    }

    public void start() {
        httpsServer.start();
    }

    public HttpContext generateContext(AbstractHttpGuide guide, boolean requireAuthenticate) {
        HttpContext context = httpsServer.createContext(guide.getPath(), guide);

        if (requireAuthenticate) {
            context.getFilters().add(loginFilter);
        }

        return context;
    }
}
