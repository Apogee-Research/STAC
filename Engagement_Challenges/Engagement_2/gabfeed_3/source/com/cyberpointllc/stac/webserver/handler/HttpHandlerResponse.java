package com.cyberpointllc.stac.webserver.handler;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.HttpURLConnection;

public class HttpHandlerResponse {

    private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

    private final int code;

    private final String response;

    public HttpHandlerResponse() {
        this(HttpURLConnection.HTTP_OK);
    }

    public HttpHandlerResponse(int code) {
        this(code, "");
    }

    public HttpHandlerResponse(String response) {
        this(HttpURLConnection.HTTP_OK, response);
    }

    public HttpHandlerResponse(int code, String response) {
        this.code = code;
        this.response = (response == null) ? "" : response;
    }

    protected String getContentType() {
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
        httpExchange.getResponseHeaders().set("Content-Type", getContentType());
    }

    protected byte[] getResponseBytes(HttpExchange httpExchange) throws IOException {
        String response = (this.response == null) ? "" : this.response;
        // Special handling of redirects
        if ((HttpURLConnection.HTTP_SEE_OTHER == code) && !response.isEmpty()) {
            httpExchange.getResponseHeaders().set("Location", response.trim());
            response = "";
        }
        return response.getBytes("UTF-8");
    }

    public void sendResponse(HttpExchange httpExchange) throws IOException {
        addResponseHeaders(httpExchange);
        byte[] bytes = getResponseBytes(httpExchange);
        if (bytes == null) {
            bytes = new byte[0];
        }
        httpExchange.sendResponseHeaders(code, bytes.length);
        httpExchange.getResponseBody().write(bytes);
        httpExchange.close();
    }
}
