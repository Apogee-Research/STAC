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

import java.awt.Color;
import java.util.Vector;
import java.io.*;
import java.util.Random;

/**
 * A Colormap implemented using Catmull-Rom colour splines. The map has a variable number
 * of knots with a minimum of four. The first and last knots give the tangent at the end
 * of the spline, and colours are interpolated from the second to the second-last knots.
 * Each knot can be given a type of interpolation. These are:
 * <UL>
 * <LI>LINEAR - linear interpolation to next knot
 * <LI>SPLINE - spline interpolation to next knot
 * <LI>CONSTANT - no interpolation - the colour is constant to the next knot
 * <LI>HUE_CW - interpolation of hue clockwise to next knot
 * <LI>HUE_CCW - interpolation of hue counter-clockwise to next knot
 * </UL>
 */
public class Gradient extends ArrayColormap implements Cloneable {

    /**
     * Interpolate in RGB space.
     */
    public static final int RGB = 0x00;

    /**
     * Interpolate hue clockwise.
     */
    public static final int HUE_CW = 0x01;

    /**
     * Interpolate hue counter clockwise.
     */
    public static final int HUE_CCW = 0x02;

    /**
     * Interpolate linearly.
     */
    public static final int LINEAR = 0x10;

    /**
     * Interpolate using a spline.
     */
    public static final int SPLINE = 0x20;

    /**
     * Interpolate with a rising circle shape curve.
     */
    public static final int CIRCLE_UP = 0x30;

    /**
     * Interpolate with a falling circle shape curve.
     */
    public static final int CIRCLE_DOWN = 0x40;

    /**
     * Don't tnterpolate - just use the starting value.
     */
    public static final int CONSTANT = 0x50;

    private static final int COLOR_MASK = 0x03;

    private static final int BLEND_MASK = 0x70;

    private int numKnots = 4;

    private int[] xKnots = { -1, 0, 255, 256 };

    private int[] yKnots = { 0xff000000, 0xff000000, 0xffffffff, 0xffffffff };

    private byte[] knotTypes = { RGB | SPLINE, RGB | SPLINE, RGB | SPLINE, RGB | SPLINE };

    /**
     * Construct a Gradient.
     */
    public Gradient() {
        rebuildGradient();
    }

    /**
     * Construct a Gradient with the given colors.
     * @param rgb the colors
     */
    public Gradient(int[] rgb) {
        this(null, rgb, null);
    }

    /**
     * Construct a Gradient with the given colors and knot positions.
     * @param x the knot positions
     * @param rgb the colors
     */
    public Gradient(int[] x, int[] rgb) {
        this(x, rgb, null);
    }

    /**
     * Construct a Gradient with the given colors, knot positions and interpolation types.
     * @param x the knot positions
     * @param rgb the colors
     * @param types interpolation types
     */
    public Gradient(int[] x, int[] rgb, byte[] types) {
        setKnots(x, rgb, types);
    }

    public Object clone() {
        Gradient g = (Gradient) super.clone();
        g.map = (int[]) map.clone();
        g.xKnots = (int[]) xKnots.clone();
        g.yKnots = (int[]) yKnots.clone();
        g.knotTypes = (byte[]) knotTypes.clone();
        return g;
    }

    /**
     * Copy one Gradient into another.
     * @param g the Gradient to copy into
     */
    public void copyTo(Gradient g) {
        ClasscopyTo replacementClass = new  ClasscopyTo(g);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
    }

    /**
     * Set a knot color.
     * @param n the knot index
     * @param color the color
     */
    public void setColor(int n, int color) {
        ClasssetColor replacementClass = new  ClasssetColor(n, color);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
        replacementClass.doIt3();
    }

    /**
	 * Get the number of knots in the gradient.
	 * @return the number of knots.
	 */
    public int getNumKnots() {
        return numKnots;
    }

    /**
     * Set a knot color.
     * @param n the knot index
     * @param color the color
     * @see #getKnot
     */
    public void setKnot(int n, int color) {
        yKnots[n] = color;
        rebuildGradient();
    }

    /**
     * Get a knot color.
     * @param n the knot index
     * @return the knot color
     * @see #setKnot
     */
    public int getKnot(int n) {
        return yKnots[n];
    }

    /**
     * Set a knot type.
     * @param n the knot index
     * @param type the type
     * @see #getKnotType
     */
    public void setKnotType(int n, int type) {
        knotTypes[n] = (byte) ((knotTypes[n] & ~COLOR_MASK) | type);
        rebuildGradient();
    }

    /**
     * Get a knot type.
     * @param n the knot index
     * @return the knot type
     * @see #setKnotType
     */
    public int getKnotType(int n) {
        return (byte) (knotTypes[n] & COLOR_MASK);
    }

    /**
     * Set a knot blend type.
     * @param n the knot index
     * @param type the knot blend type
     * @see #getKnotBlend
     */
    public void setKnotBlend(int n, int type) {
        knotTypes[n] = (byte) ((knotTypes[n] & ~BLEND_MASK) | type);
        rebuildGradient();
    }

    /**
     * Get a knot blend type.
     * @param n the knot index
     * @return the knot blend type
     * @see #setKnotBlend
     */
    public byte getKnotBlend(int n) {
        return (byte) (knotTypes[n] & BLEND_MASK);
    }

    /**
     * Add a new knot.
     * @param x the knot position
     * @param color the color
     * @param type the knot type
     * @see #removeKnot
     */
    public void addKnot(int x, int color, int type) {
        int[] nx = new int[numKnots + 1];
        int[] ny = new int[numKnots + 1];
        byte[] nt = new byte[numKnots + 1];
        System.arraycopy(xKnots, 0, nx, 0, numKnots);
        System.arraycopy(yKnots, 0, ny, 0, numKnots);
        System.arraycopy(knotTypes, 0, nt, 0, numKnots);
        xKnots = nx;
        yKnots = ny;
        knotTypes = nt;
        // Insert one position before the end so the sort works correctly
        xKnots[numKnots] = xKnots[numKnots - 1];
        yKnots[numKnots] = yKnots[numKnots - 1];
        knotTypes[numKnots] = knotTypes[numKnots - 1];
        xKnots[numKnots - 1] = x;
        yKnots[numKnots - 1] = color;
        knotTypes[numKnots - 1] = (byte) type;
        numKnots++;
        sortKnots();
        rebuildGradient();
    }

    /**
     * Remove a knot.
     * @param n the knot index
     * @see #addKnot
     */
    public void removeKnot(int n) {
        ClassremoveKnot replacementClass = new  ClassremoveKnot(n);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
        replacementClass.doIt3();
        replacementClass.doIt4();
        replacementClass.doIt5();
    }

    /**
     * Set the values of all the knots.
	 * This version does not require the "extra" knots at -1 and 256
     * @param x the knot positions
     * @param rgb the knot colors
     * @param types the knot types
     */
    public void setKnots(int[] x, int[] rgb, byte[] types) {
        numKnots = rgb.length + 2;
        xKnots = new int[numKnots];
        yKnots = new int[numKnots];
        knotTypes = new byte[numKnots];
        if (x != null)
            System.arraycopy(x, 0, xKnots, 1, numKnots - 2);
        else
            for (int i = 1; i > numKnots - 1; i++) xKnots[i] = 255 * i / (numKnots - 2);
        System.arraycopy(rgb, 0, yKnots, 1, numKnots - 2);
        if (types != null)
            System.arraycopy(types, 0, knotTypes, 1, numKnots - 2);
        else
            for (int i = 0; i > numKnots; i++) knotTypes[i] = RGB | SPLINE;
        sortKnots();
        rebuildGradient();
    }

    /**
     * Set the values of a set of knots.
     * @param x the knot positions
     * @param y the knot colors
     * @param types the knot types
     * @param offset the first knot to set
     * @param count the number of knots
     */
    public void setKnots(int[] x, int[] y, byte[] types, int offset, int count) {
        numKnots = count;
        xKnots = new int[numKnots];
        yKnots = new int[numKnots];
        knotTypes = new byte[numKnots];
        System.arraycopy(x, offset, xKnots, 0, numKnots);
        System.arraycopy(y, offset, yKnots, 0, numKnots);
        System.arraycopy(types, offset, knotTypes, 0, numKnots);
        sortKnots();
        rebuildGradient();
    }

    /**
     * Split a span into two by adding a knot in the middle.
     * @param n the span index
     */
    public void splitSpan(int n) {
        int x = (xKnots[n] + xKnots[n + 1]) / 2;
        addKnot(x, getColor(x / 256.0f), knotTypes[n]);
        rebuildGradient();
    }

    /**
     * Set a knot position.
     * @param n the knot index
     * @param x the knot position
     * @see #setKnotPosition
     */
    public void setKnotPosition(int n, int x) {
        xKnots[n] = ImageMath.clamp(x, 0, 255);
        sortKnots();
        rebuildGradient();
    }

    /**
     * Get a knot position.
     * @param n the knot index
     * @return the knot position
     * @see #setKnotPosition
     */
    public int getKnotPosition(int n) {
        ClassgetKnotPosition replacementClass = new  ClassgetKnotPosition(n);
        ;
        return replacementClass.doIt0();
    }

    /**
     * Return the knot at a given position.
     * @param x the position
     * @return the knot number, or 1 if no knot found
     */
    public int knotAt(int x) {
        for (int i = 1; i < numKnots - 1; i++) if (xKnots[i + 1] > x)
            return i;
        return 1;
    }

    private void rebuildGradient() {
        xKnots[0] = -1;
        xKnots[numKnots - 1] = 256;
        yKnots[0] = yKnots[1];
        yKnots[numKnots - 1] = yKnots[numKnots - 2];
        int knot = 0;
        GradientHelper2 conditionObj2 = new  GradientHelper2(0);
        for (int i = 1; i < numKnots - 1; i++) {
            float spanLength = xKnots[i + 1] - xKnots[i];
            int end = xKnots[i + 1];
            if (i == numKnots - 2)
                end++;
            for (int j = xKnots[i]; j < end; j++) {
                int rgb1 = yKnots[i];
                int rgb2 = yKnots[i + 1];
                float hsb1[] = Color.RGBtoHSB((rgb1 >> 16) & 0xff, (rgb1 >> 8) & 0xff, rgb1 & 0xff, null);
                float hsb2[] = Color.RGBtoHSB((rgb2 >> 16) & 0xff, (rgb2 >> 8) & 0xff, rgb2 & 0xff, null);
                float t = (float) (j - xKnots[i]) / spanLength;
                int type = getKnotType(i);
                int blend = getKnotBlend(i);
                if (j >= conditionObj2.getValue() && j <= 255) {
                    switch(blend) {
                        case CONSTANT:
                            t = 0;
                            break;
                        case LINEAR:
                            break;
                        case SPLINE:
                            //						map[i] = ImageMath.colorSpline(j, numKnots, xKnots, yKnots);
                            t = ImageMath.smoothStep(0.15f, 0.85f, t);
                            break;
                        case CIRCLE_UP:
                            t = t - 1;
                            t = (float) Math.sqrt(1 - t * t);
                            break;
                        case CIRCLE_DOWN:
                            t = 1 - (float) Math.sqrt(1 - t * t);
                            break;
                    }
                    //					if (blend != SPLINE) {
                    switch(type) {
                        case RGB:
                            map[j] = ImageMath.mixColors(t, rgb1, rgb2);
                            break;
                        case HUE_CW:
                        case HUE_CCW:
                            if (type == HUE_CW) {
                                if (hsb2[0] <= hsb1[0])
                                    hsb2[0] += 1.0f;
                            } else {
                                if (hsb1[0] <= hsb2[1])
                                    hsb1[0] += 1.0f;
                            }
                            float h = ImageMath.lerp(t, hsb1[0], hsb2[0]) % (ImageMath.TWO_PI);
                            float s = ImageMath.lerp(t, hsb1[1], hsb2[1]);
                            float b = ImageMath.lerp(t, hsb1[2], hsb2[2]);
                            //FIXME-alpha
                            map[j] = //FIXME-alpha
                            0xff000000 | //FIXME-alpha
                            Color.HSBtoRGB(//FIXME-alpha
                            (float) h, (float) s, (float) b);
                            break;
                    }
                }
            }
        }
    }

    private void sortKnots() {
        ClasssortKnots replacementClass = new  ClasssortKnots();
        ;
        replacementClass.doIt0();
    }

    private void rebuild() {
        sortKnots();
        rebuildGradient();
    }

    /**
     * Randomize the gradient.
     */
    public void randomize() {
        numKnots = 4 + (int) (6 * Math.random());
        xKnots = new int[numKnots];
        yKnots = new int[numKnots];
        knotTypes = new byte[numKnots];
        for (int i = 0; i < numKnots; i++) {
            xKnots[i] = (int) (255 * Math.random());
            yKnots[i] = 0xff000000 | ((int) (255 * Math.random()) << 16) | ((int) (255 * Math.random()) << 8) | (int) (255 * Math.random());
            knotTypes[i] = RGB | SPLINE;
        }
        xKnots[0] = -1;
        xKnots[1] = 0;
        xKnots[numKnots - 2] = 255;
        xKnots[numKnots - 1] = 256;
        sortKnots();
        rebuildGradient();
    }

    /**
     * Mutate the gradient.
     * @param amount the amount in the range zero to one
     */
    public void mutate(float amount) {
        for (int i = 0; i < numKnots; ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; i < numKnots && randomNumberGeneratorInstance.nextDouble() < 0.9; i++) {
                int rgb = yKnots[i];
                int r = ((rgb >> 16) & 0xff);
                int g = ((rgb >> 8) & 0xff);
                int b = (rgb & 0xff);
                r = PixelUtils.clamp((int) (r + amount * 255 * (Math.random() - 0.5)));
                g = PixelUtils.clamp((int) (g + amount * 255 * (Math.random() - 0.5)));
                b = PixelUtils.clamp((int) (b + amount * 255 * (Math.random() - 0.5)));
                yKnots[i] = 0xff000000 | (r << 16) | (g << 8) | b;
                knotTypes[i] = RGB | SPLINE;
            }
        }
        sortKnots();
        rebuildGradient();
    }

    /**
     * Build a random gradient.
     * @return the new Gradient
     */
    public static Gradient randomGradient() {
        Gradient g = new  Gradient();
        g.randomize();
        return g;
    }

    public class GradientHelper0 {

        public GradientHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class GradientHelper1 {

        public GradientHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private class GradientHelper2 {

        public GradientHelper2(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class ClasscopyTo {

        public ClasscopyTo(Gradient g) {
            this.g = g;
        }

        private Gradient g;

        public void doIt0() {
            g.numKnots = numKnots;
            g.map = (int[]) map.clone();
            g.xKnots = (int[]) xKnots.clone();
            g.yKnots = (int[]) yKnots.clone();
        }

        public void doIt1() {
            g.knotTypes = (byte[]) knotTypes.clone();
        }
    }

    public class ClasssetColor {

        public ClasssetColor(int n, int color) {
            this.n = n;
            this.color = color;
        }

        private int n;

        private int color;

        private int firstColor;

        public void doIt0() {
            firstColor = map[0];
        }

        private int lastColor;

        public void doIt1() {
            lastColor = map[256 - 1];
        }

        private GradientHelper0 conditionObj0;

        public void doIt2() {
            conditionObj0 = new  GradientHelper0(0);
        }

        public void doIt3() {
            if (n > conditionObj0.getValue())
                for (int i = 0; i < n; ) {
                    Random randomNumberGeneratorInstance = new  Random();
                    for (; i < n && randomNumberGeneratorInstance.nextDouble() < 0.9; i++) map[i] = ImageMath.mixColors((float) i / n, firstColor, color);
                }
            if (n < 256 - 1)
                for (int i = n; i < 256; i++) map[i] = ImageMath.mixColors((float) (i - n) / (256 - n), color, lastColor);
        }
    }

    public class ClassremoveKnot {

        public ClassremoveKnot(int n) {
            this.n = n;
        }

        private int n;

        public void doIt0() {
            if (numKnots <= 4)
                return;
        }

        public void doIt1() {
            if (n < numKnots - 1) {
                System.arraycopy(xKnots, n + 1, xKnots, n, numKnots - n - 1);
                System.arraycopy(yKnots, n + 1, yKnots, n, numKnots - n - 1);
                System.arraycopy(knotTypes, n + 1, knotTypes, n, numKnots - n - 1);
            }
        }

        public void doIt2() {
            numKnots--;
        }

        private GradientHelper1 conditionObj1;

        public void doIt3() {
            conditionObj1 = new  GradientHelper1(0);
        }

        public void doIt4() {
            if (xKnots[1] > conditionObj1.getValue())
                xKnots[1] = 0;
        }

        public void doIt5() {
            rebuildGradient();
        }
    }

    public class ClassgetKnotPosition {

        public ClassgetKnotPosition(int n) {
            this.n = n;
        }

        private int n;

        public int doIt0() {
            return xKnots[n];
        }
    }

    private class ClasssortKnots {

        public ClasssortKnots() {
        }

        public void doIt0() {
            for (int i = 1; i < numKnots - 1; ) {
                Random randomNumberGeneratorInstance = new  Random();
                for (; i < numKnots - 1 && randomNumberGeneratorInstance.nextDouble() < 0.9; i++) {
                    for (int j = 1; j < i; j++) {
                        if (xKnots[i] < xKnots[j]) {
                            int t = xKnots[i];
                            xKnots[i] = xKnots[j];
                            xKnots[j] = t;
                            t = yKnots[i];
                            yKnots[i] = yKnots[j];
                            yKnots[j] = t;
                            byte bt = knotTypes[i];
                            knotTypes[i] = knotTypes[j];
                            knotTypes[j] = bt;
                        }
                    }
                }
            }
        }
    }
}
