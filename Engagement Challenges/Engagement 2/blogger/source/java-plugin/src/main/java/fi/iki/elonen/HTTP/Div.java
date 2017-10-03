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

import java.io.InputStream;
import java.util.Scanner;

/**
 * A Div element for the DOM. Div elements may contain static content which will be
 * loaded before the response is sent.
 */
public class Div extends HTTPUnit {
    private String id;
    private String name;
    private String resource;

    public Div(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private Div(String id, String resourceUrl) {
        this.id = id;
        String name = "static/" + resourceUrl;
        InputStream res = ClassLoader.getSystemResourceAsStream(name);
        if (res != null) {
            Scanner scanner = new Scanner(res).useDelimiter("\\A");
            resource = scanner.hasNext() ? scanner.next() : "";
        } else {
            resource = "";
        }
    }

    private Div(String id, String resourceUrl, ITokenResolver tr) {
        this.id = id;
        String name = "static/" + resourceUrl;
        InputStream res = ClassLoader.getSystemResourceAsStream(name);
        if (res != null) {
            TokenReplacingReader tokenReplacingReader = new TokenReplacingReader(res, tr);
            Scanner scanner = new Scanner(tokenReplacingReader).useDelimiter("\\A");
            resource = scanner.hasNext() ? scanner.next() : "";
        } else {
            resource = "";
        }
    }

    @Override
    public String toString() {
        return "<div id=\"" + id + "\">" + resource + "</div>";
    }

    public static HTTPUnit fromStatic(String id, String urlFor) {
        return new Div(id, urlFor);
    }

    public static HTTPUnit fromStatic(String id, String urlFor, ITokenResolver tr) {
        return new Div(id, urlFor, tr);
    }
}
