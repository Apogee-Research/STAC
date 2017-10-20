package com.cyberpointllc.stac.snapservice;

import com.cyberpointllc.stac.snapservice.model.Person;
import com.sun.net.httpserver.HttpExchange;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SnapContext {

    private final List<NameValuePair> urlParams;

    private Person activePerson;

    private HttpExchange httpExchange;

    private final URI uri;

    private final Date date;

    private final long startNanoTime;

    public SnapContext(Person activePerson, HttpExchange httpExchange) {
        this.activePerson = activePerson;
        this.httpExchange = httpExchange;
        uri = httpExchange.getRequestURI();
        date = new  Date();
        startNanoTime = System.nanoTime();
        urlParams = Collections.unmodifiableList(URLEncodedUtils.parse(uri, "UTF-8"));
    }

    public Person getActivePerson() {
        return activePerson;
    }

    public URI getUri() {
        return uri;
    }

    public String getPath() {
        return getUri().getPath();
    }

    public Date getDate() {
        return date;
    }

    public List<NameValuePair> getUrlParams() {
        return urlParams;
    }

    public HttpExchange getHttpExchange() {
        return httpExchange;
    }

    /**
     * Returns the first value of a parameter with name 'name'
     * @param name the name of the parameter to return
     * @return the value of the parameter
     */
    public String getUrlParam(String name) {
        for (NameValuePair pair : urlParams) {
            if (pair.getName().equals(name)) {
                return pair.getValue();
            }
        }
        return null;
    }
}
