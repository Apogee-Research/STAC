package com.stac.image.algorithms.generics;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/**
 *
 */
public class Convolve {
    public static BufferedImage convolve(BufferedImage image, Kernel kernel) {
        return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null).filter(image, null);
    }

    public static final Kernel Gausian5x5 = new Kernel(5, 5, new float[]{
            1f / 331f,  4f / 331f,  7f / 331f,  4f / 331f,  1f / 331f,
            4f / 331f, 20f / 331f, 33f / 331f, 20f / 331f,  4f / 331f,
            7f / 331f, 33f / 331f, 55f / 331f, 33f / 331f,  7f / 331f,
            4f / 331f, 20f / 331f, 33f / 331f, 20f / 331f,  4f / 331f,
            1f / 331f,  4f / 331f,  7f / 331f,  4f / 331f,  1f / 331f
    });

    public static final Kernel Gausian3x3 = new Kernel(3, 3, new float[]{
            1f / 15f, 2f / 15f, 1f / 15f,
            2f / 15f, 3f / 15f, 2f / 15f,
            1f / 15f, 2f / 15f, 1f / 15f
    });
}
