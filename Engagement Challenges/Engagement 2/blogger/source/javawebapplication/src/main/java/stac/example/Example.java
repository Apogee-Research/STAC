package stac.example;/*
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
import fi.iki.elonen.HTTP.MapTokenResolver;
import fi.iki.elonen.HTTP.Script;
import fi.iki.elonen.HTTP.Style;
import fi.iki.elonen.HTTP.Util;
import fi.iki.elonen.JavaPluginResponse;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.RedirectException;
import fi.iki.elonen.RenderingClass;
import fi.iki.elonen.samples.BlogReader;
import fi.iki.elonen.samples.BlogWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Example implements RenderingClass {

    @Override
    public JavaPluginResponse render(NanoHTTPD.IHTTPSession session) throws RedirectException {
        BlogWriter blogWriter = new BlogWriter(BlogReader.blogs);

        if (session.getMethod() == NanoHTTPD.Method.POST) {
            try {
                Map<String, String> files = new HashMap<>();
                session.parseBody(files);
                String q = session.getQueryParameterString();
                Map<String, String> data = Util.parseQueryString(q);
                blogWriter.add(data.get("title"), data.get("author"), data.get("content"));
            } catch (IOException | NanoHTTPD.ResponseException e) {
                throw new RuntimeException(e);
            }
        }

        Document document = new Document("I lost the game");

        MapTokenResolver mtr = new MapTokenResolver();
        mtr.put("name", "Blog");
        mtr.put("navigation", Div.fromStatic("navigation", "templates/navigation.html"));
        mtr.put("blogwrite", blogWriter);

        document.getHead()
                .addUnit(Style.fromStatic("css/bootstrap.css"))
                .addUnit(Style.fromStatic("css/homeStyle.css"))
                .addUnit(Script.fromStatic("js/jquery.js"))
                .addUnit(Script.fromStatic("js/bootstrap.js"));
        document.getBody()
                .addUnit(Div.fromStatic("rootContainer", "templates/ilostthegame.html", mtr))
                .addUnit(new Script("lostTheGame.js"));

        return document.toJavaPluginResponse();
    }


}
