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
package com.jhlabs.composite;

import java.awt.*;
import java.awt.image.*;

public abstract class RGBComposite implements Composite {

    protected float extraAlpha;

    public RGBComposite() {
        this(1.0f);
    }

    public RGBComposite(float alpha) {
        if (alpha < 0.0f || alpha > 1.0f)
            throw new  IllegalArgumentException("RGBComposite: alpha must be between 0 and 1");
        this.extraAlpha = alpha;
    }

    public float getAlpha() {
        return extraAlpha;
    }

    public int hashCode() {
        ClasshashCode replacementClass = new  ClasshashCode();
        ;
        return replacementClass.doIt0();
    }

    public boolean equals(Object o) {
        if (!(o instanceof RGBComposite))
            return false;
        RGBComposite c = (RGBComposite) o;
        if (extraAlpha != c.extraAlpha)
            return false;
        return true;
    }

    public abstract static class RGBCompositeContext implements CompositeContext {

        private float alpha;

        private ColorModel srcColorModel;

        private ColorModel dstColorModel;

        public RGBCompositeContext(float alpha, ColorModel srcColorModel, ColorModel dstColorModel) {
            this.alpha = alpha;
            this.srcColorModel = srcColorModel;
            this.dstColorModel = dstColorModel;
        }

        public void dispose() {
        }

        // Multiply two numbers in the range 0..255 such that 255*255=255
        static int multiply255(int a, int b) {
            int t = a * b + 0x80;
            return ((t >> 8) + t) >> 8;
        }

        static int clamp(int a) {
            RGBCompositeContextHelper0 conditionObj0 = new  RGBCompositeContextHelper0(0);
            RGBCompositeContextHelper1 conditionObj1 = new  RGBCompositeContextHelper1(255);
            return a < conditionObj0.getValue() ? 0 : a > conditionObj1.getValue() ? 255 : a;
        }

        public abstract void composeRGB(int[] src, int[] dst, float alpha);

        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
            Classcompose replacementClass = new  Classcompose(src, dstIn, dstOut);
            ;
            replacementClass.doIt0();
            replacementClass.doIt1();
        }

        static class RGBCompositeContextHelper0 {

            public RGBCompositeContextHelper0(int conditionRHS) {
                this.conditionRHS = conditionRHS;
            }

            private int conditionRHS;

            public int getValue() {
                return conditionRHS;
            }
        }

        static class RGBCompositeContextHelper1 {

            public RGBCompositeContextHelper1(int conditionRHS) {
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

        public class Classcompose {

            public Classcompose(Raster src, Raster dstIn, WritableRaster dstOut) {
                this.src = src;
                this.dstIn = dstIn;
                this.dstOut = dstOut;
            }

            private Raster src;

            private Raster dstIn;

            private WritableRaster dstOut;

            private float alpha;

            private int[] srcPix;

            private int[] dstPix;

            private int x;

            public void doIt0() {
                alpha = RGBCompositeContext.this.alpha;
                srcPix = null;
                dstPix = null;
                x = dstOut.getMinX();
            }

            private int w;

            private int y0;

            private int y1;

            public void doIt1() {
                w = dstOut.getWidth();
                y0 = dstOut.getMinY();
                y1 = y0 + dstOut.getHeight();
                for (int y = y0; y < y1; y++) {
                    srcPix = src.getPixels(x, y, w, 1, srcPix);
                    dstPix = dstIn.getPixels(x, y, w, 1, dstPix);
                    composeRGB(srcPix, dstPix, alpha);
                    dstOut.setPixels(x, y, w, 1, dstPix);
                }
            }
        }
    }

    public class ClasshashCode {

        public ClasshashCode() {
        }

        public int doIt0() {
            return Float.floatToIntBits(extraAlpha);
        }
    }
}
