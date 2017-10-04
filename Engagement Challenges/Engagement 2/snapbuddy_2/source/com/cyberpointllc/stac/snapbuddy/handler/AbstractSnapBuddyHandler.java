package com.cyberpointllc.stac.snapbuddy.handler;

import com.cyberpointllc.stac.snapservice.SnapService;
import com.cyberpointllc.stac.snapservice.model.Person;
import com.cyberpointllc.stac.snapservice.model.Photo;
import com.cyberpointllc.stac.webserver.handler.AbstractHttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractSnapBuddyHandler extends AbstractHttpHandler {

    private static final String PHOTO_URL = "/photo/";

    private static final String THUMB_PHOTO_URL = "/thumb/";

    private static final String PROFILE_PHOTO_NAME = "profile.jpg";

    private static final Photo DEFAULT_PHOTO = new  Photo("snapbuddy.jpg", true, null, null, null);

    private final SnapService snapService;

    protected AbstractSnapBuddyHandler(SnapService snapService) {
        if (snapService == null) {
            throw new  IllegalArgumentException("SnapService may not be null");
        }
        this.snapService = snapService;
    }

    protected SnapService getSnapService() {
        return snapService;
    }

    /**
     * Returns the <code>Person</code> associated with the Principal
     * in the specified <code>HttpExchange</code>.
     * If a valid <code>Person</code> cannot be returned,
     * a <code>RuntimeException</code> is raised.
     *
     * @param httpExchange used to find the Person
     * @return Person associated with the exchange
     * @throws RuntimeException if Person cannot be found
     */
    protected Person getPerson(HttpExchange httpExchange) {
        assert (httpExchange != null) : "HttpExchange may not be null";
        Person user;
        Object userIdObj = httpExchange.getAttribute("userId");
        if (userIdObj instanceof String) {
            String userId = (String) userIdObj;
            user = getSnapService().getPerson(userId);
        } else {
            throw new  IllegalStateException("Person is not authenticated");
        }
        if (user == null) {
            throw new  IllegalStateException("Person does not exist");
        }
        return user;
    }

    /**
     * Returns a valid Photo instance to use by default.
     * If a valid Photo instance is required,
     * this can be used as a reasonable substitute.
     * It should not be modified.
     *
     * @return Photo to use when a valid Photo is needed
     */
    protected static Photo getDefaultPhoto() {
        return DEFAULT_PHOTO;
    }

    /**
     * Creates an identity for the specified Person and photo name.
     * Photos are owned by the Person and so their photo identity
     * is bound by the Person's identity.
     *
     * @param person creating a Photo
     * @param name   of the Photo
     * @return String representing the Photo's identity;
     * guaranteed to not be <code>null</code>
     * @throws IllegalArgumentException if either argument is <code>null</code>
     *                                  or the name argument is empty
     */
    protected static String getPhotoIdentity(Person person, String name) {
        if (person == null) {
            throw new  IllegalArgumentException("Person may not be null");
        }
        if (StringUtils.isBlank(name)) {
            throw new  IllegalArgumentException("Photo name may not be null or empty");
        }
        return person.getIdentity() + "/" + name;
    }

    /**
     * Returns the Photo identity associated with the specified Person.
     * The identity is unique for the Person but may not be
     * associated with an actual Photo since a Person is not
     * required to have a profile Photo.
     *
     * @param person used to look up an associated profile Photo
     * @return String representing the identity of the Person's profile
     * photo; guaranteed to not be <code>null</code>
     * @throws IllegalArgumentException if the argument is <code>null</code>
     */
    protected static String getProfilePhotoIdentity(Person person) {
        if (person == null) {
            throw new  IllegalArgumentException("Person may not be null");
        }
        return getPhotoIdentity(person, PROFILE_PHOTO_NAME);
    }

    protected static String getProfilePhotoName() {
        return PROFILE_PHOTO_NAME;
    }

    /**
     * Returns an image loading URL for the specified Person's
     * profile photo.
     * While the return value will exist, it may not actually
     * reference an actual photo since a Person may not have
     * an associated profile Photo.
     *
     * @param person used to look up a profile photo URL
     * @return String representing the URL to the profile photo;
     * guaranteed to not be <code>null</code>
     * @throws IllegalArgumentException if the argument is <code>null</code>
     */
    protected static String getProfilePhotoUrl(Person person) {
        if (person == null) {
            throw new  IllegalArgumentException("Person may not be null");
        }
        return THUMB_PHOTO_URL + getProfilePhotoIdentity(person);
    }

    /**
     * Returns an image loading URL for the specified Photo.
     *
     * @param photo used to create an image loading URL
     * @return String representing the URL to the photo;
     * guaranteed to not be <code>null</code>
     * @throws IllegalArgumentException if the argument is <code>null</code>
     */
    protected static String getPhotoUrl(Photo photo) {
        if (photo == null) {
            throw new  IllegalArgumentException("Photo may not be null");
        }
        return PHOTO_URL + photo.getIdentity();
    }

    /**
     * Returns an image loading URL for the specified Photo.
     * The image returned from this URL will be a thumbnail
     * size of the actual image.
     *
     * @param photo used to create an image loading URL
     * @return String representing the URL to the photo;
     * guaranteed to not be <code>null</code>
     * @throws IllegalArgumentException if the argument is <code>null</code>
     */
    protected static String getThumbPhotoUrl(Photo photo) {
        if (photo == null) {
            throw new  IllegalArgumentException("Photo may not be null");
        }
        return THUMB_PHOTO_URL + photo.getIdentity();
    }
}
