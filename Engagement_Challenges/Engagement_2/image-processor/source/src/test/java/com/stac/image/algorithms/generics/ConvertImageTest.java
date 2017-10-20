package com.stac.image.algorithms.generics;

import com.stac.image.utilities.ARGB;
import org.junit.Assert;
import org.junit.Test;

import java.awt.image.BufferedImage;

/**
 *
 */
public class ConvertImageTest {

    @Test
    public void testGrayScale() throws Exception {
        BufferedImage im = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);

        for (int i = 0; i < im.getWidth(); i++) {
            for (int j = 0; j < im.getHeight(); j++) {
                im.setRGB(i, j, ARGB.toARGB(1, .1f, .5f, .9f));
            }
        }

        BufferedImage grayscale = ConvertImage.grayscale(im);

        int px = grayscale.getRGB(0, 0);
        Assert.assertEquals(ARGB.getB(px), ARGB.getR(px), 0.001);
    }

    @Test
    public void testBoostedGray() throws Exception {
        BufferedImage im = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);

        for (int i = 0; i < im.getWidth(); i++) {
            for (int j = 0; j < im.getHeight(); j++) {
                im.setRGB(i, j, ARGB.toARGB(1, 1, 1, 1));
            }
        }

        BufferedImage grayscale = ConvertImage.boostedGray(im);

        int px = grayscale.getRGB(0, 0);
        Assert.assertEquals(ARGB.getB(px), ARGB.getR(px), 0.001);
    }
}