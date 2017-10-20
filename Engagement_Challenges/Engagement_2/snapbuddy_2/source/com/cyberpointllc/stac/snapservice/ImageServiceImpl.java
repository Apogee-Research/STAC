package com.cyberpointllc.stac.snapservice;

import com.cyberpointllc.stac.snapservice.model.Filter;
import com.cyberpointllc.stac.snapservice.model.Photo;
import com.jhlabs.image.ScaleFilter;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImageServiceImpl implements ImageService {

    private static final String PHOTOS_SUBDIR = "photos";

    private static final int MAX_DIMENSIONS = 1024 * 1024;

    private static final BufferedImageOp THUMB_FILTER = new  ScaleFilter(100, 100);

    private final String basePath;

    /**
     * @param basePath the full path to the storage location
     */
    public ImageServiceImpl(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public BufferedImage getThumbnailImage(Photo photo) {
        return getFilteredImage(photo, null);
    }

    @Override
    public BufferedImage getPhotoImage(Photo photo) {
        return getFilteredImage(photo, null);
    }

    public Path getPath(Photo photo) {
        return Paths.get(basePath, PHOTOS_SUBDIR, photo.getPath());
    }

    public Path getBasePhotosPath() {
        return Paths.get(basePath, PHOTOS_SUBDIR);
    }

    public boolean isSmallPhoto(Photo photo) {
        Path photoPath = getPath(photo);
        try {
            BufferedImage image = ImageIO.read(photoPath.toFile());
            return isSmall(image);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private BufferedImage getFilteredImage(Photo photo, BufferedImageOp preFilter) {
        Path photoPath = getPath(photo);
        try {
            BufferedImage image = ImageIO.read(photoPath.toFile());
            // we check them before we let the user add a photo
            if (!isSmall(image)) {
                throw new  IllegalArgumentException("The dimensions of the image are too large");
            }
            if (preFilter != null) {
                image = preFilter.filter(image, null);
            }
            for (Filter filter : photo.getFilters()) {
                image = filter.filter(image);
            }
            return image;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean isSmall(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        return ((width * height) <= MAX_DIMENSIONS);
    }
}
