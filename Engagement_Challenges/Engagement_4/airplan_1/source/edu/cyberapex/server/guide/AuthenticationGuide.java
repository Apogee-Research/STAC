package edu.cyberapex.server.guide;

import edu.cyberapex.template.TemplateEngine;
import edu.cyberapex.server.WebTemplate;
import com.sun.net.httpserver.HttpExchange;
import edu.cyberapex.template.TemplateEngineBuilder;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AuthenticationGuide extends AbstractHttpGuide {
    private final String redirectResponsePath;
    private final WebTemplate template;
    private static final String PATH = "/authenticate";
    private static final String TITLE = "Authenticate Server";
    private static final String KEY_FIELD = "A";
    private static final String TIMESTAMP_FIELD = "setTimestamp";
    private static final TemplateEngine TEMPLATE_ENGINE = new TemplateEngineBuilder().defineText("<center>" +
            "<form action=\"" + PATH + "\" method=\"post\" enctype=\"multipart/form-data\"/>" +
            "    <textarea name=\"" + KEY_FIELD + "\" placeholder=\"Enter your public key\"" +
            "       rows=\"10\" cols=\"100\"/></textarea><br/>" +
            "    <input type=\"submit\" value=\"Compute the master secret\" name=\"submit\" />" +
            "    <input type=\"hidden\" name=\"" + TIMESTAMP_FIELD + "\" value=\"{{includeTimestamp}}\">" +
            "</form>" +
            "</center>").generateTemplateEngine();

    public AuthenticationGuide(String redirectResponsePath) {
        this.redirectResponsePath = redirectResponsePath;
        this.template = new WebTemplate("basiccontenttemplate.html", getClass());
    }

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    protected HttpGuideResponse handleGrab(HttpExchange httpExchange) {
        Map<String, String> contentsTemplateMap = new HashMap<>();
        Map<String, String> templateMap = new HashMap<>();

        String suppressTimestamp = takeUrlParam(httpExchange, "suppressTimestamp");
        if (suppressTimestamp == null || !suppressTimestamp.equals("true")) {
            contentsTemplateMap.put("includeTimestamp", "true");
            templateMap.put("timestamp", (new Date()).toString());
            templateMap.put("duration", String.valueOf(fetchDuration(httpExchange)));
        } else {
            contentsTemplateMap.put("includeTimestamp", "false");
        }
        templateMap.put("contents", TEMPLATE_ENGINE.replaceTags(contentsTemplateMap));
        templateMap.put("title", TITLE);

        return takeResponse(template.getEngine().replaceTags(templateMap));
    }

    @Override
    protected HttpGuideResponse handlePost(HttpExchange httpExchange) {
        Set<String> fieldNames = new HashSet<>(Arrays.asList(KEY_FIELD, TIMESTAMP_FIELD));
        Map<String, List<String>> fieldNameItems = MultipartHelper.fetchMultipartValues(httpExchange, fieldNames);
        String membersPublicKey = "";
        boolean includeTimestamp = true;
        List<String> membersPublicKeyList = fieldNameItems.get(KEY_FIELD);
        if (membersPublicKeyList != null && membersPublicKeyList.size() == 1) {
            membersPublicKey = membersPublicKeyList.get(0);
        }

        List<String> includeTimestampList = fieldNameItems.get(TIMESTAMP_FIELD);
        if (includeTimestampList != null && includeTimestampList.size() == 1) {
            String timestamp = includeTimestampList.get(0);
            if (timestamp.equals("false")) {
                includeTimestamp = false;
            }
        }
        String urlEnd = "";
        if (membersPublicKey != null) {
            urlEnd = membersPublicKey;
        }

        String suppressTimestamp = takeUrlParam(httpExchange, "suppressTimestamp");
        if (!includeTimestamp || (suppressTimestamp != null && suppressTimestamp.equals("true"))) {
            urlEnd += "?suppressTimestamp=true";
        }
        return takeRedirectResponse(redirectResponsePath + "/" + urlEnd);
    }
}
