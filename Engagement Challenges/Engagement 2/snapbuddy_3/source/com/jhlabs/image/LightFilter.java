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

/**
 * A filter which produces lighting and embossing effects.
 */
public class LightFilter extends WholeImageFilter {

    /**
     * Take the output colors from the input image.
     */
    public static final int COLORS_FROM_IMAGE = 0;

    /**
     * Use constant material color.
     */
    public static final int COLORS_CONSTANT = 1;

    /**
     * Use the input image brightness as the bump map.
     */
    public static final int BUMPS_FROM_IMAGE = 0;

    /**
     * Use the input image alpha as the bump map.
     */
    public static final int BUMPS_FROM_IMAGE_ALPHA = 1;

    /**
     * Use a separate image alpha channel as the bump map.
     */
    public static final int BUMPS_FROM_MAP = 2;

    /**
     * Use a custom function as the bump map.
     */
    public static final int BUMPS_FROM_BEVEL = 3;

    private float bumpHeight;

    private float bumpSoftness;

    private int bumpShape;

    private float viewDistance = 10000.0f;

    Material material;

    private Vector lights;

    private int colorSource = COLORS_FROM_IMAGE;

    private int bumpSource = BUMPS_FROM_IMAGE;

    private Function2D bumpFunction;

    private Image environmentMap;

    private int[] envPixels;

    private int envWidth = 1, envHeight = 1;

    // Temporary variables used to avoid per-pixel memory allocation while filtering
    private Vector3f l;

    private Vector3f v;

    private Vector3f n;

    private Color4f shadedColor;

    private Color4f diffuse_color;

    private Color4f specular_color;

    private Vector3f tmpv, tmpv2;

    public LightFilter() {
        lights = new  Vector();
        addLight(new  DistantLight());
        bumpHeight = 1.0f;
        bumpSoftness = 5.0f;
        bumpShape = 0;
        material = new  Material();
        l = new  Vector3f();
        v = new  Vector3f();
        n = new  Vector3f();
        shadedColor = new  Color4f();
        diffuse_color = new  Color4f();
        specular_color = new  Color4f();
        tmpv = new  Vector3f();
        tmpv2 = new  Vector3f();
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        ClassgetMaterial replacementClass = new  ClassgetMaterial();
        ;
        return replacementClass.doIt0();
    }

    public void setBumpFunction(Function2D bumpFunction) {
        this.bumpFunction = bumpFunction;
    }

    public Function2D getBumpFunction() {
        return bumpFunction;
    }

    public void setBumpHeight(float bumpHeight) {
        ClasssetBumpHeight replacementClass = new  ClasssetBumpHeight(bumpHeight);
        ;
        replacementClass.doIt0();
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

    public void setBumpShape(int bumpShape) {
        ClasssetBumpShape replacementClass = new  ClasssetBumpShape(bumpShape);
        ;
        replacementClass.doIt0();
    }

    public int getBumpShape() {
        return bumpShape;
    }

    public void setViewDistance(float viewDistance) {
        this.viewDistance = viewDistance;
    }

    public float getViewDistance() {
        return viewDistance;
    }

    public void setEnvironmentMap(BufferedImage environmentMap) {
        ClasssetEnvironmentMap replacementClass = new  ClasssetEnvironmentMap(environmentMap);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
    }

    public Image getEnvironmentMap() {
        ClassgetEnvironmentMap replacementClass = new  ClassgetEnvironmentMap();
        ;
        return replacementClass.doIt0();
    }

    public void setColorSource(int colorSource) {
        this.colorSource = colorSource;
    }

    public int getColorSource() {
        return colorSource;
    }

    public void setBumpSource(int bumpSource) {
        this.bumpSource = bumpSource;
    }

    public int getBumpSource() {
        ClassgetBumpSource replacementClass = new  ClassgetBumpSource();
        ;
        return replacementClass.doIt0();
    }

    public void setDiffuseColor(int diffuseColor) {
        material.diffuseColor = diffuseColor;
    }

    public int getDiffuseColor() {
        return material.diffuseColor;
    }

    public void addLight(Light light) {
        lights.addElement(light);
    }

    public void removeLight(Light light) {
        lights.removeElement(light);
    }

    public Vector getLights() {
        return lights;
    }

    protected static final float r255 = 1.0f / 255.0f;

    protected void setFromRGB(Color4f c, int argb) {
        c.set(((argb >> 16) & 0xff) * r255, ((argb >> 8) & 0xff) * r255, (argb & 0xff) * r255, ((argb >> 24) & 0xff) * r255);
    }

    protected int[] filterPixels(int width, int height, int[] inPixels, Rectangle transformedSpace) {
        ClassfilterPixels replacementClass = new  ClassfilterPixels(width, height, inPixels, transformedSpace);
        ;
        return replacementClass.doIt0();
    }

    protected Color4f phongShade(Vector3f position, Vector3f viewpoint, Vector3f normal, Color4f diffuseColor, Color4f specularColor, Material material, Light[] lightsArray) {
        shadedColor.set(diffuseColor);
        shadedColor.scale(material.ambientIntensity);
        for (int i = 0; i < lightsArray.length; i++) {
            Light light = lightsArray[i];
            n.set(normal);
            l.set(light.position);
            if (light.type != DISTANT)
                l.sub(position);
            l.normalize();
            float nDotL = n.dot(l);
            if (nDotL >= 0.0) {
                float dDotL = 0;
                v.set(viewpoint);
                v.sub(position);
                v.normalize();
                // Spotlight
                if (light.type == SPOT) {
                    dDotL = light.direction.dot(l);
                    if (dDotL < light.cosConeAngle)
                        continue;
                }
                n.scale(2.0f * nDotL);
                n.sub(l);
                float rDotV = n.dot(v);
                float rv;
                if (rDotV < 0.0)
                    rv = 0.0f;
                else
                    //					rv = (float)Math.pow(rDotV, material.highlight);
                    // Fast approximation to pow
                    rv = rDotV / (material.highlight - material.highlight * rDotV + rDotV);
                // Spotlight
                if (light.type == SPOT) {
                    dDotL = light.cosConeAngle / dDotL;
                    float e = dDotL;
                    e *= e;
                    e *= e;
                    e *= e;
                    e = (float) Math.pow(dDotL, light.focus * 10) * (1 - e);
                    rv *= e;
                    nDotL *= e;
                }
                diffuse_color.set(diffuseColor);
                diffuse_color.scale(material.diffuseReflectivity);
                diffuse_color.x *= light.realColor.x * nDotL;
                diffuse_color.y *= light.realColor.y * nDotL;
                diffuse_color.z *= light.realColor.z * nDotL;
                specular_color.set(specularColor);
                specular_color.scale(material.specularReflectivity);
                specular_color.x *= light.realColor.x * rv;
                specular_color.y *= light.realColor.y * rv;
                specular_color.z *= light.realColor.z * rv;
                diffuse_color.add(specular_color);
                diffuse_color.clamp(0, 1);
                shadedColor.add(diffuse_color);
            }
        }
        shadedColor.clamp(0, 1);
        return shadedColor;
    }

    private int getEnvironmentMap(Vector3f normal, int[] inPixels, int width, int height) {
        if (environmentMap != null) {
            float angle = (float) Math.acos(-normal.y);
            float x, y;
            y = angle / ImageMath.PI;
            if (y == 0.0f || y == 1.0f)
                x = 0.0f;
            else {
                float f = normal.x / (float) Math.sin(angle);
                if (f > 1.0f)
                    f = 1.0f;
                else if (f < -1.0f)
                    f = -1.0f;
                x = (float) Math.acos(f) / ImageMath.PI;
            }
            // A bit of empirical scaling....
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
        ClasstoString replacementClass = new  ClasstoString();
        ;
        return replacementClass.doIt0();
    }

    /**
     * A class representing material properties.
     */
    public static class Material {

        int diffuseColor;

        int specularColor;

        float ambientIntensity;

        float diffuseReflectivity;

        float specularReflectivity;

        float highlight;

        float reflectivity;

        float opacity = 1;

        public Material() {
            ambientIntensity = 0.5f;
            diffuseReflectivity = 1.0f;
            specularReflectivity = 1.0f;
            highlight = 3.0f;
            reflectivity = 0.0f;
            diffuseColor = 0xff888888;
            specularColor = 0xffffffff;
        }

        public void setDiffuseColor(int diffuseColor) {
            this.diffuseColor = diffuseColor;
        }

        public int getDiffuseColor() {
            return diffuseColor;
        }

        public void setOpacity(float opacity) {
            this.opacity = opacity;
        }

        public float getOpacity() {
            return opacity;
        }
    }

    public static final int AMBIENT = 0;

    public static final int DISTANT = 1;

    public static final int POINT = 2;

    public static final int SPOT = 3;

    /**
     * A class representing a light.
     */
    public static class Light implements Cloneable {

        int type = AMBIENT;

        Vector3f position;

        Vector3f direction;

        Color4f realColor = new  Color4f();

        int color = 0xffffffff;

        float intensity;

        float azimuth;

        float elevation;

        float focus = 0.5f;

        float centreX = 0.5f, centreY = 0.5f;

        float coneAngle = ImageMath.PI / 6;

        float cosConeAngle;

        float distance = 100.0f;

        public Light() {
            this(270 * ImageMath.PI / 180.0f, 0.5235987755982988f, 1.0f);
        }

        public Light(float azimuth, float elevation, float intensity) {
            this.azimuth = azimuth;
            this.elevation = elevation;
            this.intensity = intensity;
        }

        public void setAzimuth(float azimuth) {
            ClasssetAzimuth replacementClass = new  ClasssetAzimuth(azimuth);
            ;
            replacementClass.doIt0();
        }

        public float getAzimuth() {
            return azimuth;
        }

        public void setElevation(float elevation) {
            this.elevation = elevation;
        }

        public float getElevation() {
            return elevation;
        }

        public void setDistance(float distance) {
            this.distance = distance;
        }

        public float getDistance() {
            ClassgetDistance replacementClass = new  ClassgetDistance();
            ;
            return replacementClass.doIt0();
        }

        public void setIntensity(float intensity) {
            ClasssetIntensity replacementClass = new  ClasssetIntensity(intensity);
            ;
            replacementClass.doIt0();
        }

        public float getIntensity() {
            return intensity;
        }

        public void setConeAngle(float coneAngle) {
            this.coneAngle = coneAngle;
        }

        public float getConeAngle() {
            return coneAngle;
        }

        public void setFocus(float focus) {
            this.focus = focus;
        }

        public float getFocus() {
            return focus;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }

        /**
         * Set the centre of the light in the X direction as a proportion of the image size.
         * @param centreX the center
         * @see #getCentreX
         */
        public void setCentreX(float x) {
            centreX = x;
        }

        /**
         * Get the centre of the light in the X direction as a proportion of the image size.
         * @return the center
         * @see #setCentreX
         */
        public float getCentreX() {
            ClassgetCentreX replacementClass = new  ClassgetCentreX();
            ;
            return replacementClass.doIt0();
        }

        /**
         * Set the centre of the light in the Y direction as a proportion of the image size.
         * @param centreY the center
         * @see #getCentreY
         */
        public void setCentreY(float y) {
            centreY = y;
        }

        /**
         * Get the centre of the light in the Y direction as a proportion of the image size.
         * @return the center
         * @see #setCentreY
         */
        public float getCentreY() {
            return centreY;
        }

        /**
         * Prepare the light for rendering.
         * @param width the output image width
         * @param height the output image height
         */
        public void prepare(int width, int height) {
            float lx = (float) (Math.cos(azimuth) * Math.cos(elevation));
            float ly = (float) (Math.sin(azimuth) * Math.cos(elevation));
            float lz = (float) Math.sin(elevation);
            direction = new  Vector3f(lx, ly, lz);
            direction.normalize();
            if (type != DISTANT) {
                lx *= distance;
                ly *= distance;
                lz *= distance;
                lx += width * centreX;
                ly += height * centreY;
            }
            position = new  Vector3f(lx, ly, lz);
            realColor.set(new  Color(color));
            realColor.scale(intensity);
            cosConeAngle = (float) Math.cos(coneAngle);
        }

        public Object clone() {
            try {
                Light copy = (Light) super.clone();
                return copy;
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }

        public String toString() {
            return "Light";
        }

        public class ClasssetAzimuth {

            public ClasssetAzimuth(float azimuth) {
                this.azimuth = azimuth;
            }

            private float azimuth;

            public void doIt0() {
                Light.this.azimuth = azimuth;
            }
        }

        public class ClassgetDistance {

            public ClassgetDistance() {
            }

            public float doIt0() {
                return distance;
            }
        }

        public class ClasssetIntensity {

            public ClasssetIntensity(float intensity) {
                this.intensity = intensity;
            }

            private float intensity;

            public void doIt0() {
                Light.this.intensity = intensity;
            }
        }

        public class ClassgetCentreX {

            public ClassgetCentreX() {
            }

            public float doIt0() {
                return centreX;
            }
        }
    }

    public class AmbientLight extends Light {

        public String toString() {
            return "Ambient Light";
        }
    }

    public class PointLight extends Light {

        public PointLight() {
            type = POINT;
        }

        public String toString() {
            return "Point Light";
        }
    }

    public class DistantLight extends Light {

        public DistantLight() {
            type = DISTANT;
        }

        public String toString() {
            ClasstoString1 replacementClass = new  ClasstoString1();
            ;
            return replacementClass.doIt0();
        }

        public class ClasstoString1 {

            public ClasstoString1() {
            }

            public String doIt0() {
                return "Distant Light";
            }
        }
    }

    public class SpotLight extends Light {

        public SpotLight() {
            type = SPOT;
        }

        public String toString() {
            return "Spotlight";
        }
    }

    protected class LightFilterHelper0 {

        public LightFilterHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    protected class LightFilterHelper1 {

        public LightFilterHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    protected class LightFilterHelper2 {

        public LightFilterHelper2(int conditionRHS) {
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

    public class ClassgetMaterial {

        public ClassgetMaterial() {
        }

        public Material doIt0() {
            return material;
        }
    }

    public class ClasssetBumpHeight {

        public ClasssetBumpHeight(float bumpHeight) {
            this.bumpHeight = bumpHeight;
        }

        private float bumpHeight;

        public void doIt0() {
            LightFilter.this.bumpHeight = bumpHeight;
        }
    }

    public class ClasssetBumpShape {

        public ClasssetBumpShape(int bumpShape) {
            this.bumpShape = bumpShape;
        }

        private int bumpShape;

        public void doIt0() {
            LightFilter.this.bumpShape = bumpShape;
        }
    }

    public class ClasssetEnvironmentMap {

        public ClasssetEnvironmentMap(BufferedImage environmentMap) {
            this.environmentMap = environmentMap;
        }

        private BufferedImage environmentMap;

        public void doIt0() {
            LightFilter.this.environmentMap = environmentMap;
        }

        public void doIt1() {
            if (environmentMap != null) {
                envWidth = environmentMap.getWidth();
                envHeight = environmentMap.getHeight();
                envPixels = getRGB(environmentMap, 0, 0, envWidth, envHeight, null);
            } else {
                envWidth = envHeight = 1;
                envPixels = null;
            }
        }
    }

    public class ClassgetEnvironmentMap {

        public ClassgetEnvironmentMap() {
        }

        public Image doIt0() {
            return environmentMap;
        }
    }

    public class ClassgetBumpSource {

        public ClassgetBumpSource() {
        }

        public int doIt0() {
            return bumpSource;
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

        private int[] outPixels;

        private float width45;

        private LightFilterHelper0 conditionObj0;

        private boolean invertBumps;

        private Vector3f position;

        private Vector3f viewpoint;

        private Vector3f normal;

        private Color4f envColor;

        private Color4f diffuseColor;

        private Color4f specularColor;

        private Function2D bump;

        private float reflectivity;

        private float areflectivity;

        private Vector3f v1;

        private Vector3f v2;

        private Vector3f n;

        private Light[] lightsArray;

        private float[][] heightWindow;

        private LightFilterHelper1 conditionObj1;

        private LightFilterHelper2 conditionObj2;

        public int[] doIt0() {
            index = 0;
            outPixels = new int[width * height];
            width45 = Math.abs(6.0f * bumpHeight);
            conditionObj0 = new  LightFilterHelper0(0);
            invertBumps = bumpHeight < conditionObj0.getValue();
            position = new  Vector3f(0.0f, 0.0f, 0.0f);
            viewpoint = new  Vector3f((float) width / 2.0f, (float) height / 2.0f, viewDistance);
            normal = new  Vector3f();
            envColor = new  Color4f();
            diffuseColor = new  Color4f(new  Color(material.diffuseColor));
            specularColor = new  Color4f(new  Color(material.specularColor));
            bump = bumpFunction;
            // Apply the bump softness
            if (bumpSource == BUMPS_FROM_IMAGE || bumpSource == BUMPS_FROM_IMAGE_ALPHA || bumpSource == BUMPS_FROM_MAP || bump == null) {
                if (bumpSoftness != 0) {
                    int bumpWidth = width;
                    int bumpHeight = height;
                    int[] bumpPixels = inPixels;
                    if (bumpSource == BUMPS_FROM_MAP && bumpFunction instanceof ImageFunction2D) {
                        ImageFunction2D if2d = (ImageFunction2D) bumpFunction;
                        bumpWidth = if2d.getWidth();
                        bumpHeight = if2d.getHeight();
                        bumpPixels = if2d.getPixels();
                    }
                    int[] tmpPixels = new int[bumpWidth * bumpHeight];
                    int[] softPixels = new int[bumpWidth * bumpHeight];
                    /*
				for (int i = 0; i < 3; i++ ) {
					BoxBlurFilter.blur( bumpPixels, tmpPixels, bumpWidth, bumpHeight, (int)bumpSoftness );
					BoxBlurFilter.blur( tmpPixels, softPixels, bumpHeight, bumpWidth, (int)bumpSoftness );
				}
*/
                    Kernel kernel = GaussianFilter.makeKernel(bumpSoftness);
                    GaussianFilter.convolveAndTranspose(kernel, bumpPixels, tmpPixels, bumpWidth, bumpHeight, true, false, false, GaussianFilter.WRAP_EDGES);
                    GaussianFilter.convolveAndTranspose(kernel, tmpPixels, softPixels, bumpHeight, bumpWidth, true, false, false, GaussianFilter.WRAP_EDGES);
                    bump = new  ImageFunction2D(softPixels, bumpWidth, bumpHeight, ImageFunction2D.CLAMP, bumpSource == BUMPS_FROM_IMAGE_ALPHA);
                    final Function2D bbump = bump;
                    if (bumpShape != 0) {
                        bump = new  Function2D() {

                            private Function2D original = bbump;

                            public float evaluate(float x, float y) {
                                float v = original.evaluate(x, y);
                                switch(bumpShape) {
                                    case 1:
                                        //				v = v > 0.5f ? 0.5f : v;
                                        v *= ImageMath.smoothStep(0.45f, 0.55f, v);
                                        break;
                                    case 2:
                                        v = v < 0.5f ? 0.5f : v;
                                        break;
                                    case 3:
                                        v = ImageMath.triangle(v);
                                        break;
                                    case 4:
                                        v = ImageMath.circleDown(v);
                                        break;
                                    case 5:
                                        v = ImageMath.gain(v, 0.75f);
                                        break;
                                }
                                return v;
                            }
                        };
                    }
                } else if (bumpSource != BUMPS_FROM_MAP)
                    bump = new  ImageFunction2D(inPixels, width, height, ImageFunction2D.CLAMP, bumpSource == BUMPS_FROM_IMAGE_ALPHA);
            }
            reflectivity = material.reflectivity;
            areflectivity = (1 - reflectivity);
            v1 = new  Vector3f();
            v2 = new  Vector3f();
            n = new  Vector3f();
            lightsArray = new Light[lights.size()];
            lights.copyInto(lightsArray);
            for (int i = 0; i < lightsArray.length; i++) lightsArray[i].prepare(width, height);
            heightWindow = new float[3][width];
            for (int x = 0; x < width; x++) heightWindow[1][x] = width45 * bump.evaluate(x, 0);
            conditionObj1 = new  LightFilterHelper1(0);
            conditionObj2 = new  LightFilterHelper2(0);
            // Loop through each source pixel
            for (int y = 0; y < height; y++) {
                boolean y0 = y > conditionObj2.getValue();
                boolean y1 = y < height - 1;
                position.y = y;
                for (int x = 0; x < width; x++) heightWindow[2][x] = width45 * bump.evaluate(x, y + 1);
                for (int x = 0; x < width; x++) {
                    boolean x0 = x > conditionObj1.getValue();
                    boolean x1 = x < width - 1;
                    // Calculate the normal at this point
                    if (bumpSource != BUMPS_FROM_BEVEL) {
                        // Complicated and slower method
                        // Calculate four normals using the gradients in +/- X/Y directions
                        int count = 0;
                        normal.x = normal.y = normal.z = 0;
                        float m0 = heightWindow[1][x];
                        float m1 = x0 ? heightWindow[1][x - 1] - m0 : 0;
                        float m2 = y0 ? heightWindow[0][x] - m0 : 0;
                        float m3 = x1 ? heightWindow[1][x + 1] - m0 : 0;
                        float m4 = y1 ? heightWindow[2][x] - m0 : 0;
                        if (x0 && y1) {
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
                        if (x0 && y0) {
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
                        if (y0 && x1) {
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
                        if (x1 && y1) {
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
                        if (colorSource == COLORS_FROM_IMAGE)
                            setFromRGB(diffuseColor, inPixels[index]);
                        else
                            setFromRGB(diffuseColor, material.diffuseColor);
                        if (reflectivity != 0 && environmentMap != null) {
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
                            setFromRGB(envColor, getEnvironmentMap(//FIXME-interpolate()
                            tmpv, //FIXME-interpolate()
                            inPixels, width, height));
                            diffuseColor.x = reflectivity * envColor.x + areflectivity * diffuseColor.x;
                            diffuseColor.y = reflectivity * envColor.y + areflectivity * diffuseColor.y;
                            diffuseColor.z = reflectivity * envColor.z + areflectivity * diffuseColor.z;
                        }
                        // Shade the pixel
                        Color4f c = phongShade(position, viewpoint, normal, diffuseColor, specularColor, material, lightsArray);
                        int alpha = inPixels[index] & 0xff000000;
                        int rgb = ((int) (c.x * 255) << 16) | ((int) (c.y * 255) << 8) | (int) (c.z * 255);
                        outPixels[index++] = alpha | rgb;
                    } else
                        outPixels[index++] = 0;
                }
                float[] t = heightWindow[0];
                heightWindow[0] = heightWindow[1];
                heightWindow[1] = heightWindow[2];
                heightWindow[2] = t;
            }
            return outPixels;
        }
    }

    public class ClasstoString {

        public ClasstoString() {
        }

        public String doIt0() {
            return "Stylize/Light Effects...";
        }
    }
}
