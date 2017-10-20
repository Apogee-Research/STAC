package com.stac.image.algorithms.detectors;

import com.stac.image.algorithms.Detector;
import com.stac.image.utilities.ARGB;
import org.junit.Assert;
import org.junit.Test;

import java.awt.image.BufferedImage;

/**
 *
 */
public class RedDetectorTest {
    Detector detector = new RedDetector();

    @Test
    public void testImageOverrides() throws Exception {
        BufferedImage im = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB) {
            @Override
            public int getRGB(int x, int y) {
                return 100;
            }
        };

        Assert.assertEquals(5, im.getHeight());
        Assert.assertEquals(5, im.getWidth());
        Assert.assertEquals(BufferedImage.TYPE_INT_ARGB, im.getType());

        Assert.assertEquals(100, im.getRGB(0,0));
    }

    @Test
    public void testGreen() throws Exception {
        BufferedImage image = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB) {
            @Override
            public int getRGB(int x, int y) {
                return ARGB.toARGB(1f, 0f, 1f, 0f);
            }
        };

        Assert.assertEquals(0, detector.detect(image), 0.1);
    }

    @Test
    public void testRed() throws Exception {
        BufferedImage image = new BufferedImage(5,5, BufferedImage.TYPE_INT_ARGB) {
            @Override
            public int getRGB(int x, int y) {
                return ARGB.toARGB(1f, 1f, 0f, 0f);
            }
        };

        Assert.assertEquals(1, detector.detect(image), 0.1);
    }

    @Test
    public void testBlue() throws Exception {
        BufferedImage image = new BufferedImage(5,5, BufferedImage.TYPE_INT_ARGB) {
            @Override
            public int getRGB(int x, int y) {
                return ARGB.toARGB(1f, 0f, 0f, 1f);
            }
        };

        Assert.assertEquals(0, detector.detect(image), 0.1);
    }

    @Test
    public void testBlack() throws Exception {
        BufferedImage image = new BufferedImage(5,5, BufferedImage.TYPE_INT_ARGB) {
            @Override
            public int getRGB(int x, int y) {
                return ARGB.toARGB(1f, 0f, 0f, 0f);
            }
        };

        Assert.assertEquals(0, detector.detect(image),  0.1);
    }

    @Test
    public void testWhite() throws Exception {
        BufferedImage image = new BufferedImage(5,5, BufferedImage.TYPE_INT_ARGB) {
            @Override
            public int getRGB(int x, int y) {
                return ARGB.toARGB(1f, 1f, 1f, 1f);
            }
        };

        Assert.assertEquals(0, detector.detect(image), 0.1);
    }
}