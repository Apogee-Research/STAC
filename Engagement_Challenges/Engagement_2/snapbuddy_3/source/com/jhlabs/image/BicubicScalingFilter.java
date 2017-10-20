/*
Derived fom BicubicScaleFiler.java. In this version we adjust the
dimensions by a multiple instead of targeting a fixed destination
dimension. - Tom Hennen - CyberPoint LLC

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
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

/**
 * Scales an image using bi-cubic interpolation, which can't be done with AffineTransformOp.
 */
public class BicubicScalingFilter extends AbstractBufferedImageOp {

    private double widthScale;

    private double heightScale;

    /**
     * Construct a BicubicScaleFilter which resizes to 32x32 pixels.
     */
    public BicubicScalingFilter() {
        this(0.5, 0.5);
    }

    /**
     * @param widthScale the factor to scale the width by
     * @param heightScale the factor to scale the height by
	 */
    public BicubicScalingFilter(double widthScale, double heightScale) {
        this.widthScale = widthScale;
        this.heightScale = heightScale;
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        Classfilter replacementClass = new  Classfilter(src, dst);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
        replacementClass.doIt3();
        return replacementClass.doIt4();
    }

    public String toString() {
        return "Distort/Bicubic Scaling";
    }

    public class Classfilter {

        public Classfilter(BufferedImage src, BufferedImage dst) {
            this.src = src;
            this.dst = dst;
        }

        private BufferedImage src;

        private BufferedImage dst;

        private int w;

        public void doIt0() {
            w = src.getWidth();
        }

        private int h;

        private int dstWidth;

        public void doIt1() {
            h = src.getHeight();
            dstWidth = (int) (w * widthScale);
        }

        private int dstHeight;

        private Graphics2D g;

        public void doIt2() {
            dstHeight = (int) (h * heightScale);
            if (dst == null) {
                ColorModel dstCM = src.getColorModel();
                dst = new  BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(dstWidth, dstHeight), dstCM.isAlphaPremultiplied(), null);
            }
            g = dst.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        }

        public void doIt3() {
            g.drawImage(src, 0, 0, dstWidth, dstHeight, null);
        }

        public BufferedImage doIt4() {
            g.dispose();
            return dst;
        }
    }
}
