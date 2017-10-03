package fi.iki.elonen;

/*
 * #%L
 * NanoHttpd-Webserver-Java-Plugin
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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Static provides the ability to serve static content from a directory on the classpath.
 * This sits on the classpath and implements rendering class, so it is an example of a
 * working rendering class.
 */
public class Static implements RenderingClass {

    private Logger LOG = Logger.getLogger(Static.class.getName());

    /*
     * Loads and determines the type of a static file on the classpath.
     * Static means no pre-processing is performed before the content is delivered to the
     * user.
     */
    @Override
    public JavaPluginResponse render(NanoHTTPD.IHTTPSession session) throws FileNotFoundException {
        String resource;
        String resourceName = getResourceName(session);

        InputStream res = ClassLoader.getSystemResourceAsStream("static/" + resourceName);
        if (res != null) {
            Scanner scanner = new Scanner(res).useDelimiter("\\A");
            resource = scanner.hasNext() ? scanner.next() : "";
            LOG.info("Loading static/" + resourceName);

            switch (resourceName.substring(resourceName.lastIndexOf('.'), resourceName.length())) {
                case ".css":
                    return new JavaPluginResponse(resource, "text/css");
                case ".js":
                    return new JavaPluginResponse(resource, "application/javascript");
                case ".ico":
                    return new JavaPluginResponse(resource, "image/x-icon");
                case ".png":
                    return new JavaPluginResponse(resource, "image/png");
                default:
                    throw new RuntimeException("Invalid Mime extension");
            }
        } else {
            LOG.warning("404 - static/" + resourceName);
            throw new FileNotFoundException("static/" + resourceName);
        }
    }

    /*
     * retrieves the resource name from an incomming nanoHTTPd session object.
     */
    private String getResourceName(NanoHTTPD.IHTTPSession session) {
        String q = session.getQueryParameterString();
        if (q != null) {
            for (String s : q.split("&")) {
                String[] split = s.split("=");
                if (split.length > 1) {
                    if (split[0].equals("q")) {
                        return split[1];
                    }
                }
            }
        } else {
            throw new RuntimeException("Query string is null");
        }
        throw new RuntimeException("Static could not be loaded");
    }

    /*
     * Returns a URL which is able to retrieve a resource by its path.
     */
    public static String urlFor(String resourcePath) {
        String aClass = Static.class.getCanonicalName();
        return "/" + aClass + "?q=" + resourcePath;
    }
}
