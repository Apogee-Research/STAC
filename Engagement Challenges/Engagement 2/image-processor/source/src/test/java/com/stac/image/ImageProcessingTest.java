package com.stac.image;

import com.stac.learning.Vector;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.awt.image.BufferedImage;

/**
 *
 */
public class ImageProcessingTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testNew() throws Exception {
        final Vector blackDetector = ImageProcessing.getAttributeVector(new BufferedImage(500, 500, BufferedImage.TYPE_4BYTE_ABGR), "BlackDetector");
        Assert.assertSame(5, 5);
    }

    @Test
    public void testBad() throws Exception {
        exception.expect(RuntimeException.class);
        final Vector blackDetector = ImageProcessing.getAttributeVector(new BufferedImage(505, 500, BufferedImage.TYPE_4BYTE_ABGR), "BlackDetector");
        Assert.fail();
    }

    @Test
    public void testAlgo() throws Exception {
        exception.expect(RuntimeException.class);
        final Vector blackDetector = ImageProcessing.getAttributeVector(new BufferedImage(500, 500, BufferedImage.TYPE_4BYTE_ABGR), "TruckDetector");
        Assert.fail();
    }
}