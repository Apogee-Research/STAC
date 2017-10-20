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

import java.awt.image.*;
import java.util.*;

/**
 * A filter which adds random noise into an image.
 */
public class NoiseFilter extends PointFilter {

    /**
     * Gaussian distribution for the noise.
     */
    public static final int GAUSSIAN = 0;

    /**
     * Uniform distribution for the noise.
     */
    public static final int UNIFORM = 1;

    private int amount = 25;

    private int distribution = UNIFORM;

    private boolean monochrome = false;

    private float density = 1;

    private Random randomNumbers = new  Random();

    public NoiseFilter() {
    }

    /**
	 * Set the amount of effect.
	 * @param amount the amount
     * @min-value 0
     * @max-value 1
     * @see #getAmount
	 */
    public void setAmount(int amount) {
        ClasssetAmount replacementClass = new  ClasssetAmount(amount);
        ;
        replacementClass.doIt0();
    }

    /**
	 * Get the amount of noise.
	 * @return the amount
     * @see #setAmount
	 */
    public int getAmount() {
        return amount;
    }

    /**
	 * Set the distribution of the noise.
	 * @param distribution the distribution
     * @see #getDistribution
	 */
    public void setDistribution(int distribution) {
        this.distribution = distribution;
    }

    /**
	 * Get the distribution of the noise.
	 * @return the distribution
     * @see #setDistribution
	 */
    public int getDistribution() {
        ClassgetDistribution replacementClass = new  ClassgetDistribution();
        ;
        return replacementClass.doIt0();
    }

    /**
	 * Set whether to use monochrome noise.
	 * @param monochrome true for monochrome noise
     * @see #getMonochrome
	 */
    public void setMonochrome(boolean monochrome) {
        this.monochrome = monochrome;
    }

    /**
	 * Get whether to use monochrome noise.
	 * @return true for monochrome noise
     * @see #setMonochrome
	 */
    public boolean getMonochrome() {
        return monochrome;
    }

    /**
	 * Set the density of the noise.
	 * @param density the density
     * @see #getDensity
	 */
    public void setDensity(float density) {
        this.density = density;
    }

    /**
	 * Get the density of the noise.
	 * @return the density
     * @see #setDensity
	 */
    public float getDensity() {
        return density;
    }

    private int random(int x) {
        x += (int) (((distribution == GAUSSIAN ? randomNumbers.nextGaussian() : 2 * randomNumbers.nextFloat() - 1)) * amount);
        NoiseFilterHelper0 conditionObj0 = new  NoiseFilterHelper0(0);
        if (x < conditionObj0.getValue())
            x = 0;
        else if (x > 0xff)
            x = 0xff;
        return x;
    }

    public int filterRGB(int x, int y, int rgb) {
        if (randomNumbers.nextFloat() <= density) {
            int a = rgb & 0xff000000;
            int r = (rgb >> 16) & 0xff;
            int g = (rgb >> 8) & 0xff;
            int b = rgb & 0xff;
            if (monochrome) {
                int n = (int) (((distribution == GAUSSIAN ? randomNumbers.nextGaussian() : 2 * randomNumbers.nextFloat() - 1)) * amount);
                r = PixelUtils.clamp(r + n);
                g = PixelUtils.clamp(g + n);
                b = PixelUtils.clamp(b + n);
            } else {
                r = random(r);
                g = random(g);
                b = random(b);
            }
            return a | (r << 16) | (g << 8) | b;
        }
        return rgb;
    }

    public String toString() {
        ClasstoString replacementClass = new  ClasstoString();
        ;
        return replacementClass.doIt0();
    }

    private class NoiseFilterHelper0 {

        public NoiseFilterHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class ClasssetAmount {

        public ClasssetAmount(int amount) {
            this.amount = amount;
        }

        private int amount;

        public void doIt0() {
            NoiseFilter.this.amount = amount;
        }
    }

    public class ClassgetDistribution {

        public ClassgetDistribution() {
        }

        public int doIt0() {
            return distribution;
        }
    }

    public class ClasstoString {

        public ClasstoString() {
        }

        public String doIt0() {
            return "Stylize/Add Noise...";
        }
    }
}
