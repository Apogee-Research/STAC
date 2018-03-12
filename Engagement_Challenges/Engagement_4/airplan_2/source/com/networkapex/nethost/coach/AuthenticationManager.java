package com.networkapex.nethost.coach;

import com.networkapex.nethost.WebTemplateBuilder;
import com.networkapex.template.TemplateEngine;
import com.networkapex.nethost.WebTemplate;
import com.sun.net.httpserver.HttpExchange;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AuthenticationManager extends AbstractHttpManager {
    private final String redirectResponseTrail;
    private final WebTemplate template;
    private static final String TRAIL = "/authenticate";
    private static final String TITLE = "Authenticate Server";
    private static final String KEY_FIELD = "A";
    private static final String TIMESTAMP_FIELD = "setTimestamp";
    private static final TemplateEngine TEMPLATE_ENGINE = new TemplateEngine(
            "<center>" +
            "<form action=\"" + TRAIL +"\" method=\"post\" enctype=\"multipart/form-data\"/>" +
            "    <textarea name=\"" + KEY_FIELD + "\" placeholder=\"Enter your public key\"" +
            "       rows=\"10\" cols=\"100\"/></textarea><br/>" +
            "    <input type=\"submit\" value=\"Compute the master secret\" name=\"submit\" />" +
            "    <input type=\"hidden\" name=\"" + TIMESTAMP_FIELD +"\" value=\"{{includeTimestamp}}\">" +
            "</form>" +
            "</center>");

    public AuthenticationManager(String redirectResponseTrail) {
        this.redirectResponseTrail = redirectResponseTrail;
        this.template = new WebTemplateBuilder().defineResourceName("basiccontenttemplate.html").defineLoader(getClass()).generateWebTemplate();
    }

    @Override
    public String obtainTrail() {
        return TRAIL;
    }

    @Override
    protected HttpManagerResponse handleFetch(HttpExchange httpExchange) {
        Map<String, String> contentsTemplateMap = new HashMap<>();
        Map<String, String> templateMap = new HashMap<>();

        String suppressTimestamp = takeUrlParam(httpExchange, "suppressTimestamp");
        if (suppressTimestamp == null || !suppressTimestamp.equals("true")) {
            handleFetchAdviser(httpExchange, contentsTemplateMap, templateMap);
        } else {
            contentsTemplateMap.put("includeTimestamp", "false");
        }
        templateMap.put("contents", TEMPLATE_ENGINE.replaceTags(contentsTemplateMap));
        templateMap.put("title", TITLE);

        return grabResponse(template.takeEngine().replaceTags(templateMap));
    }

    private void handleFetchAdviser(HttpExchange httpExchange, Map<String, String> contentsTemplateMap, Map<String, String> templateMap) {
        contentsTemplateMap.put("includeTimestamp", "true");
        templateMap.put("timestamp", (new Date()).toString());
        templateMap.put("duration", String.valueOf(getDuration(httpExchange)));
    }

    @Override
    protected HttpManagerResponse handlePost(HttpExchange httpExchange) {
        Set<String> fieldNames = new HashSet<>(Arrays.asList(KEY_FIELD, TIMESTAMP_FIELD));
        Map<String, List<String>> fieldNameItems = MultipartHelper.getMultipartValues(httpExchange, fieldNames);
        String personsPublicKey = "";
        boolean includeTimestamp = true;
        List<String> personsPublicKeyList = fieldNameItems.get(KEY_FIELD);
        if (personsPublicKeyList != null && personsPublicKeyList.size() == 1) {
            personsPublicKey = personsPublicKeyList.get(0);
        }

        List<String> includeTimestampList = fieldNameItems.get(TIMESTAMP_FIELD);
        if (includeTimestampList != null && includeTimestampList.size() == 1) {
            String timestamp = includeTimestampList.get(0);
            if (timestamp.equals("false")) {
                includeTimestamp = false;
            }
        }
        String urlEnd = "";
        if (personsPublicKey != null) {
            urlEnd = personsPublicKey;
        }

        String suppressTimestamp = takeUrlParam(httpExchange, "suppressTimestamp");
        if (!includeTimestamp || (suppressTimestamp != null && suppressTimestamp.equals("true"))) {
            urlEnd += "?suppressTimestamp=true";
        }
        return obtainRedirectResponse(redirectResponseTrail + "/" + urlEnd);
    }
}
