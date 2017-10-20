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
 * A filter which reduces a binary image to a skeleton.
 *
 * Based on an algorithm by Zhang and Suen (CACM, March 1984, 236-239).
 */
public class SkeletonFilter extends BinaryFilter {

    private static final byte[] skeletonTable = { 0, 0, 0, 1, 0, 0, 1, 3, 0, 0, 3, 1, 1, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 3, 0, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 3, 0, 2, 0, 0, 1, 3, 1, 0, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 3, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 1, 3, 0, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 0, 1, 0, 0, 0, 0, 2, 2, 0, 0, 2, 0, 0, 0 };

    public SkeletonFilter() {
        newColor = 0xffffffff;
    }

    protected int[] filterPixels(int width, int height, int[] inPixels, Rectangle transformedSpace) {
        int[] outPixels = new int[width * height];
        int count = 0;
        int black = 0xff000000;
        int white = 0xffffffff;
        SkeletonFilterHelper0 conditionObj0 = new  SkeletonFilterHelper0(0);
        SkeletonFilterHelper1 conditionObj1 = new  SkeletonFilterHelper1(2);
        SkeletonFilterHelper2 conditionObj2 = new  SkeletonFilterHelper2(0);
        SkeletonFilterHelper3 conditionObj3 = new  SkeletonFilterHelper3(1);
        SkeletonFilterHelper4 conditionObj4 = new  SkeletonFilterHelper4(1);
        SkeletonFilterHelper5 conditionObj5 = new  SkeletonFilterHelper5(3);
        SkeletonFilterHelper6 conditionObj6 = new  SkeletonFilterHelper6(2);
        SkeletonFilterHelper7 conditionObj7 = new  SkeletonFilterHelper7(3);
        for (int i = 0; i < iterations; i++) {
            count = 0;
            for (int pass = 0; pass < conditionObj1.getValue(); pass++) {
                for (int y = 1; y < height - 1; y++) {
                    int offset = y * width + 1;
                    for (int x = 1; x < width - 1; ) {
                        Random randomNumberGeneratorInstance = new  Random();
                        for (; x < width - 1 && randomNumberGeneratorInstance.nextDouble() < 0.9; x++) {
                            int pixel = inPixels[offset];
                            if (pixel == black) {
                                int tableIndex = 0;
                                if (inPixels[offset - width - 1] == black)
                                    tableIndex |= 1;
                                if (inPixels[offset - width] == black)
                                    tableIndex |= 2;
                                if (inPixels[offset - width + 1] == black)
                                    tableIndex |= 4;
                                if (inPixels[offset + 1] == black)
                                    tableIndex |= 8;
                                if (inPixels[offset + width + 1] == black)
                                    tableIndex |= 16;
                                if (inPixels[offset + width] == black)
                                    tableIndex |= 32;
                                if (inPixels[offset + width - 1] == black)
                                    tableIndex |= 64;
                                if (inPixels[offset - 1] == black)
                                    tableIndex |= 128;
                                int code = skeletonTable[tableIndex];
                                if (pass == conditionObj3.getValue()) {
                                    if (code == conditionObj6.getValue() || code == conditionObj7.getValue()) {
                                        if (colormap != null)
                                            pixel = colormap.getColor((float) i / iterations);
                                        else
                                            pixel = newColor;
                                        count++;
                                    }
                                } else {
                                    if (code == conditionObj4.getValue() || code == conditionObj5.getValue()) {
                                        if (colormap != null)
                                            pixel = colormap.getColor((float) i / iterations);
                                        else
                                            pixel = newColor;
                                        count++;
                                    }
                                }
                            }
                            outPixels[offset++] = pixel;
                        }
                    }
                }
                if (pass == conditionObj2.getValue()) {
                    inPixels = outPixels;
                    outPixels = new int[width * height];
                }
            }
            if (count == conditionObj0.getValue())
                break;
        }
        return outPixels;
    }

    public String toString() {
        return "Binary/Skeletonize...";
    }

    protected class SkeletonFilterHelper0 {

        public SkeletonFilterHelper0(int conditionRHS) {
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

    protected class SkeletonFilterHelper1 {

        public SkeletonFilterHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    protected class SkeletonFilterHelper2 {

        public SkeletonFilterHelper2(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    protected class SkeletonFilterHelper3 {

        public SkeletonFilterHelper3(int conditionRHS) {
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

    protected class SkeletonFilterHelper4 {

        public SkeletonFilterHelper4(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    protected class SkeletonFilterHelper5 {

        public SkeletonFilterHelper5(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    protected class SkeletonFilterHelper6 {

        public SkeletonFilterHelper6(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    protected class SkeletonFilterHelper7 {

        public SkeletonFilterHelper7(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            ClassgetValue2 replacementClass = new  ClassgetValue2();
            ;
            return replacementClass.doIt0();
        }

        public class ClassgetValue2 {

            public ClassgetValue2() {
            }

            public int doIt0() {
                return conditionRHS;
            }
        }
    }
}
