package com.stac.image.algorithms;

import com.stac.image.ImageAlgorithm;

import java.awt.image.BufferedImage;

/**
 *
 */
abstract public class Filter extends ImageAlgorithm {

    /**
     * runAlgorithm runs the filter on the image.
     * @param image The image to process.
     */
    @Override
    public void runAlgorithm(BufferedImage image) {
        filter(image);
    }

    /**
     * Filters implement the filter method.
     *
     * Filters do not save values and therefore have no return value.
     *
     * @param image The image to run the filter on.
     */
    public abstract void filter(BufferedImage image);
}
