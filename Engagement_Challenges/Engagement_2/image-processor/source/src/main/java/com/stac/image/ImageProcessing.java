package com.stac.image;

import com.stac.learning.EuclideanVectorFactory;
import com.stac.learning.Vector;
import com.stac.learning.VectorFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.ArrayList;

/**
 *
 */
public class ImageProcessing {

    /**
     * This vector factory is used to construct vectors which use specific functions to compare against other vectors.
     */
    private static final VectorFactory VECTOR_FACTORY = new EuclideanVectorFactory(); // Use the Euclidean distance function.

    /**
     * Image algorithms are filters and detectors which are executed in order.
     */
    private final ArrayList<ImageAlgorithm> ALGORITHMS = new ArrayList<>();

    /**
     * image is the buffered image read from the imageFile.
     */
    private final BufferedImage image;

    private ImageProcessing() {
        image=null;
    }

    /**
     * Constructs an image processor from the image specified by filename.
     * @param imageFile The image file object.
     * @param processingChain
     * @throws IOException When processing the image fails due to IO errors.
     */
    private ImageProcessing(BufferedImage imageFile, String processingChain) throws IOException {
        image = imageFile;
        if (image.getWidth() * image.getHeight() > 250000) {
            throw new RuntimeException("This image is too large. Please reduce your image size to less than 250000 pixels");
        }

        final String[] algorithms = processingChain.split(",\\s*");
        for (String algorithm : algorithms) {
            final ImageAlgorithm ia = ImageAlgorithmStore.getAlgorithm(algorithm);
            if (ia == null) {
                throw new RuntimeException("Unknown algorithm: " + algorithm);
            }
            ALGORITHMS.add(ia);
        }
    }

    private int countDetectors() {
        int c = 0;
        for (ImageAlgorithm algorithm : ALGORITHMS) {
            if (algorithm.hasValue()) {
                c++;
            }
        }
        return c;
    }

    /**
     * Extract the features from the image loaded during construction.
     * @return A vector of feature attributes.
     */
    private Vector featureExtract() throws InvalidObjectException {
        Vector.VectorBuilder vectorBuilder = new Vector.VectorBuilder(VECTOR_FACTORY, countDetectors());

        for (ImageAlgorithm imageAlgorithm : ALGORITHMS) {
            imageAlgorithm.runAlgorithm(image);
            if (imageAlgorithm.hasValue()) {
                vectorBuilder.add(imageAlgorithm.getValue());
            }
        }

        return vectorBuilder.build();
    }

    /**
     * Run feature detection on the image at the specified filename.
     * @param imageFile The image file to load.
     * @param processingChain
     * @return The vector of extracted feature attributes.
     * @throws IOException when processing the image fails due to IO errors.
     */
    public static Vector getAttributeVector(File imageFile, String processingChain) throws IOException {
        return getAttributeVector(ImageIO.read(imageFile), processingChain);
    }

    /**
     * Run feature detection on the image at the specified filename.
     * @param image An image to process
     * @param processingChain
     * @return The vector of extracted feature attributes.
     * @throws IOException when processing the image fails due to IO errors.
     */
    public static Vector getAttributeVector(BufferedImage image, String processingChain) throws IOException {
        return new ImageProcessing(image, processingChain).featureExtract();
    }
}
