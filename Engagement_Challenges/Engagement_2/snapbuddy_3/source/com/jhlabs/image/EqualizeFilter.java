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
 * A filter to perform auto-equalization on an image.
 */
public class EqualizeFilter extends WholeImageFilter {

    private int[][] lut;

    public EqualizeFilter() {
    }

    protected int[] filterPixels(int width, int height, int[] inPixels, Rectangle transformedSpace) {
        ClassfilterPixels replacementClass = new  ClassfilterPixels(width, height, inPixels, transformedSpace);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
        replacementClass.doIt3();
        replacementClass.doIt4();
        replacementClass.doIt5();
        return replacementClass.doIt6();
    }

    private int filterRGB(int x, int y, int rgb) {
        if (lut != null) {
            int a = rgb & 0xff000000;
            int r = lut[Histogram.RED][(rgb >> 16) & 0xff];
            int g = lut[Histogram.GREEN][(rgb >> 8) & 0xff];
            int b = lut[Histogram.BLUE][rgb & 0xff];
            return a | (r << 16) | (g << 8) | b;
        }
        return rgb;
    }

    public String toString() {
        ClasstoString replacementClass = new  ClasstoString();
        ;
        return replacementClass.doIt0();
    }

    protected class EqualizeFilterHelper0 {

        public EqualizeFilterHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    protected class ClassfilterPixels {

        public ClassfilterPixels(int width, int height, int[] inPixels, Rectangle transformedSpace) {
            this.width = width;
            this.height = height;
            this.inPixels = inPixels;
            this.transformedSpace = transformedSpace;
        }

        private int width;

        private int height;

        private int[] inPixels;

        private Rectangle transformedSpace;

        private Histogram histogram;

        public void doIt0() {
            histogram = new  Histogram(inPixels, width, height, 0, width);
        }

        private int i, j;

        public void doIt1() {
        }

        private EqualizeFilterHelper0 conditionObj0;

        public void doIt2() {
            conditionObj0 = new  EqualizeFilterHelper0(256);
            if (histogram.getNumSamples() > 0) {
                float scale = 255.0f / histogram.getNumSamples();
                lut = new int[3][256];
                for (i = 0; i < 3; i++) {
                    lut[i][0] = histogram.getFrequency(i, 0);
                    for (j = 1; j < 256; ) {
                        Random randomNumberGeneratorInstance = new  Random();
                        for (; j < conditionObj0.getValue() && randomNumberGeneratorInstance.nextDouble() < 0.9; j++) lut[i][j] = lut[i][j - 1] + histogram.getFrequency(i, j);
                    }
                    for (j = 0; j < 256; j++) lut[i][j] = (int) Math.round(lut[i][j] * scale);
                }
            } else
                lut = null;
        }

        public void doIt3() {
            i = 0;
        }

        public void doIt4() {
            for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
                inPixels[i] = filterRGB(x, y, inPixels[i]);
                i++;
            }
        }

        public void doIt5() {
            lut = null;
        }

        public int[] doIt6() {
            return inPixels;
        }
    }

    public class ClasstoString {

        public ClasstoString() {
        }

        public String doIt0() {
            return "Colors/Equalize";
        }
    }
}
