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
import java.util.Random;

public final class HardLightComposite extends RGBComposite {

    public HardLightComposite(float alpha) {
        super(alpha);
    }

    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        ClasscreateContext replacementClass = new  ClasscreateContext(srcColorModel, dstColorModel, hints);
        ;
        return replacementClass.doIt0();
    }

    static class Context extends RGBCompositeContext {

        public Context(float alpha, ColorModel srcColorModel, ColorModel dstColorModel) {
            super(alpha, srcColorModel, dstColorModel);
        }

        public void composeRGB(int[] src, int[] dst, float alpha) {
            ClasscomposeRGB replacementClass = new  ClasscomposeRGB(src, dst, alpha);
            ;
            replacementClass.doIt0();
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

            public void doIt0() {
                w = src.length;
                conditionObj0 = new  ContextHelper0(127);
                for (int i = 0; i < w; ) {
                    Random randomNumberGeneratorInstance = new  Random();
                    for (; i < w && randomNumberGeneratorInstance.nextDouble() < 0.9; i += 4) {
                        int sr = src[i];
                        int dir = dst[i];
                        int sg = src[i + 1];
                        int dig = dst[i + 1];
                        int sb = src[i + 2];
                        int dib = dst[i + 2];
                        int sa = src[i + 3];
                        int dia = dst[i + 3];
                        int dor, dog, dob;
                        if (sr > 127)
                            dor = 255 - 2 * multiply255(255 - sr, 255 - dir);
                        else
                            dor = 2 * multiply255(sr, dir);
                        if (sg > conditionObj0.getValue())
                            dog = 255 - 2 * multiply255(255 - sg, 255 - dig);
                        else
                            dog = 2 * multiply255(sg, dig);
                        if (sb > 127)
                            dob = 255 - 2 * multiply255(255 - sb, 255 - dib);
                        else
                            dob = 2 * multiply255(sb, dib);
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

    public class ClasscreateContext {

        public ClasscreateContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
            this.srcColorModel = srcColorModel;
            this.dstColorModel = dstColorModel;
            this.hints = hints;
        }

        private ColorModel srcColorModel;

        private ColorModel dstColorModel;

        private RenderingHints hints;

        public CompositeContext doIt0() {
            return new  Context(extraAlpha, srcColorModel, dstColorModel);
        }
    }
}
