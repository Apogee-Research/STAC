package com.cyberpointllc.stac.snapbuddy.handler;

import java.util.Map;
import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.snapservice.SnapContext;
import com.cyberpointllc.stac.snapservice.SnapService;
import com.cyberpointllc.stac.snapservice.model.Person;
import com.cyberpointllc.stac.snapservice.model.Photo;
import com.cyberpointllc.stac.template.TemplateEngine;
import com.cyberpointllc.stac.webserver.handler.HttpHandlerResponse;
import com.cyberpointllc.stac.webserver.handler.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;

public class CaptionHandler extends AbstractTemplateSnapBuddyHandler {

    private final String redirectResponsePath;

    private static final String PATH = "/editcaption/";

    private static final String TITLE = "Edit Caption";

    private static final String FIELD_NAME = "caption";

    private static final TemplateEngine TEMPLATE = new  TemplateEngine("<form action=\"" + PATH + "{{pid}}\" method=\"post\" enctype=\"multipart/form-data\">" + "    <center>" + "        <div class=\"photos\"> " + "            <img src=\"{{photoPath}}\" alt=\"{{currentCaption}}\" /><br/>" + "            {{currentCaption}}" + "        </div>" + "        Caption: <input type=\"text\" name=\"" + FIELD_NAME + "\" placeholder=\"{{currentCaption}}\"/> <br/>" + "        <input type=\"submit\" value=\"Update Caption\" name=\"submit\" />" + "    </center> " + "</form>");

    public CaptionHandler(SnapService snapService, String redirectResponsePath) {
        super(snapService);
        this.redirectResponsePath = redirectResponsePath;
    }

    @Override
    protected String getTitle(SnapContext context) {
        return TITLE;
    }

    @Override
    protected String getContents(SnapContext context) {
        ClassgetContents replacementClass = new  ClassgetContents(context);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        return replacementClass.doIt2();
    }

    @Override
    public String getPath() {
        ClassgetPath replacementClass = new  ClassgetPath();
        ;
        return replacementClass.doIt0();
    }

    @Override
    protected HttpHandlerResponse handlePost(HttpExchange httpExchange) {
        String path = httpExchange.getRequestURI().getPath();
        Person person = getPerson(httpExchange);
        if (path.startsWith(getPath())) {
            path = path.substring(getPath().length());
        }
        Photo photo = getSnapService().getPhoto(path);
        // we do not want to add a caption to the profile photo
        if (photo.getIdentity().equals(getProfilePhotoIdentity(person))) {
            return getDefaultRedirectResponse();
        }
        String newCaption = MultipartHelper.getMultipartFieldContent(httpExchange, FIELD_NAME);
        getSnapService().setCaption(photo, newCaption);
        return getRedirectResponse(redirectResponsePath + photo.getIdentity());
    }

    protected class ClassgetContents {

        public ClassgetContents(SnapContext context) {
            this.context = context;
        }

        private SnapContext context;

        private String path;

        public void doIt0() {
            path = context.getPath();
            if (path.startsWith(getPath())) {
                path = path.substring(getPath().length());
            }
        }

        private Person activePerson;

        public void doIt1() {
            activePerson = context.getActivePerson();
        }

        private Photo photo;

        private Map<String, String> map;

        public String doIt2() {
            photo = getSnapService().getPhoto(path);
            map = new  HashMap();
            if (activePerson.getPhotos().contains(path)) {
                map.put("pid", photo.getIdentity());
                map.put("photoPath", getPhotoUrl(photo));
                map.put("currentCaption", photo.getCaption());
            } else {
                throw new  IllegalArgumentException("This is not your photo.");
            }
            return TEMPLATE.replaceTags(map);
        }
    }

    public class ClassgetPath {

        public ClassgetPath() {
        }

        public String doIt0() {
            return PATH;
        }
    }
}
