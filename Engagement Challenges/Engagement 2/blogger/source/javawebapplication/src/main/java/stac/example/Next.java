package stac.example;

/*
 * #%L
 * NanoHttpd-JavaWebApplication
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

import fi.iki.elonen.JavaPluginResponse;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.RenderingClass;

import java.io.IOException;
import java.util.TreeMap;

/**
 *
 */
public class Next implements RenderingClass {
    @Override
    public JavaPluginResponse render(NanoHTTPD.IHTTPSession session) {
        if(session.getMethod() == NanoHTTPD.Method.POST) {
            TreeMap<String, String> files = new TreeMap<>();
            try {
                session.parseBody(files);
            } catch (IOException | NanoHTTPD.ResponseException e) {
                throw new RuntimeException(e);
            }
            String input = files.get("postData");

            if (input.equals("Initial")) {
                return new JavaPluginResponse("You", "text/plain");
            } else if (input.equals("You")) {
                return new JavaPluginResponse("Lost", "text/plain");
            } else if (input.equals("Lost")) {
                return new JavaPluginResponse("The", "text/plain");
            } else if (input.equals("The")) {
                return new JavaPluginResponse("Game", "text/plain");
            }

            return new JavaPluginResponse("Halt", "text/plain");
        }
        else throw new RuntimeException("Something is broken");
    }
}
