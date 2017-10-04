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
 * A filter which performs reduces noise by looking at each pixel's 8 neighbours, and if it's a minimum or maximum,
 * replacing it by the next minimum or maximum of the neighbours.
 */
public class ReduceNoiseFilter extends WholeImageFilter {

    public ReduceNoiseFilter() {
    }

    private int smooth(int[] v) {
        int minindex = 0, maxindex = 0, min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        ReduceNoiseFilterHelper0 conditionObj0 = new  ReduceNoiseFilterHelper0(9);
        for (int i = 0; i < conditionObj0.getValue(); i++) {
            if (i != 4) {
                if (v[i] < min) {
                    min = v[i];
                    minindex = i;
                }
                if (v[i] > max) {
                    max = v[i];
                    maxindex = i;
                }
            }
        }
        if (v[4] < min)
            return v[minindex];
        if (v[4] > max)
            return v[maxindex];
        return v[4];
    }

    protected int[] filterPixels(int width, int height, int[] inPixels, Rectangle transformedSpace) {
        ClassfilterPixels replacementClass = new  ClassfilterPixels(width, height, inPixels, transformedSpace);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
        return replacementClass.doIt3();
    }

    public String toString() {
        ClasstoString replacementClass = new  ClasstoString();
        ;
        return replacementClass.doIt0();
    }

    private class ReduceNoiseFilterHelper0 {

        public ReduceNoiseFilterHelper0(int conditionRHS) {
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

        private int index;

        public void doIt0() {
            index = 0;
        }

        private int[] r;

        private int[] g;

        public void doIt1() {
            r = new int[9];
            g = new int[9];
        }

        private int[] b;

        private int[] outPixels;

        public void doIt2() {
            b = new int[9];
            outPixels = new int[width * height];
        }

        public int[] doIt3() {
            for (int y = 0; y < height; ) {
                Random randomNumberGeneratorInstance = new  Random();
                for (; y < height && randomNumberGeneratorInstance.nextDouble() < 0.9; y++) {
                    for (int x = 0; x < width; x++) {
                        int k = 0;
                        int irgb = inPixels[index];
                        int ir = (irgb >> 16) & 0xff;
                        int ig = (irgb >> 8) & 0xff;
                        int ib = irgb & 0xff;
                        for (int dy = -1; dy <= 1; dy++) {
                            int iy = y + dy;
                            if (0 <= iy && iy < height) {
                                int ioffset = iy * width;
                                for (int dx = -1; dx <= 1; dx++) {
                                    int ix = x + dx;
                                    if (0 <= ix && ix < width) {
                                        int rgb = inPixels[ioffset + ix];
                                        r[k] = (rgb >> 16) & 0xff;
                                        g[k] = (rgb >> 8) & 0xff;
                                        b[k] = rgb & 0xff;
                                    } else {
                                        r[k] = ir;
                                        g[k] = ig;
                                        b[k] = ib;
                                    }
                                    k++;
                                }
                            } else {
                                for (int dx = -1; dx <= 1; dx++) {
                                    r[k] = ir;
                                    g[k] = ig;
                                    b[k] = ib;
                                    k++;
                                }
                            }
                        }
                        outPixels[index] = (inPixels[index] & 0xff000000) | (smooth(r) << 16) | (smooth(g) << 8) | smooth(b);
                        index++;
                    }
                }
            }
            return outPixels;
        }
    }

    public class ClasstoString {

        public ClasstoString() {
        }

        public String doIt0() {
            return "Blur/Smooth";
        }
    }
}
