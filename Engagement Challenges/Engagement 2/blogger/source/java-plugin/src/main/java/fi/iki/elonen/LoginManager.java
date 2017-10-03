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

import java.util.Date;
import java.util.LinkedHashMap;

/**
 *
 */
public class LoginManager {
    private static LinkedHashMap<Integer, UserLogin> usermap = new LinkedHashMap<>(16, 0.75f, true);
    private static int loginLength = 1209600 /* 14 Days by default */;

    /**
     * Updates the length of a login. It defaults to 14 days (1.2096 Ms)
     *
     * @param seconds Number of seconds that this login remains valid for if not otherwise closed.
     */
    public static void setLoginLength(int seconds) {
        loginLength = seconds;
    }

    public static int getLoginPeriod() {
        return loginLength;
    }

    /**
     * Retrieve the userLogin for the session id (id)
     *
     * @param id Session Id
     * @return The UserLogin object.
     */
    public synchronized static UserLogin getUser(int id) {
        return usermap.get(id);
    }

    /**
     * Creates a new UserLogin for a new user,
     *
     * Purges the User list for expired users.
     *
     * @return The new userId
     */
    public synchronized static int newUser() {
        UserLogin user;
        int i;
        for (i = 0; i < usermap.keySet().size() && (user = usermap.get(i)) != null; i++) {
            if (user.initiation.getTime() + loginLength < new Date().getTime()) {
                usermap.remove(i);
                break;
            }
        }

        usermap.put(i, new UserLogin());
        return i;
    }

    /**
     * Destroys a userLogin object.
     * @param id The user id of the user to terminate
     */
    public synchronized static void terminate(int id) {
        usermap.remove(id);
    }

    public synchronized static boolean hasUser(int userId) {
        return usermap.containsKey(userId);
    }
}
