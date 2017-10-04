package com.cyberpointllc.stac.webserver.handler;

import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.template.TemplateEngine;
import com.cyberpointllc.stac.webserver.WebTemplate;
import com.sun.net.httpserver.HttpExchange;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AuthenticationHandler extends AbstractHttpHandler {

    private final String redirectResponsePath;

    private final WebTemplate template;

    private static final String PATH = "/authenticate";

    private static final String TITLE = "Authenticate Server";

    private static final String KEY_FIELD = "A";

    private static final String TIMESTAMP_FIELD = "setTimestamp";

    private static final TemplateEngine TEMPLATE_ENGINE = new  TemplateEngine("<center>" + "<form action=\"" + PATH + "\" method=\"post\" enctype=\"multipart/form-data\"/>" + "    <textarea name=\"" + KEY_FIELD + "\" placeholder=\"Enter your public key\"" + "       rows=\"10\" cols=\"100\"/></textarea><br/>" + "    <input type=\"submit\" value=\"Compute the master secret\" name=\"submit\" />" + "    <input type=\"hidden\" name=\"" + TIMESTAMP_FIELD + "\" value=\"{{includeTimestamp}}\">" + "</form>" + "</center>");

    public AuthenticationHandler(String redirectResponsePath) {
        this.redirectResponsePath = redirectResponsePath;
        this.template = new  WebTemplate("basiccontenttemplate.html", getClass());
    }

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    protected HttpHandlerResponse handleGet(HttpExchange httpExchange) {
        Map<String, String> contentsTemplateMap = new  HashMap();
        Map<String, String> templateMap = new  HashMap();
        String suppressTimestamp = getUrlParam(httpExchange, "suppressTimestamp");
        if (suppressTimestamp == null || !suppressTimestamp.equals("true")) {
            handleGetHelper(templateMap, httpExchange, contentsTemplateMap);
        } else {
            contentsTemplateMap.put("includeTimestamp", "false");
        }
        templateMap.put("contents", TEMPLATE_ENGINE.replaceTags(contentsTemplateMap));
        templateMap.put("title", TITLE);
        return getResponse(template.getEngine().replaceTags(templateMap));
    }

    @Override
    protected HttpHandlerResponse handlePost(HttpExchange httpExchange) {
        Set<String> fieldNames = new  HashSet(Arrays.asList(KEY_FIELD, TIMESTAMP_FIELD));
        Map<String, List<String>> fieldNameItems = MultipartHelper.getMultipartValues(httpExchange, fieldNames);
        String usersPublicKey = "";
        boolean includeTimestamp = true;
        List<String> usersPublicKeyList = fieldNameItems.get(KEY_FIELD);
        AuthenticationHandlerHelper0 conditionObj0 = new  AuthenticationHandlerHelper0(1);
        if (usersPublicKeyList != null && usersPublicKeyList.size() == conditionObj0.getValue()) {
            usersPublicKey = usersPublicKeyList.get(0);
        }
        List<String> includeTimestampList = fieldNameItems.get(TIMESTAMP_FIELD);
        AuthenticationHandlerHelper1 conditionObj1 = new  AuthenticationHandlerHelper1(1);
        if (includeTimestampList != null && includeTimestampList.size() == conditionObj1.getValue()) {
            String timestamp = includeTimestampList.get(0);
            if (timestamp.equals("false")) {
                includeTimestamp = false;
            }
        }
        String urlEnd = "";
        if (usersPublicKey != null) {
            urlEnd = usersPublicKey;
        }
        String suppressTimestamp = getUrlParam(httpExchange, "suppressTimestamp");
        if (!includeTimestamp || (suppressTimestamp != null && suppressTimestamp.equals("true"))) {
            urlEnd += "?suppressTimestamp=true";
        }
        return getRedirectResponse(redirectResponsePath + "/" + urlEnd);
    }

    protected class AuthenticationHandlerHelper0 {

        public AuthenticationHandlerHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    protected class AuthenticationHandlerHelper1 {

        public AuthenticationHandlerHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private void handleGetHelper(Map<String, String> templateMap, HttpExchange httpExchange, Map<String, String> contentsTemplateMap) {
        contentsTemplateMap.put("includeTimestamp", "true");
        templateMap.put("timestamp", (new  Date()).toString());
        templateMap.put("duration", String.valueOf(getDuration(httpExchange)));
    }
}
