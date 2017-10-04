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
        ClassgetUrlParams replacementClass = new  ClassgetUrlParams();
        ;
        return replacementClass.doIt0();
    }

    public HttpExchange getHttpExchange() {
        ClassgetHttpExchange replacementClass = new  ClassgetHttpExchange();
        ;
        return replacementClass.doIt0();
    }

    /**
     * Returns the first value of a parameter with name 'name'
     * @param name the name of the parameter to return
     * @return the value of the parameter
     */
    public String getUrlParam(String name) {
        ClassgetUrlParam replacementClass = new  ClassgetUrlParam(name);
        ;
        return replacementClass.doIt0();
    }

    public class ClassgetUrlParams {

        public ClassgetUrlParams() {
        }

        public List<NameValuePair> doIt0() {
            return urlParams;
        }
    }

    public class ClassgetHttpExchange {

        public ClassgetHttpExchange() {
        }

        public HttpExchange doIt0() {
            return httpExchange;
        }
    }

    public class ClassgetUrlParam {

        public ClassgetUrlParam(String name) {
            this.name = name;
        }

        private String name;

        public String doIt0() {
            for (NameValuePair pair : urlParams) {
                if (pair.getName().equals(name)) {
                    return pair.getValue();
                }
            }
            return null;
        }
    }
}
