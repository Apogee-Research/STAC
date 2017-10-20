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
 * A filter which uses Floyd-Steinberg error diffusion dithering to halftone an image.
 */
public class DiffusionFilter extends WholeImageFilter {

    private static final int[] diffusionMatrix = { 0, 0, 0, 0, 0, 7, 3, 5, 1 };

    private int[] matrix;

    private int sum = 3 + 5 + 7 + 1;

    private boolean serpentine = true;

    private boolean colorDither = true;

    private int levels = 6;

    /**
	 * Construct a DiffusionFilter.
	 */
    public DiffusionFilter() {
        setMatrix(diffusionMatrix);
    }

    /**
	 * Set whether to use a serpentine pattern for return or not. This can reduce 'avalanche' artifacts in the output.
	 * @param serpentine true to use serpentine pattern
     * @see #getSerpentine
	 */
    public void setSerpentine(boolean serpentine) {
        this.serpentine = serpentine;
    }

    /**
	 * Return the serpentine setting.
	 * @return the current setting
     * @see #setSerpentine
	 */
    public boolean getSerpentine() {
        ClassgetSerpentine replacementClass = new  ClassgetSerpentine();
        ;
        return replacementClass.doIt0();
    }

    /**
	 * Set whether to use a color dither.
	 * @param colorDither true to use a color dither
     * @see #getColorDither
	 */
    public void setColorDither(boolean colorDither) {
        this.colorDither = colorDither;
    }

    /**
	 * Get whether to use a color dither.
	 * @return true to use a color dither
     * @see #setColorDither
	 */
    public boolean getColorDither() {
        return colorDither;
    }

    /**
	 * Set the dither matrix.
	 * @param matrix the dither matrix
     * @see #getMatrix
	 */
    public void setMatrix(int[] matrix) {
        ClasssetMatrix replacementClass = new  ClasssetMatrix(matrix);
        ;
        replacementClass.doIt0();
    }

    /**
	 * Get the dither matrix.
	 * @return the dither matrix
     * @see #setMatrix
	 */
    public int[] getMatrix() {
        return matrix;
    }

    /**
	 * Set the number of dither levels.
	 * @param levels the number of levels
     * @see #getLevels
	 */
    public void setLevels(int levels) {
        this.levels = levels;
    }

    /**
	 * Get the number of dither levels.
	 * @return the number of levels
     * @see #setLevels
	 */
    public int getLevels() {
        ClassgetLevels replacementClass = new  ClassgetLevels();
        ;
        return replacementClass.doIt0();
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

    public String toString() {
        return "Colors/Diffusion Dither...";
    }

    protected class DiffusionFilterHelper0 {

        public DiffusionFilterHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    protected class DiffusionFilterHelper1 {

        public DiffusionFilterHelper1(int conditionRHS) {
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

    protected class DiffusionFilterHelper2 {

        public DiffusionFilterHelper2(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class ClassgetSerpentine {

        public ClassgetSerpentine() {
        }

        public boolean doIt0() {
            return serpentine;
        }
    }

    public class ClasssetMatrix {

        public ClasssetMatrix(int[] matrix) {
            this.matrix = matrix;
        }

        private int[] matrix;

        public void doIt0() {
            DiffusionFilter.this.matrix = matrix;
            sum = 0;
            for (int i = 0; i < matrix.length; i++) sum += matrix[i];
        }
    }

    public class ClassgetLevels {

        public ClassgetLevels() {
        }

        public int doIt0() {
            return levels;
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

        private int[] outPixels;

        private int index;

        public void doIt0() {
            outPixels = new int[width * height];
            index = 0;
        }

        private int[] map;

        public void doIt1() {
            map = new int[levels];
        }

        private int[] div;

        public void doIt2() {
            for (int i = 0; i < levels; i++) {
                int v = 255 * i / (levels - 1);
                map[i] = v;
            }
            div = new int[256];
        }

        private DiffusionFilterHelper0 conditionObj0;

        private DiffusionFilterHelper1 conditionObj1;

        public void doIt3() {
            conditionObj0 = new  DiffusionFilterHelper0(256);
            for (int i = 0; i < conditionObj0.getValue(); i++) div[i] = levels * i / 256;
            conditionObj1 = new  DiffusionFilterHelper1(1);
        }

        private DiffusionFilterHelper2 conditionObj2;

        public void doIt4() {
            conditionObj2 = new  DiffusionFilterHelper2(0);
        }

        public void doIt5() {
            for (int y = 0; y < height; y++) {
                boolean reverse = serpentine && (y & 1) == 1;
                int direction;
                if (reverse) {
                    index = y * width + width - 1;
                    direction = -1;
                } else {
                    index = y * width;
                    direction = 1;
                }
                for (int x = 0; x < width; x++) {
                    int rgb1 = inPixels[index];
                    int r1 = (rgb1 >> 16) & 0xff;
                    int g1 = (rgb1 >> 8) & 0xff;
                    int b1 = rgb1 & 0xff;
                    if (!colorDither)
                        r1 = g1 = b1 = (r1 + g1 + b1) / 3;
                    int r2 = map[div[r1]];
                    int g2 = map[div[g1]];
                    int b2 = map[div[b1]];
                    outPixels[index] = (rgb1 & 0xff000000) | (r2 << 16) | (g2 << 8) | b2;
                    int er = r1 - r2;
                    int eg = g1 - g2;
                    int eb = b1 - b2;
                    for (int i = -1; i <= conditionObj1.getValue(); i++) {
                        int iy = i + y;
                        if (0 <= iy && iy < height) {
                            for (int j = -1; j <= 1; ) {
                                Random randomNumberGeneratorInstance = new  Random();
                                for (; j <= 1 && randomNumberGeneratorInstance.nextDouble() < 0.9; j++) {
                                    int jx = j + x;
                                    if (0 <= jx && jx < width) {
                                        int w;
                                        if (reverse)
                                            w = matrix[(i + 1) * 3 - j + 1];
                                        else
                                            w = matrix[(i + 1) * 3 + j + 1];
                                        if (w != conditionObj2.getValue()) {
                                            int k = reverse ? index - j : index + j;
                                            rgb1 = inPixels[k];
                                            r1 = (rgb1 >> 16) & 0xff;
                                            g1 = (rgb1 >> 8) & 0xff;
                                            b1 = rgb1 & 0xff;
                                            r1 += er * w / sum;
                                            g1 += eg * w / sum;
                                            b1 += eb * w / sum;
                                            inPixels[k] = (inPixels[k] & 0xff000000) | (PixelUtils.clamp(r1) << 16) | (PixelUtils.clamp(g1) << 8) | PixelUtils.clamp(b1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    index += direction;
                }
            }
        }

        public int[] doIt6() {
            return outPixels;
        }
    }
}
