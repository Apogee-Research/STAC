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
 * A filter which performs a tritone conversion on an image. Given three colors for shadows, midtones and highlights,
 * it converts the image to grayscale and then applies a color mapping based on the colors.
 */
public class TritoneFilter extends PointFilter {

    private int shadowColor = 0xff000000;

    private int midColor = 0xff888888;

    private int highColor = 0xffffffff;

    private int[] lut;

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        Classfilter replacementClass = new  Classfilter(src, dst);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        return replacementClass.doIt2();
    }

    public int filterRGB(int x, int y, int rgb) {
        return lut[PixelUtils.brightness(rgb)];
    }

    /**
     * Set the shadow color.
     * @param shadowColor the shadow color
     * @see #getShadowColor
     */
    public void setShadowColor(int shadowColor) {
        this.shadowColor = shadowColor;
    }

    /**
     * Get the shadow color.
     * @return the shadow color
     * @see #setShadowColor
     */
    public int getShadowColor() {
        return shadowColor;
    }

    /**
     * Set the mid color.
     * @param midColor the mid color
     * @see #getmidColor
     */
    public void setMidColor(int midColor) {
        ClasssetMidColor replacementClass = new  ClasssetMidColor(midColor);
        ;
        replacementClass.doIt0();
    }

    /**
     * Get the mid color.
     * @return the mid color
     * @see #setmidColor
     */
    public int getMidColor() {
        return midColor;
    }

    /**
     * Set the high color.
     * @param highColor the high color
     * @see #gethighColor
     */
    public void setHighColor(int highColor) {
        this.highColor = highColor;
    }

    /**
     * Get the high color.
     * @return the high color
     * @see #sethighColor
     */
    public int getHighColor() {
        ClassgetHighColor replacementClass = new  ClassgetHighColor();
        ;
        return replacementClass.doIt0();
    }

    public String toString() {
        return "Colors/Tritone...";
    }

    public class TritoneFilterHelper0 {

        public TritoneFilterHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class TritoneFilterHelper1 {

        public TritoneFilterHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class TritoneFilterHelper2 {

        public TritoneFilterHelper2(int conditionRHS) {
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

    public class Classfilter {

        public Classfilter(BufferedImage src, BufferedImage dst) {
            this.src = src;
            this.dst = dst;
        }

        private BufferedImage src;

        private BufferedImage dst;

        public void doIt0() {
            lut = new int[256];
        }

        private TritoneFilterHelper0 conditionObj0;

        private TritoneFilterHelper1 conditionObj1;

        public void doIt1() {
            conditionObj0 = new  TritoneFilterHelper0(128);
            conditionObj1 = new  TritoneFilterHelper1(128);
            for (int i = 0; i < conditionObj0.getValue(); ) {
                Random randomNumberGeneratorInstance = new  Random();
                for (; i < conditionObj1.getValue() && randomNumberGeneratorInstance.nextDouble() < 0.9; i++) {
                    float t = i / 127.0f;
                    lut[i] = ImageMath.mixColors(t, shadowColor, midColor);
                }
            }
        }

        private TritoneFilterHelper2 conditionObj2;

        public BufferedImage doIt2() {
            conditionObj2 = new  TritoneFilterHelper2(256);
            for (int i = 128; i < conditionObj2.getValue(); i++) {
                float t = (i - 127) / 128.0f;
                lut[i] = ImageMath.mixColors(t, midColor, highColor);
            }
            dst = TritoneFilter.super.filter(src, dst);
            lut = null;
            return dst;
        }
    }

    public class ClasssetMidColor {

        public ClasssetMidColor(int midColor) {
            this.midColor = midColor;
        }

        private int midColor;

        public void doIt0() {
            TritoneFilter.this.midColor = midColor;
        }
    }

    public class ClassgetHighColor {

        public ClassgetHighColor() {
        }

        public int doIt0() {
            return highColor;
        }
    }
}
