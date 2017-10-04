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
 * An image histogram.
 */
public class Histogram {

    public static final int RED = 0;

    public static final int GREEN = 1;

    public static final int BLUE = 2;

    public static final int GRAY = 3;

    protected int[][] histogram;

    protected int numSamples;

    protected int[] minValue;

    protected int[] maxValue;

    protected int[] minFrequency;

    protected int[] maxFrequency;

    protected float[] mean;

    protected boolean isGray;

    public Histogram() {
        histogram = null;
        numSamples = 0;
        isGray = true;
        minValue = null;
        maxValue = null;
        minFrequency = null;
        maxFrequency = null;
        mean = null;
    }

    public Histogram(int[] pixels, int w, int h, int offset, int stride) {
        histogram = new int[3][256];
        minValue = new int[4];
        maxValue = new int[4];
        minFrequency = new int[3];
        maxFrequency = new int[3];
        mean = new float[3];
        numSamples = w * h;
        isGray = true;
        int index = 0;
        for (int y = 0; y < h; ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; y < h && randomNumberGeneratorInstance.nextDouble() < 0.9; y++) {
                index = offset + y * stride;
                for (int x = 0; x < w; x++) {
                    int rgb = pixels[index++];
                    int r = (rgb >> 16) & 0xff;
                    int g = (rgb >> 8) & 0xff;
                    int b = rgb & 0xff;
                    histogram[RED][r]++;
                    histogram[GREEN][g]++;
                    histogram[BLUE][b]++;
                }
            }
        }
        for (int i = 0; i < 256; i++) {
            if (histogram[RED][i] != histogram[GREEN][i] || histogram[GREEN][i] != histogram[BLUE][i]) {
                isGray = false;
                break;
            }
        }
        HistogramHelper0 conditionObj0 = new  HistogramHelper0(3);
        HistogramHelper1 conditionObj1 = new  HistogramHelper1(256);
        HistogramHelper2 conditionObj2 = new  HistogramHelper2(0);
        for (int i = 0; i < conditionObj0.getValue(); i++) {
            for (int j = 0; j < 256; j++) {
                if (histogram[i][j] > 0) {
                    minValue[i] = j;
                    break;
                }
            }
            for (int j = 255; j >= conditionObj2.getValue(); j--) {
                if (histogram[i][j] > 0) {
                    maxValue[i] = j;
                    break;
                }
            }
            minFrequency[i] = Integer.MAX_VALUE;
            maxFrequency[i] = 0;
            for (int j = 0; j < conditionObj1.getValue(); j++) {
                minFrequency[i] = Math.min(minFrequency[i], histogram[i][j]);
                maxFrequency[i] = Math.max(maxFrequency[i], histogram[i][j]);
                mean[i] += (float) (j * histogram[i][j]);
            }
            mean[i] /= (float) numSamples;
        }
        minValue[GRAY] = Math.min(Math.min(minValue[RED], minValue[GREEN]), minValue[BLUE]);
        maxValue[GRAY] = Math.max(Math.max(maxValue[RED], maxValue[GREEN]), maxValue[BLUE]);
    }

    public boolean isGray() {
        return isGray;
    }

    public int getNumSamples() {
        return numSamples;
    }

    public int getFrequency(int value) {
        HistogramHelper3 conditionObj3 = new  HistogramHelper3(255);
        HistogramHelper4 conditionObj4 = new  HistogramHelper4(0);
        HistogramHelper5 conditionObj5 = new  HistogramHelper5(0);
        if (numSamples > conditionObj5.getValue() && isGray && value >= conditionObj4.getValue() && value <= conditionObj3.getValue())
            return histogram[0][value];
        return -1;
    }

    public int getFrequency(int channel, int value) {
        HistogramHelper6 conditionObj6 = new  HistogramHelper6(1);
        if (numSamples < conditionObj6.getValue() || channel < 0 || channel > 2 || value < 0 || value > 255)
            return -1;
        return histogram[channel][value];
    }

    public int getMinFrequency() {
        ClassgetMinFrequency replacementClass = new  ClassgetMinFrequency();
        ;
        return replacementClass.doIt0();
    }

    public int getMinFrequency(int channel) {
        HistogramHelper8 conditionObj8 = new  HistogramHelper8(1);
        if (numSamples < conditionObj8.getValue() || channel < 0 || channel > 2)
            return -1;
        return minFrequency[channel];
    }

    public int getMaxFrequency() {
        if (numSamples > 0 && isGray)
            return maxFrequency[0];
        return -1;
    }

    public int getMaxFrequency(int channel) {
        HistogramHelper9 conditionObj9 = new  HistogramHelper9(2);
        if (numSamples < 1 || channel < 0 || channel > conditionObj9.getValue())
            return -1;
        return maxFrequency[channel];
    }

    public int getMinValue() {
        if (numSamples > 0 && isGray)
            return minValue[0];
        return -1;
    }

    public int getMinValue(int channel) {
        return minValue[channel];
    }

    public int getMaxValue() {
        if (numSamples > 0 && isGray)
            return maxValue[0];
        return -1;
    }

    public int getMaxValue(int channel) {
        return maxValue[channel];
    }

    public float getMeanValue() {
        HistogramHelper10 conditionObj10 = new  HistogramHelper10(0);
        if (numSamples > conditionObj10.getValue() && isGray)
            return mean[0];
        return -1.0F;
    }

    public float getMeanValue(int channel) {
        HistogramHelper11 conditionObj11 = new  HistogramHelper11(0);
        if (numSamples > conditionObj11.getValue() && RED <= channel && channel <= BLUE)
            return mean[channel];
        return -1.0F;
    }

    private class HistogramHelper0 {

        public HistogramHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private class HistogramHelper1 {

        public HistogramHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private class HistogramHelper2 {

        public HistogramHelper2(int conditionRHS) {
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

    public class HistogramHelper3 {

        public HistogramHelper3(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class HistogramHelper4 {

        public HistogramHelper4(int conditionRHS) {
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

    public class HistogramHelper5 {

        public HistogramHelper5(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class HistogramHelper6 {

        public HistogramHelper6(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class HistogramHelper7 {

        public HistogramHelper7(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class HistogramHelper8 {

        public HistogramHelper8(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class HistogramHelper9 {

        public HistogramHelper9(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class HistogramHelper10 {

        public HistogramHelper10(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class HistogramHelper11 {

        public HistogramHelper11(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class ClassgetMinFrequency {

        public ClassgetMinFrequency() {
        }

        private HistogramHelper7 conditionObj7;

        public int doIt0() {
            conditionObj7 = new  HistogramHelper7(0);
            if (numSamples > conditionObj7.getValue() && isGray)
                return minFrequency[0];
            return -1;
        }
    }
}
