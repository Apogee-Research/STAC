package com.cyberpointllc.stac.snapbuddy.handler;

import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.snapservice.SnapContext;
import com.cyberpointllc.stac.snapservice.SnapService;
import com.cyberpointllc.stac.snapservice.model.Person;
import com.cyberpointllc.stac.webserver.WebTemplate;
import com.cyberpointllc.stac.webserver.handler.HttpHandlerResponse;
import com.sun.net.httpserver.HttpExchange;
import java.util.Map;

public abstract class AbstractTemplateSnapBuddyHandler extends AbstractSnapBuddyHandler {

    private static final String TEMPLATE_RESOURCE = "template.html";

    public static final String SUPPRESS_TIMESTAMP = "suppressTimestamp";

    private WebTemplate template;

    protected AbstractTemplateSnapBuddyHandler(SnapService snapService) {
        super(snapService);
    }

    protected abstract String getTitle(SnapContext context);

    protected abstract String getContents(SnapContext context);

    protected String getTemplateResource() {
        return TEMPLATE_RESOURCE;
    }

    protected Map<String, String> getTemplateMap(SnapContext context) {
        assert (context != null) : "Context may not be null";
        Map<String, String> map = new  HashMap();
        Person person = context.getActivePerson();
        map.put("name", person.getName());
        map.put("location", person.getLocation().getCity());
        map.put("pid", getProfilePhotoIdentity(person));
        map.put("profileURL", getProfilePhotoUrl(person));
        map.put("title", getTitle(context));
        map.put("contents", getContents(context));
        String suppressTimestamp = context.getUrlParam(SUPPRESS_TIMESTAMP);
        if (suppressTimestamp == null || suppressTimestamp.equalsIgnoreCase("false")) {
            // Assign these two fields AFTER calling getContents
            map.put("timestamp", context.getDate().toString());
            map.put("duration", String.valueOf(getDuration(context.getHttpExchange())));
        }
        return map;
    }

    private WebTemplate getTemplate() {
        if (template == null) {
            template = new  WebTemplate(getTemplateResource(), getClass());
        }
        return template;
    }

    protected HttpHandlerResponse process(SnapContext context) {
        String response = getTemplate().getEngine().replaceTags(getTemplateMap(context));
        return getResponse(response);
    }

    @Override
    protected HttpHandlerResponse handleGet(HttpExchange httpExchange) {
        SnapContext snapContext = new  SnapContext(getPerson(httpExchange), httpExchange);
        return process(snapContext);
    }
}
