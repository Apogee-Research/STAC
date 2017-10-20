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

public abstract class TransferFilter extends PointFilter {

    protected int[] rTable, gTable, bTable;

    protected boolean initialized = false;

    public TransferFilter() {
        canFilterIndexColorModel = true;
    }

    public int filterRGB(int x, int y, int rgb) {
        ClassfilterRGB replacementClass = new  ClassfilterRGB(x, y, rgb);
        ;
        replacementClass.doIt0();
        return replacementClass.doIt1();
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        if (!initialized)
            initialize();
        return super.filter(src, dst);
    }

    protected void initialize() {
        initialized = true;
        rTable = gTable = bTable = makeTable();
    }

    protected int[] makeTable() {
        int[] table = new int[256];
        TransferFilterHelper0 conditionObj0 = new  TransferFilterHelper0(256);
        TransferFilterHelper1 conditionObj1 = new  TransferFilterHelper1(256);
        for (int i = 0; i < conditionObj0.getValue(); ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; i < conditionObj1.getValue() && randomNumberGeneratorInstance.nextDouble() < 0.9; i++) table[i] = PixelUtils.clamp((int) (255 * transferFunction(i / 255.0f)));
        }
        return table;
    }

    protected float transferFunction(float v) {
        return 0;
    }

    public int[] getLUT() {
        if (!initialized)
            initialize();
        int[] lut = new int[256];
        for (int i = 0; i < 256; i++) {
            lut[i] = filterRGB(0, 0, (i << 24) | (i << 16) | (i << 8) | i);
        }
        return lut;
    }

    protected class TransferFilterHelper0 {

        public TransferFilterHelper0(int conditionRHS) {
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

    protected class TransferFilterHelper1 {

        public TransferFilterHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
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
            a = rgb & 0xff000000;
            r = (rgb >> 16) & 0xff;
            g = (rgb >> 8) & 0xff;
            b = rgb & 0xff;
        }

        public int doIt1() {
            r = rTable[r];
            g = gTable[g];
            b = bTable[b];
            return a | (r << 16) | (g << 8) | b;
        }
    }
}
