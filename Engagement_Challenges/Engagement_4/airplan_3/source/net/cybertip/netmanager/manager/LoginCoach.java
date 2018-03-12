package net.cybertip.netmanager.manager;

import net.cybertip.auth.KeyExchangeServer;
import net.cybertip.DESHelper;
import net.cybertip.netmanager.WebSessionBuilder;
import net.cybertip.template.TemplateEngine;
import net.cybertip.netmanager.Member;
import net.cybertip.netmanager.MemberOverseer;
import net.cybertip.netmanager.WebSession;
import net.cybertip.netmanager.WebSessionService;
import net.cybertip.netmanager.WebTemplate;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LoginCoach extends AbstractHttpCoach {
    private static final String PATH = "/login";
    private static final String TITLE = "Login";
    private static final String USERNAME_FIELD = "username";
    private static final String PASSWORD_FIELD = "password";

    private static final String AUTH_LOGIN_TEMPLATE_FILE = "logintemplate.html";
    private static final String LOGIN_TEMPLATE_FILE = "simplelogintemplate.html";

    private final MemberOverseer memberOverseer;
    private final WebSessionService webSessionService;
    private final KeyExchangeServer keyExchangeServer;
    private final WebTemplate template;
    private final String destinationPath;
    private final String passwordKey;

    /**
     * Creates a login handler to manage the username and password page.
     * The specified user manager is used to lookup the user.
     * The specified web session service is used to bind the user's
     * session to a client-side cookie.
     * An optional key exchange server is used to add the DH
     * authentication process to the login workflow.
     * The optional destination path indicates where successful logins
     * should be redirected.  If not specified, the default destination
     * is used instead.
     * The specified password key is used encrypt inbound passwords.
     * If this key is <code>null</code>, encryption of passwords is omitted.
     *
     * @param memberOverseer       used to lookup users
     * @param webSessionService used bind users to a session
     * @param keyExchangeServer used to attach authenication to login process;
     *                          may be <code>null</code> to exclude this step
     * @param destinationPath   used to indicate where successful logins go next;
     *                          may be <code>null</code> to indicate default location
     * @param passwordKey       used to encrypt user's passwords;
     *                          may be <code>null</code> if encryption is not needed
     */
    public LoginCoach(MemberOverseer memberOverseer, WebSessionService webSessionService,
                      KeyExchangeServer keyExchangeServer, String destinationPath,
                      String passwordKey) {
        this.memberOverseer = Objects.requireNonNull(memberOverseer, "UserManager must be specified");
        this.webSessionService = Objects.requireNonNull(webSessionService, "WebSessionService must be specified");
        this.keyExchangeServer = keyExchangeServer;
        this.template = new WebTemplate((keyExchangeServer != null) ? AUTH_LOGIN_TEMPLATE_FILE : LOGIN_TEMPLATE_FILE, getClass());
        this.destinationPath = destinationPath;
        this.passwordKey = passwordKey;
    }

    @Override
    public String grabPath() {
        return PATH;
    }

    @Override
    protected HttpCoachResponse handleTake(HttpExchange httpExchange) {
        String path = httpExchange.getRequestURI().getPath();
        if (path.startsWith(grabPath())) {
            path = path.substring(grabPath().length());
            // Check for slash after the path
            if ((path.length() > 0) && path.startsWith("/")) {
                path = path.substring(1);
            }
        }

        TemplateEngine templateEngine = template.getEngine();
        Map<String, String> templateMap = new HashMap<>();
        templateMap.put("title", TITLE);
        templateMap.put("path", grabPath());

        if ((keyExchangeServer != null) && (path.length() > 0) && (path.length() < 10000)) {
            // if the path has a length, the user's public key was specified
            BigInteger membersPublicKey;

            try {
                if (path.startsWith("0x")) {
                    membersPublicKey = new BigInteger(path.substring(2), 16);
                } else {
                    membersPublicKey = new BigInteger(path);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Error: key must be hexadecimal or decimal");
            }

            BigInteger masterSecret = keyExchangeServer.generateMasterSecret(membersPublicKey);
            templateMap.put("masterSecret", masterSecret.toString());
        } else {
            // if the path length is 0, the user's public key was not specified
            templateMap.put("masterSecret", "Null");
        }

        String suppressTimeStamp = takeUrlParam(httpExchange, "suppressTimestamp");
        if (StringUtils.isBlank(suppressTimeStamp) || !suppressTimeStamp.equalsIgnoreCase("true")) {
            templateMap.put("duration", String.valueOf(obtainDuration(httpExchange)));
            templateMap.put("timestamp", (new Date()).toString());
        }

        return grabResponse(templateEngine.replaceTags(templateMap));
    }

    @Override
    protected HttpCoachResponse handlePost(HttpExchange httpExchange) {

        Set<String> fieldNames = new HashSet<>();
        fieldNames.add(USERNAME_FIELD);
        fieldNames.add(PASSWORD_FIELD);
        Map<String, List<String>> loginCredentials = MultipartHelper.pullMultipartValues(httpExchange, fieldNames);
        List<String> usernames = loginCredentials.get(USERNAME_FIELD);
        List<String> passwords = loginCredentials.get(PASSWORD_FIELD);

        if ((usernames != null) && (usernames.size() == 1) && (passwords != null) && (passwords.size() == 1)) {
            String username = usernames.get(0);
            String password = passwords.get(0);

            String encryptedPw = password; // Default is not-encrypted
            if (passwordKey != null) {
                // password is stored encrypted, so encrypt this before comparing...
                encryptedPw = DESHelper.takeEncryptedString(password, passwordKey);
            }

            Member currentMember = memberOverseer.getMemberByUsername(username);
            if ((currentMember != null) && currentMember.matches(username, encryptedPw)) {
                return handlePostCoach(httpExchange, currentMember);
            }
        }

        // User didn't enter a correct username/password
        throw new IllegalArgumentException("Invalid username or password (or both)");
    }

    private HttpCoachResponse handlePostCoach(HttpExchange httpExchange, Member currentMember) {
        WebSession webSession = new WebSessionBuilder().assignMemberId(currentMember.takeIdentity()).makeWebSession();
        webSessionService.addSession(httpExchange, webSession);
        if (destinationPath == null) {
            return obtainDefaultRedirectResponse();
        } else {
            return fetchRedirectResponse(destinationPath);
        }
    }
}
