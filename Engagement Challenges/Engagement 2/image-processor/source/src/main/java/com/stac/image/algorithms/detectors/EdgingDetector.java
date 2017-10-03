package com.stac.image.algorithms.detectors;

import com.stac.image.algorithms.Detector;
import com.stac.image.algorithms.generics.CannyEdgeDetect;

import java.awt.image.BufferedImage;

/**
 *
 */
public class EdgingDetector extends Detector {
    /**
     * Detectors implement the detect method to detect features in the image.
     *
     * @param image The image to run the detector over.
     * @return The value the detector wishes to save. See {@link #getValue()}.
     */
    @Override
    public float detect(BufferedImage image) {
        BufferedImage cannied = CannyEdgeDetect.detect(image, 125, 220);

        return new WhiteDetector().detect(cannied);
    }
}
