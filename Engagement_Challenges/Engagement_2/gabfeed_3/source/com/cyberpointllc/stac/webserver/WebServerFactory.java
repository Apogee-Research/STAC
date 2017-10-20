package com.cyberpointllc.stac.webserver;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * Creates an HTTPS server which can be used to serve things.
 */
public class WebServerFactory {

    public static HttpsServer createServer(int port, InputStream resourceStream, String resourcePassword) throws IOException, GeneralSecurityException {
        SSLContext sslContext = createContext(resourceStream, resourcePassword.toCharArray());
        HttpsServer server = HttpsServer.create(new  InetSocketAddress(port), 0);
        server.setHttpsConfigurator(new  HttpsConfigurator(sslContext) {

            @Override
            public void configure(HttpsParameters params) {
                // we get consistent results
                try {
                    // initialise the SSL context
                    SSLContext c = SSLContext.getDefault();
                    SSLEngine engine = c.createSSLEngine();
                    String[] suites = { "TLS_RSA_WITH_AES_128_CBC_SHA256" };
                    params.setCipherSuites(suites);
                    params.setProtocols(engine.getEnabledProtocols());
                    // get the default parameters
                    SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                    params.setSSLParameters(defaultSSLParameters);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        // Make this single threaded
        server.setExecutor(null);
        return server;
    }

    private static SSLContext createContext(InputStream inputStream, char[] password) throws IOException, GeneralSecurityException {
        // Initialise the keystore
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(inputStream, password);
        // Setup the key manager factory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, password);
        // Setup the HTTPS context and parameters
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, new  SecureRandom());
        return sslContext;
    }
}
