package com.roboticcusp.network.coach;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class HttpCoachResponse {
    private static final String CONTENT_TYPE = "text/html; charset=UTF-8";
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private final int code;
    private final String response;

    public HttpCoachResponse() {
        this(HttpURLConnection.HTTP_OK);
    }

    public HttpCoachResponse(int code) {
        this(code, "");
    }

    public HttpCoachResponse(String response) {
        this(HttpURLConnection.HTTP_OK, response);
    }

    public HttpCoachResponse(int code, String response) {
        this.code = code;
        this.response = (response == null) ? "" : response;
    }

    protected String pullContentType() {
        return CONTENT_TYPE;
    }

    protected void addResponseHeaders(HttpExchange httpExchange) throws IOException {
        // Forces caches to obtain a new copy of the page from the server
        httpExchange.getResponseHeaders().set("Cache-Control", "no-cache");

        // Directs caches not to store the page under any circumstance
        httpExchange.getResponseHeaders().add("Cache-Control", "no-store");

        // Causes the proxy cache to see the page as "stale"
        httpExchange.getResponseHeaders().set("Expires", "0");

        // HTTP 1.0 backward compatibility for caching
        httpExchange.getResponseHeaders().set("Pragma", "no-cache");

        // Make sure Content-Type is properly set
        httpExchange.getResponseHeaders().set("Content-Type", pullContentType());
    }

    protected byte[] grabResponseBytes(HttpExchange httpExchange) throws IOException {
        String response = (this.response == null) ? "" : this.response;

        // Special handling of redirects
        if ((HttpURLConnection.HTTP_SEE_OTHER == code) && !response.isEmpty()) {
            httpExchange.getResponseHeaders().set("Location", response.trim());
            response = "";
        }

        return response.getBytes("UTF-8");
    }

    public void deliverResponse(HttpExchange httpExchange) throws IOException {
        addResponseHeaders(httpExchange);

        byte[] bytes = grabResponseBytes(httpExchange);

        if (bytes == null) {
            bytes = new byte[0];
        }

        httpExchange.sendResponseHeaders(code, bytes.length);

        httpExchange.getResponseBody().write(bytes);
        drain(httpExchange.getRequestBody());
        httpExchange.close();
    }

    /**
     * Reads and discards any remaining bytes in the input stream.
     * Closing an HttpExchange without consuming all of the request
     * body (InputStream) is not an error but may make the underlying
     * TCP connection unusable for following exchanges.
     * The default InputStream assigned by the HttpExchange will be
     * a subclass of sun.net.httpserver.LeftOverInputStream which,
     * on a call to close, will attempt to drain the InputStream.
     * However, that implementation will only drain up to a fixed size
     * (see ServerConfig.getDrainAmount - default: 64 x 1024 bytes).
     * This method completely drains the stream so, when close
     * is called, the stream has been completely read.
     */
    private void drain(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int count;
        do {
            count = inputStream.read(buffer);
        } while (count != -1);
    }
}
