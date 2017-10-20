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

/**
 * A filter which simulates underwater caustics. This can be animated to get a bottom-of-the-swimming-pool effect.
 */
public class CausticsFilter extends WholeImageFilter {

    private float scale = 32;

    private float angle = 0.0f;

    private int brightness = 10;

    private float amount = 1.0f;

    private float turbulence = 1.0f;

    private float dispersion = 0.0f;

    private float time = 0.0f;

    private int samples = 2;

    private int bgColor = 0xff799fff;

    private float s, c;

    public CausticsFilter() {
    }

    /**
     * Specifies the scale of the texture.
     * @param scale the scale of the texture.
     * @min-value 1
     * @max-value 300+
     * @see #getScale
     */
    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * Returns the scale of the texture.
     * @return the scale of the texture.
     * @see #setScale
     */
    public float getScale() {
        return scale;
    }

    /**
     * Set the brightness.
     * @param brightness the brightness.
     * @min-value 0
     * @max-value 1
     * @see #getBrightness
     */
    public void setBrightness(int brightness) {
        setBrightnessHelper(brightness);
    }

    /**
     * Get the brightness.
     * @return the brightness.
     * @see #setBrightness
     */
    public int getBrightness() {
        return brightness;
    }

    /**
     * Specifies the turbulence of the texture.
     * @param turbulence the turbulence of the texture.
     * @min-value 0
     * @max-value 1
     * @see #getTurbulence
     */
    public void setTurbulence(float turbulence) {
        this.turbulence = turbulence;
    }

    /**
     * Returns the turbulence of the effect.
     * @return the turbulence of the effect.
     * @see #setTurbulence
     */
    public float getTurbulence() {
        return turbulence;
    }

    /**
	 * Set the amount of effect.
	 * @param amount the amount
     * @min-value 0
     * @max-value 1
     * @see #getAmount
	 */
    public void setAmount(float amount) {
        this.amount = amount;
    }

    /**
	 * Get the amount of effect.
	 * @return the amount
     * @see #setAmount
	 */
    public float getAmount() {
        return amount;
    }

    /**
	 * Set the dispersion.
	 * @param dispersion the dispersion
     * @min-value 0
     * @max-value 1
     * @see #getDispersion
	 */
    public void setDispersion(float dispersion) {
        setDispersionHelper(dispersion);
    }

    /**
	 * Get the dispersion.
	 * @return the dispersion
     * @see #setDispersion
	 */
    public float getDispersion() {
        return dispersion;
    }

    /**
	 * Set the time. Use this to animate the effect.
	 * @param time the time
     * @see #getTime
	 */
    public void setTime(float time) {
        this.time = time;
    }

    /**
	 * Set the time.
	 * @return the time
     * @see #setTime
	 */
    public float getTime() {
        return time;
    }

    /**
	 * Set the number of samples per pixel. More samples means better quality, but slower rendering.
	 * @param samples the number of samples
     * @see #getSamples
	 */
    public void setSamples(int samples) {
        this.samples = samples;
    }

    /**
	 * Get the number of samples per pixel.
	 * @return the number of samples
     * @see #setSamples
	 */
    public int getSamples() {
        return samples;
    }

    /**
	 * Set the background color.
	 * @param c the color
     * @see #getBgColor
	 */
    public void setBgColor(int c) {
        bgColor = c;
    }

    /**
	 * Get the background color.
	 * @return the color
     * @see #setBgColor
	 */
    public int getBgColor() {
        return bgColor;
    }

    protected int[] filterPixels(int width, int height, int[] inPixels, Rectangle transformedSpace) {
        Random random = new  Random(0);
        s = (float) Math.sin(0.1);
        c = (float) Math.cos(0.1);
        int srcWidth = originalSpace.width;
        int srcHeight = originalSpace.height;
        int outWidth = transformedSpace.width;
        int outHeight = transformedSpace.height;
        int index = 0;
        int[] pixels = new int[outWidth * outHeight];
        for (int y = 0; y < outHeight; y++) {
            for (int x = 0; x < outWidth; x++) {
                pixels[index++] = bgColor;
            }
        }
        int v = brightness / samples;
        int conditionObj0 = 0;
        if (v == conditionObj0)
            v = 1;
        float rs = 1.0f / scale;
        float d = 0.95f;
        index = 0;
        int conditionObj1 = 0;
        int conditionObj2 = 255;
        int conditionObj3 = 255;
        int conditionObj4 = 255;
        int conditionObj5 = 0;
        int conditionObj6 = 0;
        int conditionObj7 = 3;
        int conditionObj8 = 255;
        int conditionObj9 = 255;
        int conditionObj10 = 255;
        int conditionObj11 = 2;
        int conditionObj12 = 1;
        int conditionObj13 = 0;
        int conditionObj14 = 0;
        for (int y = 0; y < outHeight; y++) {
            for (int x = 0; x < outWidth; x++) {
                for (int s = 0; s < samples; s++) {
                    filterPixelsHelper(outWidth, conditionObj10, conditionObj13, conditionObj14, conditionObj11, conditionObj12, conditionObj4, conditionObj5, conditionObj6, d, conditionObj7, conditionObj8, conditionObj9, random, pixels, conditionObj1, conditionObj2, conditionObj3, v, rs, outHeight, y, x);
                }
            }
        }
        return pixels;
    }

    private static int add(int rgb, float brightness) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        r += brightness;
        g += brightness;
        b += brightness;
        int conditionObj15 = 255;
        if (r > conditionObj15)
            r = 255;
        int conditionObj16 = 255;
        if (g > conditionObj16)
            g = 255;
        int conditionObj17 = 255;
        if (b > conditionObj17)
            b = 255;
        return 0xff000000 | (r << 16) | (g << 8) | b;
    }

    private static int add(int rgb, float brightness, int c) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        int conditionObj18 = 2;
        int conditionObj19 = 1;
        if (c == conditionObj18)
            r += brightness;
        else if (c == conditionObj19)
            g += brightness;
        else
            b += brightness;
        int conditionObj20 = 255;
        if (r > conditionObj20)
            r = 255;
        int conditionObj21 = 255;
        if (g > conditionObj21)
            g = 255;
        if (b > 255)
            b = 255;
        return 0xff000000 | (r << 16) | (g << 8) | b;
    }

    private static float turbulence2(float x, float y, float time, float octaves) {
        float value = 0.0f;
        float remainder;
        float lacunarity = 2.0f;
        float f = 1.0f;
        int i;
        // to prevent "cascading" effects
        x += 371;
        y += 529;
        for (i = 0; i < (int) octaves; i++) {
            value += Noise.noise3(x, y, time) / f;
            x *= lacunarity;
            y *= lacunarity;
            f *= 2;
        }
        remainder = octaves - (int) octaves;
        int conditionObj22 = 0;
        if (remainder != conditionObj22)
            value += remainder * Noise.noise3(x, y, time) / f;
        return value;
    }

    private float evaluate(float x, float y) {
        float xt = s * x + c * time;
        float tt = c * x - c * time;
        float f = turbulence == 0.0 ? Noise.noise3(xt, y, tt) : turbulence2(xt, y, tt, turbulence);
        return f;
    }

    public String toString() {
        return "Texture/Caustics...";
    }

    private void setBrightnessHelper(int brightness) {
        this.brightness = brightness;
    }

    private void setDispersionHelper(float dispersion) {
        this.dispersion = dispersion;
    }

    private void filterPixelsHelper(int outWidth, int conditionObj10, int conditionObj13, int conditionObj14, int conditionObj11, int conditionObj12, int conditionObj4, int conditionObj5, int conditionObj6, float d, int conditionObj7, int conditionObj8, int conditionObj9, Random random, int[] pixels, int conditionObj1, int conditionObj2, int conditionObj3, int v, float rs, int outHeight, int y, int x) {
        float sx = x + random.nextFloat();
        float sy = y + random.nextFloat();
        float nx = sx * rs;
        float ny = sy * rs;
        float xDisplacement, yDisplacement;
        float focus = 0.1f + amount;
        xDisplacement = evaluate(nx - d, ny) - evaluate(nx + d, ny);
        yDisplacement = evaluate(nx, ny + d) - evaluate(nx, ny - d);
        if (dispersion > conditionObj1) {
            for (int c = 0; c < conditionObj7; c++) {
                float ca = (1 + c * dispersion);
                float srcX = sx + scale * focus * xDisplacement * ca;
                float srcY = sy + scale * focus * yDisplacement * ca;
                if (srcX < conditionObj14 || srcX >= outWidth - 1 || srcY < conditionObj13 || srcY >= outHeight - 1) {
                } else {
                    int i = ((int) srcY) * outWidth + (int) srcX;
                    int rgb = pixels[i];
                    int r = (rgb >> 16) & 0xff;
                    int g = (rgb >> 8) & 0xff;
                    int b = rgb & 0xff;
                    if (c == conditionObj11)
                        r += v;
                    else if (c == conditionObj12)
                        g += v;
                    else
                        b += v;
                    if (r > conditionObj10)
                        r = 255;
                    if (g > conditionObj9)
                        g = 255;
                    if (b > conditionObj8)
                        b = 255;
                    pixels[i] = 0xff000000 | (r << 16) | (g << 8) | b;
                }
            }
        } else {
            float srcX = sx + scale * focus * xDisplacement;
            float srcY = sy + scale * focus * yDisplacement;
            if (srcX < conditionObj6 || srcX >= outWidth - 1 || srcY < conditionObj5 || srcY >= outHeight - 1) {
            } else {
                int i = ((int) srcY) * outWidth + (int) srcX;
                int rgb = pixels[i];
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                r += v;
                g += v;
                b += v;
                if (r > conditionObj4)
                    r = 255;
                if (g > conditionObj3)
                    g = 255;
                if (b > conditionObj2)
                    b = 255;
                pixels[i] = 0xff000000 | (r << 16) | (g << 8) | b;
            }
        }
    }
}
