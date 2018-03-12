package net.cybertip.netmanager.manager;

import net.cybertip.template.TemplateEngine;
import net.cybertip.netmanager.WebTemplate;
import com.sun.net.httpserver.HttpExchange;
import net.cybertip.template.TemplateEngineBuilder;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AuthenticationCoach extends AbstractHttpCoach {
    private final String redirectResponsePath;
    private final WebTemplate template;
    private static final String PATH = "/authenticate";
    private static final String TITLE = "Authenticate Server";
    private static final String KEY_FIELD = "A";
    private static final String TIMESTAMP_FIELD = "setTimestamp";
    private static final TemplateEngine TEMPLATE_ENGINE = new TemplateEngineBuilder().setText("<center>" +
            "<form action=\"" + PATH + "\" method=\"post\" enctype=\"multipart/form-data\"/>" +
            "    <textarea name=\"" + KEY_FIELD + "\" placeholder=\"Enter your public key\"" +
            "       rows=\"10\" cols=\"100\"/></textarea><br/>" +
            "    <input type=\"submit\" value=\"Compute the master secret\" name=\"submit\" />" +
            "    <input type=\"hidden\" name=\"" + TIMESTAMP_FIELD + "\" value=\"{{includeTimestamp}}\">" +
            "</form>" +
            "</center>").makeTemplateEngine();

    public AuthenticationCoach(String redirectResponsePath) {
        this.redirectResponsePath = redirectResponsePath;
        this.template = new WebTemplate("basiccontenttemplate.html", getClass());
    }

    @Override
    public String grabPath() {
        return PATH;
    }

    @Override
    protected HttpCoachResponse handleTake(HttpExchange httpExchange) {
        Map<String, String> contentsTemplateMap = new HashMap<>();
        Map<String, String> templateMap = new HashMap<>();

        String suppressTimestamp = takeUrlParam(httpExchange, "suppressTimestamp");
        if (suppressTimestamp == null || !suppressTimestamp.equals("true")) {
            handleTakeAssist(httpExchange, contentsTemplateMap, templateMap);
        } else {
            handleTakeHerder(contentsTemplateMap);
        }
        templateMap.put("contents", TEMPLATE_ENGINE.replaceTags(contentsTemplateMap));
        templateMap.put("title", TITLE);

        return grabResponse(template.getEngine().replaceTags(templateMap));
    }

    private void handleTakeHerder(Map<String, String> contentsTemplateMap) {
        contentsTemplateMap.put("includeTimestamp", "false");
    }

    private void handleTakeAssist(HttpExchange httpExchange, Map<String, String> contentsTemplateMap, Map<String, String> templateMap) {
        contentsTemplateMap.put("includeTimestamp", "true");
        templateMap.put("timestamp", (new Date()).toString());
        templateMap.put("duration", String.valueOf(obtainDuration(httpExchange)));
    }

    @Override
    protected HttpCoachResponse handlePost(HttpExchange httpExchange) {
        Set<String> fieldNames = new HashSet<>(Arrays.asList(KEY_FIELD, TIMESTAMP_FIELD));
        Map<String, List<String>> fieldNameItems = MultipartHelper.pullMultipartValues(httpExchange, fieldNames);
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
        return fetchRedirectResponse(redirectResponsePath + "/" + urlEnd);
    }
}
