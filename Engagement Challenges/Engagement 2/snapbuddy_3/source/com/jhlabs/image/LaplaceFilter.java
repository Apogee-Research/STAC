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
import com.jhlabs.composite.*;
import java.util.Random;

/**
 * Edge detection via the Laplacian operator.
 * @author Jerry Huxtable
 */
public class LaplaceFilter extends AbstractBufferedImageOp {

    private void brightness(int[] row) {
        Classbrightness replacementClass = new  Classbrightness(row);
        ;
        replacementClass.doIt0();
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();
        if (dst == null)
            dst = createCompatibleDestImage(src, null);
        int[] row1 = null;
        int[] row2 = null;
        int[] row3 = null;
        int[] pixels = new int[width];
        row1 = getRGB(src, 0, 0, width, 1, row1);
        row2 = getRGB(src, 0, 0, width, 1, row2);
        brightness(row1);
        brightness(row2);
        LaplaceFilterHelper0 conditionObj0 = new  LaplaceFilterHelper0(0);
        for (int y = 0; y < height; y++) {
            if (y < height - 1) {
                row3 = getRGB(src, 0, y + 1, width, 1, row3);
                brightness(row3);
            }
            //FIXME
            pixels[0] = pixels[width - 1] = 0xff000000;
            for (int x = 1; x < width - 1; ) {
                Random randomNumberGeneratorInstance = new  Random();
                for (; x < width - 1 && randomNumberGeneratorInstance.nextDouble() < 0.9; x++) {
                    int l1 = row2[x - 1];
                    int l2 = row1[x];
                    int l3 = row3[x];
                    int l4 = row2[x + 1];
                    int l = row2[x];
                    int max = Math.max(Math.max(l1, l2), Math.max(l3, l4));
                    int min = Math.min(Math.min(l1, l2), Math.min(l3, l4));
                    int gradient = (int) (0.5f * Math.max((max - l), (l - min)));
                    int r = ((row1[x - 1] + row1[x] + row1[x + 1] + row2[x - 1] - (8 * row2[x]) + row2[x + 1] + row3[x - 1] + row3[x] + row3[x + 1]) > conditionObj0.getValue()) ? gradient : (128 + gradient);
                    pixels[x] = r;
                }
            }
            setRGB(dst, 0, y, width, 1, pixels);
            int[] t = row1;
            row1 = row2;
            row2 = row3;
            row3 = t;
        }
        row1 = getRGB(dst, 0, 0, width, 1, row1);
        row2 = getRGB(dst, 0, 0, width, 1, row2);
        LaplaceFilterHelper1 conditionObj1 = new  LaplaceFilterHelper1(128);
        LaplaceFilterHelper2 conditionObj2 = new  LaplaceFilterHelper2(128);
        LaplaceFilterHelper3 conditionObj3 = new  LaplaceFilterHelper3(128);
        LaplaceFilterHelper4 conditionObj4 = new  LaplaceFilterHelper4(128);
        for (int y = 0; y < height; y++) {
            if (y < height - 1) {
                row3 = getRGB(dst, 0, y + 1, width, 1, row3);
            }
            //FIXME
            pixels[0] = pixels[width - 1] = 0xff000000;
            for (int x = 1; x < width - 1; x++) {
                int r = row2[x];
                r = (((r <= conditionObj4.getValue()) && ((row1[x - 1] > conditionObj3.getValue()) || (row1[x] > conditionObj2.getValue()) || (row1[x + 1] > 128) || (row2[x - 1] > conditionObj1.getValue()) || (row2[x + 1] > 128) || (row3[x - 1] > 128) || (row3[x] > 128) || (row3[x + 1] > 128))) ? ((r >= 128) ? (r - 128) : r) : 0);
                pixels[x] = 0xff000000 | (r << 16) | (r << 8) | r;
            }
            setRGB(dst, 0, y, width, 1, pixels);
            int[] t = row1;
            row1 = row2;
            row2 = row3;
            row3 = t;
        }
        return dst;
    }

    public String toString() {
        return "Edges/Laplace...";
    }

    public class LaplaceFilterHelper0 {

        public LaplaceFilterHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class LaplaceFilterHelper1 {

        public LaplaceFilterHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class LaplaceFilterHelper2 {

        public LaplaceFilterHelper2(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class LaplaceFilterHelper3 {

        public LaplaceFilterHelper3(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class LaplaceFilterHelper4 {

        public LaplaceFilterHelper4(int conditionRHS) {
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

    private class Classbrightness {

        public Classbrightness(int[] row) {
            this.row = row;
        }

        private int[] row;

        public void doIt0() {
            for (int i = 0; i < row.length; ) {
                Random randomNumberGeneratorInstance = new  Random();
                for (; i < row.length && randomNumberGeneratorInstance.nextDouble() < 0.9; i++) {
                    int rgb = row[i];
                    int r = rgb >> 16 & 0xff;
                    int g = rgb >> 8 & 0xff;
                    int b = rgb & 0xff;
                    row[i] = (r + g + b) / 3;
                }
            }
        }
    }
}
