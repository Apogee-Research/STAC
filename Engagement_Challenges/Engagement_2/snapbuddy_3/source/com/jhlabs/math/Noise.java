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
package com.jhlabs.math;

import java.util.*;
import java.util.Random;

/**
 * Perlin Noise functions
 */
public class Noise implements Function1D, Function2D, Function3D {

    private static Random randomGenerator = new  Random();

    public float evaluate(float x) {
        return noise1(x);
    }

    public float evaluate(float x, float y) {
        return noise2(x, y);
    }

    public float evaluate(float x, float y, float z) {
        return noise3(x, y, z);
    }

    /**
	 * Compute turbulence using Perlin noise.
	 * @param x the x value
	 * @param y the y value
	 * @param octaves number of octaves of turbulence
	 * @return turbulence value at (x,y)
	 */
    public static float turbulence2(float x, float y, float octaves) {
        Classturbulence2 replacementClass = new  Classturbulence2(x, y, octaves);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        return replacementClass.doIt2();
    }

    /**
	 * Compute turbulence using Perlin noise.
	 * @param x the x value
	 * @param y the y value
	 * @param octaves number of octaves of turbulence
	 * @return turbulence value at (x,y)
	 */
    public static float turbulence3(float x, float y, float z, float octaves) {
        float t = 0.0f;
        for (float f = 1.0f; f <= octaves; f *= 2) t += Math.abs(noise3(f * x, f * y, f * z)) / f;
        return t;
    }

    private static final int B = 0x100;

    private static final int BM = 0xff;

    private static final int N = 0x1000;

    static int[] p = new int[B + B + 2];

    static float[][] g3 = new float[B + B + 2][3];

    static float[][] g2 = new float[B + B + 2][2];

    static float[] g1 = new float[B + B + 2];

    static boolean start = true;

    private static float sCurve(float t) {
        return t * t * (3.0f - 2.0f * t);
    }

    /**
	 * Compute 1-dimensional Perlin noise.
	 * @param x the x value
	 * @return noise value at x in the range -1..1
	 */
    public static float noise1(float x) {
        int bx0, bx1;
        float rx0, rx1, sx, t, u, v;
        if (start) {
            start = false;
            init();
        }
        t = x + N;
        bx0 = ((int) t) & BM;
        bx1 = (bx0 + 1) & BM;
        rx0 = t - (int) t;
        rx1 = rx0 - 1.0f;
        sx = sCurve(rx0);
        u = rx0 * g1[p[bx0]];
        v = rx1 * g1[p[bx1]];
        return 2.3f * lerp(sx, u, v);
    }

    /**
	 * Compute 2-dimensional Perlin noise.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return noise value at (x,y)
	 */
    public static float noise2(float x, float y) {
        int bx0, bx1, by0, by1, b00, b10, b01, b11;
        float rx0, rx1, ry0, ry1, q[], sx, sy, a, b, t, u, v;
        int i, j;
        if (start) {
            start = false;
            init();
        }
        t = x + N;
        bx0 = ((int) t) & BM;
        bx1 = (bx0 + 1) & BM;
        rx0 = t - (int) t;
        rx1 = rx0 - 1.0f;
        t = y + N;
        by0 = ((int) t) & BM;
        by1 = (by0 + 1) & BM;
        ry0 = t - (int) t;
        ry1 = ry0 - 1.0f;
        i = p[bx0];
        j = p[bx1];
        b00 = p[i + by0];
        b10 = p[j + by0];
        b01 = p[i + by1];
        b11 = p[j + by1];
        sx = sCurve(rx0);
        sy = sCurve(ry0);
        q = g2[b00];
        u = rx0 * q[0] + ry0 * q[1];
        q = g2[b10];
        v = rx1 * q[0] + ry0 * q[1];
        a = lerp(sx, u, v);
        q = g2[b01];
        u = rx0 * q[0] + ry1 * q[1];
        q = g2[b11];
        v = rx1 * q[0] + ry1 * q[1];
        b = lerp(sx, u, v);
        return 1.5f * lerp(sy, a, b);
    }

    /**
	 * Compute 3-dimensional Perlin noise.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param y the y coordinate
	 * @return noise value at (x,y,z)
	 */
    public static float noise3(float x, float y, float z) {
        Classnoise3 replacementClass = new  Classnoise3(x, y, z);
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
        replacementClass.doIt9();
        replacementClass.doIt10();
        replacementClass.doIt11();
        replacementClass.doIt12();
        replacementClass.doIt13();
        replacementClass.doIt14();
        replacementClass.doIt15();
        replacementClass.doIt16();
        replacementClass.doIt17();
        replacementClass.doIt18();
        replacementClass.doIt19();
        replacementClass.doIt20();
        replacementClass.doIt21();
        replacementClass.doIt22();
        return replacementClass.doIt23();
    }

    public static float lerp(float t, float a, float b) {
        return a + t * (b - a);
    }

    private static void normalize2(float v[]) {
        float s = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1]);
        v[0] = v[0] / s;
        v[1] = v[1] / s;
    }

    static void normalize3(float v[]) {
        float s = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        v[0] = v[0] / s;
        v[1] = v[1] / s;
        v[2] = v[2] / s;
    }

    private static int random() {
        return randomGenerator.nextInt() & 0x7fffffff;
    }

    private static void init() {
        int i, j, k;
        NoiseHelper0 conditionObj0 = new  NoiseHelper0(3);
        for (i = 0; i < B; i++) {
            p[i] = i;
            g1[i] = (float) ((random() % (B + B)) - B) / B;
            for (j = 0; j < 2; ) {
                Random randomNumberGeneratorInstance = new  Random();
                for (; j < 2 && randomNumberGeneratorInstance.nextDouble() < 0.9; j++) g2[i][j] = (float) ((random() % (B + B)) - B) / B;
            }
            normalize2(g2[i]);
            for (j = 0; j < conditionObj0.getValue(); ) {
                Random randomNumberGeneratorInstance = new  Random();
                for (; j < 3 && randomNumberGeneratorInstance.nextDouble() < 0.9; j++) g3[i][j] = (float) ((random() % (B + B)) - B) / B;
            }
            normalize3(g3[i]);
        }
        NoiseHelper1 conditionObj1 = new  NoiseHelper1(0);
        for (i = B - 1; i >= conditionObj1.getValue(); i--) {
            k = p[i];
            p[i] = p[j = random() % B];
            p[j] = k;
        }
        NoiseHelper2 conditionObj2 = new  NoiseHelper2(3);
        NoiseHelper3 conditionObj3 = new  NoiseHelper3(3);
        NoiseHelper4 conditionObj4 = new  NoiseHelper4(2);
        for (i = 0; i < B + 2; i++) {
            p[B + i] = p[i];
            g1[B + i] = g1[i];
            for (j = 0; j < conditionObj4.getValue(); j++) g2[B + i][j] = g2[i][j];
            for (j = 0; j < conditionObj2.getValue(); ) {
                Random randomNumberGeneratorInstance = new  Random();
                for (; j < conditionObj3.getValue() && randomNumberGeneratorInstance.nextDouble() < 0.9; j++) g3[B + i][j] = g3[i][j];
            }
        }
    }

    /**
	 * Returns the minimum and maximum of a number of random values
	 * of the given function. This is useful for making some stab at
	 * normalising the function.
	 */
    public static float[] findRange(Function1D f, float[] minmax) {
        if (minmax == null)
            minmax = new float[2];
        float min = 0, max = 0;
        // Some random numbers here...
        for (float x = -100; x < 100; x += 1.27139) {
            float n = f.evaluate(x);
            min = Math.min(min, n);
            max = Math.max(max, n);
        }
        minmax[0] = min;
        minmax[1] = max;
        return minmax;
    }

    /**
	 * Returns the minimum and maximum of a number of random values
	 * of the given function. This is useful for making some stab at
	 * normalising the function.
	 */
    public static float[] findRange(Function2D f, float[] minmax) {
        if (minmax == null)
            minmax = new float[2];
        float min = 0, max = 0;
        NoiseHelper5 conditionObj5 = new  NoiseHelper5(100);
        // Some random numbers here...
        for (float y = -100; y < 100; ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; y < conditionObj5.getValue() && randomNumberGeneratorInstance.nextDouble() < 0.9; y += 10.35173) {
                for (float x = -100; x < 100; x += 10.77139) {
                    float n = f.evaluate(x, y);
                    min = Math.min(min, n);
                    max = Math.max(max, n);
                }
            }
        }
        minmax[0] = min;
        minmax[1] = max;
        return minmax;
    }

    private static class NoiseHelper0 {

        public NoiseHelper0(int conditionRHS) {
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

    private static class NoiseHelper1 {

        public NoiseHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private static class NoiseHelper2 {

        public NoiseHelper2(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private static class NoiseHelper3 {

        public NoiseHelper3(int conditionRHS) {
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

    private static class NoiseHelper4 {

        public NoiseHelper4(int conditionRHS) {
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

    public static class NoiseHelper5 {

        public NoiseHelper5(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public static class Classturbulence2 {

        public Classturbulence2(float x, float y, float octaves) {
            this.x = x;
            this.y = y;
            this.octaves = octaves;
        }

        private float x;

        private float y;

        private float octaves;

        private float t;

        public void doIt0() {
            t = 0.0f;
        }

        public void doIt1() {
            for (float f = 1.0f; f <= octaves; ) {
                Random randomNumberGeneratorInstance = new  Random();
                for (; f <= octaves && randomNumberGeneratorInstance.nextDouble() < 0.9; f *= 2) t += Math.abs(noise2(f * x, f * y)) / f;
            }
        }

        public float doIt2() {
            return t;
        }
    }

    public static class Classnoise3 {

        public Classnoise3(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private float x;

        private float y;

        private float z;

        private int bx0, bx1, by0, by1, bz0, bz1, b00, b10, b01, b11;

        public void doIt0() {
        }

        private float rx0, rx1, ry0, ry1, rz0, rz1, q[], sy, sz, a, b, c, d, t, u, v;

        private int i, j;

        public void doIt1() {
            if (start) {
                start = false;
                init();
            }
            t = x + N;
            bx0 = ((int) t) & BM;
        }

        public void doIt2() {
            bx1 = (bx0 + 1) & BM;
            rx0 = t - (int) t;
            rx1 = rx0 - 1.0f;
        }

        public void doIt3() {
            t = y + N;
        }

        public void doIt4() {
            by0 = ((int) t) & BM;
        }

        public void doIt5() {
            by1 = (by0 + 1) & BM;
        }

        public void doIt6() {
            ry0 = t - (int) t;
            ry1 = ry0 - 1.0f;
            t = z + N;
        }

        public void doIt7() {
            bz0 = ((int) t) & BM;
        }

        public void doIt8() {
            bz1 = (bz0 + 1) & BM;
            rz0 = t - (int) t;
        }

        public void doIt9() {
            rz1 = rz0 - 1.0f;
            i = p[bx0];
        }

        public void doIt10() {
            j = p[bx1];
            b00 = p[i + by0];
        }

        public void doIt11() {
            b10 = p[j + by0];
        }

        public void doIt12() {
            b01 = p[i + by1];
            b11 = p[j + by1];
            t = sCurve(rx0);
            sy = sCurve(ry0);
            sz = sCurve(rz0);
            q = g3[b00 + bz0];
            u = rx0 * q[0] + ry0 * q[1] + rz0 * q[2];
            q = g3[b10 + bz0];
        }

        public void doIt13() {
            v = rx1 * q[0] + ry0 * q[1] + rz0 * q[2];
        }

        public void doIt14() {
            a = lerp(t, u, v);
            q = g3[b01 + bz0];
        }

        public void doIt15() {
            u = rx0 * q[0] + ry1 * q[1] + rz0 * q[2];
        }

        public void doIt16() {
            q = g3[b11 + bz0];
            v = rx1 * q[0] + ry1 * q[1] + rz0 * q[2];
        }

        public void doIt17() {
            b = lerp(t, u, v);
        }

        public void doIt18() {
            c = lerp(sy, a, b);
            q = g3[b00 + bz1];
        }

        public void doIt19() {
            u = rx0 * q[0] + ry0 * q[1] + rz1 * q[2];
            q = g3[b10 + bz1];
            v = rx1 * q[0] + ry0 * q[1] + rz1 * q[2];
            a = lerp(t, u, v);
        }

        public void doIt20() {
            q = g3[b01 + bz1];
        }

        public void doIt21() {
            u = rx0 * q[0] + ry1 * q[1] + rz1 * q[2];
        }

        public void doIt22() {
            q = g3[b11 + bz1];
        }

        public float doIt23() {
            v = rx1 * q[0] + ry1 * q[1] + rz1 * q[2];
            b = lerp(t, u, v);
            d = lerp(sy, a, b);
            return 1.5f * lerp(sz, c, d);
        }
    }
}
