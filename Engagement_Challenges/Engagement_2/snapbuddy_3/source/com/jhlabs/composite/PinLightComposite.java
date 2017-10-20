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

public final class PinLightComposite extends RGBComposite {

    public PinLightComposite(float alpha) {
        super(alpha);
    }

    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return new  Context(extraAlpha, srcColorModel, dstColorModel);
    }

    static class Context extends RGBCompositeContext {

        public Context(float alpha, ColorModel srcColorModel, ColorModel dstColorModel) {
            super(alpha, srcColorModel, dstColorModel);
        }

        public void composeRGB(int[] src, int[] dst, float alpha) {
            ClasscomposeRGB replacementClass = new  ClasscomposeRGB(src, dst, alpha);
            ;
            replacementClass.doIt0();
            replacementClass.doIt1();
        }

        public class ContextHelper0 {

            public ContextHelper0(int conditionRHS) {
                this.conditionRHS = conditionRHS;
            }

            private int conditionRHS;

            public int getValue() {
                return conditionRHS;
            }
        }

        public class ContextHelper1 {

            public ContextHelper1(int conditionRHS) {
                this.conditionRHS = conditionRHS;
            }

            private int conditionRHS;

            public int getValue() {
                return conditionRHS;
            }
        }

        public class ContextHelper2 {

            public ContextHelper2(int conditionRHS) {
                this.conditionRHS = conditionRHS;
            }

            private int conditionRHS;

            public int getValue() {
                return conditionRHS;
            }
        }

        public class ClasscomposeRGB {

            public ClasscomposeRGB(int[] src, int[] dst, float alpha) {
                this.src = src;
                this.dst = dst;
                this.alpha = alpha;
            }

            private int[] src;

            private int[] dst;

            private float alpha;

            private int w;

            private ContextHelper0 conditionObj0;

            private ContextHelper1 conditionObj1;

            public void doIt0() {
                w = src.length;
                conditionObj0 = new  ContextHelper0(127);
                conditionObj1 = new  ContextHelper1(127);
            }

            private ContextHelper2 conditionObj2;

            public void doIt1() {
                conditionObj2 = new  ContextHelper2(127);
                for (int i = 0; i < w; i += 4) {
                    int sr = src[i];
                    int dir = dst[i];
                    int sg = src[i + 1];
                    int dig = dst[i + 1];
                    int sb = src[i + 2];
                    int dib = dst[i + 2];
                    int sa = src[i + 3];
                    int dia = dst[i + 3];
                    int dor, dog, dob;
                    dor = sr > conditionObj2.getValue() ? Math.max(sr, dir) : Math.min(sr, dir);
                    dog = sg > conditionObj1.getValue() ? Math.max(sg, dig) : Math.min(sg, dig);
                    dob = sb > conditionObj0.getValue() ? Math.max(sb, dib) : Math.min(sb, dib);
                    float a = alpha * sa / 255f;
                    float ac = 1 - a;
                    dst[i] = (int) (a * dor + ac * dir);
                    dst[i + 1] = (int) (a * dog + ac * dig);
                    dst[i + 2] = (int) (a * dob + ac * dib);
                    dst[i + 3] = (int) (sa * alpha + dia * ac);
                }
            }
        }
    }
}
