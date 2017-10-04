package com.cyberpointllc.stac.snapservice.model;

import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Photo {

    private final String path;

    private final boolean publicPhoto;

    private final String caption;

    private final Location location;

    private final List<Filter> filters;

    public Photo(String path, boolean isPublicPhoto, String caption, Location location, List<Filter> filters) {
        if (StringUtils.isBlank(path)) {
            throw new  IllegalArgumentException("Photo relative path may not be null or empty");
        }
        this.path = path;
        this.publicPhoto = isPublicPhoto;
        this.caption = StringUtils.isBlank(caption) ? "" : caption;
        this.location = (location != null) ? location : Location.UNKNOWN;
        this.filters = new  ArrayList();
        if (filters != null) {
            this.filters.addAll(filters);
        }
    }

    /**
     * Returns the identity for this Photo.
     * The identity may not be modified.
     *
     * @return String representing the identity;
     * guaranteed to not be <code>null</code>
     */
    public String getIdentity() {
        return path;
    }

    /**
     * Returns the relative path to the Photo image.
     * The path is the identity for the Photo and
     * may not be modified.
     *
     * @return String representing the relative path to the image;
     * guaranteed to not be empty or <code>null</code>
     */
    public String getPath() {
        return path;
    }

    /**
     * A public photo can be seen by anyone;
     * non-public photos can only be seen by the
     * owner and their friends.
     * By default, a photo is not public (<code>false</code>).
     *
     * @return boolean true if this photo is for public viewing
     */
    public boolean isPublicPhoto() {
        return publicPhoto;
    }

    /**
     * Returns the optional caption assigned to the photo.
     * This field is always assigned but may be blank
     * if no caption was assigned initially.
     *
     * @return String representing the photo caption;
     * may be empty but guaranteed to not <code>null</code>
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Returns the location where the photo was taken.
     * Returns the unknown location if not known.
     *
     * @return Location where the photo was taken;
     * guaranteed to not be <code>null</code>
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Returns an unmodifiable list of the filters assigned
     * to this photo.
     *
     * @return List of filters assigned to this photo;
     * may be empty but guaranteed to not be <code>null</code>
     */
    public List<Filter> getFilters() {
        return Collections.unmodifiableList(filters);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Photo photo = (Photo) obj;
        return path.equals(photo.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
