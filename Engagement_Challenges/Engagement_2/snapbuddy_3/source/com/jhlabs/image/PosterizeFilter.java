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
 * A filter to posterize an image.
 */
public class PosterizeFilter extends PointFilter {

    private int numLevels;

    private int[] levels;

    private boolean initialized = false;

    public PosterizeFilter() {
        setNumLevels(6);
    }

    /**
     * Set the number of levels in the output image.
     * @param numLevels the number of levels
     * @see #getNumLevels
     */
    public void setNumLevels(int numLevels) {
        this.numLevels = numLevels;
        initialized = false;
    }

    /**
     * Get the number of levels in the output image.
     * @return the number of levels
     * @see #setNumLevels
     */
    public int getNumLevels() {
        ClassgetNumLevels replacementClass = new  ClassgetNumLevels();
        ;
        return replacementClass.doIt0();
    }

    /**
     * Initialize the filter.
     */
    protected void initialize() {
        Classinitialize replacementClass = new  Classinitialize();
        ;
        replacementClass.doIt0();
    }

    public int filterRGB(int x, int y, int rgb) {
        ClassfilterRGB replacementClass = new  ClassfilterRGB(x, y, rgb);
        ;
        replacementClass.doIt0();
        return replacementClass.doIt1();
    }

    public String toString() {
        return "Colors/Posterize...";
    }

    protected class PosterizeFilterHelper0 {

        public PosterizeFilterHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class ClassgetNumLevels {

        public ClassgetNumLevels() {
        }

        public int doIt0() {
            return numLevels;
        }
    }

    protected class Classinitialize {

        public Classinitialize() {
        }

        private PosterizeFilterHelper0 conditionObj0;

        public void doIt0() {
            levels = new int[256];
            conditionObj0 = new  PosterizeFilterHelper0(1);
            if (numLevels != conditionObj0.getValue())
                for (int i = 0; i < 256; i++) levels[i] = 255 * (numLevels * i / 256) / (numLevels - 1);
        }
    }

    public class ClassfilterRGB {

        public ClassfilterRGB(int x, int y, int rgb) {
            this.x = x;
            this.y = y;
            this.rgb = rgb;
        }

        private int x;

        private int y;

        private int rgb;

        private int a;

        private int r;

        private int g;

        private int b;

        public void doIt0() {
            if (!initialized) {
                initialized = true;
                initialize();
            }
            a = rgb & 0xff000000;
            r = (rgb >> 16) & 0xff;
            g = (rgb >> 8) & 0xff;
            b = rgb & 0xff;
        }

        public int doIt1() {
            r = levels[r];
            g = levels[g];
            b = levels[b];
            return a | (r << 16) | (g << 8) | b;
        }
    }
}
