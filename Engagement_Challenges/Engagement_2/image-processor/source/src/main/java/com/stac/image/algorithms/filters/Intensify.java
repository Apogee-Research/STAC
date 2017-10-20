package com.stac.image.algorithms.filters;

import com.stac.image.algorithms.Filter;
import com.stac.image.utilities.ARGB;
import com.stac.mathematics.Mathematics;

import java.awt.image.BufferedImage;

/**
 * The Intensify filter is where the bug resides. More specifically the three calls to
 * {@link Mathematics#intensify(int, int, int, int)} in this filter.
 */
public class Intensify extends Filter {
    /**
     * Filters implement the filter method.
     * <p>
     * Filters do not save values and therefore have no return value.
     *
     * @param image The image to run the filter on.
     */
    @Override
    public void filter(BufferedImage image) {
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                int rgb00 = image.getRGB(bound(0, image.getWidth(), i-1), bound(0, image.getHeight(), j-1));
                int rgb01 = image.getRGB(bound(0, image.getWidth(), i-1), bound(0, image.getHeight(), j));
                int rgb02 = image.getRGB(bound(0, image.getWidth(), i-1), bound(0, image.getHeight(), j+1));
                int rgb11 = image.getRGB(i, j);
                int rgb20 = image.getRGB(bound(0, image.getWidth(), i+1), bound(0, image.getHeight(), j-1));
                int rgb21 = image.getRGB(bound(0, image.getWidth(), i+1), bound(0, image.getHeight(), j));
                int rgb22 = image.getRGB(bound(0, image.getWidth(), i+1), bound(0, image.getHeight(), j+1));

                int m = Mathematics.intensify(ARGB.rawA(rgb11), ARGB.rawR(rgb00), ARGB.rawG(rgb11), ARGB.rawB(rgb22));
                int n = Mathematics.intensify(ARGB.rawA(rgb11), ARGB.rawR(rgb01), ARGB.rawG(rgb11), ARGB.rawB(rgb21));
                int o = Mathematics.intensify(ARGB.rawA(rgb11), ARGB.rawR(rgb02), ARGB.rawG(rgb11), ARGB.rawB(rgb20));

                long avg = (long) m + m + m + n + n + o + o + o;

                image.setRGB(i, j, ((int)avg>>3) | 0xFF000000);
            }
        }
    }

    private int bound(int min, int max, int i) {
        if (i >= min) {
            if (i < max) return i;
            return max - 1;
        }
        return 0;
    }
}
