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
        ClassgetTitle replacementClass = new  ClassgetTitle(context);
        ;
        return replacementClass.doIt0();
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
        ClasshandlePost replacementClass = new  ClasshandlePost(httpExchange);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
        return replacementClass.doIt3();
    }

    protected class NameHandlerHelper0 {

        public NameHandlerHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            ClassgetValue replacementClass = new  ClassgetValue();
            ;
            return replacementClass.doIt0();
        }

        public class ClassgetValue {

            public ClassgetValue() {
            }

            public int doIt0() {
                return conditionRHS;
            }
        }
    }

    protected class ClassgetTitle {

        public ClassgetTitle(SnapContext context) {
            this.context = context;
        }

        private SnapContext context;

        public String doIt0() {
            return TITLE;
        }
    }

    protected class ClasshandlePost {

        public ClasshandlePost(HttpExchange httpExchange) {
            this.httpExchange = httpExchange;
        }

        private HttpExchange httpExchange;

        private Person person;

        public void doIt0() {
            person = getPerson(httpExchange);
        }

        private List<String> names;

        public void doIt1() {
            names = MultipartHelper.getMultipartFieldItems(httpExchange, FIELD_NAME);
        }

        private String name;

        private NameHandlerHelper0 conditionObj0;

        public void doIt2() {
            name = "";
            conditionObj0 = new  NameHandlerHelper0(1);
            // if there is more than one element, the name does not get changed
            if (names.size() == conditionObj0.getValue()) {
                name = names.get(0);
            }
            if (!StringUtils.isBlank(name)) {
                getSnapService().setName(person, name);
            }
        }

        public HttpHandlerResponse doIt3() {
            return getDefaultRedirectResponse();
        }
    }
}
