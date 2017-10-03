package com.stac.image.algorithms.filters;

import com.stac.image.utilities.ARGB;
import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 *
 */
public class IntensifyTest {

    @Test
    public void testIntensifyRealImage() throws Exception {
        BufferedImage before = ImageIO.read(ClassLoader.getSystemResource("3kitties.jpg"));

        BufferedImage after = new BufferedImage(before.getWidth(), before.getHeight(), before.getType());

        after.setData(before.getData());

        //new ShowImage("before_intense", before);

        new Intensify().filter(after);

        //new ShowImage("after_intensified", after);

        int rgb = after.getRGB(0, 0);

        Assert.assertEquals(ARGB.rawA(rgb), 0xFF);
    }

    @Test
    public void testIntensifyBlack() throws Exception {
        BufferedImage before;
        before = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < before.getWidth(); i++) {
            for (int j = 0; j < before.getHeight(); j++) {
                before.setRGB(i, j, 0xFF000000);
            }
        }

        BufferedImage after = new BufferedImage(before.getWidth(), before.getHeight(), before.getType());

        after.setData(before.getData());

        new Intensify().filter(after);

        //new ShowImage("after_intensified", after);

        int rgb = after.getRGB(0, 0);
    }

    @Test
    public void testIntensifyBad() throws Exception {
        BufferedImage before;
        before = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < before.getWidth(); i++) {
            for (int j = 0; j < before.getHeight(); j++) {
                before.setRGB(i, j, 0xFF0F0F0F);
            }
        }

        BufferedImage after = new BufferedImage(before.getWidth(), before.getHeight(), before.getType());

        after.setData(before.getData());

        new Intensify().filter(after);

        //new ShowImage("after_intensified", after);

        int rgb = after.getRGB(0, 0);

        Assert.assertEquals(ARGB.rawA(rgb), 0xFF);
    }
}