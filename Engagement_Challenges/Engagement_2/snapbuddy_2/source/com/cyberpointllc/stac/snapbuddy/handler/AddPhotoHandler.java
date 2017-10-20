package com.cyberpointllc.stac.snapbuddy.handler;

import com.cyberpointllc.stac.snapservice.ImageService;
import com.cyberpointllc.stac.snapservice.SnapContext;
import com.cyberpointllc.stac.snapservice.SnapService;
import com.cyberpointllc.stac.snapservice.model.Location;
import com.cyberpointllc.stac.snapservice.model.Person;
import com.cyberpointllc.stac.snapservice.model.Photo;
import com.cyberpointllc.stac.webserver.handler.HttpHandlerResponse;
import com.cyberpointllc.stac.webserver.handler.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.StringUtils;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AddPhotoHandler extends AbstractTemplateSnapBuddyHandler {

    private static final String PATH = "/addphoto";

    private static final String TITLE = "Add Photo";

    private static final String PHOTO_FIELD_NAME = "addphoto";

    private static final String CAPTION_FIELD_NAME = "caption";

    private static final String PUBLIC_FIELD_NAME = "public";

    private static final String CONTENTS = "<form action=\"" + PATH + "\" method=\"post\" enctype=\"multipart/form-data\">" + "    <label for=\"file\"> Photo: </label>" + "    <input type=\"file\" id=\"file\" name=\"" + PHOTO_FIELD_NAME + "\" autofocus required accept=\"image/*\"/>" + "    <br/>" + "    Caption: <input type=\"text\" name=\"" + CAPTION_FIELD_NAME + "\" placeholder=\"Enter an Optional Caption\" /> <br/>" + "    <input type=\"checkbox\" name=\"" + PUBLIC_FIELD_NAME + "\" />Make Photo Publicly Visible<br/>" + "    <input type=\"submit\" value=\"Add Photo\" name=\"submit\" id=\"submit\" />" + "</form>";

    private static final Set<String> ALL_FIELDS = new  HashSet();

    static {
        ALL_FIELDS.add(PHOTO_FIELD_NAME);
        ALL_FIELDS.add(CAPTION_FIELD_NAME);
        ALL_FIELDS.add(PUBLIC_FIELD_NAME);
    }

    private final ImageService imageService;

    public AddPhotoHandler(SnapService snapService, ImageService imageService) {
        super(snapService);
        if (imageService == null) {
            throw new  IllegalArgumentException("ImageService may not be null");
        }
        this.imageService = imageService;
    }

    @Override
    protected String getTitle(SnapContext context) {
        return TITLE;
    }

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    protected String getContents(SnapContext context) {
        return CONTENTS;
    }

    @Override
    protected HttpHandlerResponse handlePost(HttpExchange httpExchange) {
        Person person = getPerson(httpExchange);
        Path destination = Paths.get(imageService.getBasePhotosPath().toString(), person.getIdentity());
        Map<String, String> fieldNameInputs = MultipartHelper.getMultipartPhoto(httpExchange, ALL_FIELDS, PHOTO_FIELD_NAME, destination, null);
        // take the map and create a new photo
        String photoName = fieldNameInputs.get(PHOTO_FIELD_NAME);
        if (StringUtils.isBlank(photoName)) {
            throw new  IllegalArgumentException("A POST image file was required for field " + PHOTO_FIELD_NAME);
        }
        // Photo identity is also its relative path
        String path = getPhotoIdentity(person, photoName);
        String photoCaption = fieldNameInputs.get(CAPTION_FIELD_NAME);
        Location photoLocation = person.getLocation();
        boolean isPhotoPublic = fieldNameInputs.containsKey(PUBLIC_FIELD_NAME);
        Photo photo = new  Photo(path, isPhotoPublic, photoCaption, photoLocation, null);
        // check that the image isn't too big
        if (!imageService.isSmallPhoto(photo)) {
            return getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "The dimensions of the image are too large");
        }
        // add photo to person
        getSnapService().addPhoto(person, photo);
        return getDefaultRedirectResponse();
    }
}
