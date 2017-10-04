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
 * This filter tries to apply the Swing "flush 3D" effect to the black lines in an image.
 */
public class Flush3DFilter extends WholeImageFilter {

    public Flush3DFilter() {
    }

    protected int[] filterPixels(int width, int height, int[] inPixels, Rectangle transformedSpace) {
        int index = 0;
        int[] outPixels = new int[width * height];
        Flush3DFilterHelper0 conditionObj0 = new  Flush3DFilterHelper0(0xff000000);
        Flush3DFilterHelper1 conditionObj1 = new  Flush3DFilterHelper1(0xff000000);
        Flush3DFilterHelper2 conditionObj2 = new  Flush3DFilterHelper2(0xff000000);
        Flush3DFilterHelper3 conditionObj3 = new  Flush3DFilterHelper3(0xff000000);
        Flush3DFilterHelper4 conditionObj4 = new  Flush3DFilterHelper4(0);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = inPixels[y * width + x];
                if (pixel != conditionObj3.getValue() && y > conditionObj4.getValue() && x > 0) {
                    int count = 0;
                    if (inPixels[y * width + x - 1] == conditionObj2.getValue())
                        count++;
                    if (inPixels[(y - 1) * width + x] == conditionObj1.getValue())
                        count++;
                    if (inPixels[(y - 1) * width + x - 1] == conditionObj0.getValue())
                        count++;
                    if (count >= 2)
                        pixel = 0xffffffff;
                }
                outPixels[index++] = pixel;
            }
        }
        return outPixels;
    }

    public String toString() {
        ClasstoString replacementClass = new  ClasstoString();
        ;
        return replacementClass.doIt0();
    }

    protected class Flush3DFilterHelper0 {

        public Flush3DFilterHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    protected class Flush3DFilterHelper1 {

        public Flush3DFilterHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    protected class Flush3DFilterHelper2 {

        public Flush3DFilterHelper2(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    protected class Flush3DFilterHelper3 {

        public Flush3DFilterHelper3(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    protected class Flush3DFilterHelper4 {

        public Flush3DFilterHelper4(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class ClasstoString {

        public ClasstoString() {
        }

        public String doIt0() {
            return "Stylize/Flush 3D...";
        }
    }
}
