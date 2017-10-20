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
import java.util.*;

/**
 * A filter which allows channels to be swapped. You provide a matrix with specifying the input channel for 
 * each output channel.
 */
public class SwizzleFilter extends PointFilter {

    private int[] matrix = { 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0 };

    public SwizzleFilter() {
    }

    /**
     * Set the swizzle matrix.
     * @param matrix the matrix
     * @see #getMatrix
     */
    public void setMatrix(int[] matrix) {
        ClasssetMatrix replacementClass = new  ClasssetMatrix(matrix);
        ;
        replacementClass.doIt0();
    }

    /**
     * Get the swizzle matrix.
     * @return the matrix
     * @see #setMatrix
     */
    public int[] getMatrix() {
        return matrix;
    }

    public int filterRGB(int x, int y, int rgb) {
        ClassfilterRGB replacementClass = new  ClassfilterRGB(x, y, rgb);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
        replacementClass.doIt3();
        return replacementClass.doIt4();
    }

    public String toString() {
        return "Channels/Swizzle...";
    }

    public class ClasssetMatrix {

        public ClasssetMatrix(int[] matrix) {
            this.matrix = matrix;
        }

        private int[] matrix;

        public void doIt0() {
            SwizzleFilter.this.matrix = matrix;
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

        public void doIt0() {
            a = (rgb >> 24) & 0xff;
            r = (rgb >> 16) & 0xff;
            g = (rgb >> 8) & 0xff;
        }

        private int b;

        public void doIt1() {
            b = rgb & 0xff;
            a = matrix[0] * a + matrix[1] * r + matrix[2] * g + matrix[3] * b + matrix[4] * 255;
            r = matrix[5] * a + matrix[6] * r + matrix[7] * g + matrix[8] * b + matrix[9] * 255;
            g = matrix[10] * a + matrix[11] * r + matrix[12] * g + matrix[13] * b + matrix[14] * 255;
            b = matrix[15] * a + matrix[16] * r + matrix[17] * g + matrix[18] * b + matrix[19] * 255;
            a = PixelUtils.clamp(a);
            r = PixelUtils.clamp(r);
        }

        public void doIt2() {
            g = PixelUtils.clamp(g);
        }

        public void doIt3() {
            b = PixelUtils.clamp(b);
        }

        public int doIt4() {
            return (a << 24) | (r << 16) | (g << 8) | b;
        }
    }
}
