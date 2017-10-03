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
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 */
public class Login implements RenderingClass {
    public static boolean doNotDrop;

    @Override
    public JavaPluginResponse render(NanoHTTPD.IHTTPSession session) throws FileNotFoundException, RedirectException {
        if (session.getMethod() == NanoHTTPD.Method.POST) {
            TreeMap<String, String> files = new TreeMap<>();
            try {
                session.parseBody(files);
            } catch (IOException | NanoHTTPD.ResponseException e) {
                throw new RuntimeException(e);
            }
            NanoHTTPD.CookieHandler cookies = session.getCookies();
            String userIdString = cookies.read("userId");
            int userId;
            if (userIdString == null) {
                userId = LoginManager.newUser();
            } else {
                userId = Integer.valueOf(userIdString);
                if (!LoginManager.hasUser(userId)) {
                    userId = LoginManager.newUser();
                }
            }

            Map<String, String> parms = session.getParms();
            if (parms.containsKey("username") &&
                        parms.containsKey("password") &&
                        CredentialManager.insecureCheckPasswd(parms.get("username"), parms.get("password"))) {
                cookies.set("userId", String.valueOf(userId), LoginManager.getLoginPeriod());
                UserLogin user = LoginManager.getUser(userId);
                user.authenticated = true;
                user.userName = parms.get("username");
            } else {
                return new JavaPluginResponse("{ \"message\": \"Invalid Username or Password\" }", "application/json");
            }
            return new JavaPluginResponse("{\"message\": \"Logged In\"}", "application/json");
        }
        throw new RuntimeException("You can't make that request here.");
    }
}
