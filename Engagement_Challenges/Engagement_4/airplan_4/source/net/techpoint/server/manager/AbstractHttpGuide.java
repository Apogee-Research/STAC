package net.techpoint.server.manager;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;

public abstract class AbstractHttpGuide implements HttpHandler {
    private static final String BAD_METHOD_FORMAT = "The requested resource [%s] does not support the http method '%s'.";

    private static final String FETCH = "GET";
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
            new HttpGuideResponse(HttpURLConnection.HTTP_NOT_FOUND).sendResponse(httpExchange);
            return;
        }

        HttpGuideResponse response;

        String requestMethod = httpExchange.getRequestMethod();

        try {
            httpExchange.setAttribute("time", System.nanoTime());
            if (FETCH.equalsIgnoreCase(requestMethod)) {
                response = handleGrab(httpExchange);
            } else if (POST.equalsIgnoreCase(requestMethod)) {
                response = handlePost(httpExchange);
            } else if (INSERT.equalsIgnoreCase(requestMethod)) {
                response = handleInsert(httpExchange);
            } else if (DELETE.equalsIgnoreCase(requestMethod)) {
                response = handleDelete(httpExchange);
            } else {
                response = fetchBadMethodResponse(httpExchange);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            response = getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }

        if (response == null) {
            response = new HttpGuideResponse();
        }

        response.sendResponse(httpExchange);
    }

    protected HttpGuideResponse handleGrab(HttpExchange httpExchange) {
        return fetchBadMethodResponse(httpExchange);
    }

    protected HttpGuideResponse handlePost(HttpExchange httpExchange) {
        return fetchBadMethodResponse(httpExchange);
    }

    protected HttpGuideResponse handleInsert(HttpExchange httpExchange) {
        return fetchBadMethodResponse(httpExchange);
    }

    protected HttpGuideResponse handleDelete(HttpExchange httpExchange) {
        return fetchBadMethodResponse(httpExchange);
    }

    public static HttpGuideResponse takeDefaultRedirectResponse() {
        return getRedirectResponse("/");
    }

    public static HttpGuideResponse getRedirectResponse(String redirectLocation) {
        return new HttpGuideResponse(HttpURLConnection.HTTP_SEE_OTHER, redirectLocation);
    }

    public static HttpGuideResponse fetchBadMethodResponse(HttpExchange httpExchange) {
        String reason = String.format(BAD_METHOD_FORMAT,
                httpExchange.getRequestURI().toString(),
                httpExchange.getRequestMethod()
        );

        return getErrorResponse(HttpURLConnection.HTTP_BAD_METHOD, reason);
    }

    public static HttpGuideResponse getErrorResponse(int code, String reason) {
        String html = String.format("<html>%n<body>%n<h1>Error</h1>%nError code = %d%nMessage = %s%n</body>%n</html>%n",
                code, reason);

        return new HttpGuideResponse(code, html);
    }

    public static HttpGuideResponse getResponse(String html) {
        return obtainResponse(HttpURLConnection.HTTP_OK, html);
    }

    public static HttpGuideResponse obtainResponse(int code, String html) {
        return new HttpGuideResponse(code, html);
    }

    public static String pullUrlParam(HttpExchange httpExchange, String name) {
        URI uri = httpExchange.getRequestURI();
        List<NameValuePair> urlParams = URLEncodedUtils.parse(uri, "UTF-8");
        for (int k = 0; k < urlParams.size(); k++) {
            String pair = fetchUrlParamGuide(name, urlParams, k);
            if (pair != null) return pair;
        }
        return null;
    }

    private static String fetchUrlParamGuide(String name, List<NameValuePair> urlParams, int b) {
        NameValuePair pair = urlParams.get(b);
        if (pair.getName().equals(name)) {
            return pair.getValue();
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
