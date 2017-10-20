package com.cyberpointllc.stac.webserver;

import com.cyberpointllc.stac.auth.KeyExchangeServer;
import com.cyberpointllc.stac.webserver.handler.AbstractHttpHandler;
import com.cyberpointllc.stac.webserver.handler.AuthenticationHandler;
import com.cyberpointllc.stac.webserver.handler.LoginFilter;
import com.cyberpointllc.stac.webserver.handler.LoginHandler;
import com.cyberpointllc.stac.webserver.handler.LogoutHandler;
import com.cyberpointllc.stac.webserver.handler.NoLoginFilter;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsServer;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

public class WebServer {

    private final HttpsServer httpsServer;

    private final WebSessionService webSessionService;

    private final KeyExchangeServer keyExchangeServer;

    private final String passwordKey;

    private Filter loginFilter;

    public WebServer(String appBaseName, int port, InputStream resourceStream, String resourcePassword, File authKeyFile, File passwordKeyFile) throws IOException, GeneralSecurityException {
        httpsServer = WebServerFactory.createServer(port, resourceStream, resourcePassword);
        // session times out after 10 minutes
        webSessionService = new  WebSessionService(appBaseName, 1440L);
        // Read in private key string for auth
        String authKey = FileUtils.readFileToString(authKeyFile);
        keyExchangeServer = new  KeyExchangeServer(authKey);
        // Read in private key used for password storage...
        passwordKey = FileUtils.readFileToString(passwordKeyFile);
    }

    public HttpsServer getServer() {
        return httpsServer;
    }

    public void addDefaultAuthHandlers(UserManager userManager, String userId) throws IOException {
        addDefaultAuthHandlersHelper(userId, userManager);
    }

    public void addAuthHandlers(UserManager userManager, String loginDestinationPath) throws IOException {
        LoginHandler loginHandler = new  LoginHandler(userManager, webSessionService, keyExchangeServer, loginDestinationPath, passwordKey);
        AuthenticationHandler authenticationHandler = new  AuthenticationHandler(loginHandler.getPath());
        LogoutHandler logoutHandler = new  LogoutHandler(webSessionService);
        loginFilter = new  LoginFilter(userManager, webSessionService, authenticationHandler.getPath());
        createContext(loginHandler, false);
        createContext(authenticationHandler, false);
        createContext(logoutHandler, false);
    }

    public WebSessionService getWebSessionService() {
        return webSessionService;
    }

    public void stop(int secondsToWaitToClose) {
        httpsServer.stop(secondsToWaitToClose);
    }

    public void start() {
        startHelper();
    }

    public HttpContext createContext(AbstractHttpHandler handler, boolean requireAuth) {
        HttpContext context = httpsServer.createContext(handler.getPath(), handler);
        if (requireAuth) {
            context.getFilters().add(loginFilter);
        }
        return context;
    }

    private void addDefaultAuthHandlersHelper(String userId, UserManager userManager) throws IOException {
        loginFilter = new  NoLoginFilter(userManager, webSessionService, userId);
    }

    private void startHelper() {
        httpsServer.start();
    }
}
