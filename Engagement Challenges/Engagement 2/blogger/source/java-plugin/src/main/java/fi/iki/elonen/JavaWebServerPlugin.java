package fi.iki.elonen;

/*
 * #%L
 * NanoHttpd-Webserver-Java-Plugin
 * %%
 * Copyright (C) 2015 Raytheon BBN Technologies
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

import fi.iki.elonen.NanoHTTPD.Response.Status;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fi.iki.elonen.NanoHTTPD.IHTTPSession;
import static fi.iki.elonen.NanoHTTPD.MIME_HTML;
import static fi.iki.elonen.NanoHTTPD.MIME_PLAINTEXT;
import static fi.iki.elonen.NanoHTTPD.Response;

/*
 * The web server plugin does two things.
 *
 * First:
 *     It verifies the URL is valid by running the URIVerifier against it.
 *
 * Second:
 *     It finds and loads the java class which matches the verified URL and runs its
 *     render method. Each java class must be a class which implements the RenderingClass
 *     interface.
 */
public class JavaWebServerPlugin implements WebServerPlugin {
    /* LocalStatus is required to prevent circular dependency issues */
    enum LocalStatus implements Response.IStatus {
        FOUND(302, "Found");
        private String desc;
        private int code;
        LocalStatus(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        @Override
        public String getDescription() {
            return desc;
        }
        @Override
        public int getRequestStatus() {
            return code;
        }
    }

    private static final Logger LOG = Logger.getLogger(JavaWebServerPlugin.class.getName());

    /*
     * Ensures that the URI can be served by this plugin.
     * 
     * @param uri
     *            The URI to check.
     * @return True when this plugin can serve this.
     */
    @Override
    public boolean canServeUri(String uri, File rootDir) {
        uri = uri.substring(1).replaceAll("/", ".");
        return uri.isEmpty() || new URIVerifier().verify(uri);
    }

    /*
     * Not used.
     * 
     * @param commandLineOptions
     *            Unused
     */
    @Override
    public void initialize(Map<String, String> commandLineOptions) {
    }

    /*
     * Performs the serve operation for a serviceable URI
     *
     * Any exceptions thrown by this method are caught and responded to by the web server itself.
     *
     * @param uri      The resource to serve.
     * @param headers  The headers of the request, currently unused.
     * @param session  The session of the request, passed to rendering class.
     * @param file     Unused.
     * @param mimeType Unused.
     * @return         A response to the request.
     */
    @Override
    public Response serveFile(String uri, Map<String, String> headers, IHTTPSession session, File file, String mimeType) {
        RenderingClass renderingClass;

        // Determine if we are looking for the index file for the / URL then perform / to . replacement.
        if (uri.equals("/")) {
            uri = "Index";
        } else {
            uri = uri.substring(1).replaceAll("/", ".");
        }

        // Attempt to load the rendering class from the classpath.
        try {
            boolean should404 = true;
            Class<?> aClass = Class.forName(uri);
            Class<?>[] interfaces = aClass.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                if (anInterface.getSimpleName().equals("RenderingClass")) {
                    should404 = false;
                }
            }
            if (should404) {
                throw new ClassNotFoundException();
            } else {
                renderingClass = (RenderingClass) aClass.newInstance();
            }
            LOG.log(Level.INFO, session.getMethod().toString() + ": " + uri );
        } catch (InstantiationException | IllegalAccessException e) {
            String msg = e.getClass().getName() + " Detected in Java Plugin";
            LOG.log(Level.SEVERE, msg, e);
            return new Response(
                    Status.INTERNAL_ERROR, MIME_HTML, new ByteArrayInputStream(msg.getBytes()), msg.length()
            );
        } catch (ClassNotFoundException | ClassCastException e) {
            // ClassCastException is caught to change the response to a 404 instead of a 500
            LOG.log(Level.WARNING, "404 - Not Found: " + session.getUri());
            String msg = "404 - /" + uri + " Not Found";
            return new Response(
                    Status.NOT_FOUND, MIME_HTML, new ByteArrayInputStream(msg.getBytes()), msg.length()
            );
        }

        // Run the renderer.
        JavaPluginResponse render;
        try {
            render = renderingClass.render(session);
        } catch (FileNotFoundException e) {
            LOG.log(Level.WARNING, "404 - Not Found: " + session.getUri());
            String msg = "404 - /" + uri + " Not Found";
            return new Response(
                    Status.NOT_FOUND, MIME_HTML, new ByteArrayInputStream(msg.getBytes()), msg.length()
            );
        } catch (RedirectException e) {
            Response response = new Response(LocalStatus.FOUND, MIME_PLAINTEXT, null, 0);
            response.addHeader("Location", e.to);
            return response;
        }

        byte[] s = render.getBytes();
        return new Response(Status.OK, render.getContentType(), new ByteArrayInputStream(s), s.length);
    }
}
