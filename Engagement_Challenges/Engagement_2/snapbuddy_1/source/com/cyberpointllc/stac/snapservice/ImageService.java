package com.cyberpointllc.stac.snapservice;

import com.cyberpointllc.stac.snapservice.model.Photo;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public interface ImageService {

    /**
     * Returns the image associated with the specified Photo
     * but returns it as a thumbnail image.
     * Any associated filters will be applied to the image.
     *
     * @param photo to be returned as a thumbnail image
     * @return BufferedImage associated with the photo
     */
    BufferedImage getThumbnailImage(Photo photo);

    /**
     * Returns the image associated with the specified Photo.
     * Any associated filters will be applied to the image.
     *
     * @param photo to be returned as an image
     * @return BufferedImage associated with the photo
     */
    BufferedImage getPhotoImage(Photo photo);

    Path getPath(Photo photo);

    Path getBasePhotosPath();

    boolean isSmallPhoto(Photo photo);
}
