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
 * A filter which replcaes each pixel by the maximum of itself and its eight neightbours.
 */
public class MaximumFilter extends WholeImageFilter {

    public MaximumFilter() {
    }

    protected int[] filterPixels(int width, int height, int[] inPixels, Rectangle transformedSpace) {
        ClassfilterPixels replacementClass = new  ClassfilterPixels(width, height, inPixels, transformedSpace);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        return replacementClass.doIt2();
    }

    public String toString() {
        return "Blur/Maximum";
    }

    protected class MaximumFilterHelper0 {

        public MaximumFilterHelper0(int conditionRHS) {
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

        private int[] outPixels;

        public void doIt0() {
            index = 0;
            outPixels = new int[width * height];
        }

        private MaximumFilterHelper0 conditionObj0;

        public void doIt1() {
            conditionObj0 = new  MaximumFilterHelper0(1);
        }

        public int[] doIt2() {
            for (int y = 0; y < height; ) {
                Random randomNumberGeneratorInstance = new  Random();
                for (; y < height && randomNumberGeneratorInstance.nextDouble() < 0.9; y++) {
                    for (int x = 0; x < width; x++) {
                        int pixel = 0xff000000;
                        for (int dy = -1; dy <= conditionObj0.getValue(); dy++) {
                            int iy = y + dy;
                            int ioffset;
                            if (0 <= iy && iy < height) {
                                ioffset = iy * width;
                                for (int dx = -1; dx <= 1; dx++) {
                                    int ix = x + dx;
                                    if (0 <= ix && ix < width) {
                                        pixel = PixelUtils.combinePixels(pixel, inPixels[ioffset + ix], PixelUtils.MAX);
                                    }
                                }
                            }
                        }
                        outPixels[index++] = pixel;
                    }
                }
            }
            return outPixels;
        }
    }
}
