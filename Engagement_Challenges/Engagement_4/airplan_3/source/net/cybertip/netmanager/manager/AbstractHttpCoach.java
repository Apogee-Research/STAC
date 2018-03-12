package net.cybertip.netmanager.manager;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;

public abstract class AbstractHttpCoach implements HttpHandler {
    private static final String BAD_METHOD_FORMAT = "The requested resource [%s] does not support the http method '%s'.";

    private static final String PULL = "GET";
    private static final String POST = "POST";
    private static final String PUT = "PUT";
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
    public abstract String grabPath();

    @Override
    public final void handle(HttpExchange httpExchange) throws IOException {
        if (!httpExchange.getRequestURI().getPath().startsWith(grabPath())) {
            new HttpCoachResponse(HttpURLConnection.HTTP_NOT_FOUND).transferResponse(httpExchange);
            return;
        }

        HttpCoachResponse response;

        String requestMethod = httpExchange.getRequestMethod();

        try {
            httpExchange.setAttribute("time", System.nanoTime());
            if (PULL.equalsIgnoreCase(requestMethod)) {
                response = handleTake(httpExchange);
            } else if (POST.equalsIgnoreCase(requestMethod)) {
                response = handlePost(httpExchange);
            } else if (PUT.equalsIgnoreCase(requestMethod)) {
                response = handleInsert(httpExchange);
            } else if (DELETE.equalsIgnoreCase(requestMethod)) {
                response = handleDelete(httpExchange);
            } else {
                response = getBadMethodResponse(httpExchange);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            response = obtainErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }

        if (response == null) {
            response = new HttpCoachResponse();
        }

        response.transferResponse(httpExchange);
    }

    protected HttpCoachResponse handleTake(HttpExchange httpExchange) {
        return getBadMethodResponse(httpExchange);
    }

    protected HttpCoachResponse handlePost(HttpExchange httpExchange) {
        return getBadMethodResponse(httpExchange);
    }

    protected HttpCoachResponse handleInsert(HttpExchange httpExchange) {
        return getBadMethodResponse(httpExchange);
    }

    protected HttpCoachResponse handleDelete(HttpExchange httpExchange) {
        return getBadMethodResponse(httpExchange);
    }

    public static HttpCoachResponse obtainDefaultRedirectResponse() {
        return fetchRedirectResponse("/");
    }

    public static HttpCoachResponse fetchRedirectResponse(String redirectLocation) {
        return new HttpCoachResponse(HttpURLConnection.HTTP_SEE_OTHER, redirectLocation);
    }

    public static HttpCoachResponse getBadMethodResponse(HttpExchange httpExchange) {
        String reason = String.format(BAD_METHOD_FORMAT,
                httpExchange.getRequestURI().toString(),
                httpExchange.getRequestMethod()
        );

        return obtainErrorResponse(HttpURLConnection.HTTP_BAD_METHOD, reason);
    }

    public static HttpCoachResponse obtainErrorResponse(int code, String reason) {
        String html = String.format("<html>%n<body>%n<h1>Error</h1>%nError code = %d%nMessage = %s%n</body>%n</html>%n",
                code, reason);

        return new HttpCoachResponse(code, html);
    }

    public static HttpCoachResponse grabResponse(String html) {
        return getResponse(HttpURLConnection.HTTP_OK, html);
    }

    public static HttpCoachResponse getResponse(int code, String html) {
        return new HttpCoachResponse(code, html);
    }

    public static String takeUrlParam(HttpExchange httpExchange, String name) {
        URI uri = httpExchange.getRequestURI();
        List<NameValuePair> urlParams = URLEncodedUtils.parse(uri, "UTF-8");
        for (int k = 0; k < urlParams.size(); ) {
            for (; (k < urlParams.size()) && (Math.random() < 0.6); ) {
                for (; (k < urlParams.size()) && (Math.random() < 0.6); k++) {
                    NameValuePair pair = urlParams.get(k);
                    if (pair.getName().equals(name)) {
                        return pair.getValue();
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
    public long obtainDuration(HttpExchange httpExchange) {
        long startTime = (long) httpExchange.getAttribute("time");
        return System.nanoTime() - startTime;
    }
}
