package fi.iki.elonen;

/*
 * #%L
 * nanohttpd-javawebserver
 * %%
 * Copyright (C) 2012 - 2015 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * JavaWebServer is glue code for starting the nanoHTTPd server with a java plugin.
 */
public class JavaWebServer extends NanoHTTPD {
    public static final String INTERNAL_SERVER_ERROR = "500 - Internal Server Error";

    public static final String METHOD_NOT_ALLOWED = "405 - Method Not Allowed";

    public static final String NOT_FOUND = "404 - Not Found";

    private Logger LOG = Logger.getLogger(JavaWebServer.class.getName());

    private WebServerPlugin plugin = new JavaWebServerPluginInfo().getWebServerPlugin(MIME_HTML);

    public static void main(String[] args) {
        ServerRunner.executeInstance(new JavaWebServer(null, 8080));
    }

    public JavaWebServer(int port) {
        super(port);
    }

    public JavaWebServer(String hostname, int port) {
        super(hostname, port);
    }

    /* Serve allows the web server to respond to GET and POST and responds to the user accordingly otherwise.
     * This method then delegates to the java plugin for further processing.
     */
    @Override
    public Response serve(IHTTPSession session) {
        if (plugin.canServeUri(session.getUri(), null)) {
            switch (session.getMethod()) {
                case GET:
                case POST:
                    try {
                        return plugin.serveFile(session.getUri(), session.getHeaders(), session, null, MIME_HTML);
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE, INTERNAL_SERVER_ERROR, e);
                        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_HTML, INTERNAL_SERVER_ERROR);
                    }
                default:
                    LOG.log(Level.SEVERE, METHOD_NOT_ALLOWED);
                    return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, MIME_PLAINTEXT, METHOD_NOT_ALLOWED);
            }
        } else {
            LOG.log(Level.WARNING, NOT_FOUND + ": " + session.getUri());
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, NOT_FOUND + ": " + session.getUri());
        }
    }
}
