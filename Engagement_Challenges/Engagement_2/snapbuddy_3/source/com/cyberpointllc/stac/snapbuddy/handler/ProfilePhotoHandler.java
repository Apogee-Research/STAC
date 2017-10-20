package com.cyberpointllc.stac.snapbuddy.handler;

import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import com.cyberpointllc.stac.snapservice.ImageService;
import com.cyberpointllc.stac.snapservice.SnapContext;
import com.cyberpointllc.stac.snapservice.SnapService;
import com.cyberpointllc.stac.snapservice.model.Person;
import com.cyberpointllc.stac.snapservice.model.Photo;
import com.cyberpointllc.stac.webserver.handler.HttpHandlerResponse;
import com.cyberpointllc.stac.webserver.handler.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;

public class ProfilePhotoHandler extends AbstractTemplateSnapBuddyHandler {

    private final ImageService imageService;

    private static final String PATH = "/profilephoto";

    private static final String TITLE = "Change Profile Photo";

    private static final String FIELD_NAME = "addprofilephoto";

    private static final String CONTENTS = "<form action=\"" + PATH + "\" method=\"post\" enctype=\"multipart/form-data\">" + "    <label for=\"file\"> Photo: </label>" + "    <input type=\"file\" id=\"file\" name=\"" + FIELD_NAME + "\" autofocus accept=\"image/*\"/>" + "    <br/>" + "    <input type=\"submit\" value=\"Add Photo\" name=\"submit\">" + "</form>";

    public ProfilePhotoHandler(SnapService snapService, ImageService imageService) {
        super(snapService);
        this.imageService = imageService;
    }

    @Override
    protected String getTitle(SnapContext context) {
        ClassgetTitle replacementClass = new  ClassgetTitle(context);
        ;
        return replacementClass.doIt0();
    }

    @Override
    protected String getContents(SnapContext context) {
        return CONTENTS;
    }

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    protected HttpHandlerResponse handlePost(HttpExchange httpExchange) {
        Person person = getPerson(httpExchange);
        String id = person.getIdentity();
        SnapService snapService = getSnapService();
        Set<String> allFieldNames = new  HashSet<String>();
        allFieldNames.add(FIELD_NAME);
        Path destDir = Paths.get(imageService.getBasePhotosPath().toString(), id);
        MultipartHelper.getMultipartPhoto(httpExchange, allFieldNames, FIELD_NAME, destDir, getProfilePhotoName());
        // make profile photo
        String photoPath = id + "/" + getProfilePhotoName();
        Photo photo = new  Photo(photoPath, true, null, null, null);
        // check that the image isn't too big
        if (!imageService.isSmallPhoto(photo)) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "The dimensions of the image are too large");
        }
        snapService.addPhoto(person, photo);
        return getDefaultRedirectResponse();
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
}
