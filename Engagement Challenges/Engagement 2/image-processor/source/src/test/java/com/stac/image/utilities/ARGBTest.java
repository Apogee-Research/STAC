package com.stac.image.utilities;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class ARGBTest {

    @Test
    public void testGetA() throws Exception {
        Assert.assertEquals(1, ARGB.getA(-1), 0.1);
        Assert.assertEquals(0, ARGB.getA(0), 0.1);
        Assert.assertEquals(1, ARGB.getA(0xFF000000), 0.1);
        Assert.assertEquals(0, ARGB.getA(0x00FFFFFF), 0.1);
    }

    @Test
    public void testGetR() throws Exception {
        Assert.assertEquals(1, ARGB.getR(-1), 0.1);
        Assert.assertEquals(0, ARGB.getR(0), 0.1);
        Assert.assertEquals(1, ARGB.getR(0x00FF0000), 0.1);
        Assert.assertEquals(0, ARGB.getR(0xFF00FFFF), 0.1);
    }

    @Test
    public void testGetG() throws Exception {
        Assert.assertEquals(1, ARGB.getG(-1), 0.1);
        Assert.assertEquals(0, ARGB.getG(0), 0.1);
        Assert.assertEquals(1, ARGB.getG(0x0000FF00), 0.1);
        Assert.assertEquals(0, ARGB.getG(0xFFFF00FF), 0.1);
    }

    @Test
    public void testGetB() throws Exception {
        Assert.assertEquals(1, ARGB.getB(-1), 0.1);
        Assert.assertEquals(0, ARGB.getB(0), 0.1);
        Assert.assertEquals(1, ARGB.getB(0x000000FF), 0.1);
        Assert.assertEquals(0, ARGB.getB(0xFFFFFF00), 0.1);
    }

    @Test
    public void testToARGB() throws Exception {
        Assert.assertEquals(0x000000FF, ARGB.toARGB(0f, 0f, 0f, 1f));
        Assert.assertEquals(0x0000FF00, ARGB.toARGB(0f, 0f, 1f, 0f));
        Assert.assertEquals(0x00FF0000, ARGB.toARGB(0f, 1f, 0f, 0f));
        Assert.assertEquals(0xFF000000, ARGB.toARGB(1f, 0f, 0f, 0f));
        Assert.assertEquals(0xFFFF0000, ARGB.toARGB(1f, 1f, 0f, 0f));
        Assert.assertEquals(0x00FFFF00, ARGB.toARGB(0f, 1f, 1f, 0f));
        Assert.assertEquals(0x0000FFFF, ARGB.toARGB(0f, 0f, 1f, 1f));
        Assert.assertEquals(0x000000FF, ARGB.toARGB(0, 0, 0, 255));
        Assert.assertEquals(0x0000FF00, ARGB.toARGB(0, 0, 255, 0));
        Assert.assertEquals(0x00FF0000, ARGB.toARGB(0, 255, 0, 0));
        Assert.assertEquals(0xFF000000, ARGB.toARGB(255, 0, 0, 0));
        Assert.assertEquals(0xFFFF0000, ARGB.toARGB(255, 255, 0, 0));
        Assert.assertEquals(0x00FFFF00, ARGB.toARGB(0, 255, 255, 0));
        Assert.assertEquals(0x0000FFFF, ARGB.toARGB(0, 0, 255, 255));
    }

    @Test
    public void testToHSVA() throws Exception {
        Assert.assertEquals(0, ARGB.toHSVA(ARGB.toARGB(1f, 1f, 0f, 0f))[0], 0.1);
        Assert.assertEquals(120, ARGB.toHSVA(ARGB.toARGB(1f, 0f, 1f, 0f))[0], 0.1);
        Assert.assertEquals(240, ARGB.toHSVA(ARGB.toARGB(1f, 0f, 0f, 1f))[0], 0.1);
    }
}