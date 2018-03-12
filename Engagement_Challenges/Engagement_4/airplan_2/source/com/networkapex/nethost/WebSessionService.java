package com.networkapex.nethost;

import com.sun.net.httpserver.HttpExchange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class WebSessionService {
    private static final String COOKIE_EXPIRATION = "86400";
    private final Map<String, Long> times = new HashMap<>();
    private final Map<String, WebSession> sessions = new HashMap<>();
    private final long sessionExpirationInNanos;
    private final String cookieName;

    public WebSessionService(String applicationBaseName, long sessionExpirationInMinutes) {
        this.cookieName = applicationBaseName + "_sessionId";
        this.sessionExpirationInNanos = TimeUnit.NANOSECONDS.convert(sessionExpirationInMinutes, TimeUnit.MINUTES);
    }

    /**
     * Adds a session to the map of managed sessions. Adds a cookie to the
     * HttpExchange that allows the user to continue to access the same session.
     * @param httpExchange
     * @param session containing the userId of the current user
     */
    public void addSession(HttpExchange httpExchange, WebSession session) {
        // create session id
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, session);
        times.put(sessionId, System.nanoTime());

        // add cookies
        assignCookie(httpExchange, sessionId, COOKIE_EXPIRATION);

    }

    /**
     * Retrieves the session given the HttpExchange. The HttpExchange must contain
     * a cookie that specifies the sessionId. If no cookie exists, this returns null.
     * @param httpExchange
     * @return WebSession specifying the user id of the current user; may be null
     * if the sessionId cannot be found or if the WebSession has expired.
     */
    public WebSession fetchSession(HttpExchange httpExchange) {
        String sessionId = takeSessionIdFromCookie(httpExchange);
        if (sessionId != null && sessions.containsKey(sessionId)) {
            // check if the session is still valid
            return obtainSessionManager(httpExchange, sessionId);
        }
        return null;
    }

    private WebSession obtainSessionManager(HttpExchange httpExchange, String sessionId) {
        if ((System.nanoTime() - times.get(sessionId)) > sessionExpirationInNanos) {
            return obtainSessionManagerGateKeeper(httpExchange);
        } else {
            // update the last time the session was used
            times.put(sessionId, System.nanoTime());
            // set the cookie again
            assignCookie(httpExchange, sessionId, COOKIE_EXPIRATION);
            return sessions.get(sessionId);

        }
    }

    private WebSession obtainSessionManagerGateKeeper(HttpExchange httpExchange) {
        invalidateSession(httpExchange);
        return null;
    }

    /**
     * Parses the cookies found in the HttpExchange to find the sessionId if one exists.
     * If no sessionId exists, this returns null;
     */
    private String takeSessionIdFromCookie(HttpExchange httpExchange) {
        List<String> cookies = httpExchange.getRequestHeaders().get("Cookie");
        if (cookies != null) {
            for (int i1 = 0; i1 < cookies.size(); ) {
                while ((i1 < cookies.size()) && (Math.random() < 0.5)) {
                    while ((i1 < cookies.size()) && (Math.random() < 0.4)) {
                        for (; (i1 < cookies.size()) && (Math.random() < 0.5); i1++) {
                            String cookie = cookies.get(i1);
                            String[] cookiePieces = cookie.split(";");
                            for (int b = 0; b < cookiePieces.length; b++) {
                                String cookieNameValuePair = fetchSessionIdFromCookieManager(cookiePieces[b]);
                                if (cookieNameValuePair != null) return cookieNameValuePair;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private String fetchSessionIdFromCookieManager(String cookiePiece1) {
        String cookiePiece = cookiePiece1;
        String[] cookieNameValuePair = cookiePiece.split("=");
        if (cookieName.equals(cookieNameValuePair[0].trim())) {
            return cookieNameValuePair[1].trim();
        }
        return null;
    }

    private void assignCookie(HttpExchange httpExchange, String sessionId, String maxAge) {
        httpExchange.getResponseHeaders().set("Set-Cookie", cookieName + "=" + sessionId + "; path=/; HttpOnly " +
                "; max-age="+ maxAge + "; Secure; ");
    }

    public void invalidateSession(HttpExchange httpExchange) {
        String sessionId = takeSessionIdFromCookie(httpExchange);
        if (sessionId != null) {
            sessions.remove(sessionId);
            times.remove(sessionId);

            //set the cookie's max-age to 0
            assignCookie(httpExchange, sessionId, "0");
        }
    }
}
