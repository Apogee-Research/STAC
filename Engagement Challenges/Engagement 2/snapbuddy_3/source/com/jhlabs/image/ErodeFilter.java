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

/**
 * Given a binary image, this filter performs binary erosion, setting all removed pixels to the given 'new' color.
 */
public class ErodeFilter extends BinaryFilter {

    private int threshold = 2;

    public ErodeFilter() {
        newColor = 0xffffffff;
    }

    /**
	 * Set the threshold - the number of neighbouring pixels for dilation to occur.
	 * @param threshold the new threshold
     * @see #getThreshold
	 */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    /**
	 * Return the threshold - the number of neighbouring pixels for dilation to occur.
	 * @return the current threshold
     * @see #setThreshold
	 */
    public int getThreshold() {
        return threshold;
    }

    protected int[] filterPixels(int width, int height, int[] inPixels, Rectangle transformedSpace) {
        int[] outPixels = new int[width * height];
        ErodeFilterHelper0 conditionObj0 = new  ErodeFilterHelper0(1);
        ErodeFilterHelper1 conditionObj1 = new  ErodeFilterHelper1(0);
        for (int i = 0; i < iterations; i++) {
            int index = 0;
            if (i > conditionObj1.getValue()) {
                int[] t = inPixels;
                inPixels = outPixels;
                outPixels = t;
            }
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = inPixels[y * width + x];
                    if (blackFunction.isBlack(pixel)) {
                        int neighbours = 0;
                        for (int dy = -1; dy <= 1; dy++) {
                            int iy = y + dy;
                            int ioffset;
                            if (0 <= iy && iy < height) {
                                ioffset = iy * width;
                                for (int dx = -1; dx <= conditionObj0.getValue(); dx++) {
                                    int ix = x + dx;
                                    if (!(dy == 0 && dx == 0) && 0 <= ix && ix < width) {
                                        int rgb = inPixels[ioffset + ix];
                                        if (!blackFunction.isBlack(rgb))
                                            neighbours++;
                                    }
                                }
                            }
                        }
                        if (neighbours >= threshold) {
                            if (colormap != null)
                                pixel = colormap.getColor((float) i / iterations);
                            else
                                pixel = newColor;
                        }
                    }
                    outPixels[index++] = pixel;
                }
            }
        }
        return outPixels;
    }

    public String toString() {
        return "Binary/Erode...";
    }

    protected class ErodeFilterHelper0 {

        public ErodeFilterHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    protected class ErodeFilterHelper1 {

        public ErodeFilterHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            ClassgetValue replacementClass = new  ClassgetValue();
            ;
            return replacementClass.doIt0();
        }

        public class ClassgetValue {

            public ClassgetValue() {
            }

            public int doIt0() {
                return conditionRHS;
            }
        }
    }
}
