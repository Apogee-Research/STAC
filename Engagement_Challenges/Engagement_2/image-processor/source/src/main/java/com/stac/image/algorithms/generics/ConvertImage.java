package com.stac.image.algorithms.generics;

import com.stac.image.utilities.ARGB;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

/**
 *
 */
public class ConvertImage {
    public static BufferedImage grayscale(BufferedImage image) {
        return new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(image, null);
    }

    public static BufferedImage boostedGray(BufferedImage image) {
        BufferedImage out = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                int argb = image.getRGB(i,j);
                int a = ARGB.rawA(argb);
                int r = ARGB.rawR(argb);
                int g = ARGB.rawG(argb);
                int b = ARGB.rawB(argb);

                int val = (r+r+r+b+g+g+g+g)/8;
                out.setRGB(i,j, ARGB.toARGB(a, val, val, val));
            }
        }
        return out;
    }

    public static BufferedImage otherGray(BufferedImage image) {
        BufferedImage out = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = out.getGraphics();
        g.drawImage(image, 0, 0, null);

        return out;
    }
}
