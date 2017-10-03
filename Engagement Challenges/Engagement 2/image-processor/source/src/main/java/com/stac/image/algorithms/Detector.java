package com.stac.image.algorithms;

import com.stac.image.ImageAlgorithm;

import java.awt.image.BufferedImage;

/**
 *
 */
abstract public class Detector extends ImageAlgorithm {
    /**
     * value stores the value which this detector returns.
     */
    private float value = 0;

    /**
     * Runs the detector and saves the value to {@link #value}.
     * @param image The image to process.
     */
    @Override
    public void runAlgorithm(BufferedImage image) {
        value = detect(image);
    }

    /**
     * hasValue returns true for all detectors.
     * @return true.
     */
    @Override
    public boolean hasValue() {
        return true;
    }

    /**
     * getValue returns a stored value from a previous run of the detector.
     * @return The detector's response to the image.
     */
    @Override
    public float getValue() {
        return value;
    }

    /**
     * Detectors implement the detect method to detect features in the image.
     * @param image The image to run the detector over.
     * @return The value the detector wishes to save. See {@link #getValue()}.
     */
    public abstract float detect(BufferedImage image);
}
