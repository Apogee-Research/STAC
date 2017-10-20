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
import com.jhlabs.math.*;
import java.util.Random;

public class SkyFilter extends PointFilter {

    private float scale = 0.1f;

    private float stretch = 1.0f;

    private float angle = 0.0f;

    private float amount = 1.0f;

    private float H = 1.0f;

    private float octaves = 8.0f;

    private float lacunarity = 2.0f;

    private float gain = 1.0f;

    private float bias = 0.6f;

    private int operation;

    private float min;

    private float max;

    private boolean ridged;

    private FBM fBm;

    protected Random random = new  Random();

    private Function2D basis;

    private float cloudCover = 0.5f;

    private float cloudSharpness = 0.5f;

    private float time = 0.3f;

    private float glow = 0.5f;

    private float glowFalloff = 0.5f;

    private float haziness = 0.96f;

    private float t = 0.0f;

    private float sunRadius = 10f;

    private int sunColor = 0xffffffff;

    private float sunR, sunG, sunB;

    private float sunAzimuth = 0.5f;

    private float sunElevation = 0.5f;

    private float windSpeed = 0.0f;

    private float cameraAzimuth = 0.0f;

    private float cameraElevation = 0.0f;

    private float fov = 1.0f;

    private float[] exponents;

    private float[] tan;

    private BufferedImage skyColors;

    private int[] skyPixels;

    private static final float r255 = 1.0f / 255.0f;

    private float width, height;

    public SkyFilter() {
        if (skyColors == null) {
            skyColors = ImageUtils.createImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("SkyColors.png")).getSource());
        }
    }

    public void setAmount(float amount) {
        ClasssetAmount replacementClass = new  ClasssetAmount(amount);
        ;
        replacementClass.doIt0();
    }

    public float getAmount() {
        return amount;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public int getOperation() {
        return operation;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return scale;
    }

    public void setStretch(float stretch) {
        this.stretch = stretch;
    }

    public float getStretch() {
        return stretch;
    }

    public void setT(float t) {
        this.t = t;
    }

    public float getT() {
        return t;
    }

    public void setFOV(float fov) {
        ClasssetFOV replacementClass = new  ClasssetFOV(fov);
        ;
        replacementClass.doIt0();
    }

    public float getFOV() {
        return fov;
    }

    public void setCloudCover(float cloudCover) {
        this.cloudCover = cloudCover;
    }

    public float getCloudCover() {
        return cloudCover;
    }

    public void setCloudSharpness(float cloudSharpness) {
        this.cloudSharpness = cloudSharpness;
    }

    public float getCloudSharpness() {
        ClassgetCloudSharpness replacementClass = new  ClassgetCloudSharpness();
        ;
        return replacementClass.doIt0();
    }

    public void setTime(float time) {
        this.time = time;
    }

    public float getTime() {
        return time;
    }

    public void setGlow(float glow) {
        ClasssetGlow replacementClass = new  ClasssetGlow(glow);
        ;
        replacementClass.doIt0();
    }

    public float getGlow() {
        ClassgetGlow replacementClass = new  ClassgetGlow();
        ;
        return replacementClass.doIt0();
    }

    public void setGlowFalloff(float glowFalloff) {
        this.glowFalloff = glowFalloff;
    }

    public float getGlowFalloff() {
        return glowFalloff;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float getAngle() {
        return angle;
    }

    public void setOctaves(float octaves) {
        this.octaves = octaves;
    }

    public float getOctaves() {
        return octaves;
    }

    public void setH(float H) {
        this.H = H;
    }

    public float getH() {
        return H;
    }

    public void setLacunarity(float lacunarity) {
        this.lacunarity = lacunarity;
    }

    public float getLacunarity() {
        return lacunarity;
    }

    public void setGain(float gain) {
        this.gain = gain;
    }

    public float getGain() {
        return gain;
    }

    public void setBias(float bias) {
        this.bias = bias;
    }

    public float getBias() {
        ClassgetBias replacementClass = new  ClassgetBias();
        ;
        return replacementClass.doIt0();
    }

    public void setHaziness(float haziness) {
        this.haziness = haziness;
    }

    public float getHaziness() {
        return haziness;
    }

    public void setSunElevation(float sunElevation) {
        this.sunElevation = sunElevation;
    }

    public float getSunElevation() {
        return sunElevation;
    }

    public void setSunAzimuth(float sunAzimuth) {
        this.sunAzimuth = sunAzimuth;
    }

    public float getSunAzimuth() {
        return sunAzimuth;
    }

    public void setSunColor(int sunColor) {
        ClasssetSunColor replacementClass = new  ClasssetSunColor(sunColor);
        ;
        replacementClass.doIt0();
    }

    public int getSunColor() {
        ClassgetSunColor replacementClass = new  ClassgetSunColor();
        ;
        return replacementClass.doIt0();
    }

    public void setCameraElevation(float cameraElevation) {
        this.cameraElevation = cameraElevation;
    }

    public float getCameraElevation() {
        ClassgetCameraElevation replacementClass = new  ClassgetCameraElevation();
        ;
        return replacementClass.doIt0();
    }

    public void setCameraAzimuth(float cameraAzimuth) {
        ClasssetCameraAzimuth replacementClass = new  ClasssetCameraAzimuth(cameraAzimuth);
        ;
        replacementClass.doIt0();
    }

    public float getCameraAzimuth() {
        return cameraAzimuth;
    }

    public void setWindSpeed(float windSpeed) {
        ClasssetWindSpeed replacementClass = new  ClasssetWindSpeed(windSpeed);
        ;
        replacementClass.doIt0();
    }

    public float getWindSpeed() {
        return windSpeed;
    }

    float mn, mx;

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        long start = System.currentTimeMillis();
        sunR = (float) ((sunColor >> 16) & 0xff) * r255;
        sunG = (float) ((sunColor >> 8) & 0xff) * r255;
        sunB = (float) (sunColor & 0xff) * r255;
        mn = 10000;
        mx = -10000;
        exponents = new float[(int) octaves + 1];
        float frequency = 1.0f;
        for (int i = 0; i <= (int) octaves; ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; i <= (int) octaves && randomNumberGeneratorInstance.nextDouble() < 0.9; i++) {
                exponents[i] = (float) Math.pow(2, -i);
                frequency *= lacunarity;
            }
        }
        min = -1;
        max = 1;
        //min = -1.2f;
        //max = 1.2f;
        width = src.getWidth();
        height = src.getHeight();
        int h = src.getHeight();
        tan = new float[h];
        for (int i = 0; i < h; ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; i < h && randomNumberGeneratorInstance.nextDouble() < 0.9; i++) tan[i] = (float) Math.tan(fov * (float) i / h * Math.PI * 0.5);
        }
        if (dst == null)
            dst = createCompatibleDestImage(src, null);
        int t = (int) (63 * time);
        //		skyPixels = getRGB( skyColors, t, 0, 1, 64, skyPixels );
        Graphics2D g = dst.createGraphics();
        g.drawImage(skyColors, 0, 0, dst.getWidth(), dst.getHeight(), t, 0, t + 1, 64, null);
        g.dispose();
        BufferedImage clouds = super.filter(dst, dst);
        //		g.drawRenderedImage( clouds, null );
        //		g.dispose();
        long finish = System.currentTimeMillis();
        System.out.println(mn + " " + mx + " " + (finish - start) * 0.001f);
        exponents = null;
        tan = null;
        return dst;
    }

    public float evaluate(float x, float y) {
        Classevaluate replacementClass = new  Classevaluate(x, y);
        ;
        replacementClass.doIt0();
        return replacementClass.doIt1();
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

    public String toString() {
        return "Texture/Sky...";
    }

    public class SkyFilterHelper0 {

        public SkyFilterHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class ClasssetAmount {

        public ClasssetAmount(float amount) {
            this.amount = amount;
        }

        private float amount;

        public void doIt0() {
            SkyFilter.this.amount = amount;
        }
    }

    public class ClasssetFOV {

        public ClasssetFOV(float fov) {
            this.fov = fov;
        }

        private float fov;

        public void doIt0() {
            SkyFilter.this.fov = fov;
        }
    }

    public class ClassgetCloudSharpness {

        public ClassgetCloudSharpness() {
        }

        public float doIt0() {
            return cloudSharpness;
        }
    }

    public class ClasssetGlow {

        public ClasssetGlow(float glow) {
            this.glow = glow;
        }

        private float glow;

        public void doIt0() {
            SkyFilter.this.glow = glow;
        }
    }

    public class ClassgetGlow {

        public ClassgetGlow() {
        }

        public float doIt0() {
            return glow;
        }
    }

    public class ClassgetBias {

        public ClassgetBias() {
        }

        public float doIt0() {
            return bias;
        }
    }

    public class ClasssetSunColor {

        public ClasssetSunColor(int sunColor) {
            this.sunColor = sunColor;
        }

        private int sunColor;

        public void doIt0() {
            SkyFilter.this.sunColor = sunColor;
        }
    }

    public class ClassgetSunColor {

        public ClassgetSunColor() {
        }

        public int doIt0() {
            return sunColor;
        }
    }

    public class ClassgetCameraElevation {

        public ClassgetCameraElevation() {
        }

        public float doIt0() {
            return cameraElevation;
        }
    }

    public class ClasssetCameraAzimuth {

        public ClasssetCameraAzimuth(float cameraAzimuth) {
            this.cameraAzimuth = cameraAzimuth;
        }

        private float cameraAzimuth;

        public void doIt0() {
            SkyFilter.this.cameraAzimuth = cameraAzimuth;
        }
    }

    public class ClasssetWindSpeed {

        public ClasssetWindSpeed(float windSpeed) {
            this.windSpeed = windSpeed;
        }

        private float windSpeed;

        public void doIt0() {
            SkyFilter.this.windSpeed = windSpeed;
        }
    }

    public class Classevaluate {

        public Classevaluate(float x, float y) {
            this.x = x;
            this.y = y;
        }

        private float x;

        private float y;

        private float value;

        private float remainder;

        private int i;

        public void doIt0() {
            value = 0.0f;
            // to prevent "cascading" effects
            x += 371;
        }

        public float doIt1() {
            y += 529;
            for (i = 0; i < (int) octaves; i++) {
                value += Noise.noise3(x, y, t) * exponents[i];
                x *= lacunarity;
                y *= lacunarity;
            }
            remainder = octaves - (int) octaves;
            if (remainder != 0)
                value += remainder * Noise.noise3(x, y, t) * exponents[i];
            return value;
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

        private float fx;

        public void doIt0() {
            fx = (float) x / width;
        }

        private float fy;

        public void doIt1() {
            fy = y / height;
        }

        private float haze;

        public void doIt2() {
            haze = (float) Math.pow(haziness, 100 * fy * fy);
        }

        private float r;

        private float g;

        public void doIt3() {
            r = (float) ((rgb >> 16) & 0xff) * r255;
            g = (float) ((rgb >> 8) & 0xff) * r255;
        }

        private float b;

        private float cx;

        public void doIt4() {
            b = (float) (rgb & 0xff) * r255;
            cx = width * 0.5f;
        }

        private float nx;

        private float ny;

        public void doIt5() {
            nx = x - cx;
            ny = y;
            //ny = (float)Math.tan( fov * fy * Math.PI * 0.5 );
            ny = tan[y];
            nx = (fx - 0.5f) * (1 + ny);
            // Wind towards the camera
            ny += t * windSpeed;
        }

        private float f;

        public void doIt6() {
            //		float xscale = scale/(1+y*bias*0.1f);
            nx /= scale;
            ny /= scale * stretch;
            f = evaluate(nx, ny);
        }

        private float fg;

        public void doIt7() {
            fg = f;
            //		f = (f-min)/(max-min);
            f = (f + 1.23f) / 2.46f;
        }

        private int a;

        private int v;

        private float c;

        private SkyFilterHelper0 conditionObj0;

        private float cloudAlpha;

        public void doIt8() {
            a = rgb & 0xff000000;
            c = f - cloudCover;
            conditionObj0 = new  SkyFilterHelper0(0);
            if (c < conditionObj0.getValue())
                c = 0;
            cloudAlpha = 1 - (float) Math.pow(cloudSharpness, c);
        }

        public void doIt9() {
            //	cloudAlpha = 1;
            mn = Math.min(mn, cloudAlpha);
        }

        public void doIt10() {
            mx = Math.max(mx, cloudAlpha);
        }

        private float centreX;

        public void doIt11() {
            centreX = width * sunAzimuth;
        }

        private float centreY;

        private float dx;

        public void doIt12() {
            centreY = height * sunElevation;
            dx = x - centreX;
        }

        private float dy;

        private float distance2;

        public void doIt13() {
            dy = y - centreY;
            distance2 = dx * dx + dy * dy;
        }

        public void doIt14() {
            //distance2 = (float)Math.sqrt(distance2);
            distance2 = (float) Math.pow(distance2, glowFalloff);
        }

        private float sun;

        public void doIt15() {
            sun = 10 * (float) Math.exp(-distance2 * glow * 0.1f);
        }

        public void doIt16() {
            // Sun glow onto sky
            r += sun * sunR;
            g += sun * sunG;
            b += sun * sunB;
        }

        private float ca;

        private float cloudR;

        private float cloudG;

        private float cloudB;

        public void doIt17() {
            ca = (1 - cloudAlpha * cloudAlpha * cloudAlpha * cloudAlpha) * amount;
            cloudR = sunR * ca;
            cloudG = sunG * ca;
            cloudB = sunB * ca;
        }

        public void doIt18() {
            // Apply the haziness as we move further away
            cloudAlpha *= haze;
        }

        private float iCloudAlpha;

        private float exposure;

        public void doIt19() {
            iCloudAlpha = (1 - cloudAlpha);
            r = iCloudAlpha * r + cloudAlpha * cloudR;
            g = iCloudAlpha * g + cloudAlpha * cloudG;
            b = iCloudAlpha * b + cloudAlpha * cloudB;
            exposure = gain;
        }

        public void doIt20() {
            r = 1 - (float) Math.exp(-r * exposure);
        }

        private int ir;

        private int ig;

        private int ib;

        public void doIt21() {
            g = 1 - (float) Math.exp(-g * exposure);
            b = 1 - (float) Math.exp(-b * exposure);
            ir = (int) (255 * r) << 16;
            ig = (int) (255 * g) << 8;
            ib = (int) (255 * b);
        }

        public void doIt22() {
            v = 0xff000000 | ir | ig | ib;
        }

        public int doIt23() {
            return v;
        }
    }
}
