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

public class WeaveFilter extends PointFilter {

    private float xWidth = 16;

    private float yWidth = 16;

    private float xGap = 6;

    private float yGap = 6;

    private int rows = 4;

    private int cols = 4;

    private int rgbX = 0xffff8080;

    private int rgbY = 0xff8080ff;

    private boolean useImageColors = true;

    private boolean roundThreads = false;

    private boolean shadeCrossings = true;

    public int[][] matrix = { { 0, 1, 0, 1 }, { 1, 0, 1, 0 }, { 0, 1, 0, 1 }, { 1, 0, 1, 0 } };

    public WeaveFilter() {
    }

    public void setXGap(float xGap) {
        this.xGap = xGap;
    }

    public void setXWidth(float xWidth) {
        this.xWidth = xWidth;
    }

    public float getXWidth() {
        return xWidth;
    }

    public void setYWidth(float yWidth) {
        this.yWidth = yWidth;
    }

    public float getYWidth() {
        ClassgetYWidth replacementClass = new  ClassgetYWidth();
        ;
        return replacementClass.doIt0();
    }

    public float getXGap() {
        return xGap;
    }

    public void setYGap(float yGap) {
        this.yGap = yGap;
    }

    public float getYGap() {
        return yGap;
    }

    public void setCrossings(int[][] matrix) {
        this.matrix = matrix;
    }

    public int[][] getCrossings() {
        ClassgetCrossings replacementClass = new  ClassgetCrossings();
        ;
        return replacementClass.doIt0();
    }

    public void setUseImageColors(boolean useImageColors) {
        this.useImageColors = useImageColors;
    }

    public boolean getUseImageColors() {
        return useImageColors;
    }

    public void setRoundThreads(boolean roundThreads) {
        this.roundThreads = roundThreads;
    }

    public boolean getRoundThreads() {
        return roundThreads;
    }

    public void setShadeCrossings(boolean shadeCrossings) {
        this.shadeCrossings = shadeCrossings;
    }

    public boolean getShadeCrossings() {
        return shadeCrossings;
    }

    public int filterRGB(int x, int y, int rgb) {
        ClassfilterRGB replacementClass = new  ClassfilterRGB(x, y, rgb);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
        replacementClass.doIt3();
        replacementClass.doIt4();
        replacementClass.doIt5();
        replacementClass.doIt6();
        replacementClass.doIt7();
        replacementClass.doIt8();
        return replacementClass.doIt9();
    }

    public String toString() {
        return "Texture/Weave...";
    }

    public class WeaveFilterHelper0 {

        public WeaveFilterHelper0(int conditionRHS) {
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

    public class WeaveFilterHelper1 {

        public WeaveFilterHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class WeaveFilterHelper2 {

        public WeaveFilterHelper2(int conditionRHS) {
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

    public class ClassgetYWidth {

        public ClassgetYWidth() {
        }

        public float doIt0() {
            return yWidth;
        }
    }

    public class ClassgetCrossings {

        public ClassgetCrossings() {
        }

        public int[][] doIt0() {
            return matrix;
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

        public void doIt0() {
            x += xWidth + xGap / 2;
            y += yWidth + yGap / 2;
        }

        private float nx;

        private float ny;

        public void doIt1() {
            nx = ImageMath.mod(x, xWidth + xGap);
            ny = ImageMath.mod(y, yWidth + yGap);
        }

        private int ix;

        public void doIt2() {
            ix = (int) (x / (xWidth + xGap));
        }

        private int iy;

        public void doIt3() {
            iy = (int) (y / (yWidth + yGap));
        }

        private boolean inX;

        private boolean inY;

        public void doIt4() {
            inX = nx < xWidth;
            inY = ny < yWidth;
        }

        private float dX, dY;

        public void doIt5() {
        }

        private float cX, cY;

        private int lrgbX, lrgbY;

        public void doIt6() {
        }

        private int v;

        public void doIt7() {
            if (roundThreads) {
                dX = Math.abs(xWidth / 2 - nx) / xWidth / 2;
                dY = Math.abs(yWidth / 2 - ny) / yWidth / 2;
            } else {
                dX = dY = 0;
            }
            if (shadeCrossings) {
                cX = ImageMath.smoothStep(xWidth / 2, xWidth / 2 + xGap, Math.abs(xWidth / 2 - nx));
                cY = ImageMath.smoothStep(yWidth / 2, yWidth / 2 + yGap, Math.abs(yWidth / 2 - ny));
            } else {
                cX = cY = 0;
            }
            if (useImageColors) {
                lrgbX = lrgbY = rgb;
            } else {
                lrgbX = rgbX;
                lrgbY = rgbY;
            }
        }

        private int ixc;

        private int iyr;

        private int m;

        public void doIt8() {
            ixc = ix % cols;
            iyr = iy % rows;
            m = matrix[iyr][ixc];
        }

        private WeaveFilterHelper0 conditionObj0;

        private WeaveFilterHelper1 conditionObj1;

        private WeaveFilterHelper2 conditionObj2;

        public int doIt9() {
            conditionObj0 = new  WeaveFilterHelper0(0);
            conditionObj1 = new  WeaveFilterHelper1(1);
            conditionObj2 = new  WeaveFilterHelper2(1);
            if (inX) {
                if (inY) {
                    v = m == conditionObj2.getValue() ? lrgbX : lrgbY;
                    v = ImageMath.mixColors(2 * (m == conditionObj1.getValue() ? dX : dY), v, 0xff000000);
                } else {
                    if (shadeCrossings) {
                        if (m != matrix[(iy + 1) % rows][ixc]) {
                            if (m == conditionObj0.getValue())
                                cY = 1 - cY;
                            cY *= 0.5f;
                            lrgbX = ImageMath.mixColors(cY, lrgbX, 0xff000000);
                        } else if (m == 0)
                            lrgbX = ImageMath.mixColors(0.5f, lrgbX, 0xff000000);
                    }
                    v = ImageMath.mixColors(2 * dX, lrgbX, 0xff000000);
                }
            } else if (inY) {
                if (shadeCrossings) {
                    if (m != matrix[iyr][(ix + 1) % cols]) {
                        if (m == 1)
                            cX = 1 - cX;
                        cX *= 0.5f;
                        lrgbY = ImageMath.mixColors(cX, lrgbY, 0xff000000);
                    } else if (m == 1)
                        lrgbY = ImageMath.mixColors(0.5f, lrgbY, 0xff000000);
                }
                v = ImageMath.mixColors(2 * dY, lrgbY, 0xff000000);
            } else
                v = 0x00000000;
            return v;
        }
    }
}
