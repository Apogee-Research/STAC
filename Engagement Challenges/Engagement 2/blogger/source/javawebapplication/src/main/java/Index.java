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

import fi.iki.elonen.HTTP.Div;
import fi.iki.elonen.HTTP.Document;
import fi.iki.elonen.LoginManager;
import fi.iki.elonen.HTTP.MapTokenResolver;
import fi.iki.elonen.Navigator;
import fi.iki.elonen.HTTP.Raw;
import fi.iki.elonen.HTTP.Script;
import fi.iki.elonen.HTTP.Style;
import fi.iki.elonen.JavaPluginResponse;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.RenderingClass;
import fi.iki.elonen.samples.BlogReader;

/**
 *
 */
public class Index implements RenderingClass {

    @Override
    public JavaPluginResponse render(NanoHTTPD.IHTTPSession session) {
        Document document = new Document("I lost the game");
        int userId = 0;

        NanoHTTPD.CookieHandler cookies = session.getCookies();
        String userIdString = cookies.read("userId");
        if (userIdString == null) {
            userId = LoginManager.newUser();
        } else {
            userId = Integer.valueOf(userIdString);
            if (!LoginManager.hasUser(userId)) {
                userId = LoginManager.newUser();
            }
        }
        cookies.set("userId", String.valueOf(userId), LoginManager.getLoginPeriod());
        
        MapTokenResolver mtr = new MapTokenResolver();
        mtr.put("name", "Blog");
        mtr.put("navigation", Div.fromStatic("navigation", "templates/navigation.html"));
        mtr.put("blog", new BlogReader());

        document.getHead()
                .addUnit(Style.fromStatic("css/bootstrap.css"))
                .addUnit(Style.fromStatic("css/homeStyle.css"))
                .addUnit(Script.fromStatic("js/jquery.js"))
                .addUnit(Script.fromStatic("js/bootstrap.js"))
                .addUnit(new Raw("<link rel=\"apple-touch-icon\" sizes=\"57x57\" href=\"/fi.iki.elonen.Static?q=favicon/apple-touch-icon-57x57.png\">\n" +
                        "        <link rel=\"apple-touch-icon\" sizes=\"60x60\" href=\"/fi.iki.elonen.Static?q=favicon/apple-touch-icon-60x60.png\">\n" +
                        "        <link rel=\"apple-touch-icon\" sizes=\"72x72\" href=\"/fi.iki.elonen.Static?q=favicon/apple-touch-icon-72x72.png\">\n" +
                        "        <link rel=\"apple-touch-icon\" sizes=\"76x76\" href=\"/fi.iki.elonen.Static?q=favicon/apple-touch-icon-76x76.png\">\n" +
                        "        <link rel=\"apple-touch-icon\" sizes=\"114x114\" href=\"/fi.iki.elonen.Static?q=favicon/apple-touch-icon-114x114.png\">\n" +
                        "        <link rel=\"apple-touch-icon\" sizes=\"120x120\" href=\"/fi.iki.elonen.Static?q=favicon/apple-touch-icon-120x120.png\">\n" +
                        "        <link rel=\"apple-touch-icon\" sizes=\"144x144\" href=\"/fi.iki.elonen.Static?q=favicon/apple-touch-icon-144x144.png\">\n" +
                        "        <link rel=\"apple-touch-icon\" sizes=\"152x152\" href=\"/fi.iki.elonen.Static?q=favicon/apple-touch-icon-152x152.png\">\n" +
                        "        <link rel=\"apple-touch-icon\" sizes=\"180x180\" href=\"/fi.iki.elonen.Static?q=favicon/apple-touch-icon-180x180.png\">\n" +
                        "        <link rel=\"icon\" type=\"image/png\" href=\"/fi.iki.elonen.Static?q=favicon/favicon-32x32.png\" sizes=\"32x32\">\n" +
                        "        <link rel=\"icon\" type=\"image/png\" href=\"/fi.iki.elonen.Static?q=favicon/android-chrome-192x192.png\" sizes=\"192x192\">\n" +
                        "        <link rel=\"icon\" type=\"image/png\" href=\"/fi.iki.elonen.Static?q=favicon/favicon-96x96.png\" sizes=\"96x96\">\n" +
                        "        <link rel=\"icon\" type=\"image/png\" href=\"/fi.iki.elonen.Static?q=favicon/favicon-16x16.png\" sizes=\"16x16\">\n" +
                        "        <link rel=\"manifest\" href=\"/fi.iki.elonen.Static?q=favicon/manifest.json\">\n" +
                        "        <link rel=\"shortcut icon\" href=\"/fi.iki.elonen.Static?q=favicon/favicon.ico\">\n" +
                        "        <meta name=\"msapplication-TileColor\" content=\"#da532c\">\n" +
                        "        <meta name=\"msapplication-TileImage\" content=\"/fi.iki.elonen.Static?q=favicon/mstile-144x144.png\">\n" +
                        "        <meta name=\"msapplication-config\" content=\"/fi.iki.elonen.Static?q=favicon/browserconfig.xml\">\n" +
                        "        <meta name=\"theme-color\" content=\"#ffffff\">"));

        document.getBody()
                .addUnit(new Navigator(userId))
                .addUnit(Div.fromStatic("rootContainer", "templates/root.html", mtr));

        return document.toJavaPluginResponse();
    }
}
