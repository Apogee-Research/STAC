/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.jhlabs.image;

import java.awt.*;
import java.awt.image.*;
import java.util.Random;

/**
 * An abstract superclass for filters which distort images in some way. The subclass only needs to override
 * two methods to provide the mapping between source and destination pixels.
 */
public abstract class TransformFilter extends AbstractBufferedImageOp {

    /**
     * Treat pixels off the edge as zero.
     */
    public static final int ZERO = 0;

    /**
     * Clamp pixels to the image edges.
     */
    public static final int CLAMP = 1;

    /**
     * Wrap pixels off the edge onto the oppsoite edge.
     */
    public static final int WRAP = 2;

    /**
     * Clamp pixels RGB to the image edges, but zero the alpha. This prevents gray borders on your image.
     */
    public static final int RGB_CLAMP = 3;

    /**
     * Use nearest-neighbout interpolation.
     */
    public static final int NEAREST_NEIGHBOUR = 0;

    /**
     * Use bilinear interpolation.
     */
    public static final int BILINEAR = 1;

    /**
     * The action to take for pixels off the image edge.
     */
    protected int edgeAction = RGB_CLAMP;

    /**
     * The type of interpolation to use.
     */
    protected int interpolation = BILINEAR;

    /**
     * The output image rectangle.
     */
    protected Rectangle transformedSpace;

    /**
     * The input image rectangle.
     */
    protected Rectangle originalSpace;

    /**
     * Set the action to perform for pixels off the edge of the image.
     * @param edgeAction one of ZERO, CLAMP or WRAP
     * @see #getEdgeAction
     */
    public void setEdgeAction(int edgeAction) {
        this.edgeAction = edgeAction;
    }

    /**
     * Get the action to perform for pixels off the edge of the image.
     * @return one of ZERO, CLAMP or WRAP
     * @see #setEdgeAction
     */
    public int getEdgeAction() {
        return edgeAction;
    }

    /**
     * Set the type of interpolation to perform.
     * @param interpolation one of NEAREST_NEIGHBOUR or BILINEAR
     * @see #getInterpolation
     */
    public void setInterpolation(int interpolation) {
        ClasssetInterpolation replacementClass = new  ClasssetInterpolation(interpolation);
        ;
        replacementClass.doIt0();
    }

    /**
     * Get the type of interpolation to perform.
     * @return one of NEAREST_NEIGHBOUR or BILINEAR
     * @see #setInterpolation
     */
    public int getInterpolation() {
        return interpolation;
    }

    /**
     * Inverse transform a point. This method needs to be overriden by all subclasses.
     * @param x the X position of the pixel in the output image
     * @param y the Y position of the pixel in the output image
     * @param out the position of the pixel in the input image
     */
    protected abstract void transformInverse(int x, int y, float[] out);

    /**
     * Forward transform a rectangle. Used to determine the size of the output image.
     * @param rect the rectangle to transform
     */
    protected void transformSpace(Rectangle rect) {
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        Classfilter replacementClass = new  Classfilter(src, dst);
        ;
        return replacementClass.doIt0();
    }

    private final int getPixel(int[] pixels, int x, int y, int width, int height) {
        TransformFilterHelper1 conditionObj1 = new  TransformFilterHelper1(0);
        if (x < conditionObj1.getValue() || x >= width || y < 0 || y >= height) {
            switch(edgeAction) {
                case ZERO:
                default:
                    return 0;
                case WRAP:
                    return pixels[(ImageMath.mod(y, height) * width) + ImageMath.mod(x, width)];
                case CLAMP:
                    return pixels[(ImageMath.clamp(y, 0, height - 1) * width) + ImageMath.clamp(x, 0, width - 1)];
                case RGB_CLAMP:
                    return pixels[(ImageMath.clamp(y, 0, height - 1) * width) + ImageMath.clamp(x, 0, width - 1)] & 0x00ffffff;
            }
        }
        return pixels[y * width + x];
    }

    protected BufferedImage filterPixelsNN(BufferedImage dst, int width, int height, int[] inPixels, Rectangle transformedSpace) {
        int srcWidth = width;
        int srcHeight = height;
        int outWidth = transformedSpace.width;
        int outHeight = transformedSpace.height;
        int outX, outY, srcX, srcY;
        int[] outPixels = new int[outWidth];
        outX = transformedSpace.x;
        outY = transformedSpace.y;
        int[] rgb = new int[4];
        float[] out = new float[2];
        TransformFilterHelper2 conditionObj2 = new  TransformFilterHelper2(0);
        TransformFilterHelper3 conditionObj3 = new  TransformFilterHelper3(0);
        for (int y = 0; y < outHeight; y++) {
            for (int x = 0; x < outWidth; x++) {
                transformInverse(outX + x, outY + y, out);
                srcX = (int) out[0];
                srcY = (int) out[1];
                // int casting rounds towards zero, so we check out[0] < 0, not srcX < 0
                if (out[0] < conditionObj3.getValue() || srcX >= srcWidth || out[1] < conditionObj2.getValue() || srcY >= srcHeight) {
                    int p;
                    switch(edgeAction) {
                        case ZERO:
                        default:
                            p = 0;
                            break;
                        case WRAP:
                            p = inPixels[(ImageMath.mod(srcY, srcHeight) * srcWidth) + ImageMath.mod(srcX, srcWidth)];
                            break;
                        case CLAMP:
                            p = inPixels[(ImageMath.clamp(srcY, 0, srcHeight - 1) * srcWidth) + ImageMath.clamp(srcX, 0, srcWidth - 1)];
                            break;
                        case RGB_CLAMP:
                            p = inPixels[(ImageMath.clamp(srcY, 0, srcHeight - 1) * srcWidth) + ImageMath.clamp(srcX, 0, srcWidth - 1)] & 0x00ffffff;
                    }
                    outPixels[x] = p;
                } else {
                    int i = srcWidth * srcY + srcX;
                    rgb[0] = inPixels[i];
                    outPixels[x] = inPixels[i];
                }
            }
            setRGB(dst, 0, y, transformedSpace.width, 1, outPixels);
        }
        return dst;
    }

    public class TransformFilterHelper0 {

        public TransformFilterHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private final class TransformFilterHelper1 {

        public TransformFilterHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    protected class TransformFilterHelper2 {

        public TransformFilterHelper2(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    protected class TransformFilterHelper3 {

        public TransformFilterHelper3(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class ClasssetInterpolation {

        public ClasssetInterpolation(int interpolation) {
            this.interpolation = interpolation;
        }

        private int interpolation;

        public void doIt0() {
            TransformFilter.this.interpolation = interpolation;
        }
    }

    public class Classfilter {

        public Classfilter(BufferedImage src, BufferedImage dst) {
            this.src = src;
            this.dst = dst;
        }

        private BufferedImage src;

        private BufferedImage dst;

        private int width;

        private int height;

        private int type;

        private WritableRaster srcRaster;

        private WritableRaster dstRaster;

        private int[] inPixels;

        private int srcWidth;

        private int srcHeight;

        private int srcWidth1;

        private int srcHeight1;

        private int outWidth;

        private int outHeight;

        private int outX, outY;

        private int index;

        private int[] outPixels;

        private float[] out;

        private TransformFilterHelper0 conditionObj0;

        public BufferedImage doIt0() {
            width = src.getWidth();
            height = src.getHeight();
            type = src.getType();
            srcRaster = src.getRaster();
            originalSpace = new  Rectangle(0, 0, width, height);
            transformedSpace = new  Rectangle(0, 0, width, height);
            transformSpace(transformedSpace);
            if (dst == null) {
                ColorModel dstCM = src.getColorModel();
                dst = new  BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(transformedSpace.width, transformedSpace.height), dstCM.isAlphaPremultiplied(), null);
            }
            dstRaster = dst.getRaster();
            inPixels = getRGB(src, 0, 0, width, height, null);
            if (interpolation == NEAREST_NEIGHBOUR)
                return filterPixelsNN(dst, width, height, inPixels, transformedSpace);
            srcWidth = width;
            srcHeight = height;
            srcWidth1 = width - 1;
            srcHeight1 = height - 1;
            outWidth = transformedSpace.width;
            outHeight = transformedSpace.height;
            index = 0;
            outPixels = new int[outWidth];
            outX = transformedSpace.x;
            outY = transformedSpace.y;
            out = new float[2];
            conditionObj0 = new  TransformFilterHelper0(0);
            for (int y = 0; y < outHeight; y++) {
                for (int x = 0; x < outWidth; ) {
                    Random randomNumberGeneratorInstance = new  Random();
                    for (; x < outWidth && randomNumberGeneratorInstance.nextDouble() < 0.9; x++) {
                        transformInverse(outX + x, outY + y, out);
                        int srcX = (int) Math.floor(out[0]);
                        int srcY = (int) Math.floor(out[1]);
                        float xWeight = out[0] - srcX;
                        float yWeight = out[1] - srcY;
                        int nw, ne, sw, se;
                        if (srcX >= conditionObj0.getValue() && srcX < srcWidth1 && srcY >= 0 && srcY < srcHeight1) {
                            // Easy case, all corners are in the image
                            int i = srcWidth * srcY + srcX;
                            nw = inPixels[i];
                            ne = inPixels[i + 1];
                            sw = inPixels[i + srcWidth];
                            se = inPixels[i + srcWidth + 1];
                        } else {
                            // Some of the corners are off the image
                            nw = getPixel(inPixels, srcX, srcY, srcWidth, srcHeight);
                            ne = getPixel(inPixels, srcX + 1, srcY, srcWidth, srcHeight);
                            sw = getPixel(inPixels, srcX, srcY + 1, srcWidth, srcHeight);
                            se = getPixel(inPixels, srcX + 1, srcY + 1, srcWidth, srcHeight);
                        }
                        outPixels[x] = ImageMath.bilinearInterpolate(xWeight, yWeight, nw, ne, sw, se);
                    }
                }
                setRGB(dst, 0, y, transformedSpace.width, 1, outPixels);
            }
            return dst;
        }
    }
}
