package com.stac.image.algorithms.detectors;

import com.stac.image.algorithms.Detector;
import com.stac.image.utilities.ARGB;

import java.awt.image.BufferedImage;

/**
 *
 */
public class RedDetector extends Detector {
    @Override
    public float detect(BufferedImage image) {
        final int width = image.getWidth();
        final int height = image.getHeight();
        int count = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float[] hsva = ARGB.toHSVA(image.getRGB(x, y));
                if (hsva[2] >= 0.50 && hsva[1] >= 0.15 && (hsva[0] > 300 || hsva[0] < 60)) {
                    count++;
                }
            }
        }
        return count/((float) width * height);
    }
}
