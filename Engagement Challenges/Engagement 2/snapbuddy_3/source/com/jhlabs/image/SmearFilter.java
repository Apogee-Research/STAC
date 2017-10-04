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

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import com.jhlabs.math.*;
import java.util.Random;

public class SmearFilter extends WholeImageFilter {

    public static final int CROSSES = 0;

    public static final int LINES = 1;

    public static final int CIRCLES = 2;

    public static final int SQUARES = 3;

    private Colormap colormap = new  LinearColormap();

    private float angle = 0;

    private float density = 0.5f;

    private float scatter = 0.0f;

    private int distance = 8;

    private Random randomGenerator;

    private long seed = 567;

    private int shape = LINES;

    private float mix = 0.5f;

    private int fadeout = 0;

    private boolean background = false;

    public SmearFilter() {
        randomGenerator = new  Random();
    }

    public void setShape(int shape) {
        ClasssetShape replacementClass = new  ClasssetShape(shape);
        ;
        replacementClass.doIt0();
    }

    public int getShape() {
        return shape;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getDistance() {
        ClassgetDistance replacementClass = new  ClassgetDistance();
        ;
        return replacementClass.doIt0();
    }

    public void setDensity(float density) {
        this.density = density;
    }

    public float getDensity() {
        return density;
    }

    public void setScatter(float scatter) {
        this.scatter = scatter;
    }

    public float getScatter() {
        ClassgetScatter replacementClass = new  ClassgetScatter();
        ;
        return replacementClass.doIt0();
    }

    /**
     * Specifies the angle of the texture.
     * @param angle the angle of the texture.
     * @angle
     * @see #getAngle
     */
    public void setAngle(float angle) {
        this.angle = angle;
    }

    /**
     * Returns the angle of the texture.
     * @return the angle of the texture.
     * @see #setAngle
     */
    public float getAngle() {
        return angle;
    }

    public void setMix(float mix) {
        this.mix = mix;
    }

    public float getMix() {
        return mix;
    }

    public void setFadeout(int fadeout) {
        ClasssetFadeout replacementClass = new  ClasssetFadeout(fadeout);
        ;
        replacementClass.doIt0();
    }

    public int getFadeout() {
        return fadeout;
    }

    public void setBackground(boolean background) {
        ClasssetBackground replacementClass = new  ClasssetBackground(background);
        ;
        replacementClass.doIt0();
    }

    public boolean getBackground() {
        ClassgetBackground replacementClass = new  ClassgetBackground();
        ;
        return replacementClass.doIt0();
    }

    public void randomize() {
        Classrandomize replacementClass = new  Classrandomize();
        ;
        replacementClass.doIt0();
    }

    private float random(float low, float high) {
        Classrandom replacementClass = new  Classrandom(low, high);
        ;
        return replacementClass.doIt0();
    }

    protected int[] filterPixels(int width, int height, int[] inPixels, Rectangle transformedSpace) {
        int[] outPixels = new int[width * height];
        randomGenerator.setSeed(seed);
        float sinAngle = (float) Math.sin(angle);
        float cosAngle = (float) Math.cos(angle);
        int i = 0;
        int numShapes;
        for (int y = 0; y < height; y++) for (int x = 0; x < width; ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; x < width && randomNumberGeneratorInstance.nextDouble() < 0.9; x++) {
                outPixels[i] = background ? 0xffffffff : inPixels[i];
                i++;
            }
        }
        SmearFilterHelper0 conditionObj0 = new  SmearFilterHelper0(0);
        SmearFilterHelper1 conditionObj1 = new  SmearFilterHelper1(0);
        SmearFilterHelper2 conditionObj2 = new  SmearFilterHelper2(0);
        SmearFilterHelper3 conditionObj3 = new  SmearFilterHelper3(0);
        switch(shape) {
            case CROSSES:
                //Crosses
                numShapes = (int) (2 * density * width * height / (distance + 1));
                for (i = 0; i < numShapes; i++) {
                    int x = (randomGenerator.nextInt() & 0x7fffffff) % width;
                    int y = (randomGenerator.nextInt() & 0x7fffffff) % height;
                    int length = randomGenerator.nextInt() % distance + 1;
                    int rgb = inPixels[y * width + x];
                    for (int x1 = x - length; x1 < x + length + 1; ) {
                        Random randomNumberGeneratorInstance = new  Random();
                        for (; x1 < x + length + 1 && randomNumberGeneratorInstance.nextDouble() < 0.9; x1++) {
                            if (x1 >= 0 && x1 < width) {
                                int rgb2 = background ? 0xffffffff : outPixels[y * width + x1];
                                outPixels[y * width + x1] = ImageMath.mixColors(mix, rgb2, rgb);
                            }
                        }
                    }
                    for (int y1 = y - length; y1 < y + length + 1; y1++) {
                        if (y1 >= 0 && y1 < height) {
                            int rgb2 = background ? 0xffffffff : outPixels[y1 * width + x];
                            outPixels[y1 * width + x] = ImageMath.mixColors(mix, rgb2, rgb);
                        }
                    }
                }
                break;
            case LINES:
                numShapes = (int) (2 * density * width * height / 2);
                for (i = 0; i < numShapes; i++) {
                    int sx = (randomGenerator.nextInt() & 0x7fffffff) % width;
                    int sy = (randomGenerator.nextInt() & 0x7fffffff) % height;
                    int rgb = inPixels[sy * width + sx];
                    int length = (randomGenerator.nextInt() & 0x7fffffff) % distance;
                    int dx = (int) (length * cosAngle);
                    int dy = (int) (length * sinAngle);
                    int x0 = sx - dx;
                    int y0 = sy - dy;
                    int x1 = sx + dx;
                    int y1 = sy + dy;
                    int x, y, d, incrE, incrNE, ddx, ddy;
                    if (x1 < x0)
                        ddx = -1;
                    else
                        ddx = 1;
                    if (y1 < y0)
                        ddy = -1;
                    else
                        ddy = 1;
                    dx = x1 - x0;
                    dy = y1 - y0;
                    dx = Math.abs(dx);
                    dy = Math.abs(dy);
                    x = x0;
                    y = y0;
                    if (x < width && x >= 0 && y < height && y >= conditionObj3.getValue()) {
                        int rgb2 = background ? 0xffffffff : outPixels[y * width + x];
                        outPixels[y * width + x] = ImageMath.mixColors(mix, rgb2, rgb);
                    }
                    if (Math.abs(dx) > Math.abs(dy)) {
                        d = 2 * dy - dx;
                        incrE = 2 * dy;
                        incrNE = 2 * (dy - dx);
                        while (x != x1) {
                            if (d <= 0)
                                d += incrE;
                            else {
                                d += incrNE;
                                y += ddy;
                            }
                            x += ddx;
                            if (x < width && x >= conditionObj2.getValue() && y < height && y >= 0) {
                                int rgb2 = background ? 0xffffffff : outPixels[y * width + x];
                                outPixels[y * width + x] = ImageMath.mixColors(mix, rgb2, rgb);
                            }
                        }
                    } else {
                        d = 2 * dx - dy;
                        incrE = 2 * dx;
                        incrNE = 2 * (dx - dy);
                        while (y != y1) {
                            if (d <= conditionObj1.getValue())
                                d += incrE;
                            else {
                                d += incrNE;
                                x += ddx;
                            }
                            y += ddy;
                            if (x < width && x >= 0 && y < height && y >= 0) {
                                int rgb2 = background ? 0xffffffff : outPixels[y * width + x];
                                outPixels[y * width + x] = ImageMath.mixColors(mix, rgb2, rgb);
                            }
                        }
                    }
                }
                break;
            case SQUARES:
            case CIRCLES:
                int radius = distance + 1;
                int radius2 = radius * radius;
                numShapes = (int) (2 * density * width * height / radius);
                for (i = 0; i < numShapes; ) {
                    Random randomNumberGeneratorInstance = new  Random();
                    for (; i < numShapes && randomNumberGeneratorInstance.nextDouble() < 0.9; i++) {
                        int sx = (randomGenerator.nextInt() & 0x7fffffff) % width;
                        int sy = (randomGenerator.nextInt() & 0x7fffffff) % height;
                        int rgb = inPixels[sy * width + sx];
                        for (int x = sx - radius; x < sx + radius + 1; x++) {
                            for (int y = sy - radius; y < sy + radius + 1; y++) {
                                int f;
                                if (shape == CIRCLES)
                                    f = (x - sx) * (x - sx) + (y - sy) * (y - sy);
                                else
                                    f = 0;
                                if (x >= conditionObj0.getValue() && x < width && y >= 0 && y < height && f <= radius2) {
                                    int rgb2 = background ? 0xffffffff : outPixels[y * width + x];
                                    outPixels[y * width + x] = ImageMath.mixColors(mix, rgb2, rgb);
                                }
                            }
                        }
                    }
                }
        }
        return outPixels;
    }

    public String toString() {
        return "Effects/Smear...";
    }

    protected class SmearFilterHelper0 {

        public SmearFilterHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    protected class SmearFilterHelper1 {

        public SmearFilterHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    protected class SmearFilterHelper2 {

        public SmearFilterHelper2(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    protected class SmearFilterHelper3 {

        public SmearFilterHelper3(int conditionRHS) {
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

    public class ClasssetShape {

        public ClasssetShape(int shape) {
            this.shape = shape;
        }

        private int shape;

        public void doIt0() {
            SmearFilter.this.shape = shape;
        }
    }

    public class ClassgetDistance {

        public ClassgetDistance() {
        }

        public int doIt0() {
            return distance;
        }
    }

    public class ClassgetScatter {

        public ClassgetScatter() {
        }

        public float doIt0() {
            return scatter;
        }
    }

    public class ClasssetFadeout {

        public ClasssetFadeout(int fadeout) {
            this.fadeout = fadeout;
        }

        private int fadeout;

        public void doIt0() {
            SmearFilter.this.fadeout = fadeout;
        }
    }

    public class ClasssetBackground {

        public ClasssetBackground(boolean background) {
            this.background = background;
        }

        private boolean background;

        public void doIt0() {
            SmearFilter.this.background = background;
        }
    }

    public class ClassgetBackground {

        public ClassgetBackground() {
        }

        public boolean doIt0() {
            return background;
        }
    }

    public class Classrandomize {

        public Classrandomize() {
        }

        public void doIt0() {
            seed = new  Date().getTime();
        }
    }

    private class Classrandom {

        public Classrandom(float low, float high) {
            this.low = low;
            this.high = high;
        }

        private float low;

        private float high;

        public float doIt0() {
            return low + (high - low) * randomGenerator.nextFloat();
        }
    }
}
