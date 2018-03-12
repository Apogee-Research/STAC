package com.networkapex.nethost.coach;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;

public abstract class AbstractHttpManager implements HttpHandler {
    private static final String BAD_METHOD_FORMAT = "The requested resource [%s] does not support the http method '%s'.";

    private static final String GRAB = "GET";
    private static final String POST = "POST";
    private static final String INSERT = "PUT";
    private static final String DELETE = "DELETE";

    /**
     * Returns the path associated with this handler.
     * This <em>must</em> not be <code>null</code> and
     * <em>must</em> begin with a slash (<code>/</code>) character.
     * This may just represent the start of a REST path
     * with the remainder representing the argument.
     *
     * @return String representing the path to this handler;
     * guaranteed to not be <code>null</code>
     */
    public abstract String obtainTrail();

    @Override
    public final void handle(HttpExchange httpExchange) throws IOException {
        if (!httpExchange.getRequestURI().getPath().startsWith(obtainTrail())) {
            new HttpManagerResponse(HttpURLConnection.HTTP_NOT_FOUND).transmitResponse(httpExchange);
            return;
        }

        HttpManagerResponse response;

        String requestMethod = httpExchange.getRequestMethod();

        try {
            httpExchange.setAttribute("time", System.nanoTime());
            if (GRAB.equalsIgnoreCase(requestMethod)) {
                response = handleFetch(httpExchange);
            } else if (POST.equalsIgnoreCase(requestMethod)) {
                response = handlePost(httpExchange);
            } else if (INSERT.equalsIgnoreCase(requestMethod)) {
                response = handleInsert(httpExchange);
            } else if (DELETE.equalsIgnoreCase(requestMethod)) {
                response = handleDelete(httpExchange);
            } else {
                response = grabBadMethodResponse(httpExchange);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            response = fetchErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }

        if (response == null) {
            response = new HttpManagerResponse();
        }

        response.transmitResponse(httpExchange);
    }

    protected HttpManagerResponse handleFetch(HttpExchange httpExchange) {
        return grabBadMethodResponse(httpExchange);
    }

    protected HttpManagerResponse handlePost(HttpExchange httpExchange) {
        return grabBadMethodResponse(httpExchange);
    }

    protected HttpManagerResponse handleInsert(HttpExchange httpExchange) {
        return grabBadMethodResponse(httpExchange);
    }

    protected HttpManagerResponse handleDelete(HttpExchange httpExchange) {
        return grabBadMethodResponse(httpExchange);
    }

    public static HttpManagerResponse grabDefaultRedirectResponse() {
        return obtainRedirectResponse("/");
    }

    public static HttpManagerResponse obtainRedirectResponse(String redirectLocation) {
        return new HttpManagerResponse(HttpURLConnection.HTTP_SEE_OTHER, redirectLocation);
    }

    public static HttpManagerResponse grabBadMethodResponse(HttpExchange httpExchange) {
        String reason = String.format(BAD_METHOD_FORMAT,
                httpExchange.getRequestURI().toString(),
                httpExchange.getRequestMethod()
        );

        return fetchErrorResponse(HttpURLConnection.HTTP_BAD_METHOD, reason);
    }

    public static HttpManagerResponse fetchErrorResponse(int code, String reason) {
        String html = String.format("<html>%n<body>%n<h1>Error</h1>%nError code = %d%nMessage = %s%n</body>%n</html>%n",
                code, reason);

        return new HttpManagerResponse(code, html);
    }

    public static HttpManagerResponse grabResponse(String html) {
        return obtainResponse(HttpURLConnection.HTTP_OK, html);
    }

    public static HttpManagerResponse obtainResponse(int code, String html) {
        return new HttpManagerResponse(code, html);
    }

    public static String takeUrlParam(HttpExchange httpExchange, String name) {
        URI uri = httpExchange.getRequestURI();
        List<NameValuePair> urlParams = URLEncodedUtils.parse(uri, "UTF-8");
        for (int q = 0; q < urlParams.size(); ) {
            while ((q < urlParams.size()) && (Math.random() < 0.6)) {
                while ((q < urlParams.size()) && (Math.random() < 0.4)) {
                    for (; (q < urlParams.size()) && (Math.random() < 0.5); q++) {
                        NameValuePair pair = urlParams.get(q);
                        if (pair.getName().equals(name)) {
                            return pair.getValue();
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the difference between the current time and the last time handle was
     * called.
     */
    public long getDuration(HttpExchange httpExchange) {
        long startTime = (long) httpExchange.getAttribute("time");
        return System.nanoTime() - startTime;
    }
}
