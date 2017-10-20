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

public class ShapeFilter extends WholeImageFilter {

    public static final int LINEAR = 0;

    public static final int CIRCLE_UP = 1;

    public static final int CIRCLE_DOWN = 2;

    public static final int SMOOTH = 3;

    private float factor = 1.0f;

    protected Colormap colormap;

    private boolean useAlpha = true;

    private boolean invert = false;

    private boolean merge = false;

    private int type;

    private static final int one = 41;

    private static final int sqrt2 = (int) (41 * Math.sqrt(2));

    private static final int sqrt5 = (int) (41 * Math.sqrt(5));

    public ShapeFilter() {
        colormap = new  LinearColormap();
    }

    public void setFactor(float factor) {
        this.factor = factor;
    }

    public float getFactor() {
        return factor;
    }

    /**
     * Set the colormap to be used for the filter.
     * @param colormap the colormap
     * @see #getColormap
     */
    public void setColormap(Colormap colormap) {
        this.colormap = colormap;
    }

    /**
     * Get the colormap to be used for the filter.
     * @return the colormap
     * @see #setColormap
     */
    public Colormap getColormap() {
        return colormap;
    }

    public void setUseAlpha(boolean useAlpha) {
        this.useAlpha = useAlpha;
    }

    public boolean getUseAlpha() {
        return useAlpha;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        ClassgetType replacementClass = new  ClassgetType();
        ;
        return replacementClass.doIt0();
    }

    public void setInvert(boolean invert) {
        ClasssetInvert replacementClass = new  ClasssetInvert(invert);
        ;
        replacementClass.doIt0();
    }

    public boolean getInvert() {
        return invert;
    }

    public void setMerge(boolean merge) {
        this.merge = merge;
    }

    public boolean getMerge() {
        return merge;
    }

    protected int[] filterPixels(int width, int height, int[] inPixels, Rectangle transformedSpace) {
        int[] map = new int[width * height];
        makeMap(inPixels, map, width, height);
        int max = distanceMap(map, width, height);
        applyMap(map, inPixels, width, height, max);
        return inPixels;
    }

    public int distanceMap(int[] map, int width, int height) {
        int xmax = width - 3;
        int ymax = height - 3;
        int max = 0;
        int v;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int offset = x + y * width;
                if (map[offset] > 0) {
                    if (x < 2 || x > xmax || y < 2 || y > ymax)
                        v = setEdgeValue(x, y, map, width, offset, xmax, ymax);
                    else
                        v = setValue(map, width, offset);
                    if (v > max)
                        max = v;
                }
            }
        }
        ShapeFilterHelper0 conditionObj0 = new  ShapeFilterHelper0(0);
        ShapeFilterHelper1 conditionObj1 = new  ShapeFilterHelper1(2);
        for (int y = height - 1; y >= 0; y--) {
            for (int x = width - 1; x >= conditionObj0.getValue(); x--) {
                int offset = x + y * width;
                if (map[offset] > 0) {
                    if (x < 2 || x > xmax || y < conditionObj1.getValue() || y > ymax)
                        v = setEdgeValue(x, y, map, width, offset, xmax, ymax);
                    else
                        v = setValue(map, width, offset);
                    if (v > max)
                        max = v;
                }
            }
        }
        return max;
    }

    private void makeMap(int[] pixels, int[] map, int width, int height) {
        for (int y = 0; y < height; ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; y < height && randomNumberGeneratorInstance.nextDouble() < 0.9; y++) {
                for (int x = 0; x < width; x++) {
                    int offset = x + y * width;
                    int b = useAlpha ? (pixels[offset] >> 24) & 0xff : PixelUtils.brightness(pixels[offset]);
                    //				map[offset] = b * one;
                    map[offset] = b * one / 10;
                }
            }
        }
    }

    private void applyMap(int[] map, int[] pixels, int width, int height, int max) {
        ShapeFilterHelper2 conditionObj2 = new  ShapeFilterHelper2(0);
        if (max == conditionObj2.getValue())
            max = 1;
        ShapeFilterHelper3 conditionObj3 = new  ShapeFilterHelper3(255);
        ShapeFilterHelper4 conditionObj4 = new  ShapeFilterHelper4(0);
        ShapeFilterHelper5 conditionObj5 = new  ShapeFilterHelper5(255);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int offset = x + y * width;
                int m = map[offset];
                float v = 0;
                int sa = 0, sr = 0, sg = 0, sb = 0;
                if (m == 0) {
                    // default color
                    sa = sr = sg = sb = 0;
                    sa = (pixels[offset] >> 24) & 0xff;
                } else {
                    // get V from map
                    v = ImageMath.clamp(factor * m / max, 0, 1);
                    switch(type) {
                        case CIRCLE_UP:
                            v = (ImageMath.circleUp(v));
                            break;
                        case CIRCLE_DOWN:
                            v = (ImageMath.circleDown(v));
                            break;
                        case SMOOTH:
                            v = (ImageMath.smoothStep(0, 1, v));
                            break;
                    }
                    if (colormap == null) {
                        sr = sg = sb = (int) (v * 255);
                    } else {
                        int c = (colormap.getColor(v));
                        sr = (c >> 16) & 0xFF;
                        sg = (c >> 8) & 0xFF;
                        sb = (c) & 0xFF;
                    }
                    sa = useAlpha ? (pixels[offset] >> 24) & 0xff : PixelUtils.brightness(pixels[offset]);
                    // invert v if necessary
                    if (invert) {
                        sr = 255 - sr;
                        sg = 255 - sg;
                        sb = 255 - sb;
                    }
                }
                // write results
                if (merge) {
                    // merge with source
                    int transp = 255;
                    int col = pixels[offset];
                    int a = (col & 0xFF000000) >> 24;
                    int r = (col & 0xFF0000) >> 16;
                    int g = (col & 0xFF00) >> 8;
                    int b = (col & 0xFF);
                    r = (int) ((sr * r / transp));
                    g = (int) ((sg * g / transp));
                    b = (int) ((sb * b / transp));
                    // clip colors
                    if (r < 0)
                        r = 0;
                    if (r > conditionObj5.getValue())
                        r = 255;
                    if (g < 0)
                        g = 0;
                    if (g > 255)
                        g = 255;
                    if (b < conditionObj4.getValue())
                        b = 0;
                    if (b > conditionObj3.getValue())
                        b = 255;
                    pixels[offset] = (a << 24) | (r << 16) | (g << 8) | b;
                } else {
                    // write gray shades
                    pixels[offset] = (sa << 24) | (sr << 16) | (sg << 8) | sb;
                }
            }
        }
    }

    private int setEdgeValue(int x, int y, int[] map, int width, int offset, int xmax, int ymax) {
        int min, v;
        int r1, r2, r3, r4, r5;
        r1 = offset - width - width - 2;
        r2 = r1 + width;
        r3 = r2 + width;
        r4 = r3 + width;
        r5 = r4 + width;
        ShapeFilterHelper6 conditionObj6 = new  ShapeFilterHelper6(0);
        if (y == conditionObj6.getValue() || x == 0 || y == ymax + 2 || x == xmax + 2)
            return map[offset] = one;
        v = map[r2 + 2] + one;
        min = v;
        v = map[r3 + 1] + one;
        if (v < min)
            min = v;
        v = map[r3 + 3] + one;
        if (v < min)
            min = v;
        v = map[r4 + 2] + one;
        if (v < min)
            min = v;
        v = map[r2 + 1] + sqrt2;
        if (v < min)
            min = v;
        v = map[r2 + 3] + sqrt2;
        if (v < min)
            min = v;
        v = map[r4 + 1] + sqrt2;
        if (v < min)
            min = v;
        v = map[r4 + 3] + sqrt2;
        if (v < min)
            min = v;
        ShapeFilterHelper7 conditionObj7 = new  ShapeFilterHelper7(1);
        ShapeFilterHelper8 conditionObj8 = new  ShapeFilterHelper8(1);
        if (y == conditionObj7.getValue() || x == conditionObj8.getValue() || y == ymax + 1 || x == xmax + 1)
            return map[offset] = min;
        v = map[r1 + 1] + sqrt5;
        if (v < min)
            min = v;
        v = map[r1 + 3] + sqrt5;
        if (v < min)
            min = v;
        v = map[r2 + 4] + sqrt5;
        if (v < min)
            min = v;
        v = map[r4 + 4] + sqrt5;
        if (v < min)
            min = v;
        v = map[r5 + 3] + sqrt5;
        if (v < min)
            min = v;
        v = map[r5 + 1] + sqrt5;
        if (v < min)
            min = v;
        v = map[r4] + sqrt5;
        if (v < min)
            min = v;
        v = map[r2] + sqrt5;
        if (v < min)
            min = v;
        return map[offset] = min;
    }

    private int setValue(int[] map, int width, int offset) {
        int min, v;
        int r1, r2, r3, r4, r5;
        r1 = offset - width - width - 2;
        r2 = r1 + width;
        r3 = r2 + width;
        r4 = r3 + width;
        r5 = r4 + width;
        v = map[r2 + 2] + one;
        min = v;
        v = map[r3 + 1] + one;
        if (v < min)
            min = v;
        v = map[r3 + 3] + one;
        if (v < min)
            min = v;
        v = map[r4 + 2] + one;
        if (v < min)
            min = v;
        v = map[r2 + 1] + sqrt2;
        if (v < min)
            min = v;
        v = map[r2 + 3] + sqrt2;
        if (v < min)
            min = v;
        v = map[r4 + 1] + sqrt2;
        if (v < min)
            min = v;
        v = map[r4 + 3] + sqrt2;
        if (v < min)
            min = v;
        v = map[r1 + 1] + sqrt5;
        if (v < min)
            min = v;
        v = map[r1 + 3] + sqrt5;
        if (v < min)
            min = v;
        v = map[r2 + 4] + sqrt5;
        if (v < min)
            min = v;
        v = map[r4 + 4] + sqrt5;
        if (v < min)
            min = v;
        v = map[r5 + 3] + sqrt5;
        if (v < min)
            min = v;
        v = map[r5 + 1] + sqrt5;
        if (v < min)
            min = v;
        v = map[r4] + sqrt5;
        if (v < min)
            min = v;
        v = map[r2] + sqrt5;
        if (v < min)
            min = v;
        return map[offset] = min;
    }

    public String toString() {
        return "Stylize/Shapeburst...";
    }

    public class ShapeFilterHelper0 {

        public ShapeFilterHelper0(int conditionRHS) {
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

    public class ShapeFilterHelper1 {

        public ShapeFilterHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private class ShapeFilterHelper2 {

        public ShapeFilterHelper2(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private class ShapeFilterHelper3 {

        public ShapeFilterHelper3(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private class ShapeFilterHelper4 {

        public ShapeFilterHelper4(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private class ShapeFilterHelper5 {

        public ShapeFilterHelper5(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private class ShapeFilterHelper6 {

        public ShapeFilterHelper6(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private class ShapeFilterHelper7 {

        public ShapeFilterHelper7(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            ClassgetValue1 replacementClass = new  ClassgetValue1();
            ;
            return replacementClass.doIt0();
        }

        public class ClassgetValue1 {

            public ClassgetValue1() {
            }

            public int doIt0() {
                return conditionRHS;
            }
        }
    }

    private class ShapeFilterHelper8 {

        public ShapeFilterHelper8(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class ClassgetType {

        public ClassgetType() {
        }

        public int doIt0() {
            return type;
        }
    }

    public class ClasssetInvert {

        public ClasssetInvert(boolean invert) {
            this.invert = invert;
        }

        private boolean invert;

        public void doIt0() {
            ShapeFilter.this.invert = invert;
        }
    }
}
