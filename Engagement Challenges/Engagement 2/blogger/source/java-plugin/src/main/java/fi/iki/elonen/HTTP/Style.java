package fi.iki.elonen.HTTP;

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

import fi.iki.elonen.Static;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Style elements are similar to Script elements but the browser loads them into the style
 * sheet when they are loaded instead of executing them. They may also be loaded from static
 * uri or from strings.
 */
public class Style extends HTTPUnit {

    private String resource;
    private boolean resIsStatic = false;

    public Style(String resource) {
        this(resource, false);
    }

    private Style(String resourceUrl, boolean resIsStatic) {
        this.resIsStatic = resIsStatic;
        if (resIsStatic) {
            resource = resourceUrl;
        } else {
            InputStream res = ClassLoader.getSystemResourceAsStream("css/" + resourceUrl);
            if (res != null) {
                Scanner scanner = new Scanner(res).useDelimiter("\\A");
                resource = scanner.hasNext() ? scanner.next() : "";
            } else {
                resource = "";
            }
        }
    }

    @Override
    public String toString() {
        if (resIsStatic) {
            return "<LINK href=\""+resource+"\" rel=\"stylesheet\" type=\"text/css\"/>";
        }
        return "<style>" +
                resource +
                "</style>";
    }

    public static HTTPUnit fromStatic(String urlFor) {
        return new Style(Static.urlFor(urlFor), true);
    }
}
