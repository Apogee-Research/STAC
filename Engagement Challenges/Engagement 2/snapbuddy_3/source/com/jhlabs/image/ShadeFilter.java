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

import java.awt.image.*;
import com.jhlabs.math.*;
import com.jhlabs.vecmath.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class ShadeFilter extends WholeImageFilter {

    public static final int COLORS_FROM_IMAGE = 0;

    public static final int COLORS_CONSTANT = 1;

    public static final int BUMPS_FROM_IMAGE = 0;

    public static final int BUMPS_FROM_IMAGE_ALPHA = 1;

    public static final int BUMPS_FROM_MAP = 2;

    public static final int BUMPS_FROM_BEVEL = 3;

    private float bumpHeight;

    private float bumpSoftness;

    private float viewDistance = 10000.0f;

    private int colorSource = COLORS_FROM_IMAGE;

    private int bumpSource = BUMPS_FROM_IMAGE;

    private Function2D bumpFunction;

    private BufferedImage environmentMap;

    private int[] envPixels;

    private int envWidth = 1, envHeight = 1;

    private Vector3f l;

    private Vector3f v;

    private Vector3f n;

    private Color4f shadedColor;

    private Color4f diffuse_color;

    private Color4f specular_color;

    private Vector3f tmpv, tmpv2;

    public ShadeFilter() {
        bumpHeight = 1.0f;
        bumpSoftness = 5.0f;
        l = new  Vector3f();
        v = new  Vector3f();
        n = new  Vector3f();
        shadedColor = new  Color4f();
        diffuse_color = new  Color4f();
        specular_color = new  Color4f();
        tmpv = new  Vector3f();
        tmpv2 = new  Vector3f();
    }

    public void setBumpFunction(Function2D bumpFunction) {
        this.bumpFunction = bumpFunction;
    }

    public Function2D getBumpFunction() {
        return bumpFunction;
    }

    public void setBumpHeight(float bumpHeight) {
        this.bumpHeight = bumpHeight;
    }

    public float getBumpHeight() {
        return bumpHeight;
    }

    public void setBumpSoftness(float bumpSoftness) {
        this.bumpSoftness = bumpSoftness;
    }

    public float getBumpSoftness() {
        return bumpSoftness;
    }

    public void setEnvironmentMap(BufferedImage environmentMap) {
        this.environmentMap = environmentMap;
        if (environmentMap != null) {
            envWidth = environmentMap.getWidth();
            envHeight = environmentMap.getHeight();
            envPixels = getRGB(environmentMap, 0, 0, envWidth, envHeight, null);
        } else {
            envWidth = envHeight = 1;
            envPixels = null;
        }
    }

    public BufferedImage getEnvironmentMap() {
        return environmentMap;
    }

    public void setBumpSource(int bumpSource) {
        this.bumpSource = bumpSource;
    }

    public int getBumpSource() {
        return bumpSource;
    }

    protected static final float r255 = 1.0f / 255.0f;

    protected void setFromRGB(Color4f c, int argb) {
        ClasssetFromRGB replacementClass = new  ClasssetFromRGB(c, argb);
        ;
        replacementClass.doIt0();
    }

    protected int[] filterPixels(int width, int height, int[] inPixels, Rectangle transformedSpace) {
        ClassfilterPixels replacementClass = new  ClassfilterPixels(width, height, inPixels, transformedSpace);
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

    private int getEnvironmentMapP(Vector3f normal, int[] inPixels, int width, int height) {
        if (environmentMap != null) {
            float x = 0.5f * (1 + normal.x);
            float y = 0.5f * (1 + normal.y);
            x = ImageMath.clamp(x * envWidth, 0, envWidth - 1);
            y = ImageMath.clamp(y * envHeight, 0, envHeight - 1);
            int ix = (int) x;
            int iy = (int) y;
            float xWeight = x - ix;
            float yWeight = y - iy;
            int i = envWidth * iy + ix;
            int dx = ix == envWidth - 1 ? 0 : 1;
            int dy = iy == envHeight - 1 ? 0 : envWidth;
            return ImageMath.bilinearInterpolate(xWeight, yWeight, envPixels[i], envPixels[i + dx], envPixels[i + dy], envPixels[i + dx + dy]);
        }
        return 0;
    }

    public String toString() {
        return "Stylize/Shade...";
    }

    protected class ShadeFilterHelper0 {

        public ShadeFilterHelper0(int conditionRHS) {
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

    protected class ClasssetFromRGB {

        public ClasssetFromRGB(Color4f c, int argb) {
            this.c = c;
            this.argb = argb;
        }

        private Color4f c;

        private int argb;

        public void doIt0() {
            c.set(((argb >> 16) & 0xff) * r255, ((argb >> 8) & 0xff) * r255, (argb & 0xff) * r255, ((argb >> 24) & 0xff) * r255);
        }
    }

    protected class ClassfilterPixels {

        public ClassfilterPixels(int width, int height, int[] inPixels, Rectangle transformedSpace) {
            this.width = width;
            this.height = height;
            this.inPixels = inPixels;
            this.transformedSpace = transformedSpace;
        }

        private int width;

        private int height;

        private int[] inPixels;

        private Rectangle transformedSpace;

        private int index;

        public void doIt0() {
            index = 0;
        }

        private int[] outPixels;

        private float width45;

        private boolean invertBumps;

        private Vector3f position;

        public void doIt1() {
            outPixels = new int[width * height];
            width45 = Math.abs(6.0f * bumpHeight);
            invertBumps = bumpHeight < 0;
            position = new  Vector3f(0.0f, 0.0f, 0.0f);
        }

        private Vector3f viewpoint;

        public void doIt2() {
            viewpoint = new  Vector3f((float) width / 2.0f, (float) height / 2.0f, viewDistance);
        }

        private Vector3f normal;

        public void doIt3() {
            normal = new  Vector3f();
        }

        private Color4f c;

        private Function2D bump;

        public void doIt4() {
            c = new  Color4f();
            bump = bumpFunction;
        }

        private ShadeFilterHelper0 conditionObj0;

        public void doIt5() {
            conditionObj0 = new  ShadeFilterHelper0(0);
        }

        private Vector3f v1;

        private Vector3f v2;

        private Vector3f n;

        public void doIt6() {
            if (bumpSource == BUMPS_FROM_IMAGE || bumpSource == BUMPS_FROM_IMAGE_ALPHA || bumpSource == BUMPS_FROM_MAP || bump == null) {
                if (bumpSoftness != conditionObj0.getValue()) {
                    int bumpWidth = width;
                    int bumpHeight = height;
                    int[] bumpPixels = inPixels;
                    if (bumpSource == BUMPS_FROM_MAP && bumpFunction instanceof ImageFunction2D) {
                        ImageFunction2D if2d = (ImageFunction2D) bumpFunction;
                        bumpWidth = if2d.getWidth();
                        bumpHeight = if2d.getHeight();
                        bumpPixels = if2d.getPixels();
                    }
                    Kernel kernel = GaussianFilter.makeKernel(bumpSoftness);
                    int[] tmpPixels = new int[bumpWidth * bumpHeight];
                    int[] softPixels = new int[bumpWidth * bumpHeight];
                    GaussianFilter.convolveAndTranspose(kernel, bumpPixels, tmpPixels, bumpWidth, bumpHeight, true, false, false, ConvolveFilter.CLAMP_EDGES);
                    GaussianFilter.convolveAndTranspose(kernel, tmpPixels, softPixels, bumpHeight, bumpWidth, true, false, false, ConvolveFilter.CLAMP_EDGES);
                    bump = new  ImageFunction2D(softPixels, bumpWidth, bumpHeight, ImageFunction2D.CLAMP, bumpSource == BUMPS_FROM_IMAGE_ALPHA);
                } else
                    bump = new  ImageFunction2D(inPixels, width, height, ImageFunction2D.CLAMP, bumpSource == BUMPS_FROM_IMAGE_ALPHA);
            }
            v1 = new  Vector3f();
            v2 = new  Vector3f();
            n = new  Vector3f();
        }

        public void doIt7() {
            // Loop through each source pixel
            for (int y = 0; y < height; y++) {
                float ny = y;
                position.y = y;
                for (int x = 0; x < width; x++) {
                    float nx = x;
                    // Calculate the normal at this point
                    if (bumpSource != BUMPS_FROM_BEVEL) {
                        // Complicated and slower method
                        // Calculate four normals using the gradients in +/- X/Y directions
                        int count = 0;
                        normal.x = normal.y = normal.z = 0;
                        float m0 = width45 * bump.evaluate(nx, ny);
                        float m1 = x > 0 ? width45 * bump.evaluate(nx - 1.0f, ny) - m0 : -2;
                        float m2 = y > 0 ? width45 * bump.evaluate(nx, ny - 1.0f) - m0 : -2;
                        float m3 = x < width - 1 ? width45 * bump.evaluate(nx + 1.0f, ny) - m0 : -2;
                        float m4 = y < height - 1 ? width45 * bump.evaluate(nx, ny + 1.0f) - m0 : -2;
                        if (m1 != -2 && m4 != -2) {
                            v1.x = -1.0f;
                            v1.y = 0.0f;
                            v1.z = m1;
                            v2.x = 0.0f;
                            v2.y = 1.0f;
                            v2.z = m4;
                            n.cross(v1, v2);
                            n.normalize();
                            if (n.z < 0.0)
                                n.z = -n.z;
                            normal.add(n);
                            count++;
                        }
                        if (m1 != -2 && m2 != -2) {
                            v1.x = -1.0f;
                            v1.y = 0.0f;
                            v1.z = m1;
                            v2.x = 0.0f;
                            v2.y = -1.0f;
                            v2.z = m2;
                            n.cross(v1, v2);
                            n.normalize();
                            if (n.z < 0.0)
                                n.z = -n.z;
                            normal.add(n);
                            count++;
                        }
                        if (m2 != -2 && m3 != -2) {
                            v1.x = 0.0f;
                            v1.y = -1.0f;
                            v1.z = m2;
                            v2.x = 1.0f;
                            v2.y = 0.0f;
                            v2.z = m3;
                            n.cross(v1, v2);
                            n.normalize();
                            if (n.z < 0.0)
                                n.z = -n.z;
                            normal.add(n);
                            count++;
                        }
                        if (m3 != -2 && m4 != -2) {
                            v1.x = 1.0f;
                            v1.y = 0.0f;
                            v1.z = m3;
                            v2.x = 0.0f;
                            v2.y = 1.0f;
                            v2.z = m4;
                            n.cross(v1, v2);
                            n.normalize();
                            if (n.z < 0.0)
                                n.z = -n.z;
                            normal.add(n);
                            count++;
                        }
                        // Average the four normals
                        normal.x /= count;
                        normal.y /= count;
                        normal.z /= count;
                    }
                    if (invertBumps) {
                        normal.x = -normal.x;
                        normal.y = -normal.y;
                    }
                    position.x = x;
                    if (normal.z >= 0) {
                        // Get the material colour at this point
                        if (environmentMap != null) {
                            //FIXME-too much normalizing going on here
                            tmpv2.set(viewpoint);
                            tmpv2.sub(position);
                            tmpv2.normalize();
                            tmpv.set(normal);
                            tmpv.normalize();
                            // Reflect
                            tmpv.scale(2.0f * tmpv.dot(tmpv2));
                            tmpv.sub(v);
                            tmpv.normalize();
                            setFromRGB(c, getEnvironmentMapP(//FIXME-interpolate()
                            normal, //FIXME-interpolate()
                            inPixels, width, height));
                            int alpha = inPixels[index] & 0xff000000;
                            int rgb = ((int) (c.x * 255) << 16) | ((int) (c.y * 255) << 8) | (int) (c.z * 255);
                            outPixels[index++] = alpha | rgb;
                        } else
                            outPixels[index++] = 0;
                    } else
                        outPixels[index++] = 0;
                }
            }
        }

        public int[] doIt8() {
            return outPixels;
        }
    }
}
