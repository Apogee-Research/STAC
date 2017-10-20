package com.stac.image.algorithms.filters;

import com.stac.image.algorithms.Filter;

import java.awt.image.BufferedImage;

/**
 * The invert filter converts the image and inverts the pixel colors.
 */
public class Invert extends Filter {
    private boolean invertAlpha = false;

    /**
     * Constructs an inversion filter which inverts red, green, and blue values.
     */
    public Invert() {}

    /**
     * Constructs an inversion filter which inverts red, green, blue, and possibly alpha values.
     *
     * @param invertAlpha Set to true if inverting the alpha channel is needed.
     */
    public Invert(boolean invertAlpha) {
        this.invertAlpha = invertAlpha;
    }

    /**
     * Inverts the pixels of the image.
     * @param image The image to run the filter on.
     */
    @Override
    public void filter(BufferedImage image) {
        final int width = image.getWidth();
        final int height = image.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                final int argbIn = image.getRGB(x, y);
                int alpha = ((argbIn >> 24) & 0xFF);
                if (invertAlpha) alpha = 0xFF - alpha;
                image.setRGB(x, y, (alpha << 24) |
                        (0xFF - ((argbIn >> 16) & 0xFF) << 16) |
                        (0xFF - ((argbIn >> 8) & 0xFF) << 8) |
                        (0xFF - (argbIn & 0xFF)));
            }
        }
    }
}
