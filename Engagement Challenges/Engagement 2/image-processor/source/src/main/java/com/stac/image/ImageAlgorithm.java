package com.stac.image;

import java.awt.image.BufferedImage;

/**
 * An image algorithm takes an image, processes it, and possibly returns a value to the caller.
 *
 * Should an image algorithms return a value, they will implement the hasValue and getValue methods.
 *
 * All image algorithms must implement the runAlgorithm method.
 */
abstract public class ImageAlgorithm {
    /**
     * hasValue returns true if the getValue method should return a value.
     * Algorithms should implement this method if it wishes to return values along with the {@link #getValue()} method.
     * @return A value.
     */
    public boolean hasValue() { return false; }

    /**
     * getValue returns a stored value from a previous run of the algorithm.
     * Algorithms should implement this method in addition to the {@link #hasValue()} method.
     * @return The stored value.
     */
    public float getValue() { throw new RuntimeException("This image algorithm does not implement values"); }

    /**
     * runAlgorithm must be implemented by all algorithms to proccess the image.
     * @param image The image to process.
     */
    public abstract void runAlgorithm(BufferedImage image);
}
