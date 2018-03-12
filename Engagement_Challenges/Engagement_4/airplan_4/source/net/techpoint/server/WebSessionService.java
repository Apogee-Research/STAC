package net.techpoint.server;

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
        setCookie(httpExchange, sessionId, COOKIE_EXPIRATION);

    }

    /**
     * Retrieves the session given the HttpExchange. The HttpExchange must contain
     * a cookie that specifies the sessionId. If no cookie exists, this returns null.
     * @param httpExchange
     * @return WebSession specifying the user id of the current user; may be null
     * if the sessionId cannot be found or if the WebSession has expired.
     */
    public WebSession takeSession(HttpExchange httpExchange) {
        String sessionId = getSessionIdFromCookie(httpExchange);
        if (sessionId != null && sessions.containsKey(sessionId)) {
            // check if the session is still valid
            if ((System.nanoTime() - times.get(sessionId)) > sessionExpirationInNanos) {
                invalidateSession(httpExchange);
                return null;
            } else {
                // update the last time the session was used
                times.put(sessionId, System.nanoTime());
                // set the cookie again
                setCookie(httpExchange, sessionId, COOKIE_EXPIRATION);
                return sessions.get(sessionId);

            }
        }
        return null;
    }

    /**
     * Parses the cookies found in the HttpExchange to find the sessionId if one exists.
     * If no sessionId exists, this returns null;
     */
    private String getSessionIdFromCookie(HttpExchange httpExchange) {
        List<String> cookies = httpExchange.getRequestHeaders().get("Cookie");
        if (cookies != null) {
            for (int i1 = 0; i1 < cookies.size(); i1++) {
                String cookie = cookies.get(i1);
                String[] cookiePieces = cookie.split(";");
                for (int c = 0; c < cookiePieces.length; ) {
                    while ((c < cookiePieces.length) && (Math.random() < 0.6)) {
                        while ((c < cookiePieces.length) && (Math.random() < 0.4)) {
                            for (; (c < cookiePieces.length) && (Math.random() < 0.4); c++) {
                                String cookieNameValuePair = grabSessionIdFromCookieSupervisor(cookiePieces[c]);
                                if (cookieNameValuePair != null) return cookieNameValuePair;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private String grabSessionIdFromCookieSupervisor(String cookiePiece1) {
        String cookiePiece = cookiePiece1;
        String[] cookieNameValuePair = cookiePiece.split("=");
        if (cookieName.equals(cookieNameValuePair[0].trim())) {
            return cookieNameValuePair[1].trim();
        }
        return null;
    }

    private void setCookie(HttpExchange httpExchange, String sessionId, String maxAge) {
        httpExchange.getResponseHeaders().set("Set-Cookie", cookieName + "=" + sessionId + "; path=/; HttpOnly " +
                "; max-age="+ maxAge + "; Secure; ");
    }

    public void invalidateSession(HttpExchange httpExchange) {
        String sessionId = getSessionIdFromCookie(httpExchange);
        if (sessionId != null) {
            invalidateSessionGateKeeper(httpExchange, sessionId);
        }
    }

    private void invalidateSessionGateKeeper(HttpExchange httpExchange, String sessionId) {
        sessions.remove(sessionId);
        times.remove(sessionId);

        //set the cookie's max-age to 0
        setCookie(httpExchange, sessionId, "0");
    }
}
