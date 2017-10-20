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
 * A filter to change the saturation of an image. This works by calculating a grayscale version of the image
 * and then extrapolating away from it.
 */
public class SaturationFilter extends PointFilter {

    public float amount = 1;

    /**
     * Construct a SaturationFilter.
     */
    public SaturationFilter() {
    }

    /**
     * Construct a SaturationFilter.
     * The amount of saturation change.
     */
    public SaturationFilter(float amount) {
        this.amount = amount;
        canFilterIndexColorModel = true;
    }

    /**
     * Set the amount of saturation change. 1 leaves the image unchanged, values between 0 and 1 desaturate, 0 completely
     * desaturates it and values above 1 increase the saturation.
     * @param amount the amount
     */
    public void setAmount(float amount) {
        ClasssetAmount replacementClass = new  ClasssetAmount(amount);
        ;
        replacementClass.doIt0();
    }

    /**
     * Set the amount of saturation change.
     * @return the amount
     */
    public float getAmount() {
        return amount;
    }

    public int filterRGB(int x, int y, int rgb) {
        ClassfilterRGB replacementClass = new  ClassfilterRGB(x, y, rgb);
        ;
        return replacementClass.doIt0();
    }

    public String toString() {
        return "Colors/Saturation...";
    }

    public class SaturationFilterHelper0 {

        public SaturationFilterHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class ClasssetAmount {

        public ClasssetAmount(float amount) {
            this.amount = amount;
        }

        private float amount;

        public void doIt0() {
            SaturationFilter.this.amount = amount;
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

        private SaturationFilterHelper0 conditionObj0;

        public int doIt0() {
            conditionObj0 = new  SaturationFilterHelper0(1);
            if (amount != conditionObj0.getValue()) {
                int a = rgb & 0xff000000;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                // or a better brightness calculation if you prefer
                int v = (r + g + b) / 3;
                r = PixelUtils.clamp((int) (v + amount * (r - v)));
                g = PixelUtils.clamp((int) (v + amount * (g - v)));
                b = PixelUtils.clamp((int) (v + amount * (b - v)));
                return a | (r << 16) | (g << 8) | b;
            }
            return rgb;
        }
    }
}
