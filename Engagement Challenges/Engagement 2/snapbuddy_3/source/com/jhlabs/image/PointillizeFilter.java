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
import java.util.*;
import com.jhlabs.math.*;

public class PointillizeFilter extends CellularFilter {

    private float edgeThickness = 0.4f;

    private boolean fadeEdges = false;

    private int edgeColor = 0xff000000;

    private float fuzziness = 0.1f;

    public PointillizeFilter() {
        setScale(16);
        setRandomness(0.0f);
    }

    public void setEdgeThickness(float edgeThickness) {
        ClasssetEdgeThickness replacementClass = new  ClasssetEdgeThickness(edgeThickness);
        ;
        replacementClass.doIt0();
    }

    public float getEdgeThickness() {
        ClassgetEdgeThickness replacementClass = new  ClassgetEdgeThickness();
        ;
        return replacementClass.doIt0();
    }

    public void setFadeEdges(boolean fadeEdges) {
        this.fadeEdges = fadeEdges;
    }

    public boolean getFadeEdges() {
        return fadeEdges;
    }

    public void setEdgeColor(int edgeColor) {
        this.edgeColor = edgeColor;
    }

    public int getEdgeColor() {
        return edgeColor;
    }

    public void setFuzziness(float fuzziness) {
        this.fuzziness = fuzziness;
    }

    public float getFuzziness() {
        return fuzziness;
    }

    public int getPixel(int x, int y, int[] inPixels, int width, int height) {
        ClassgetPixel replacementClass = new  ClassgetPixel(x, y, inPixels, width, height);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
        replacementClass.doIt3();
        replacementClass.doIt4();
        replacementClass.doIt5();
        replacementClass.doIt6();
        replacementClass.doIt7();
        return replacementClass.doIt8();
    }

    public String toString() {
        return "Pixellate/Pointillize...";
    }

    public class ClasssetEdgeThickness {

        public ClasssetEdgeThickness(float edgeThickness) {
            this.edgeThickness = edgeThickness;
        }

        private float edgeThickness;

        public void doIt0() {
            PointillizeFilter.this.edgeThickness = edgeThickness;
        }
    }

    public class ClassgetEdgeThickness {

        public ClassgetEdgeThickness() {
        }

        public float doIt0() {
            return edgeThickness;
        }
    }

    public class ClassgetPixel {

        public ClassgetPixel(int x, int y, int[] inPixels, int width, int height) {
            this.x = x;
            this.y = y;
            this.inPixels = inPixels;
            this.width = width;
            this.height = height;
        }

        private int x;

        private int y;

        private int[] inPixels;

        private int width;

        private int height;

        private float nx;

        public void doIt0() {
            nx = m00 * x + m01 * y;
        }

        private float ny;

        public void doIt1() {
            ny = m10 * x + m11 * y;
            nx /= scale;
        }

        public void doIt2() {
            ny /= scale * stretch;
        }

        public void doIt3() {
            nx += 1000;
            // Reduce artifacts around 0,0
            ny += 1000;
        }

        private float f;

        public void doIt4() {
            f = evaluate(nx, ny);
        }

        private float f1;

        private int srcx;

        public void doIt5() {
            f1 = results[0].distance;
            srcx = ImageMath.clamp((int) ((results[0].x - 1000) * scale), 0, width - 1);
        }

        private int srcy;

        public void doIt6() {
            srcy = ImageMath.clamp((int) ((results[0].y - 1000) * scale), 0, height - 1);
        }

        private int v;

        public void doIt7() {
            v = inPixels[srcy * width + srcx];
        }

        public int doIt8() {
            if (fadeEdges) {
                float f2 = results[1].distance;
                srcx = ImageMath.clamp((int) ((results[1].x - 1000) * scale), 0, width - 1);
                srcy = ImageMath.clamp((int) ((results[1].y - 1000) * scale), 0, height - 1);
                int v2 = inPixels[srcy * width + srcx];
                v = ImageMath.mixColors(0.5f * f1 / f2, v, v2);
            } else {
                f = 1 - ImageMath.smoothStep(edgeThickness, edgeThickness + fuzziness, f1);
                v = ImageMath.mixColors(f, edgeColor, v);
            }
            return v;
        }
    }
}
