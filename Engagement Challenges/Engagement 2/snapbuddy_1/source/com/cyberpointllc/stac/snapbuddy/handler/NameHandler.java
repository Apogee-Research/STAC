package com.cyberpointllc.stac.snapbuddy.handler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.cyberpointllc.stac.webserver.handler.HttpHandlerResponse;
import com.cyberpointllc.stac.webserver.handler.MultipartHelper;
import org.apache.commons.lang3.StringUtils;
import com.cyberpointllc.stac.snapservice.SnapContext;
import com.cyberpointllc.stac.snapservice.SnapService;
import com.cyberpointllc.stac.snapservice.model.Person;
import com.cyberpointllc.stac.template.TemplateEngine;
import com.sun.net.httpserver.HttpExchange;

public class NameHandler extends AbstractTemplateSnapBuddyHandler {

    private static final String PATH = "/profilename";

    private static final String TITLE = "Change Name";

    private static final String FIELD_NAME = "changename";

    private static final TemplateEngine TEMPLATE = new  TemplateEngine("<form action\"" + PATH + "\" method=\"post\" enctype=\"multipart/form-data\">" + "    Updated Name: <input type=\"text\" name=\"" + FIELD_NAME + "\" placeholder=\"{{name}}\" /></br> " + "    <input type=\"submit\" value=\"Change Name\" name=\"submit\">" + "</form>");

    public NameHandler(SnapService snapService) {
        super(snapService);
    }

    @Override
    protected String getTitle(SnapContext context) {
        return TITLE;
    }

    @Override
    protected String getContents(SnapContext context) {
        assert (context != null) : "Context may not be null";
        Person person = context.getActivePerson();
        Map<String, String> map = Collections.singletonMap("name", person.getName());
        return TEMPLATE.replaceTags(map);
    }

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    protected HttpHandlerResponse handlePost(HttpExchange httpExchange) {
        Person person = getPerson(httpExchange);
        List<String> names = MultipartHelper.getMultipartFieldItems(httpExchange, FIELD_NAME);
        String name = "";
        int conditionObj0 = 1;
        // if there is more than one element, the name does not get changed
        if (names.size() == conditionObj0) {
            name = names.get(0);
        }
        if (!StringUtils.isBlank(name)) {
            handlePostHelper(person, name);
        }
        return getDefaultRedirectResponse();
    }

    private void handlePostHelper(Person person, String name) {
        getSnapService().setName(person, name);
    }
}
